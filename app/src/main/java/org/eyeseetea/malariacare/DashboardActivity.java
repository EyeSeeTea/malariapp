/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.network.PushClient;
import org.eyeseetea.malariacare.network.PushResult;
import org.eyeseetea.malariacare.services.SurveyService;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.controllers.LoadingController;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;

import java.io.IOException;
import java.util.List;


public class DashboardActivity extends BaseActivity {

    private final static String TAG=".DDetailsActivity";
    private boolean reloadOnResume=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_dashboard);

        try {
            initDataIfRequired();
            loadSessionIfRequired();
        } catch (IOException e){
            Log.e(".DashboardActivity", e.getMessage());
        }

        if (savedInstanceState == null) {
            DashboardUnsentFragment detailsFragment = new DashboardUnsentFragment();
            detailsFragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.dashboard_details_container, detailsFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            DashboardSentFragment completedFragment = new DashboardSentFragment();
            detailsFragment.setArguments(getIntent().getExtras());
            FragmentTransaction ftr = getFragmentManager().beginTransaction();
            ftr.add(R.id.dashboard_completed_container, completedFragment);
            ftr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ftr.commit();
        }


        setTitle(getString(R.string.app_name) + " app - " + Session.getUser().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()!=R.id.action_pull){
            return super.onOptionsItemSelected(item);
        }
        final List<Survey> surveysUnsentFromService = (List<Survey>) Session.popServiceValue(SurveyService.ALL_UNSENT_SURVEYS_ACTION);
        if(surveysUnsentFromService.size()>0) {
            new AlertDialog.Builder(this)
                    .setTitle(getBaseContext().getApplicationContext().getString(R.string.dialog_ask_pending_surveys))
                    .setMessage(getBaseContext().getApplicationContext().getString(R.string.dialog_ask_pending_surveys))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            sentPendingSurveys(surveysUnsentFromService);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            PullController.getInstance().pull(getBaseContext().getApplicationContext());
                        }
                    }).create().show();
        }
        else
        PullController.getInstance().pull(getBaseContext().getApplicationContext());
        return true;
    }

    private boolean sentPendingSurveys(List<Survey> surveysUnsentFromService) {
        for(int i=surveysUnsentFromService.size()-1;i>=0;i--){

            //Get credentials from preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String user=sharedPreferences.getString(getBaseContext().getString(R.string.dhis_user), "");
            String password=sharedPreferences.getString(getBaseContext().getString(R.string.dhis_password), "");

            //Launch push
            AsyncPush asyncPush = new AsyncPush(surveysUnsentFromService.get(i),this, user, password,i);
            asyncPush.execute((Void) null);
        }
        boolean isSurveySent=true;
        for(Survey survey:surveysUnsentFromService) {
            if(!survey.isSent())
                isSurveySent=false;
        }
        return isSurveySent;
    }

    public boolean isAllSurveysSent() {
        final List<Survey> surveysUnsentFromService = (List<Survey>) Session.popServiceValue(SurveyService.ALL_UNSENT_SURVEYS_ACTION);
        if(surveysUnsentFromService.size()<=0){
            return true;
        }
        return false;
    }

    public class AsyncPush extends AsyncTask<Void, Integer, PushResult> {

        private Survey survey;
        private String user;
        private String password;
        private Activity activity;
        private int countdown;


        public AsyncPush(Survey survey, Activity activity, String user, String password, Integer count) {
            this.survey = survey;
            this.user = user;
            this.password = password;
            this.activity = activity;
            this.countdown = count;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //spinner
        }

        @Override
        protected PushResult doInBackground(Void... params) {
            PushClient pushClient = new PushClient(survey, activity, user, password);
            return pushClient.push();
        }

        @Override
        protected void onPostExecute(PushResult pushResult) {
            super.onPostExecute(pushResult);
            showResponse(pushResult);
        }

        /**
         * Shows the proper response message
         *
         * @param pushResult
         */
        private boolean showResponse(PushResult pushResult) {
            String msg = "";

            if (pushResult.isSuccessful()) {
                msg = pushResult.getLocalizedMessage(activity);
            } else {
                msg = pushResult.getExceptionLocalizedMessage(activity);

            }

            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.dialog_title_push_response))
                    .setMessage(msg)
                    .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (countdown == 0) {
                                if (isAllSurveysSent())
                                    PullController.getInstance().pull(getBaseContext().getApplicationContext());
                            }
                        }
                    }).create().show();

            return pushResult.isSuccessful();
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void initTransition(){
        this.overridePendingTransition(R.transition.anim_slide_in_right, R.transition.anim_slide_out_right);
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        super.onResume();
        getSurveysFromService();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        super.onPause();
    }

    public void setReloadOnResume(boolean doReload){
        this.reloadOnResume=false;
    }

    public void getSurveysFromService(){
        Log.d(TAG, "getSurveysFromService ("+reloadOnResume+")");
        if(!reloadOnResume){
            //Flag is readjusted
            reloadOnResume=true;
            return;
        }
        Intent surveysIntent=new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "back pressed");
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit the app?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * PUll data from DHIS server and turn into our model
     * @throws IOException
     */
    private void initDataIfRequired() throws IOException {
        PullController.getInstance().pull(this);
    }

    /**
     * In case Session doesn't have the user set, here we set it to the first entry of User table
     */
    private void loadSessionIfRequired(){
        //already a user in session -> done
        if(Session.getUser()!=null){
            return;
        }

        //No user (take it from db)
        User user = new Select().from(User.class).querySingle();
        if (user==null){
            //Mocked user (this should never happen)
            user = new User();
            user.setName("");
            user.save();
        }

        Session.setUser(user);
    }

    /**
     * Logging out from sdk is an async method.
     * Thus it is required a callback to finish logout gracefully.
     *
     * XXX: So far this @subscribe annotation does not work with inheritance since relies on 'getDeclaredMethods'
     * @param uiEvent
     */
    @Subscribe
    public void onLogoutFinished(UiEvent uiEvent){
        super.onLogoutFinished(uiEvent);
    }
}

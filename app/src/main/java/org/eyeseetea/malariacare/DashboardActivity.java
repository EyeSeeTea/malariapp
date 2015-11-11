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

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

        PullController.getInstance().pull(this);
        return true;
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

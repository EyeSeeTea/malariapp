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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.dashboard.controllers.DashboardController;
import org.eyeseetea.malariacare.network.PullClient;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.persistence.models.Event;

import java.util.Date;
import java.util.List;


public class DashboardActivity extends BaseActivity{

    private final static String TAG=".DDetailsActivity";
    private boolean reloadOnResume=true;
    DashboardController dashboardController;
    static Handler handler;
    public static DashboardActivity dashboardActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
        dashboardActivity=this;

        //XXX to remove?
        initDataIfRequired();

        //get dashboardcontroller from settings.json
        dashboardController = AppSettingsBuilder.getInstance().getDashboardController();

        //layout according to config
        setContentView(dashboardController.getLayout());

        //delegate modules initialization
        dashboardController.onCreate(this,savedInstanceState);

        //inits autopush alarm
        AlarmPushReceiver.getInstance().setPushAlarm(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            getFragmentManager().popBackStack();
        }
        //Any common option
        if(item.getItemId()!=R.id.action_pull){
            return super.onOptionsItemSelected(item);
        }

        //Pull
        final List<Survey> unsentSurveys = Survey.getAllUnsentUnplannedSurveys();

        //No unsent data -> pull (no confirmation)
        if(unsentSurveys==null || unsentSurveys.size()==0){
            pullMetadata();
            return true;
        }

        final Activity activity = this;
        //check if exist a compulsory question without awnser before push and pull.
        for(Survey survey:unsentSurveys){
            SurveyAnsweredRatio surveyAnsweredRatio = survey.reloadSurveyAnsweredRatio();
            if (surveyAnsweredRatio.getTotalCompulsory()>0 && surveyAnsweredRatio.getCompulsoryAnswered() != surveyAnsweredRatio.getTotalCompulsory() ) {
                new AlertDialog.Builder(this)
                        .setTitle("Unsent surveys")
                        .setMessage(getApplicationContext().getResources().getString(R.string.dialog_incompleted_compulsory_pulling))
                        .setPositiveButton(android.R.string.ok, null)
                        .setCancelable(true)
                        .create().show();
                return true;
            }
        }
        //Unsent data -> ask if pull || push before pulling
        new AlertDialog.Builder(this)
                .setTitle("Push unsent surveys?")
                .setMessage(String.format(getResources().getString(R.string.dialog_sent_survey_on_refresh_metadata), unsentSurveys.size() + ""))
                .setNeutralButton(android.R.string.no, null)
                .setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Pull directly
                        pullMetadata();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Try to push before pull
                        pushUnsentBeforePull();
                    }
                })
                .setCancelable(true)
                .create().show();
        return true;
    }

    private void pushUnsentBeforePull() {

        //Launch Progress Push before pull
        Intent progressActivityIntent = new Intent(this, ProgressActivity.class);
        progressActivityIntent.putExtra(ProgressActivity.TYPE_OF_ACTION, ProgressActivity.ACTION_PUSH_BEFORE_PULL);
        finish();
        startActivity(progressActivityIntent);
    }

    private void pullMetadata(){
        PreferencesState.getInstance().clearOrgUnitPreference();
        finishAndGo(ProgressActivity.class);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void initTransition() {
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
        reloadDashboard();
    }

    public static void reloadDashboard(){
        Intent surveysIntent=new Intent(dashboardActivity, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        dashboardActivity.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        dashboardController.onBackPressed();
    }

    /**
     * PUll data from DHIS server and turn into our model
     */
    private void initDataIfRequired(){
//            PullController.getInstance().pull(this);
            initUserSessionIfRequired();
    }

    /**
     * In case Session doesn't have the user set, here we set it to the first entry of User table
     */
    private void initUserSessionIfRequired(){
        // already a user in session -> done
        if(Session.getUser()!=null){
            return;
        }

        // If we're in dashboard and User is not yet in session we have to put it
        // FIXME: for the moment there will be only one user in the User table, but in the future we will have to think about tagging the logged user in the DB
        User user = User.getLoggedUser();
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

    /**
     * Handler that starts or edits a given survey
     * @param survey
     */
    public void onSurveySelected(Survey survey){
        dashboardController.onSurveySelected(survey);
    }

    /**
     * Handler that marks the given sucloseFeedbackFragmentrvey as completed.
     * This includes a pair or corner cases
     * @param survey
     */
    public void onMarkAsCompleted(Survey survey){
        dashboardController.onMarkAsCompleted(survey);
    }

    /**
     * Handler that enter into the feedback for the given survey
     * @param survey
     */
    public void onFeedbackSelected(Survey survey) {
        dashboardController.onFeedbackSelected(survey);
    }

    /**
     * Moving into createSurvey fragment
     * @param view
     */
    public void onNewSurvey(View view){
        dashboardController.onNewSurvey();
    }
    /**
     * Modify survey from CreateSurveyFragment
     * If the survey will be modify, it should have a eventuid. In the convert to sdk a new fake event will be created
     */
    public void modifySurvey(OrgUnit orgUnit, TabGroup tabGroup, Event lastEventInServer){
        //Looking for that survey in local
        Survey survey = Survey.findSurveyWith(orgUnit, tabGroup, lastEventInServer);
        //Survey in server BUT not local
        if(survey==null){
            survey= SurveyPlanner.getInstance().startSurvey(orgUnit,tabGroup);
        }
        if(lastEventInServer!=null){
            survey.setEventUid(lastEventInServer.getEvent());
            EventExtended lastEventExtended = new EventExtended(lastEventInServer);
            survey.setCreationDate(lastEventExtended.getCreationDate());
            survey.setCompletionDate(lastEventExtended.getEventDate());
        }else{
            //Mark the survey as a modify attempt for pushing accordingly
            survey.setEventUid(PullClient.NO_EVENT_FOUND);
        }

        //Upgrade the uploaded date
        survey.setUploadDate(new Date());
        survey.setStatus(Constants.SURVEY_IN_PROGRESS);
        Session.setSurvey(survey);
        prepareLocationListener(survey);
        dashboardController.onSurveySelected(survey);
    }

    /**
     * Create new survey from CreateSurveyFragment
     */
    public void onCreateSurvey(final OrgUnit orgUnit,final TabGroup tabGroup) {
        createNewSurvey(orgUnit,tabGroup);
    }

    /**
     * Create new survey from VariantSpecificUtils
     */
    public void createNewSurvey(OrgUnit orgUnit, TabGroup tabGroup){
        Survey survey=SurveyPlanner.getInstance().startSurvey(orgUnit,tabGroup);
        Session.setSurvey(survey);
        prepareLocationListener(survey);
        dashboardController.onSurveySelected(survey);
    }

    //Show dialog exception from class without activity.
    public static void showException(final String title, final String errorMessage) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Run your task here
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String dialogTitle = "", dialogMessage = "";
                        if (title != null)
                            dialogTitle = title;
                        if (errorMessage != null)
                            dialogMessage = errorMessage;
                        new AlertDialog.Builder(dashboardActivity)
                                .setCancelable(false)
                                .setTitle(dialogTitle)
                                .setMessage(dialogMessage)
                                .setNeutralButton(android.R.string.ok, null)
                                .create().show();
                    }
                });
            }
        }, 1000);
    }
}

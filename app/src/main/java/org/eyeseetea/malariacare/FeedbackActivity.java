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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.eyeseetea.malariacare.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.survey.FeedbackAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomRadioButton;

import java.util.List;

/**
 * Activity that supports the data entry for the surveys.
 */
public class FeedbackActivity extends BaseActivity{

    public static final String TAG = ".FeedbackActivity";

    /**
     * Receiver of data from SurveyService
     */
    private SurveyReceiver surveyReceiver;

    /**
     * Progress dialog shown while loading
     */
    private ProgressBar progressBar;

    /**
     * Checkbox that toggle between all|failed questions
     */
    private CustomRadioButton chkFailed;

    /**
     * List view adapter for items
     */
    private FeedbackAdapter feedbackAdapter;

    /**
     * List view items
     */
    private ListView feedbackListView;

    /**
     * Menu of the activity
     */
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.feedback);
        createActionBar();
        prepareUI();

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        startProgress();
        registerReceiver();
        prepareFeedbackInfo();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_title_exit)
                .setMessage(R.string.survey_info_exit)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        ScoreRegister.clear();
                        unregisterReceiver();
                        finishAndGo(DashboardActivity.class);
                    }
                }).create().show();
    }

    @Override
    public void onPause(){
        //XXX Since feedback is a readonly action there is no need to update anything
//        Session.getSurvey().updateSurveyStatus();
        unregisterReceiver();
        super.onPause();
    }

    /**
     * Adds actionbar to the activity
     */
    private void createActionBar(){
        Survey survey = Session.getSurvey();
        //FIXME: Shall we add the tab group?
        Program program = survey.getTabGroup().getProgram();

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        LayoutUtils.setActionBarText(actionBar, survey.getOrgUnit().getName(), program.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.overall_score:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void prepareUI(){
        //Get progress
        progressBar=(ProgressBar)findViewById(R.id.survey_progress);

        //Set adapter and list
        feedbackAdapter=new FeedbackAdapter(this);
        feedbackListView=(ListView)findViewById(R.id.feedbackListView);
        feedbackListView.setAdapter(feedbackAdapter);

        //And checkbox listener
        chkFailed=(CustomRadioButton)findViewById(R.id.chkFailed);
        chkFailed.setChecked(true);
        chkFailed.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             feedbackAdapter.toggleOnlyFailed();
                                             ((CustomRadioButton) v).setChecked(feedbackAdapter.isOnlyFailed());
                                         }
                                     }
        );
    }

    private void loadItems(List<Feedback> items){
        this.feedbackAdapter.setItems(items);
        setOverallScore();
        stopProgress();
    }

    /**
     * Sets the overall composite score as an action button
     */
    private void setOverallScore(){
        MenuItem item = menu.findItem(R.id.overall_score);
        Survey survey = Session.getSurvey();
        float average = survey.getMainScore();
        item.setTitle(String.format("%.1f%%", average));
    }



    /**
     * Stops progress view and shows real data
     */
    private void stopProgress(){
        this.progressBar.setVisibility(View.GONE);
        this.feedbackListView.setVisibility(View.VISIBLE);
    }

    /**
     * Starts progress view, hiding list temporarily
     */
    private void startProgress(){
        this.feedbackListView.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
        this.progressBar.setEnabled(true);
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    public void registerReceiver() {
        Log.d(TAG, "registerReceiver");

        if(surveyReceiver==null){
            surveyReceiver=new SurveyReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.PREPARE_FEEDBACK_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void  unregisterReceiver(){
        Log.d(TAG, "unregisterReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }

    /**
     * Asks SurveyService for the current list of surveys
     */
    public void prepareFeedbackInfo(){
        Log.d(TAG, "prepareFeedbackInfo");
        Intent surveysIntent=new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PREPARE_FEEDBACK_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive");
            List<Feedback> feedbackList=(List<Feedback>)Session.popServiceValue(SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS);
            loadItems(feedbackList);
        }
    }

}

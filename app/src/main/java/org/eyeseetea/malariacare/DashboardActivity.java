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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.squareup.otto.Subscribe;


import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.fragments.CreateSurveyFragment;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.FeedbackFragment;
import org.eyeseetea.malariacare.fragments.MonitorFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.services.SurveyService;
import org.hisp.dhis.android.sdk.events.UiEvent;

import java.io.IOException;
import java.util.List;


public class DashboardActivity extends BaseActivity implements DashboardUnsentFragment.OnUnsentDashboardListener,CreateSurveyFragment.OnCreatedSurveyListener,DashboardSentFragment.OnFeedbackSelectedListener {

    private final static String TAG=".DDetailsActivity";
    private boolean reloadOnResume=true;
    TabHost tabHost;
    PlannedFragment plannedFragment;
    MonitorFragment monitorFragment;
    DashboardUnsentFragment unsentFragment;
    DashboardSentFragment sentFragment;
    CreateSurveyFragment createSurveyFragment;
    SurveyFragment surveyFragment;
    FeedbackFragment feedbackFragment;
    String currentTab;
    String currentTabName;
    boolean isMoveToLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_dashboard);
        try {
            initDataIfRequired();
            loadSessionIfRequired();
        } catch (IOException e){
            Log.e(".DashboardActivity", e.getMessage());
        }
        if(savedInstanceState==null) {
            if(!isPlanningTabHide())
                initPlanned();
            initAssess();
            initImprove();
            initMonitor();
        }
        initTabHost(savedInstanceState);
        /* set tabs in order */
        if(!isPlanningTabHide())
            setTab(getResources().getString(R.string.tab_tag_plan), R.id.tab_plan_layout, getResources().getDrawable(R.drawable.tab_plan));
        setTab(getResources().getString(R.string.tab_tag_assess), R.id.tab_assess_layout, getResources().getDrawable(R.drawable.tab_assess));
        setTab(getResources().getString(R.string.tab_tag_improve), R.id.tab_improve_layout, getResources().getDrawable(R.drawable.tab_improve));
        setTab(getResources().getString(R.string.tab_tag_monitor), R.id.tab_monitor_layout, getResources().getDrawable(R.drawable.tab_monitor));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                /** If current tab is android */

                //set the tabs background as transparent
                for(int i=0;i<tabHost.getTabWidget().getChildCount();i++){
                    tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.transparent));
                }
                currentTab = tabId;

                //If change of tab from surveyFragment or FeedbackFragment they could be closed.
                if(isSurveyFragmentActive())
                    onExitFromSurvey();
                if(isFeedbackFragmentActive())
                    closeFeedbackFragment();
                if (tabId.equalsIgnoreCase(getResources().getString(R.string.tab_tag_plan))) {
                    tabHost.getCurrentTabView().setBackgroundColor(getResources().getColor(R.color.tab_orange_plan));
                    setActionBarDashboard();
                    currentTabName=getString(R.string.plan);
                    plannedFragment.reloadPlannedItems();
                } else if (tabId.equalsIgnoreCase(getResources().getString(R.string.tab_tag_assess))) {
                    tabHost.getCurrentTabView().setBackgroundColor(getResources().getColor(R.color.tab_yellow_assess));
                    if(isCreateSurveyFragmentActive() ||isDashboardUnsentFragmentActive())
                        setActionBarDashboard();
                    currentTabName=getString(R.string.assess);
                    if(isSurveyFragmentActive())
                        setActionBarTitleForSurvey(Session.getSurvey());
                    unsentFragment.reloadData();
                } else if (tabId.equalsIgnoreCase(getResources().getString(R.string.tab_tag_improve))) {
                    tabHost.getCurrentTabView().setBackgroundColor(getResources().getColor(R.color.tab_blue_improve));
                    currentTabName=getString(R.string.improve);
                    if(!isFeedbackFragmentActive()){
                        setActionBarDashboard();
                        sentFragment.reloadSentSurveys();
                    }
                } else if (tabId.equalsIgnoreCase(getResources().getString(R.string.tab_tag_monitor))) {
                    tabHost.getCurrentTabView().setBackgroundColor(getResources().getColor(R.color.tab_green_monitor));
                    setActionBarDashboard();
                    currentTabName=getString(R.string.monitor);
                    monitorFragment.reloadSentSurveys();
                }
            }
        });

        // init tabHost
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++){
            tabHost.getTabWidget().getChildAt(i).setFocusable(false);
        }
        //set the initial selected tab background
        if(!isPlanningTabHide()) {
            tabHost.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.tab_orange_plan));
        }
        else
            tabHost.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.tab_yellow_assess));
        currentTabName=getString(R.string.plan);
        setActionBarDashboard();
    }

    public boolean isPlanningTabHide(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(getApplicationContext().getResources().getString(R.string.hide_planning_tab_key),false);
    }

    public void setActionBarDashboard(){
        String title=getString(R.string.app_name);
        String subtitle="";
        if(Session.getUser()!=null && Session.getUser().getName()!=null)
            subtitle=Session.getUser().getName();
        setActionbarTitle(title, subtitle);
    }

    public void setActionBarTitleForSurvey(Survey survey){
        String title=getString(R.string.app_name)+": "+currentTabName.toUpperCase();
        String subtitle="";

        if(Session.getUser()!=null && Session.getUser().getName()!=null)
            subtitle=Session.getUser().getName();

        Program program = survey.getTabGroup().getProgram();
        String subtitle2="";
        String subtitle3="";
        if(survey.getOrgUnit().getName()!=null)
            subtitle2=survey.getOrgUnit().getName();
        if(program.getName()!=null)
            subtitle3=program.getName();
        setActionbarMultiTitle(title, subtitle, subtitle2, subtitle3);
    }

    public void setActionbarTitle(String title1, String title2) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);
        ((TextView) findViewById(R.id.action_bar_title)).setText(title1);
        ((TextView) findViewById(R.id.action_bar_subtitle)).setText(title2);
    }

    public void setActionbarMultiTitle(String title1, String title2,String title3, String title4) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_four_title_layout);
        ((TextView) findViewById(R.id.action_bar_multititle_title)).setText(title1);
        ((TextView) findViewById(R.id.action_bar_multititle_subtitle)).setText(title2);
        ((TextView) findViewById(R.id.action_bar_multititle_subtitle2)).setText(title3);
        ((TextView) findViewById(R.id.action_bar_multititle_subtitle3)).setText(title4);
    }

    /**
     * Init the conteiner for all the tabs
     */
    private void initTabHost(Bundle savedInstanceState) {
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
    }


    /**
     * Set tab in tabHost
     * @param tab_plan is the name of the tab
     * @param layout is the id of the layout
     * @param image is the drawable with the tab icon image
     */
    private void setTab(String tab_plan, int layout,  Drawable image) {
        TabHost.TabSpec tab = tabHost.newTabSpec(tab_plan);
        tab.setContent(layout);
        tab.setIndicator("", image);
        tabHost.addTab(tab);

    }

    public void initPlanned(){
        plannedFragment = new PlannedFragment();
        plannedFragment.setArguments(getIntent().getExtras());
        replaceListFragment(R.id.dashboard_planning_tab, plannedFragment);
    }

    public void initAssess(){
        unsentFragment = new DashboardUnsentFragment();
        unsentFragment.setArguments(getIntent().getExtras());
        replaceListFragment(R.id.dashboard_details_container, unsentFragment);
    }
    public void initImprove(){
        sentFragment = new DashboardSentFragment();
        sentFragment.setArguments(getIntent().getExtras());
        replaceListFragment(R.id.dashboard_completed_container, sentFragment);
        try {
            LinearLayout filters = (LinearLayout) findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.VISIBLE);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initFeedback() {
        int  mStackLevel=0;
        mStackLevel++;
        try {
            LinearLayout filters = (LinearLayout) findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }
        feedbackFragment = FeedbackFragment.newInstance(mStackLevel);
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        replaceFragment(R.id.dashboard_completed_container, feedbackFragment);
        setActionBarTitleForSurvey(Session.getSurvey());
    }

    public void initCreateSurvey(){
        int mStackLevel=0;
        mStackLevel++;

        if(createSurveyFragment==null)
            createSurveyFragment = CreateSurveyFragment.newInstance(mStackLevel);
        replaceFragment(R.id.dashboard_details_container, createSurveyFragment);
    }

    public void initSurveyFromPlanning(){
        tabHost.setCurrentTabByTag(getResources().getString(R.string.tab_tag_assess));
        initSurvey();
    }

    public void initSurvey(){
        int  mStackLevel=0;
        mStackLevel++;
        if(surveyFragment==null)
            surveyFragment = SurveyFragment.newInstance(mStackLevel);
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
        setActionBarTitleForSurvey(Session.getSurvey());
    }

    public void initMonitor(){
        int mStackLevel=0;
        mStackLevel++;
        if(monitorFragment==null)
            monitorFragment = MonitorFragment.newInstance(mStackLevel);
        replaceFragment(R.id.dashboard_charts_container, monitorFragment);
    }


    // Add the fragment to the activity, pushing this transaction
    // on to the back stack.
    private void replaceFragment(int layout,  Fragment fragment) {
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    private void replaceListFragment(int layout,  ListFragment fragment) {
        try{
            //fix some visual problems
            View vg = findViewById (layout);
            vg.invalidate();
        }catch (Exception e){}
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    @NonNull
    private FragmentTransaction getFragmentTransaction() {
        FragmentTransaction ft = getFragmentManager ().beginTransaction();
        if(isMoveToLeft) {
            isMoveToLeft =false;
            ft.setCustomAnimations(R.animator.anim_slide_in_right, R.animator.anim_slide_out_right);
        }
        else
            ft.setCustomAnimations(R.animator.anim_slide_in_left, R.animator.anim_slide_out_left);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        return ft;
    }

    /**
     * Init the fragments
     */
    private void setFragmentTransaction(int layout, ListFragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(layout, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void setActionbarTitle() {
        android.support.v7.app.ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);
        ((TextView) findViewById(R.id.action_bar_title)).setText(getString(R.string.app_name));
        ((TextView) findViewById(R.id.action_bar_subtitle)).setText(Session.getUser().getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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

        //Unsent data -> ask if pull || push before pulling
        final Activity activity = this;
        new AlertDialog.Builder(this)
                .setTitle("Push unsent surveys?")
                .setMessage("Metadata refresh will delete your unsent data. You have "+unsentSurveys.size()+" unsent surveys. Do you to push them before refresh?")
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
        Intent surveysIntent=new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        isMoveToLeft =true;
        if(isCreateSurveyFragmentActive() && currentTab==getResources().getString(R.string.tab_tag_assess)) {
            initAssess();
            unsentFragment.reloadData();
        } else if (isSurveyFragmentActive() && currentTab == getResources().getString(R.string.tab_tag_assess)) {
            onSurveyBackPressed();
        } else if (isFeedbackFragmentActive() && currentTab == getResources().getString(R.string.tab_tag_improve)) {
            closeFeedbackFragment();
        } else {
            confirmExitApp();
        }
    }

    /**
     * Ask to send the survey or close the survey.
     * It is called when the user change the tab
     */
    private void onExitFromSurvey(){
        Survey survey = Session.getSurvey();
        SurveyAnsweredRatio surveyAnsweredRatio = survey.reloadSurveyAnsweredRatio();
        if (surveyAnsweredRatio.getCompulsoryAnswered() == surveyAnsweredRatio.getTotalCompulsory() && surveyAnsweredRatio.getTotalCompulsory() != 0) {
            askToSendCompulsoryCompletedSurvey();

        }
        closeSurveyFragment();
    }



    /**
     * It is called when the user press back in a surveyFragment
     */
    private void onSurveyBackPressed() {
        Survey survey = Session.getSurvey();
        SurveyAnsweredRatio surveyAnsweredRatio = survey.reloadSurveyAnsweredRatio();
        if (surveyAnsweredRatio.getCompulsoryAnswered() == surveyAnsweredRatio.getTotalCompulsory() && surveyAnsweredRatio.getTotalCompulsory() != 0) {
            askToSendCompulsoryCompletedSurvey();

        } else
            askToCloseSurvey();
    }

    private void confirmExitApp() {
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
     * This dialog is called when the user have a survey open, and close this survey, or when the user change of tab
     */
    private void askToCloseSurvey() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_title_exit)
                .setMessage(R.string.survey_info_exit).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Survey survey = Session.getSurvey();
                        survey.updateSurveyStatus();
                        closeSurveyFragment();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        unsentFragment.reloadData();
                    }
                }).create().show();
    }

    /**
     * This dialog is called when the user have a survey open, with compulsory questions completed, and close this survey, or when the user change of tab
     */
    private void askToSendCompulsoryCompletedSurvey() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_question_complete_survey)
                .setNegativeButton(R.string.dialog_complete_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        confirmSendCompleteSurvey();
                    }
                })
                .setPositiveButton(R.string.dialog_continue_later_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        closeSurveyFragment();
                    }
                }).create().show();
    }
    /**
     * This dialog is called to confirm before set a survey as complete
     */
    public void confirmSendCompleteSurvey() {
        //if you select complete_option, this dialog will showed.
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_are_you_sure_complete_survey)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Survey survey = Session.getSurvey();
                        survey.setCompleteSurveyState();
                        closeSurveyFragment();
                    }
                }).create().show();
    }

    public void closeSurveyFragment(){
        ScoreRegister.clear();
        surveyFragment.unregisterReceiver();
        initAssess();
        unsentFragment.reloadData();
        setActionBarDashboard();
    }

    private void closeFeedbackFragment() {
        ScoreRegister.clear();
        feedbackFragment.unregisterReceiver();
        feedbackFragment.getView().setVisibility(View.GONE);
        initImprove();
        sentFragment.reloadData();
        setActionBarDashboard();
    }

    /**
     * Called when the user clicks the New Survey button
     */
    public void newSurvey(View view) {
        initCreateSurvey();
    }


    /**
     * Checks if a survey fragment is active
     */
    private boolean isSurveyFragmentActive() {
         Fragment currentFragment = this.getFragmentManager ().findFragmentById(R.id.dashboard_details_container);
        if (currentFragment instanceof SurveyFragment) {
            return true;
        }
        return false;
    }


    /**
     * Checks if a createsurveyfragment is active
     */
    private boolean isCreateSurveyFragmentActive() {
         Fragment currentFragment = this.getFragmentManager ().findFragmentById(R.id.dashboard_details_container);
        if (currentFragment instanceof CreateSurveyFragment) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a dashboardUnsentFragment is active
     */
    private boolean isDashboardUnsentFragmentActive() {
        Fragment currentFragment = this.getFragmentManager ().findFragmentById(R.id.dashboard_details_container);
        if (currentFragment instanceof DashboardUnsentFragment) {
            return true;
        }
        return false;
    }
    /**
     * Checks if a feedbackfragment is active
     */
    private boolean isFeedbackFragmentActive() {
        Fragment currentFragment = this.getFragmentManager ().findFragmentById(R.id.dashboard_completed_container);
        if (currentFragment instanceof FeedbackFragment) {
            Log.v(TAG, "find the current fragment"+"Feedback");
            return true;
        }
        return false;
    }
    /**
     * PUll data from DHIS server and turn into our model
     * @throws IOException
     */
    private void initDataIfRequired() throws IOException {
//        PullController.getInstance().pull(this);
    }

    /**
     * In case Session doesn't have the user set, here we set it to the first entry of User table
     */
    private void loadSessionIfRequired(){
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

    @Override
    public void onFeedbackSelected(Survey survey) {
        Session.setSurvey(survey);
        tabHost.setCurrentTabByTag(getResources().getString(R.string.tab_tag_improve));
        sentFragment.getView().setVisibility(View.GONE);
        initFeedback();
    }

    @Override
    public void onSurveySelected(Survey survey) {
        //Put selected survey in session
        Session.setSurvey(survey);
        initSurvey();
    }

    @Override
    public void dialogCompulsoryQuestionIncompleted() {
        new AlertDialog.Builder(this)
                .setMessage(getApplicationContext().getResources().getString(R.string.dialog_incompleted_compulsory_survey))
                .setPositiveButton(getApplicationContext().getString(R.string.accept), null)
                .create().show();
    }

    @Override
    public void onCreateSurvey() {
        initSurvey();
    }
}

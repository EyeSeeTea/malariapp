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
import android.app.LocalActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TextView;

import com.squareup.otto.Subscribe;


import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.fragments.CreateSurveyFragment;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.MonitorFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.services.SurveyService;
import org.hisp.dhis.android.sdk.events.UiEvent;

import java.io.IOException;
import java.util.List;


public class DashboardActivity extends BaseActivity implements DashboardUnsentFragment.OnSurveySelectedListener,CreateSurveyFragment.OnCreatedSurveyListener {

    private final static String TAG=".DDetailsActivity";
    private boolean reloadOnResume=true;
    TabHost tabHost;
    PlannedFragment plannedFragment;
    MonitorFragment monitorFragment;
    DashboardUnsentFragment unsentFragment;
    DashboardSentFragment sentFragment;
    CreateSurveyFragment createSurveyFragment;
    SurveyFragment surveyFragment;
    LocalActivityManager mlam;
    static boolean viewFeedback;
    String currentTab;
    String TAB_PLAN;
    String TAB_ASSESS;
    String TAB_IMPROVE;
    String TAB_MONITOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getTags();
        if(viewFeedback) {
            viewFeedback=false;
            finishAndGo(FeedbackActivity.class);
        }
        setContentView(R.layout.tab_dashboard);
        try {
            initDataIfRequired();
            loadSessionIfRequired();
        } catch (IOException e){
            Log.e(".DashboardActivity", e.getMessage());
        }
        if(savedInstanceState==null) {
            initPlanned();
            initAssess();
            initImprove();
            initMonitor();
        }
        initTabHost(savedInstanceState);
        /* set tabs in order */
        setTab(TAB_PLAN, R.id.tab_plan_layout, getResources().getDrawable(R.drawable.tab_plan));
        setTab(TAB_ASSESS, R.id.tab_assess_layout, getResources().getDrawable(R.drawable.tab_assess));
        setTab(TAB_IMPROVE, R.id.tab_improve_layout, getResources().getDrawable(R.drawable.tab_improve));
        setTab(TAB_MONITOR, R.id.tab_monitor_layout, getResources().getDrawable(R.drawable.tab_monitor));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                /** If current tab is android */

                View currentView = tabHost.getCurrentView();

                currentTab = tabId;
                setActionBarDashboard();
                if (tabId.equalsIgnoreCase(TAB_PLAN)) {
                    currentView.setAnimation(inFromRightAnimation());
                    plannedFragment.reloadPlannedItems();
                } else if (tabId.equalsIgnoreCase(TAB_ASSESS)) {
                    if(isSurveyFragmentActive())
                        setActionBarTitleForSurveyFragment();
                    currentView.setAnimation(inFromRightAnimation());
                    unsentFragment.reloadData();
                } else if (tabId.equalsIgnoreCase(TAB_IMPROVE)) {
                    currentView.setAnimation(outToLeftAnimation());
                    sentFragment.reloadSentSurveys();
                } else if (tabId.equalsIgnoreCase(TAB_MONITOR)) {
                    currentView.setAnimation(outToLeftAnimation());
                    monitorFragment.reloadSentSurveys();
                }
            }
        });
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++){
            tabHost.getTabWidget().getChildAt(i).setFocusable(false);
        }
        setActionBarDashboard();
    }
    public void setActionBarDashboard(){
        invalidateOptionsMenu();
        String title=getString(R.string.app_name);
        String subtitle="Not valid user";
        if(Session.getUser()!=null && Session.getUser().getName()!=null)
            subtitle=Session.getUser().getName();
        setActionbarTitle(title,subtitle);
    }
    public void setActionBarTitleForSurveyFragment(){
        invalidateOptionsMenu();
        Survey survey= Session.getSurvey();
        Program program = survey.getTabGroup().getProgram();
        setActionbarTitle( survey.getOrgUnit().getName(), program.getName());
    }

    public void setActionbarTitle(String title1, String title2) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);
        ((TextView) findViewById(R.id.action_bar_title)).setText(title1);
        ((TextView) findViewById(R.id.action_bar_subtitle)).setText(title2);
    }

    public Animation inFromRightAnimation() {

        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(600);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    public Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(600);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    private void getTags() {
        TAB_PLAN=getResources().getString(R.string.tab_plan);
        TAB_ASSESS=getResources().getString(R.string.tab_assess);
        TAB_IMPROVE=getResources().getString(R.string.tab_improve);
        TAB_MONITOR=getResources().getString(R.string.tab_monitor);
    }

    /**
     * Init the conteiner for all the tabs
     */
    private void initTabHost(Bundle savedInstanceState) {
        mlam = new LocalActivityManager(this, false);
        tabHost = (TabHost)findViewById(R.id.tabHost);
        mlam.dispatchCreate(savedInstanceState);
        tabHost.setup(mlam);
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
        Log.d(TAG,"initPlanned");
        plannedFragment = new PlannedFragment();
        plannedFragment.setArguments(getIntent().getExtras());
        try{
            View vg = findViewById (R.id.dashboard_planning_tab);
            vg.invalidate();
        }catch (Exception e){}
        replaceListFragment(R.id.dashboard_planning_tab, plannedFragment);
    }
    
    public void initAssess(){
        unsentFragment = new DashboardUnsentFragment();
        unsentFragment.setArguments(getIntent().getExtras());
        try{
            View vg = findViewById (R.id.dashboard_details_container);
            vg.invalidate();
        }catch (Exception e){}
        replaceListFragment(R.id.dashboard_details_container, unsentFragment);
    }
    
    public void initImprove(){
        if(sentFragment==null) {
            sentFragment = new DashboardSentFragment();
            sentFragment.setArguments(getIntent().getExtras());
        }
        try{
            View vg = findViewById (R.id.dashboard_completed_container);
            vg.invalidate();
        }catch (Exception e){}
        replaceListFragment(R.id.dashboard_completed_container, sentFragment);
    }

    public void initCreateSurvey(){
        int mStackLevel=0;
        mStackLevel++;

        if(createSurveyFragment==null)
            createSurveyFragment = CreateSurveyFragment.newInstance(mStackLevel);
        replaceFragment(R.id.dashboard_details_container, createSurveyFragment);
    }

    public void initSurveyFromPlanning(){
        initSurvey();
        tabHost.setCurrentTabByTag(TAB_ASSESS);
    }

    public void initSurvey(){
        int  mStackLevel=0;
        mStackLevel++;
        if(surveyFragment==null)
            surveyFragment = SurveyFragment.newInstance(mStackLevel);
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
        setActionBarTitleForSurveyFragment();
    }

    public void initMonitor(){
        int mStackLevel=0;
        mStackLevel++;
        if(monitorFragment==null)
            monitorFragment = MonitorFragment.newInstance(mStackLevel);
        replaceFragment(R.id.dashboard_charts_container, monitorFragment);
    }

    private void replaceFragment(int layout,  Fragment fragment) {
         FragmentTransaction ft = getFragmentManager ().beginTransaction();
        ft.setCustomAnimations(R.animator.anim_slide_in_right, R.animator.anim_slide_out_right);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(layout, fragment);
        ft.commit();
    }

    private void replaceListFragment(int layout,  ListFragment fragment) {
        FragmentTransaction ft = getFragmentManager ().beginTransaction();
        ft.setCustomAnimations(R.animator.anim_slide_in_right, R.animator.anim_slide_out_right);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
       ft.replace(layout, fragment);
        ft.commit();
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
        final List<Survey> unsentSurveys = Survey.getAllUnsentSurveys();

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
        progressActivityIntent.putExtra(ProgressActivity.TYPE_OF_ACTION,ProgressActivity.ACTION_PUSH_BEFORE_PULL);
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
        mlam.dispatchResume();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        super.onPause();
        mlam.dispatchPause(isFinishing());
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
        if(isCreateSurveyFragmentActive() && currentTab==TAB_ASSESS) {
            initAssess();
            unsentFragment.reloadData();
        }
        else if(isSurveyFragmentActive() && currentTab==TAB_ASSESS){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.survey_title_exit)
                    .setMessage(R.string.survey_info_exit)
                    .setCancelable(false)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            ScoreRegister.clear();
                            surveyFragment.unregisterReceiver();
                            initAssess();
                            unsentFragment.reloadData();
                        }
                    }).create().show();
        }
        else {
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
    }

    /**
     * Called when the user clicks the New Survey button
     */
    @Override
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
    public void onSurveySelected(Survey survey) {
        //Put selected survey in session
        Session.setSurvey(survey);
        initSurvey();
    }

    @Override
    public void onCreateSurvey() {
        initSurvey();
    }

}

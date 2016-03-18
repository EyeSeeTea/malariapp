/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.dashboard.controllers;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.fragments.CreateSurveyFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.FeedbackFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardSettings;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the dashboard based on modules
 * Created by idelcano on 25/02/2016.
 */
public class DashboardController {

    private static final String TAG = ".DashboardController";
    /**
     * List of modules that composed the dashboard
     */
    private List<ModuleController> modules;

    /**
     * Loaded settings for the dashboard
     */
    private DashboardSettings dashboardSettings;

    /**
     * Reference to the dashboard activity, used as context
     */
    private DashboardActivity dashboardActivity;

    /**
     * Reference to the tabhost
     */
    TabHost tabHost;

    /**
     * Name of the current tab (identifier)
     */
    String currentTab="";

    /**
     * Title of the current tab
     */
    String currentTabTitle ="";


    public DashboardController(DashboardSettings dashboardSettings){
        this.dashboardSettings = dashboardSettings;
        this.modules = new ArrayList<>();
    }

    public DashboardOrientation getOrientation(){
        return dashboardSettings.getOrientation();
    }

    public int getLayout() {
        return dashboardSettings.getResLayout();
    }

    public  void addModule(ModuleController module){
        modules.add(module);
    }

    /**
     * Finds a module by its name
     * @param name
     * @return
     */
    public ModuleController getModuleByName(String name){
        for(ModuleController module:modules){
            if(module.getName().equals(name))
                return module;
        }
        return null;
    }

    public List<ModuleController> getModules() {
        return modules;
    }

    /**
     * Returns the moduleController for the first tab
     * @return
     */
    private ModuleController getFirstVisibleModule(){
        for(ModuleController module:modules){
            if(module.isVisible())
                return module;
        }
        return null;
    }

    /**
     * Returns the module in charge of the currently selected tab
     * @return
     */
    private ModuleController getCurrentModule(){
        if(currentTab==null){
            return null;
        }
        for(ModuleController module:modules){
            if(module.getName().equals(currentTab))
                return module;
        }
        return null;
    }

    //XXX


    CreateSurveyFragment createSurveyFragment;
    SurveyFragment surveyFragment;
    FeedbackFragment feedbackFragment;


    public void onCreate(DashboardActivity dashboardActivity, Bundle savedInstanceState){
        this.dashboardActivity = dashboardActivity;

        if(DashboardOrientation.VERTICAL.equals(getOrientation())) {
            onCreateVertical();
        }else {
            onCreateHorizontal(savedInstanceState);
        }
        setActionBarDashboard();
    }

    public void onCreateVertical(){
        for(ModuleController module: this.getModules()){
            module.onCreate(dashboardActivity);
            //XXX Really needed?
            module.reloadData();
        }
    }

    private void onCreateHorizontal(Bundle savedInstanceState){
        for(ModuleController module: this.getModules()){
            module.onCreate(dashboardActivity);
        }
        onCreateTabHost(savedInstanceState);
    }

    /**
     * Init the container for all the tabs
     */
    private void onCreateTabHost(Bundle savedInstanceState) {
        tabHost = (TabHost)dashboardActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        //Add visible modules to tabhost
        for(ModuleController moduleController: this.getModules()){
            if(!moduleController.isVisible()) {
                continue;
            }
            addTab(moduleController);
        }

        ModuleController firstModuleController=getFirstVisibleModule();
        tabHost.getTabWidget().getChildAt(0).setBackgroundColor(firstModuleController.getBackgroundColor());
        currentTab = firstModuleController.getName();
        currentTabTitle = firstModuleController.getTitle();

        //Add tab listener
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                //References to previous and current modules
                ModuleController currentModuleController = getCurrentModule();
                ModuleController nextModuleController = getModuleByName(tabId);

                //Reset tab colors
                resetTabBackground();
                tabHost.getCurrentTabView().setBackgroundColor(nextModuleController.getBackgroundColor());

                //Update next Tab and title
                currentTab = tabId;
                currentTabTitle = nextModuleController.getTitle();

                //Before leaving current tab
                currentModuleController.onExitTab();
                //Update action bar
                nextModuleController.setActionBarDashboard();
                //Preparing new tab
                nextModuleController.onTabChanged();
            }
        });
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    public void onBackPressed() {
        if(isCreateSurveyFragmentActive() && PreferencesState.getInstance().isVerticalDashboard() ) {
            reloadVertical();
        }
        else if (isSurveyFragmentActive() && PreferencesState.getInstance().isVerticalDashboard() ) {
            Survey survey=Session.getSurvey();
            survey.updateSurveyStatus();
            ScoreRegister.clear();
            surveyFragment.unregisterReceiver();
            reloadVertical();
        } else if (isFeedbackFragmentActive() && PreferencesState.getInstance().isVerticalDashboard() ) {
            ScoreRegister.clear();
            feedbackFragment.unregisterReceiver();
            feedbackFragment.getView().setVisibility(View.GONE);
            reloadVertical();
        }
        else if(isCreateSurveyFragmentActive() && (currentTab==dashboardActivity.getResources().getString(R.string.tab_tag_assess)) ) {
            reloadFragment(dashboardActivity.getResources().getString(R.string.tab_tag_assess));
        } else if (isSurveyFragmentActive() && currentTab == dashboardActivity.getResources().getString(R.string.tab_tag_assess)) {
            onSurveyBackPressed();
        } else if (isFeedbackFragmentActive() && currentTab == dashboardActivity.getResources().getString(R.string.tab_tag_improve)) {
            closeFeedbackFragment();
        } else {
            confirmExitApp();
        }
    }

    private void reloadVertical(){
        for(ModuleController module: getModules()){
            if(module.isVisible()) {
                module.init(dashboardActivity);
                initModule(module.getLayout(), module.getFragment());
                module.reloadData();
            }
        }
        setActionbarAppName();
    }

    public void setActionBarDashboard(){
        if(PreferencesState.getInstance().isVerticalDashboard()){
            setActionbarAppName();
        }
        else {
            String title="";
            String user="";
            if(Session.getUser()!=null && Session.getUser().getName()!=null)
                user=Session.getUser().getName();

            //Capitalize tab name
            StringBuilder tabtemp = new StringBuilder(currentTabTitle.toLowerCase());
            tabtemp.setCharAt(0, Character.toUpperCase(tabtemp.charAt(0)));
            title = tabtemp.toString();
            int appNameColor = dashboardActivity.getResources().getColor(R.color.appNameColor);
            String appNameColorString = String.format("%X", appNameColor).substring(2);
            Spanned spannedTitle= Html.fromHtml(String.format("<font color=\"#%s\"><b>", appNameColorString) + dashboardActivity.getResources().getString(R.string.app_name) + "</b></font> | " + title);
            setActionbarTitle(spannedTitle, user);
        }
    }

    public void setActionBarTitleForSurvey(Survey survey){
        String title="";
        String subtitle="";
        int appNameColor = dashboardActivity.getResources().getColor(R.color.appNameColor);
        String appNameColorString = String.format("%X", appNameColor).substring(2);
        Program program = survey.getTabGroup().getProgram();
        if(survey.getOrgUnit().getName()!=null)
            title=survey.getOrgUnit().getName();
        if(program.getName()!=null)
            subtitle=program.getName();
        if(PreferencesState.getInstance().isVerticalDashboard()) {
            setActionbarVerticalSurvey(title, subtitle);
        }
        else{
            Spanned spannedTitle = Html.fromHtml(String.format("<font color=\"#%s\"><b>", appNameColorString) + title + "</b></font>");
            setActionbarTitle(spannedTitle, subtitle);
        }
    }

    public void setActionbarVerticalSurvey(String title, String subtitle) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(subtitle);
        actionBar.setTitle(title);
    }

    public void setActionbarTitle(Spanned title, String subtitle) {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        ((TextView) dashboardActivity.findViewById(R.id.action_bar_multititle_title)).setText(title);
        ((TextView) dashboardActivity.findViewById(R.id.action_bar_multititle_subtitle)).setText(subtitle);
    }

    public void setActionbarAppName() {
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.abc_action_bar_title_item);
        actionBar.setSubtitle(null);
        actionBar.setTitle(dashboardActivity.getResources().getString(R.string.app_name));
    }

    public void initModule(int layout, Fragment fragment){
        replaceFragment(layout, fragment);
    }

    public void initSurveyFromPlanning(){
        tabHost.setCurrentTabByTag(dashboardActivity.getResources().getString(R.string.tab_tag_assess));
        initSurvey();
    }

    public void onFeedbackSelected(Survey survey) {
        Session.setSurvey(survey);
        initFeedback();
    }

    public void initSurvey(){
        if(surveyFragment==null) {
            surveyFragment = SurveyFragment.newInstance(1);
        }
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
        setActionBarTitleForSurvey(Session.getSurvey());
    }

    public void replaceFragment(int layout, Fragment fragment) {
        if(fragment instanceof ListFragment){
            try{
                //fix some visual problems
                View vg = dashboardActivity.findViewById(layout);
                vg.invalidate();
            }catch (Exception e){}
        }

        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(layout, fragment);
        ft.commit();
    }

    public FragmentTransaction getFragmentTransaction() {
        FragmentTransaction ft = dashboardActivity.getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        return ft;
    }

    private void resetTabBackground(){
        Resources resources = dashboardActivity.getResources();
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(resources.getColor(R.color.transparent));
        }
    }

    private void addTab(ModuleController moduleController){
        String tabName=moduleController.getName();
        //Add tab to tabhost
        TabHost.TabSpec tab = tabHost.newTabSpec(tabName);
        tab.setContent(moduleController.getTabLayout());
        tab.setIndicator("", moduleController.getIcon());
        tabHost.addTab(tab);

        addTagToLastTab(tabName);
    }

    /**
     * Last current tab is tagged with the given tabName
     * @param tabName
     */
    private void addTagToLastTab(String tabName){
        TabWidget tabWidget=tabHost.getTabWidget();
        int numTabs=tabWidget.getTabCount();
        LinearLayout tabIndicator=(LinearLayout)tabWidget.getChildTabViewAt(numTabs - 1);

        ImageView imageView = (ImageView)tabIndicator.getChildAt(0);
        imageView.setTag(tabName);
    }

    /**
     * Checks if a survey fragment is active
     */
    private boolean isSurveyFragmentActive() {
        Fragment currentFragment = dashboardActivity.getFragmentManager ().findFragmentById(R.id.dashboard_details_container);
        if (currentFragment instanceof SurveyFragment) {
            return true;
        }
        return false;
    }


    /**
     * Checks if a createsurveyfragment is active
     */
    private boolean isCreateSurveyFragmentActive() {
        Fragment currentFragment = dashboardActivity.getFragmentManager().findFragmentById(R.id.dashboard_details_container);
        if (currentFragment instanceof CreateSurveyFragment) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a dashboardUnsentFragment is active
     */
    private boolean isDashboardUnsentFragmentActive() {
        Fragment currentFragment = dashboardActivity.getFragmentManager().findFragmentById(R.id.dashboard_details_container);
        if (currentFragment instanceof DashboardUnsentFragment) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a feedbackfragment is active
     */
    private boolean isFeedbackFragmentActive() {
        Fragment currentFragment = dashboardActivity.getFragmentManager().findFragmentById(R.id.dashboard_completed_container);
        if (currentFragment instanceof FeedbackFragment) {
            Log.v(TAG, "find the current fragment" + "Feedback");
            return true;
        }
        return false;
    }

    /**
     * This dialog is called when the user have a survey open, with compulsory questions completed, and close this survey, or when the user change of tab
     */
    private void askToSendCompulsoryCompletedSurvey() {
        new AlertDialog.Builder(dashboardActivity)
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
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(R.string.dialog_are_you_sure_complete_survey)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Survey survey=Session.getSurvey();
                        survey.setCompleteSurveyState();
                        alertOnComplete(survey);
                        closeSurveyFragment();
                    }
                }).create().show();
    }

    public void closeSurveyFragment(){
        ScoreRegister.clear();
        surveyFragment.unregisterReceiver();
        reloadFragment(dashboardActivity.getResources().getString(R.string.tab_tag_assess));
        setActionBarDashboard();
    }

    private void closeFeedbackFragment() {
        ScoreRegister.clear();
        feedbackFragment.unregisterReceiver();
        feedbackFragment.getView().setVisibility(View.GONE);
        reloadFragment(dashboardActivity.getResources().getString(R.string.tab_tag_improve));
        setActionBarDashboard();
    }

    /**
     * Called when the user clicks the New Survey button
     */
    public void newSurvey() {
        if(PreferencesState.getInstance().isVerticalDashboard()) {
            hideImprove();
            initCreateSurvey(R.id.dashboard_details_container);
        }
        else{
            initCreateSurvey(R.id.dashboard_details_container);
        }
    }

    public void initFeedback() {
        try {
            LinearLayout filters = (LinearLayout) dashboardActivity.findViewById(R.id.filters_sentSurveys);
            filters.setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }
        feedbackFragment = FeedbackFragment.newInstance(1);
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        replaceFragment(R.id.dashboard_completed_container, feedbackFragment);
        setActionBarTitleForSurvey(Session.getSurvey());
    }

    private void hideImprove() {
        FragmentTransaction ft = getFragmentTransaction();
        ModuleController module= getModuleByName(dashboardActivity.getResources().getString(R.string.tab_tag_improve));
        if(module.getFragment()!=null) {
            ft.hide(module.getFragment());
            ft.commit();
        }
    }


    private void reloadFragment(String string) {
        ModuleController module = getModuleByName(string);
        if (module.getFragment() != null) {
            module.init(dashboardActivity);
            initModule(module.getLayout(), module.getFragment());
            module.reloadData();
        }
    }

    public void initCreateSurvey(int layout){
        if(PreferencesState.getInstance().isVerticalDashboard()) {
            setActionbarBackbutton();
            CustomTextView sentTitle = (CustomTextView) dashboardActivity.findViewById(R.id.titleCompleted);
            sentTitle.setText("");
        }

        if(createSurveyFragment==null) {
            createSurveyFragment = CreateSurveyFragment.newInstance(1);
        }
        replaceFragment(layout, createSurveyFragment);
    }

    public void setActionbarBackbutton(){
        android.support.v7.app.ActionBar actionBar = dashboardActivity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void alertOnComplete(Survey survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(R.string.dialog_info_on_complete),survey.getProgram().getName()))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .create().show();
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
        new AlertDialog.Builder(dashboardActivity)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit the app?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        dashboardActivity.startActivity(intent);
                    }
                }).create().show();
    }
    /**
     * This dialog is called when the user have a survey open, and close this survey, or when the user change of tab
     */
    private void askToCloseSurvey() {
        new AlertDialog.Builder(dashboardActivity)
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
                        reloadFragment(dashboardActivity.getResources().getString(R.string.tab_tag_assess));
                    }
                }).create().show();
    }

}

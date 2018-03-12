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
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyScheduleDB;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.data.repositories.SurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardSettings;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the dashboard based on modules
 * Created by idelcano on 25/02/2016.
 */
public class DashboardController {
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

    /**
     * Flag that tells if moving forwards or backwards
     */
    boolean navigatingBackwards;


    public DashboardController(DashboardSettings dashboardSettings){
        this.dashboardSettings = dashboardSettings;
        this.modules = new ArrayList<>();
        this.navigatingBackwards =false;
    }

    public DashboardOrientation getOrientation(){
        return dashboardSettings.getOrientation();
    }

    public int getLayout() {
        return dashboardSettings.getResLayout();
    }

    public  void addModule(ModuleController module){
        module.setDashboardController(this);
        modules.add(module);
    }

    public boolean isNavigatingBackwards(){
        return this.navigatingBackwards;
    }

    public void setNavigatingBackwards(boolean backwards){
        this.navigatingBackwards=backwards;
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

    public void onCreate(DashboardActivity dashboardActivity, Bundle savedInstanceState){
        this.dashboardActivity = dashboardActivity;

        if(DashboardOrientation.VERTICAL.equals(getOrientation())) {
            onCreateVertical();
        }else {
            onCreateHorizontal(savedInstanceState);
        }
        //First module sets the dashboard actionBar
        getFirstVisibleModule().setActionBarDashboard();
    }

    public void onCreateVertical(){
        for(ModuleController module: this.getModules()){
            module.onCreate(dashboardActivity);
            //XXX Really needed?
            module.reloadData();
        }
        currentTab = getFirstVisibleModule().getName();
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
        navigatingBackwards =true;
        ModuleController moduleController = getCurrentModule();
        moduleController.onBackPressed();
        navigatingBackwards =false;
    }

    /**
     * Starts or edits the given survey from the planning tab
     * @param survey
     */
    public void onSurveySelected(SurveyDB survey){

        if(DashboardOrientation.VERTICAL.equals(getOrientation())){
            //Mark currentTab (only necessary for vertical orientation)
            currentTab = getModuleByName(AssessModuleController.getSimpleName()).getName();
            hideAssessVerticalTitle();
            hideImproveVerticalTitle();
            hideImprove();
        }else{
            //Move into the assess tab
            tabHost.setCurrentTabByTag(AssessModuleController.getSimpleName());
        }

        //This action belongs to the assess module
        AssessModuleController assessModuleController = (AssessModuleController)getModuleByName(AssessModuleController.getSimpleName());
        assessModuleController.onSurveySelected(survey);
    }
    /**
     * Starts the org unit planning tab
     * @param orgUnit
     */
    public void onOrgUnitSelected(OrgUnitDB orgUnit) {
        PlanModuleController planModuleController = (PlanModuleController)getModuleByName(PlanModuleController.getSimpleName());
        planModuleController.onOrgUnitSelected(orgUnit);
    }
    /**
     * Starts the program planning tab
     * @param program
     */
    public void onProgramSelected(ProgramDB program) {
        PlanModuleController planModuleController = (PlanModuleController)getModuleByName(PlanModuleController.getSimpleName());
        planModuleController.onProgramSelected(program);
    }
    /**
     * Marks the given survey as selected
     * @param survey
     */
    public void onMarkAsCompleted(SurveyDB survey){
        //This action belongs to the assess module
        AssessModuleController assessModuleController = (AssessModuleController)getModuleByName(AssessModuleController.getSimpleName());
        assessModuleController.onMarkAsCompleted(survey);
    }

    /**
     * Called when the user clicks the New Survey button
     */
    public void onNewSurvey() {

        //Vertical -> Hide improve module
        if(DashboardOrientation.VERTICAL.equals(getOrientation())){
            //Mark currentTab (only necessary for vertical orientation)
            currentTab = getModuleByName(AssessModuleController.getSimpleName()).getName();
            hideAssessVerticalTitle();
            hideImproveVerticalTitle();
            hideImprove();
        }

        //Replace new survey
        AssessModuleController assessModuleController = (AssessModuleController)getModuleByName(AssessModuleController.getSimpleName());
        assessModuleController.onNewSurvey();
    }

    /**
     * Called when click on assets survey
     * @param survey
     */
    public void onAssetsSelected(SurveyDB survey) {
        assetsModelDialog(survey);
    }


    public AlertDialog assetsModelDialog(final SurveyDB survey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(dashboardActivity);

        LayoutInflater inflater = dashboardActivity.getLayoutInflater();

        View v = inflater.inflate(R.layout.modal_menu, null);

        builder.setView(v);

        final DoublePieChart mChart = (DoublePieChart) v.findViewById(R.id.pie_chart);
        Button delete = (Button) v.findViewById(R.id.delete);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        Button markComplete = (Button) v.findViewById(R.id.mark_completed);
        Button edit = (Button) v.findViewById(R.id.edit);
        final TextView overall = (TextView) v.findViewById(R.id.overall_percent);
        final TextView mandatory = (TextView) v.findViewById(R.id.mandatory_percent);

        final AlertDialog alertDialog = builder.create();

        edit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSurveySelected(survey);
                        alertDialog.dismiss();
                    }
                }

        );
        markComplete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMarkAsCompleted(survey);
                        alertDialog.dismiss();
                    }
                }

        );
        delete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(dashboardActivity)
                                .setTitle(dashboardActivity.getString(R.string.dialog_title_delete_survey))
                                .setMessage(String.format(""+dashboardActivity.getString(R.string.dialog_info_delete_survey), survey.getProgram().getName()))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        //this method create a new survey geting the getScheduledDate date of the oldsurvey, and remove it.
                                        SurveyPlanner.getInstance().deleteSurveyAndBuildNext(survey);
                                        dashboardActivity.reloadDashboard();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).create().show();
                        alertDialog.dismiss();
                    }
                }
        );
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }
        );


        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase;
        ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
                new SurveyAnsweredRatioRepository();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();
        getSurveyAnsweredRatioUseCase =
                new GetSurveyAnsweredRatioUseCase(surveyAnsweredRatioRepository, mainExecutor, asyncExecutor);

        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        Log.d(getClass().getName(), "onComplete");

                        if (surveyAnsweredRatio != null) {
                            overall.setText(surveyAnsweredRatio.getTotalStatus() +
                                    dashboardActivity.getString(R.string.percent));

                            mandatory.setText(surveyAnsweredRatio.getMandatoryStatus() +
                                    dashboardActivity.getString(R.string.percent));

                            mChart.createDoublePie(surveyAnsweredRatio.getMandatoryStatus(),
                                    surveyAnsweredRatio.getTotalStatus());

                            alertDialog.show();
                        }
                    }
                });

        return alertDialog;
    }

    /**
     * Called when entering feedback for the given survey
     * @param survey
     */
    public void onFeedbackSelected(SurveyDB survey) {
        feedbackModelDialog(survey);
    }

    public AlertDialog feedbackModelDialog(final SurveyDB survey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(dashboardActivity);

        LayoutInflater inflater = dashboardActivity.getLayoutInflater();

        View v = inflater.inflate(R.layout.modal_feedback_menu, null);

        builder.setView(v);

        builder.setCancelable(false);
        Button viewFeedback = (Button) v.findViewById(R.id.view);
        Button actionPlan = (Button) v.findViewById(R.id.action_plan);
        Button cancel = (Button) v.findViewById(R.id.cancel);

        TextView orgUnitTextView = (TextView) v.findViewById(R.id.planned_org_unit);
        TextView programTextView = (TextView) v.findViewById(R.id.planned_program);
        if(survey.getOrgUnit()!=null  && survey.getOrgUnit().getName()!=null && orgUnitTextView!=null) {
            orgUnitTextView.setText(survey.getOrgUnit().getName());
        }
        if(survey.getOrgUnit()!=null  && survey.getProgram().getName()!=null && programTextView!=null) {
            programTextView.setText(survey.getProgram().getName());
        }
        final AlertDialog alertDialog =builder.create();
        viewFeedback.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFeedback(survey);


                        alertDialog.dismiss();
                    }
                }
        );

        actionPlan.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DashboardActivity.dashboardActivity.openActionPlan(survey);
                        alertDialog.dismiss();
                    }
                }

        );
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }

        );
        alertDialog.show();
        return alertDialog;
    }

    public void onPlanPerOrgUnitMenuClicked(SurveyDB survey) {
        scheduleHistoricLogDialog(survey);
    }

    public void scheduleHistoricLogDialog(final SurveyDB survey) {
            AlertDialog.Builder builder = new AlertDialog.Builder(dashboardActivity);

            LayoutInflater inflater = dashboardActivity.getLayoutInflater();

            View v = inflater.inflate(R.layout.modal_schedule_plan_menu, null);

            builder.setView(v);

            builder.setCancelable(false);
            Button showHistory = (Button) v.findViewById(R.id.show_history);
            Button cancel = (Button) v.findViewById(R.id.cancel);


            CustomTextView orgUnitTextView = (CustomTextView) v.findViewById(R.id.planned_org_unit);
            orgUnitTextView.setText(survey.getOrgUnit().getName());

            CustomTextView programTextView = (CustomTextView) v.findViewById(R.id.planned_program);
            programTextView.setText(survey.getProgram().getName());

            final AlertDialog alertDialog =builder.create();
            showHistory.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showHistory(survey);
                            alertDialog.dismiss();
                        }
                    }
            );
            cancel.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    }

            );
            alertDialog.show();
    }

    private void showHistory(SurveyDB survey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.dashboardActivity);
        LayoutInflater inflater = DashboardActivity.dashboardActivity.getLayoutInflater();

        View v = inflater.inflate(R.layout.historical_log_dialog, null);
        builder.setView(v);
        TextView orgUnit = (TextView) v.findViewById(R.id.org_unitName);
        TextView program = (TextView) v.findViewById(R.id.programName);
        program.setText(survey.getProgram().getName());
        orgUnit.setText(survey.getOrgUnit().getName());
        Button cancel = (Button) v.findViewById(R.id.cancel);
        LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.log_content);
        View row = inflater.inflate(R.layout.item_list_dialog_header, null);
        linearLayout.addView(row);
        for(SurveyScheduleDB surveyScheduleDB: survey.getSurveySchedules()){
            row = inflater.inflate(R.layout.item_list_row_row, null);
            TextView comment = (TextView) row.findViewById(R.id.first_column);
            TextView date = (TextView) row.findViewById(R.id.second_column);
            comment.setText(surveyScheduleDB.getComment());
            date.setText(AUtils.getEuropeanFormatedDate(surveyScheduleDB.getPrevious_date()));
            linearLayout.addView(row );
        }
        final AlertDialog alertDialog = builder.create();
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }
        );

        alertDialog.show();
    }


    public void openFeedback(SurveyDB survey) {
        //Vertical -> Hide improve module
        if(DashboardOrientation.VERTICAL.equals(getOrientation())){
            //Mark currentTab (only necessary for vertical orientation)
            currentTab = getModuleByName(ImproveModuleController.getSimpleName()).getName();
            hideAssessVerticalTitle();
            hideImproveVerticalTitle();
            hideAssess();
        }
        if(DashboardOrientation.HORIZONTAL.equals(getOrientation())) {
            currentTab = getModuleByName(ImproveModuleController.getSimpleName()).getName();
            if(!currentTab.equals(getModuleByName(ImproveModuleController.getSimpleName()))){
                tabHost.setCurrentTabByTag(ImproveModuleController.getSimpleName());
            }

        }

        ImproveModuleController improveModuleController = (ImproveModuleController)getModuleByName(ImproveModuleController.getSimpleName());
        improveModuleController.onFeedbackSelected(survey);
        improveModuleController.setActionBarDashboardWithProgram();
    }


    public void onPlannedSurvey(SurveyDB survey, View.OnClickListener scheduleClickListener) {
        plannedModelDialog(survey, scheduleClickListener);
    }

    public AlertDialog plannedModelDialog(final SurveyDB survey,
            View.OnClickListener scheduleClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(dashboardActivity);

        LayoutInflater inflater = dashboardActivity.getLayoutInflater();

        View v = inflater.inflate(R.layout.modal_planned_menu, null);

        builder.setView(v);

        builder.setCancelable(false);
        Button add = (Button) v.findViewById(R.id.add);
        Button change = (Button) v.findViewById(R.id.change);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        Button showHistory = (Button) v.findViewById(R.id.show_history);


        CustomTextView orgUnitTextView = (CustomTextView) v.findViewById(R.id.planned_org_unit);
        orgUnitTextView.setText(survey.getOrgUnit().getName());

        CustomTextView programTextView = (CustomTextView) v.findViewById(R.id.planned_program);
        programTextView.setText(survey.getProgram().getName());

        if (survey.isInProgress()) {
            add.setText(R.string.option_edit);
        } else {
            add.setText(R.string.add);
        }

        final AlertDialog alertDialog =builder.create();
        add.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSurveySelected(survey);
                        alertDialog.dismiss();
                    }
                }
        );
        ((ScheduleListener) scheduleClickListener).addAlertDialog(alertDialog);
        change.setOnClickListener(scheduleClickListener);
        cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                }

        );
        showHistory.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showHistory(survey);
                        alertDialog.dismiss();
                    }
                }
        );
        alertDialog.show();
        return alertDialog;
    }

    public void reloadVertical(){
        for(ModuleController module: getModules()){
            if(!module.isVisible()){
                continue;
            }
            module.init(dashboardActivity);
            module.showVerticalTitle();
            module.replaceFragment(module.getLayout(),module.getFragment());
            module.reloadData();

        }
        getFirstVisibleModule().setActionBarDashboard();
    }

    /**
     * Adds the given module to the tabHost
     * @param moduleController
     */
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
     * Vertical orientation requires to hide assess title when entering create, survey
     */
    private void hideAssessVerticalTitle(){
        AssessModuleController assessModuleController = (AssessModuleController)getModuleByName(AssessModuleController.getSimpleName());
        //No module -> nothing to hide
        if(assessModuleController!=null){
            assessModuleController.hideVerticalTitle();
        }
    }

    /**
     * Vertical orientation requires hidden improve fragment while creating a survey
     */
    private void hideAssess(){
        AssessModuleController assessModuleController = (AssessModuleController)getModuleByName(AssessModuleController.getSimpleName());
        //No module -> nothing to hide
        if(assessModuleController!=null){
            assessModuleController.hide();
        }
    }

    /**
     * Vertical orientation requires hidden improve fragment while creating a survey
     */
    private void hideImprove(){
        ImproveModuleController improveModuleController = (ImproveModuleController)getModuleByName(ImproveModuleController.getSimpleName());
        //No module -> nothing to hide
        if(improveModuleController!=null){
            improveModuleController.hide();
        }
    }

    /**
     * Vertical orientation requires to hide assess title when entering create, survey
     */
    private void hideImproveVerticalTitle(){
        ImproveModuleController improveModuleController = (ImproveModuleController)getModuleByName(ImproveModuleController.getSimpleName());
        //No module -> nothing to hide
        if(improveModuleController!=null){
            improveModuleController.hideVerticalTitle();
        }
    }

    /**
     * Reset tabs background color to transparent
     */
    private void resetTabBackground(){
        Resources resources = dashboardActivity.getResources();
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            tabHost.getTabWidget().getChildAt(i).setBackgroundColor(resources.getColor(R.color.transparent));
        }
    }
}

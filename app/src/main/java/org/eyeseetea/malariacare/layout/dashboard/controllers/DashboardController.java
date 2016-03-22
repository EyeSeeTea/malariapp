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

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardSettings;

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
        module.setDashboardController(this);
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
        ModuleController moduleController = getCurrentModule();

        moduleController.onBackPressed();
    }

    /**
     * Starts or edits the given survey from the planning tab
     * @param survey
     */
    public void onSurveySelected(Survey survey){

        if(DashboardOrientation.VERTICAL.equals(getOrientation())){
            //Mark currentTab (only necessary for vertical orientation)
            currentTab = getModuleByName(AssessModuleController.getSimpleName()).getName();
        }else{
            //Move into the assess tab
            tabHost.setCurrentTabByTag(AssessModuleController.getSimpleName());
        }

        //This action belongs to the assess module
        AssessModuleController assessModuleController = (AssessModuleController)getModuleByName(AssessModuleController.getSimpleName());
        assessModuleController.onSurveySelected(survey);
    }

    /**
     * Marks the given survey as selected
     * @param survey
     */
    public void onMarkAsCompleted(Survey survey){
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
     * Called when entering feedback for the given survey
     * @param survey
     */
    public void onFeedbackSelected(Survey survey) {

        //Vertical -> Hide improve module
        if(DashboardOrientation.VERTICAL.equals(getOrientation())){
            //Mark currentTab (only necessary for vertical orientation)
            currentTab = getModuleByName(ImproveModuleController.getSimpleName()).getName();
            hideAssessVerticalTitle();
            hideImproveVerticalTitle();
            hideAssess();
        }

        ImproveModuleController improveModuleController = (ImproveModuleController)getModuleByName(ImproveModuleController.getSimpleName());
        improveModuleController.onFeedbackSelected(survey);
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

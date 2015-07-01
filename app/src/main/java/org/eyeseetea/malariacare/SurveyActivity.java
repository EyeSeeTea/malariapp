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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.TabArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositeScoreAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that supports the data entry for the surveys.
 */
public class SurveyActivity extends BaseActivity{

    public static final String TAG = ".SurveyActivity";

    //FIXME Better than a bunch of 'ifs' worse than it should
    private static final int ORDER_PROFILE=2;
    private static final int ORDER_C1_CLINICAL=3;
    private static final int ORDER_C1_RDT=4;
    private static final int ORDER_C2_CLINICAL=5;
    private static final int ORDER_C2_RDT=6;
    private static final int ORDER_C3_CLINICAL=7;
    private static final int ORDER_C3_RDT=8;
    private static final int ORDER_FEEDBACK=9;
    private static final int ORDER_ENVIRONMENT=10;

    private static final int[] ORDER_TABS_AVG_CLINICAL={ORDER_C1_CLINICAL,ORDER_C2_CLINICAL,ORDER_C3_CLINICAL};
    private static final int[] ORDER_TABS_RDT={ORDER_C1_RDT,ORDER_C2_RDT,ORDER_C3_RDT};
    private static final int[] ORDER_TABS_OVERALL={ORDER_PROFILE,ORDER_FEEDBACK,ORDER_ENVIRONMENT};

    private static final int[] IDS_SCORES_IN_GENERAL_TAB={
            0,                      //0
            0,                      //1
            R.id.profileScore,      //2
            R.id.clinicalCase1,     //3
            R.id.rdtCase1,          //4
            R.id.clinicalCase2,     //5
            R.id.rdtCase2,          //6
            R.id.clinicalCase3,     //7
            R.id.rdtCase3,          //8
            R.id.feedbackScore,     //9
            R.id.envAndMatScore     //10
    };

    /**
     * List of tabs that belongs to the current selected survey
     */
    private List<Tab> tabsList=new ArrayList<>();

    /**
     * Map of adapters, each tab requires a different adapter to show its form
     */
    private Map<Tab, ITabAdapter> adaptersMap = new HashMap<Tab, ITabAdapter>();

    private TabAdaptersCache tabAdaptersCache = new TabAdaptersCache();

    /**
     * Adapter for the tabs spinner
     */
    private TabArrayAdapter tabAdapter;

    /**
     * Receiver of data from SurveyService
     */
    private SurveyReceiver surveyReceiver;

    /**
     * Progress dialog shown while loading
     */
    private ProgressBar progressBar;
    /**
     * Spinner view to select change tabs
     */
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(".SurveyActivity", "onCreate");
        setContentView(R.layout.survey);
        registerReceiver();
        createActionBar();
        createMenu();
        createProgress();
    }

    public void onResume(){
        prepareSurveyInfo();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_title_exit)
                .setMessage(R.string.survey_info_exit)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        finishAndGo(DashboardDetailsActivity.class);
                    }
                }).create().show();
    }

    @Override
    public void onPause(){
        Session.getSurvey().updateSurveyStatus();
        super.onPause();
    }

    @Override
    public void onStop(){
        unregisterReceiver();
        super.onStop();
    }

    /**
     * Adds the spinner for tabs
     */
    private void createMenu() {

        Log.i(".SurveyActivity", "createMenu");
        this.tabAdapter=new TabArrayAdapter(this, tabsList);

        spinner= (Spinner) this.findViewById(R.id.tabSpinner);
        //Invisible until info ready
        spinner.setVisibility(View.GONE);
        spinner.setAdapter(this.tabAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Tab selectedTab = (Tab) spinner.getSelectedItem();
                if (selectedTab.isGeneralScore()) {
                    showGeneralScores();
                } else {
                    showTab(selectedTab);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Adds actionbar to the activity
     */
    private void createActionBar(){
        Survey survey=Session.getSurvey();
        Program program = survey.getProgram();

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        LayoutUtils.setActionBarText(actionBar, survey.getOrgUnit().getName(), program.getName());
    }


    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void createProgress(){
        progressBar=(ProgressBar)findViewById(R.id.survey_progress);
    }

    /**
     * Shows the form for the given tab.
     * @param selectedTab
     */
    private void showTab(Tab selectedTab) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();

        if(selectedTab.isCompositeScore()){
            tabAdaptersCache.cacheAllTabs();
        }
        ITabAdapter tabAdapter=tabAdaptersCache.findAdapter(selectedTab);

        View view = inflater.inflate(tabAdapter.getLayout(), parent, false);
        parent.addView(view);

        if (    selectedTab.getType() == Constants.TAB_AUTOMATIC_SCORED ||
                selectedTab.getType() == Constants.TAB_CUSTOM_SCORED    ||
                selectedTab.getType() == Constants.TAB_SCORE_SUMMARY) {
            tabAdapter.initializeSubscore();
        }

        ListView mQuestions = (ListView) this.findViewById(R.id.listView);
        mQuestions.setAdapter((BaseAdapter) tabAdapter);
    }

    /**
     * Shows the special 'Score' tab
     */
    private void showGeneralScores() {
        LayoutInflater inflater = LayoutInflater.from(this);

        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();
        View view = inflater.inflate(R.layout.scoretab, parent, false);
        parent.addView(view);

        List<ITabAdapter> adaptersList = tabAdaptersCache.list();
        Float avgClinical=0F;
        Float avgRdt=0F;
        Float avgOverall=0F;
        for(ITabAdapter adapter:adaptersList){
            updateViewInGeneralScores(adapter);
            avgClinical+=valueForClinical(adapter);
            avgRdt+=valueForRdt(adapter);
            avgOverall+=valueForOverall(adapter);
        }

        avgClinical=avgClinical/3;
        avgRdt=avgRdt/3;
        avgOverall=(avgOverall+avgClinical+avgRdt)/5;

        updateAvgInGeneralScores(R.id.clinicalAvg, avgClinical);
        updateAvgInGeneralScores(R.id.rdtAvg, avgRdt);
        updateAvgInGeneralScores(R.id.totalScore, avgOverall);
    }

    private void updateViewInGeneralScores(ITabAdapter adapter){

        if(isNotAutoTabAdapterOrNull(adapter)){
            return;
        }

        Float score=adapter.getScore();
        if(score==null){
            return;
        }
        Tab tab=((AutoTabAdapter)adapter).getTab();
        int viewId=IDS_SCORES_IN_GENERAL_TAB[tab.getOrder_pos()];
        if(viewId!=0) {
            TextView textView=((TextView) this.findViewById(viewId));
            textView.setText(Utils.round(score));
            LayoutUtils.trafficLight(textView, score, null);
        }
    }

    private Float valueForClinical(ITabAdapter adapter){
        return valueForAvg(adapter,ORDER_TABS_AVG_CLINICAL);
    }

    private Float valueForRdt(ITabAdapter adapter){
        return valueForAvg(adapter,ORDER_TABS_RDT);
    }

    private Float valueForOverall(ITabAdapter adapter){
        return valueForAvg(adapter,ORDER_TABS_OVERALL);
    }

    /**
     * Returns the score of the tab inside the given adapter if the tab is relevant to the metric according to given array of positions.
     * It the tab is NOT relevant to that metric returns 0.
     * @param adapter Adapter whose tab is evaluated.
     * @param indexToConsider Arrays of positions to consider
     * @return The score of the tab or 0 if it doesnt apply for the metric.
     */
    private Float valueForAvg(ITabAdapter adapter, int[] indexToConsider){
        if(isNotAutoTabAdapterOrNull(adapter)){
            return 0F;
        }

        Float score=adapter.getScore();
        if(score==null){
            return 0F;
        }
        Tab tab=((AutoTabAdapter)adapter).getTab();
        if(contains(indexToConsider,tab.getOrder_pos())){
            return score;
        }
        return 0F;
    }

    private boolean contains(int[] array, int value){
        boolean found=false;
        for (int i=0;i<array.length;i++){
            if(array[i]==value){
                found=true;
                break;
            }
        }
        return found;
    }

    private boolean isNotAutoTabAdapterOrNull(ITabAdapter adapter){
        return adapter==null || !(adapter instanceof AutoTabAdapter);
    }

    private void updateAvgInGeneralScores(int viewId, Float score){
        ((TextView) this.findViewById(viewId)).setText(Utils.round(score));
        LayoutUtils.trafficLight(this.findViewById(viewId), score, null);
    }

    /**
     * Stops progress view and shows real form
     */
    private void stopProgress(){
        this.progressBar.setVisibility(View.GONE);
        this.spinner.setVisibility(View.VISIBLE);
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerReceiver() {
        Log.d(TAG, "registerReceiver");

        if(surveyReceiver==null){
            surveyReceiver=new SurveyReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.PREPARE_SURVEY_ACTION));
        }
    }


    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    private void  unregisterReceiver(){
        Log.d(TAG, "unregisterReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }

    /**
     * Asks SurveyService for the current list of surveys
     */
    private void prepareSurveyInfo(){
        Intent surveysIntent=new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD,SurveyService.PREPARE_SURVEY_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Builds all the adapters required for each tab.
     * @param tabs
     * @param compositeScores
     */
    private void buildAdapters(List<Tab> tabs, List<CompositeScore> compositeScores){
        for (Tab tab : tabs) {
            if (tab.isCompositeScore())
                adaptersMap.put(tab, new CompositeScoreAdapter(compositeScores, this, R.layout.composite_score_tab, tab.getName()));
            else if (!tab.isGeneralScore()) {
                adaptersMap.put(tab, AutoTabAdapter.build(tab,this));
            }
        }
    }

    /**
     * Reloads tabs info and notifies its adapter
     * @param tabs
     */
    private void reloadTabs(List<Tab> tabs){
        this.tabsList.clear();
        this.tabsList.addAll(tabs);
        this.tabAdapter.notifyDataSetChanged();
    }


    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            List<CompositeScore> compositeScores=(List<CompositeScore>)Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES);
            List<Tab> tabs=(List<Tab>)Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_TABS);

            tabAdaptersCache.reloadAdapters(tabs,compositeScores);
            reloadTabs(tabs);
            stopProgress();
        }
    }

    /**
     * Inner class that resolves each Tab as it is required (lazy manner) instead of loading all of them at once.
     */
    private class TabAdaptersCache{

        /**
         * Cache of {tab: adapter} for each tab in the survey
         */
        private Map<Tab, ITabAdapter> adapters = new HashMap<Tab, ITabAdapter>();

        /**
         * List of composite scores of the current survey
         */
        private List<CompositeScore> compositeScores;

        /**
         * Finds the right adapter according to the selected tab.
         * Tabs are lazy trying to speed up the first load
         * @param tab Tab whose adapter is searched.
         * @return The right adapter to deal with that Tab
         */
        public ITabAdapter findAdapter(Tab tab){
            ITabAdapter adapter=adapters.get(tab);
            if(adapter==null){
                adapter=buildAdapter(tab);
                //The 'Score' tab has no adapter
                if(adapter!=null) {
                    this.adapters.put(tab, adapter);
                }
            }
            return adapter;
        }

        /**
         * Resets the state of the cache.
         * Called form the receiver once data is ready.
         * @param tabs
         * @param compositeScores
         */
        public void reloadAdapters(List<Tab> tabs, List<CompositeScore> compositeScores){
            Tab firstTab=tabs.get(0);
            this.adapters.clear();
            this.adapters.put(firstTab, AutoTabAdapter.build(firstTab, SurveyActivity.this));
            this.compositeScores=compositeScores;
        }

        /**
         * Returns the list of adapters.
         * Puts every adapter (for every tab) into the cache if is not already there.
         * @return
         */
        public List<ITabAdapter> list(){
            //The cache only has loaded Tabs
            if (this.adapters.size() < tabsList.size()){
                cacheAllTabs();
            }
            //Return full list of adapters
            return new ArrayList<ITabAdapter>(this.adapters.values());

        }

        /**
         * Puts every adapter (for every tab) into the cache if is not already there.
         */
        public void cacheAllTabs(){
            for(Tab tab:tabsList){
                findAdapter(tab);
            }
        }

        /**
         * Builds the right adapter for the given tab
         * @param tab
         * @return
         */
        private ITabAdapter buildAdapter(Tab tab){
            if (tab.isCompositeScore())
                return new CompositeScoreAdapter(this.compositeScores, SurveyActivity.this, R.layout.composite_score_tab, tab.getName());

            if(tab.isGeneralScore()){
                return null;
            }

            return AutoTabAdapter.build(tab,SurveyActivity.this);
        }
    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity that supports the data entry for the surveys.
 */
public class SurveyActivity extends BaseActivity{

    public static final String TAG = ".DetailsFragment";

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
     * Adapter for  the e of tae
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
//        prepareSurveyInfo();
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
    public void onStop(){
        unregisterReceiver();
        super.onStop();
    }

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

    private void createActionBar(){
        Survey survey=Session.getSurvey();
        Program program = survey.getProgram();

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        LayoutUtils.setActionBarText(actionBar, survey.getOrgUnit().getName(), program.getName());
    }

    private void showTab(Tab selectedTab) {
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();

//        ITabAdapter tabAdapter = adaptersMap.get(selectedTab);
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

    private void showGeneralScores() {
        LayoutInflater inflater = LayoutInflater.from(this);

        ViewGroup parent = (LinearLayout) this.findViewById(R.id.content);
        parent.removeAllViews();
        View view = inflater.inflate(R.layout.scoretab, parent, false);
        parent.addView(view);

        Float tab1 = 0F, tab2 = 0F, tab3 = 0F, tab4 = 0F, tab5 = 0F, tab6 = 0F, tab7 = 0F, tab8 = 0F, tab9 = 0F;

//        List<ITabAdapter> adaptersList = new ArrayList<ITabAdapter>(adaptersMap.values());
        List<ITabAdapter> adaptersList = tabAdaptersCache.list();
        // FIXME: This is a very ugly way of doing it, change it soon
        if (adaptersList.get(10) != null) {
            tab1 = adaptersList.get(10).getScore();
            ((TextView) this.findViewById(R.id.profileScore)).setText(Utils.round(tab1));
            LayoutUtils.trafficLight(this.findViewById(R.id.profileScore), tab1, null);
        }
        if (adaptersList.get(2) != null) {
            tab9 = adaptersList.get(2).getScore();
            ((TextView) this.findViewById(R.id.envAndMatScore)).setText(Utils.round(tab9));
            LayoutUtils.trafficLight(this.findViewById(R.id.envAndMatScore), tab9, null);
        }
        if (adaptersList.get(8) != null) {
            tab8 = adaptersList.get(8).getScore();
            ((TextView) this.findViewById(R.id.feedbackScore)).setText(Utils.round(tab8));
            LayoutUtils.trafficLight(this.findViewById(R.id.feedbackScore), tab8, null);
        }
        if (adaptersList.get(6) != null) {
            tab2 = adaptersList.get(6).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase1)).setText(Utils.round(tab2));
            LayoutUtils.trafficLight(this.findViewById(R.id.clinicalCase1), tab2, null);
        }
        if (adaptersList.get(1) != null) {
            tab4 = adaptersList.get(1).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase2)).setText(Utils.round(tab4));
            LayoutUtils.trafficLight(this.findViewById(R.id.clinicalCase2), tab4, null);
        }
        if (adaptersList.get(4) != null) {
            tab6 = adaptersList.get(4).getScore();
            ((TextView) this.findViewById(R.id.clinicalCase3)).setText(Utils.round(tab6));
            LayoutUtils.trafficLight(this.findViewById(R.id.clinicalCase3), tab6, null);
        }
        if (adaptersList.get(0) != null) {
            tab3 = adaptersList.get(0).getScore();
            ((TextView) this.findViewById(R.id.rdtCase1)).setText(Utils.round(tab3));
            LayoutUtils.trafficLight(this.findViewById(R.id.rdtCase1), tab3, null);
        }
        if (adaptersList.get(9) != null) {
            tab5 = adaptersList.get(9).getScore();
            ((TextView) this.findViewById(R.id.rdtCase2)).setText(Utils.round(tab5));
            LayoutUtils.trafficLight(this.findViewById(R.id.rdtCase2), tab5, null);
        }
        if (adaptersList.get(5) != null) {
            tab7 = adaptersList.get(5).getScore();
            ((TextView) this.findViewById(R.id.rdtCase3)).setText(Utils.round(tab7));
            LayoutUtils.trafficLight(this.findViewById(R.id.rdtCase3), tab7, null);
        }

        Float avgClinical = (tab2 + tab4 + tab6) / 3;
        Float avgRdt = (tab3 + tab5 + tab7) / 3;
        Float overall = (avgClinical + avgRdt + tab1 + tab8 + tab9) / 5;

        ((TextView) this.findViewById(R.id.clinicalAvg)).setText(Utils.round(avgClinical));
        LayoutUtils.trafficLight(this.findViewById(R.id.clinicalAvg), avgClinical, null);
        ((TextView) this.findViewById(R.id.rdtAvg)).setText(Utils.round(avgRdt));
        LayoutUtils.trafficLight(this.findViewById(R.id.rdtAvg), avgRdt, null);
        ((TextView) this.findViewById(R.id.totalScore)).setText(Utils.round(overall));
        LayoutUtils.trafficLight(this.findViewById(R.id.totalScore), overall, null);

    }

    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void createProgress(){
        progressBar=(ProgressBar)findViewById(R.id.survey_progress);
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

//            buildAdapters(tabs,compositeScores);
            tabAdaptersCache.reloadAdapters(tabs,compositeScores);
            reloadTabs(tabs);
            stopProgress();
        }
    }

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

        public void reloadAdapters(List<Tab> tabs, List<CompositeScore> compositeScores){
            Tab firstTab=tabs.get(0);
            this.adapters.clear();
            this.adapters.put(firstTab, AutoTabAdapter.build(firstTab, SurveyActivity.this));
            this.compositeScores=compositeScores;
        }

        public List<ITabAdapter> list(){
            //The cache only has loaded Tabs
            if (this.adapters.size() < tabsList.size()){
                for(Tab tab:tabsList){
                    //Ensure every tab in built and cache
                    findAdapter(tab);
                }
            }
            //Return full list of adapters
            return new ArrayList<ITabAdapter>(this.adapters.values());

        }

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

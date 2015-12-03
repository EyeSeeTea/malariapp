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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositeScoreAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositeScoreAdapterPictureApp;
import org.eyeseetea.malariacare.layout.adapters.survey.CustomAdherenceAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CustomIQTABAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity that supports the data entry for the surveys.
 */
public class SurveyActivityPictureapp extends BaseActivity {

    public static final String TAG = ".SurveyActivityPictureapp";

    /**
     * List of tabs that belongs to the current selected survey
     */
    private List<Tab> tabsList=new ArrayList<>();

    private TabAdaptersCache tabAdaptersCache = new TabAdaptersCache();

    /**
     * Receiver of data from SurveyService
     */
    private SurveyReceiver surveyReceiver;

    /**
     * Progress dialog shown while loading
     */
    private ProgressBar progressBar;

    /**
     * Parent view of main content
     */
    private LinearLayout content;

    /**
     * Flags required to decide if the survey must be deleted or not
     */
    private boolean isBackPressed=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        setContentView(R.layout.surveypictureapp);
        registerReceiver();
        createActionBar();
        createProgress();
    }

    public void onResume(){
        Log.d(TAG, "onResume");
        super.onResume();

        prepareSurveyInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        Survey survey= Session.getSurvey();
        int infoMessage=survey.isInProgress()? R.string.survey_info_exit_delete: R.string.survey_info_exit;
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_title_exit)
                .setMessage(infoMessage)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        ScoreRegister.clear();
                        unregisterReceiver();
                        isBackPressed=true;
                        finishAndGo(DashboardActivity.class);
                    }
                }).create().show();
    }

    @Override
    public void onPause(){
        beforeExit();
        super.onPause();
    }

    private void beforeExit(){
        Survey survey= Session.getSurvey();
        boolean isInProgress=survey.isInProgress();

        //Exit + InProgress -> delete
        if(isBackPressed && isInProgress){
            survey.delete();
            return;
        }

        //InProgress -> update status
        if(isInProgress){
            survey.updateSurveyStatus();
        }

        //Completed | Sent -> no action
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterReceiver();
        super.onStop();
    }

    /**
     * Finds the option from the current answer associated with the given text.
     * Only for dynamicTabAdapter, required for automated testing.
     * @param text
     * @return
     */
    public Option findOptionByText(String text){
        try {
            //Find adapter
            Tab tabZero=this.tabsList.get(0);
            DynamicTabAdapter tabAdapter=(DynamicTabAdapter)this.tabAdaptersCache.findAdapter(tabZero);

            //Get options from question
            List<Option> options=tabAdapter.progressTabStatus.getCurrentQuestion().getAnswer().getOptions();

            //Return proper option if possible
            for(Option option:options){
                if(option.getName().equals(text)){
                    return option;
                }
            }
        }catch(Exception ex){
            return null;
        }
        return null;
    }

    public class AsyncChangeTab extends AsyncTask<Void, Integer, View> {

        private Tab tab;

        public AsyncChangeTab(Tab tab) {
            this.tab = tab;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startProgress();
        }

        @Override
        protected View doInBackground(Void... params) {

            Log.d(TAG, "doInBackground("+Thread.currentThread().getId()+")..");
            View view=prepareTab(tab);
            Log.d(TAG, "doInBackground(" + Thread.currentThread().getId() + ")..DONE");
            return view;
        }

        @Override
        protected void onPostExecute(View viewContent) {
            super.onPostExecute(viewContent);

            content.removeAllViews();
            content.addView(viewContent);
            ITabAdapter tabAdapter = tabAdaptersCache.findAdapter(tab);
            if (    tab.getType() == Constants.TAB_AUTOMATIC_SCORED ||
                    tab.getType() == Constants.TAB_ADHERENCE    ||
                    tab.getType() == Constants.TAB_IQATAB ||
                    tab.getType() == Constants.TAB_SCORE_SUMMARY) {
                tabAdapter.initializeSubscore();
            }
            ListView listViewTab = (ListView) SurveyActivityPictureapp.this.findViewById(R.id.listView);
            if(tabAdapter instanceof DynamicTabAdapter ){
                ((DynamicTabAdapter)tabAdapter).addOnSwipeListener(listViewTab);
            }
            listViewTab.setAdapter((BaseAdapter) tabAdapter);
            stopProgress();
        }
    }



    /**
     * Adds actionbar to the activity
     */
    private void createActionBar(){
        Survey survey= Session.getSurvey();
        Program program = survey.getProgram();

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        LayoutUtils.setActionBarText(actionBar, getApplicationContext().getResources().getString(R.string.organisation_unit), program.getName());
    }


    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void createProgress(){
        content = (LinearLayout) this.findViewById(R.id.content);
        progressBar=(ProgressBar)findViewById(R.id.survey_progress);
    }

    /**
     * Prepares the selected tab to be shown
     * @param selectedTab
     * @return
     */
    private View prepareTab(Tab selectedTab) {
        LayoutInflater inflater = LayoutInflater.from(this);

        if(selectedTab.isCompositeScore()){
            //Initialize scores x question not loaded yet
            List<Tab> notLoadedTabs=tabAdaptersCache.getNotLoadedTabs();
            ScoreRegister.initScoresForQuestions(Question.listAllByTabs(notLoadedTabs), Session.getSurvey());
        }
        ITabAdapter tabAdapter=tabAdaptersCache.findAdapter(selectedTab);

        return inflater.inflate(tabAdapter.getLayout(), content, false);
    }

    /**
     * Stops progress view and shows real form
     */
    private void stopProgress(){
        this.progressBar.setVisibility(View.GONE);
        this.content.setVisibility(View.VISIBLE);

    }

    private void startProgress(){
        this.content.setVisibility(View.GONE);
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
            LocalBroadcastManager.getInstance(this).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.PREPARE_SURVEY_ACTION));
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
    public void prepareSurveyInfo(){
        Log.d(TAG, "prepareSurveyInfo");
        Intent surveysIntent=new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PREPARE_SURVEY_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Reloads tabs info and notifies its adapter
     * @param tabs
     */
    private void reloadTabs(List<Tab> tabs){
        Log.d(TAG, "reloadTabs(" + tabs.size() + ")");

        this.tabsList.clear();
        this.tabsList.addAll(tabs);

        new AsyncChangeTab(tabs.get(0)).execute((Void) null);

        Log.d(TAG, "reloadTabs(" + tabs.size() + ")..DONE");
    }


    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive");
            List<CompositeScore> compositeScores=(List<CompositeScore>) Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES);
            List<Tab> tabs=(List<Tab>) Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_TABS);

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
         * Flag that optimizes the load of compositeScore the next time
         */
        private boolean compositeScoreTabShown=false;

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

        public List<Tab> getNotLoadedTabs(){
            List<Tab> notLoadedTabs=new ArrayList<Tab>();
            //If has already been shown NOTHING to reload
            if(compositeScoreTabShown){
                return notLoadedTabs;
            }

            compositeScoreTabShown=true;
            notLoadedTabs=new ArrayList<>(tabsList);
            Set<Tab> loadedTabs=adapters.keySet();
            notLoadedTabs.removeAll(loadedTabs);
            return notLoadedTabs;
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
            this.adapters.put(firstTab,buildAdapter(firstTab));
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
                return new CompositeScoreAdapterPictureApp(this.compositeScores, SurveyActivityPictureapp.this, R.layout.composite_score_tab, tab.getName());

            if (tab.isAdherenceTab()) {
                Log.d(TAG, "Creating an Adherence Adapter");
                return CustomAdherenceAdapter.build(tab, SurveyActivityPictureapp.this);
            }

            if (tab.isIQATab())
                return CustomIQTABAdapter.build(tab, SurveyActivityPictureapp.this);


            if(tab.isGeneralScore()){
                return null;
            }

            if (tab.isDynamicTab()){
                return new DynamicTabAdapter(tab,SurveyActivityPictureapp.this);
            }

            return AutoTabAdapter.build(tab, SurveyActivityPictureapp.this);
        }
    }
}

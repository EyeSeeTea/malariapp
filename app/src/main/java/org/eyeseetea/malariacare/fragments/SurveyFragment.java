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

package org.eyeseetea.malariacare.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.TabArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ignac on 05/01/2016.
 */
public class SurveyFragment extends  Fragment {
    private String TAG=".SurveyFragment";
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
     * List of all tabs
     */
    List<Tab> allTabs;

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

    /**
     * Parent view of main content
     */
    private LinearLayout content;

    /**
     * Actual layout to be accessible in the fragment
     */
    RelativeLayout llLayout;

    String moduleName=Constants.FRAGMENT_FEEDBACK_KEY;

    public static SurveyFragment newInstance(int index) {
        SurveyFragment f = new SurveyFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }
    @Override
         public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }
        llLayout = (RelativeLayout) inflater.inflate(R.layout.survey, container, false);
        registerReceiver();
        createMenu(moduleName);
        createProgress();
        prepareSurveyInfo();
        return llLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        this.tabAdapter.notifyDataSetChanged();
    }

    public void exit(){
        unregisterReceiver();
    }
    @Override
    public void onPause(){
        Survey survey = Session.getSurveyByModule(moduleName);
        if(survey!=null){
            survey.updateSurveyStatus();
        }
        unregisterReceiver();
        super.onPause();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterReceiver();
        super.onStop();
    }

    public void setModuleName(String simpleName) {
        this.moduleName=simpleName;

    }

    /**
     * Adds the spinner and imagebutons for tabs
     */
    private void createMenu(final String moduleName) {

        Log.d(TAG, "createMenu");
        this.tabAdapter = new TabArrayAdapter(getActivity().getApplicationContext(), tabsList);
        spinner = (Spinner) llLayout.findViewById(R.id.tabSpinner);
        //If the spinner is null, is a survey without header tabs)
        if (spinner != null) {
            //Invisible until info ready
            spinner.setVisibility(View.GONE);
            spinner.setAdapter(this.tabAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected..");
                    final Tab selectedTab = (Tab) spinner.getSelectedItem();
                    llLayout.findViewById(R.id.previous_tab).setAlpha(0f);
                    llLayout.findViewById(R.id.next_tab).setAlpha(0f);
                    new AsyncChangeTab(selectedTab).executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR,(Void) null);
                    Log.d(TAG, "onItemSelected(" + Thread.currentThread().getId() + ")..DONE");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            if (!PreferencesState.getInstance().isVerticalDashboard())
                tabPagination();
        }
    }

    private void tabPagination() {
        ImageButton nextButton = (ImageButton) llLayout.findViewById(R.id.next_tab);
        ImageButton previousButton = (ImageButton) llLayout.findViewById(R.id.previous_tab);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = currentTabPosition();
                position++;
                if (position < spinner.getAdapter().getCount())
                    setCurrentTab(position);
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = currentTabPosition();
                position--;
                if (position >= 0)
                    setCurrentTab(position);
            }
        });

    }


    /**
     * set the current tab to the position
     */
    private void setCurrentTab(int position) {
        spinner.setSelection(position);
        final Tab selectedTab = (Tab) spinner.getSelectedItem();
        new AsyncChangeTab(selectedTab).executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR,(Void) null);
        Log.d(TAG, "onItemSelected(" + Thread.currentThread().getId() + ")..DONE");
    }

    private int currentTabPosition() {
        Log.d(TAG, "onItemSelect(" + spinner.getSelectedItemPosition() + ")");
        return spinner.getSelectedItemPosition();
    }

    private void preLoadItems(){
        for(Tab tab: allTabs) {
            Intent preLoadService = new Intent(getActivity().getApplicationContext(), SurveyService.class);
            preLoadService.putExtra(Constants.MODULE_KEY, moduleName);
            preLoadService.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PRELOAD_TAB_ITEMS);
            preLoadService.putExtra("tab", tab.getId_tab());
            getActivity().getApplicationContext().startService(preLoadService);
        }
    }

    public class AsyncChangeTab extends AsyncTask<Void, Integer, View> {

        private Tab tab;

        String module;

        public AsyncChangeTab(Tab tab) {
            this.tab = tab;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //spinner
            startProgress();
        }

        @Override
        protected View doInBackground(Void... params) {
            Log.d(TAG, "doInBackground("+Thread.currentThread().getId()+")..");
            View view=null;
            try {
                if (tab.isGeneralScore()) {
                    showGeneralScores();
                } else {
                    view=prepareTab(tab, moduleName);
                }
            }catch (Exception e){
            }
            Log.d(TAG, "doInBackground(" + Thread.currentThread().getId() + ")..DONE");
            return view;
        }

        @Override
        protected void onPostExecute(View viewContent) {
            super.onPostExecute(viewContent);
            try {
                content.removeAllViews();
                content.addView(viewContent);
                ITabAdapter tabAdapter = tabAdaptersCache.findAdapter(tab);
                if (tab.getType() == Constants.TAB_AUTOMATIC ||
                        tab.getType() == Constants.TAB_ADHERENCE ||
                        tab.getType() == Constants.TAB_IQATAB ||
                        tab.getType() == Constants.TAB_REPORTING ||
                        tab.getType() == Constants.TAB_COMPOSITE_SCORE) {
                    tabAdapter.initializeSubscore();
                }
                ListView listView = (ListView) llLayout.findViewById(R.id.listView);
                if (tabAdapter instanceof DynamicTabAdapter) {
                    ((DynamicTabAdapter) tabAdapter).addOnSwipeListener(listView);
                }
                listView.setAdapter((BaseAdapter) tabAdapter);
                listView.setOnScrollListener(new UnfocusScrollListener());
                stopProgress();
                checkArrows();
            }catch (Exception e){};
        }
    }

    //Show and hide the arrows alpha=0f == transparent alpha 1f 100% visible
    private void checkArrows() {
        int position=currentTabPosition();
        if(position==0)
            llLayout.findViewById(R.id.previous_tab).setAlpha(0f);
        else
            llLayout.findViewById(R.id.previous_tab).setAlpha(1f);
        if(position==spinner.getAdapter().getCount()-1)
            llLayout.findViewById(R.id.next_tab).setAlpha(0f);
        else
            llLayout.findViewById(R.id.next_tab).setAlpha(1f);
    }

    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void createProgress(){
        content = (LinearLayout)  llLayout.findViewById(R.id.content);
        progressBar = (ProgressBar) llLayout.findViewById(R.id.survey_progress);
    }

    /**
     * Prepares the selected tab to be shown
     * @param selectedTab
     * @return
     */
    private View prepareTab(Tab selectedTab, String module) {
        LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());

        if(selectedTab.isCompositeScore()){
            //Initialize scores x question not loaded yet
            List<Tab> notLoadedTabs=tabAdaptersCache.getNotLoadedTabs();
            ScoreRegister.initScoresForQuestions(Question.listAllByTabs(notLoadedTabs), Session.getSurveyByModule(module), module);
        }
        ITabAdapter tabAdapter=tabAdaptersCache.findAdapter(selectedTab);

        return inflater.inflate(tabAdapter.getLayout(), content, false);
    }

    /**
     * Shows the special 'Score' tab
     */
    private void showGeneralScores() {
        LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());

        content.removeAllViews();
        View view = inflater.inflate(R.layout.scoretab, content, false);
        content.addView(view);

        List<ITabAdapter> adaptersList = tabAdaptersCache.list();
        Float avgClinical = 0F;
        Float avgRdt = 0F;
        Float avgOverall = 0F;
        for(ITabAdapter adapter:adaptersList){
            updateViewInGeneralScores(adapter);
            avgClinical += valueForClinical(adapter);
            avgRdt += valueForRdt(adapter);
            avgOverall += valueForOverall(adapter);
        }

        avgClinical = avgClinical/3;
        avgRdt = avgRdt/3;
        avgOverall = (avgOverall+avgClinical+avgRdt)/5;

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
            CustomTextView customTextView =((CustomTextView) llLayout.findViewById(viewId));
            customTextView.setText(AUtils.round(score));
            LayoutUtils.trafficLight(customTextView, score, null);
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
        ((CustomTextView) llLayout.findViewById(viewId)).setText(AUtils.round(score));
        LayoutUtils.trafficLight(llLayout.findViewById(viewId), score, null);
    }

    /**
     * Stops progress view and shows real form
     */
    private void stopProgress(){
        this.progressBar.setVisibility(View.INVISIBLE);
        if(this.spinner!=null)
            this.spinner.setVisibility(View.VISIBLE);
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
            LocalBroadcastManager.getInstance( getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.PREPARE_SURVEY_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void  unregisterReceiver(){
        Log.d(TAG, "unregisterReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance( getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }

    /**
     * Asks SurveyService for the current list of surveys
     */
    public void prepareSurveyInfo(){
        Log.d(TAG, "prepareSurveyInfo");
        Intent surveysIntent=new Intent(getActivity().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(Constants.MODULE_KEY,moduleName);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD,SurveyService.PREPARE_SURVEY_ACTION);
        getActivity().getApplicationContext().startService(surveysIntent);
    }
    /**
     * Reloads tabs info and notifies its adapter
     * @param tabs
     */
    private void reloadTabs(List<Tab> tabs){
        Log.d(TAG, "reloadTabs("+tabs.size()+")");

        this.tabsList.clear();
        this.tabsList.addAll(tabs);
        if(PreferencesState.getInstance().isAutomaticAdapter())
            this.tabAdapter.notifyDataSetChanged();
        else if(PreferencesState.getInstance().isDynamicAdapter()){
            new AsyncChangeTab(tabs.get(0)).executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR,(Void) null);
        }

        Log.d(TAG, "reloadTabs(" + tabs.size() + ")..DONE");
    }

    /*
    * ScrollListener added to avoid bug ocurred when checkbox pressed in a listview after this view is gone out from the focus
    * see more here: http://stackoverflow.com/questions/7100555/preventing-catching-illegalargumentexception-parameter-must-be-a-descendant-of
    */
    protected class UnfocusScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            // do nothing
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
                View currentFocus =  getActivity().getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.clearFocus();
                    // Remove the virtual keyboard from the screen
                    InputMethodManager imm = (InputMethodManager)getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }

    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive");
            List<CompositeScore> compositeScores=(List<CompositeScore>)Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES);
            List<Tab> tabs=(List<Tab>)Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_TABS);

            tabAdaptersCache.reloadAdapters(tabs, compositeScores);
            reloadTabs(tabs);
            stopProgress();
            if(PreferencesState.getInstance().isAutomaticAdapter()) {
                allTabs = (List<Tab>) Session.popServiceValue(SurveyService.PREPARE_ALL_TABS);
                // After loading first tab we start the individual services that preload the items for the rest of tabs
                preLoadItems();
            }
        }
    }

    /**
     * Inner class that resolves each Tab as it is required (lazy manner) instead of loading all of them at once.
     */
    private class TabAdaptersCache{

        /**
         * Cache of {tab: adapter} for each tab in the survey
         */
        private Map<Tab, ITabAdapter> adapters = new HashMap<>();

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
            List<Tab> notLoadedTabs=new ArrayList<>();
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
            if (PreferencesState.getInstance().isDynamicAdapter())
                this.adapters.put(firstTab, DynamicTabAdapter.build(firstTab, getActivity(),Session.getSurveyByModule(moduleName).getId_survey(), moduleName));
            if (PreferencesState.getInstance().isAutomaticAdapter())
                this.adapters.put(firstTab, AutoTabAdapter.build(firstTab, getActivity(),Session.getSurveyByModule(moduleName).getId_survey(), moduleName));
            this.compositeScores = compositeScores;
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
            return new ArrayList<>(this.adapters.values());

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
        private ITabAdapter buildAdapter(Tab tab) {
            if (PreferencesState.getInstance().isDynamicAdapter()) {
                if (tab.isDynamicTab())
                    return new DynamicTabAdapter(tab, getActivity(), Session.getSurveyByModule(moduleName).getId_survey(), moduleName);
                return null;
            }
            if (PreferencesState.getInstance().isAutomaticAdapter()) {
                return AutoTabAdapter.build(tab, getActivity(), Session.getSurveyByModule(moduleName).getId_survey(), moduleName);
            }
            return null;
        }
    }
}

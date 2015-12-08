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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.TabArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapterPictureApp;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositeScoreAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CompositeScoreAdapterPictureApp;
import org.eyeseetea.malariacare.layout.adapters.survey.CustomAdherenceAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CustomAdherenceAdapterPictureApp;
import org.eyeseetea.malariacare.layout.adapters.survey.CustomIQTABAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.CustomReportingAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.DynamicTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.hisp.dhis.android.sdk.events.UiEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity that supports the data entry for the surveys.
 */
public class SurveyActivity extends BaseActivity {

    public static final String TAG = ".SurveyActivity";

    //FIXME Better than a bunch of 'ifs' worse than it should
    private static final int ORDER_PROFILE = 2;
    private static final int ORDER_C1_CLINICAL = 3;
    private static final int ORDER_C1_RDT = 4;
    private static final int ORDER_C2_CLINICAL = 5;
    private static final int ORDER_C2_RDT = 6;
    private static final int ORDER_C3_CLINICAL = 7;
    private static final int ORDER_C3_RDT = 8;
    private static final int ORDER_FEEDBACK = 9;
    private static final int ORDER_ENVIRONMENT = 10;

    private static final int[] ORDER_TABS_AVG_CLINICAL = {ORDER_C1_CLINICAL, ORDER_C2_CLINICAL, ORDER_C3_CLINICAL};
    private static final int[] ORDER_TABS_RDT = {ORDER_C1_RDT, ORDER_C2_RDT, ORDER_C3_RDT};
    private static final int[] ORDER_TABS_OVERALL = {ORDER_PROFILE, ORDER_FEEDBACK, ORDER_ENVIRONMENT};

    private static final int[] IDS_SCORES_IN_GENERAL_TAB = {
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
    private List<Tab> tabsList = new ArrayList<>();

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
     * Flags required to decide if the survey must be deleted or not
     */
    private boolean isBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
try {
    Log.d(TAG, "onCreate");
    if (Utils.isPictureQuestion()) {
        setContentView(R.layout.surveypictureapp);
        registerReceiver();
        createActionBar();
        createProgress();
    } else {
        setContentView(R.layout.survey);
        registerReceiver();
        createActionBar();
        createMenu();
        createProgress();
        prepareSurveyInfo();
    }
    }catch (Exception ex) { Log.e(TAG, ex.toString());}

    }

    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (Utils.isPictureQuestion()) {
            prepareSurveyInfo();
        } else
            this.tabAdapter.notifyDataSetChanged();
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
        int infoMessage;
        if (Utils.isPictureQuestion()) {
            Survey survey = Session.getSurvey();
            infoMessage = survey.isInProgress() ? R.string.survey_info_exit_delete : R.string.survey_info_exit;
        } else {
            infoMessage = R.string.survey_info_exit;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.survey_title_exit)
                .setMessage(infoMessage)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        ScoreRegister.clear();
                        unregisterReceiver();
                        if (Utils.isPictureQuestion()) {
                            isBackPressed = true;
                        }
                        finishAndGo(DashboardActivity.class);
                    }
                }).create().show();
    }

    @Override
    public void onPause() {
        if (Utils.isPictureQuestion()) {
            beforeExit();
        } else {
            Session.getSurvey().updateSurveyStatus();
            unregisterReceiver();
        }
        super.onPause();
    }

    private void beforeExit() {
        Survey survey = Session.getSurvey();
        boolean isInProgress = survey.isInProgress();

        //Exit + InProgress -> delete
        if (isBackPressed && isInProgress) {
            survey.delete();
            return;
        }

        //InProgress -> update status
        if (isInProgress) {
            survey.updateSurveyStatus();
        }

        //Completed | Sent -> no action
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        unregisterReceiver();
        super.onStop();
    }

    /**
     * Adds the spinner for tabs
     */
    private void createMenu() {
        if(!Utils.isPictureQuestion()) {
            Log.d(TAG, "createMenu");
            this.tabAdapter = new TabArrayAdapter(this, tabsList);
            spinner = (Spinner) this.findViewById(R.id.tabSpinner);

            //Invisible until info ready
            spinner.setVisibility(View.GONE);
            spinner.setAdapter(this.tabAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected..");
                    final Tab selectedTab = (Tab) spinner.getSelectedItem();
                    new AsyncChangeTab(selectedTab).execute((Void) null);
                    Log.d(TAG, "onItemSelected(" + Thread.currentThread().getId() + ")..DONE");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void preLoadItems() {
        List<Tab> tabs = new Select().all().from(Tab.class).queryList();
        for (Tab tab : tabs) {
            Intent preLoadService = new Intent(this, SurveyService.class);
            preLoadService.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PRELOAD_TAB_ITEMS);
            preLoadService.putExtra("tab", tab.getId_tab());
            this.startService(preLoadService);
        }
    }

    /**
     * Finds the option from the current answer associated with the given text.
     * Only for dynamicTabAdapter, required for automated testing.
     *
     * @param text
     * @return
     */
    public Option findOptionByText(String text) {
        try {
            //Find adapter
            Tab tabZero = this.tabsList.get(0);
            DynamicTabAdapter tabAdapter = (DynamicTabAdapter) this.tabAdaptersCache.findAdapter(tabZero);

            //Get options from question
            List<Option> options = tabAdapter.progressTabStatus.getCurrentQuestion().getAnswer().getOptions();

            //Return proper option if possible
            for (Option option : options) {
                if (option.getName().equals(text)) {
                    return option;
                }
            }
        } catch (Exception ex) {
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
            //spinner
            startProgress();
        }

        @Override
        protected View doInBackground(Void... params) {

            Log.d(TAG, "doInBackground(" + Thread.currentThread().getId() + ")..");

            View view = null;
            if (Utils.isPictureQuestion()) {
                view = prepareTab(tab);
            } else {
                if (tab.isGeneralScore()) {
                    showGeneralScores();
                } else {
                    view = prepareTab(tab);
                }
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
                if (Utils.isPictureQuestion()) {

                    ITabAdapter tabAdapter = tabAdaptersCache.findAdapter(tab);
                    if (tab.getType() == Constants.TAB_AUTOMATIC_SCORED ||
                            tab.getType() == Constants.TAB_ADHERENCE ||
                            tab.getType() == Constants.TAB_IQATAB ||
                            tab.getType() == Constants.TAB_SCORE_SUMMARY) {
                        tabAdapter.initializeSubscore();
                    }
                        ListView listViewTab = (ListView) SurveyActivity.this.findViewById(R.id.listView);
                        if (tabAdapter instanceof DynamicTabAdapter) {
                            ((DynamicTabAdapter) tabAdapter).addOnSwipeListener(listViewTab);
                        }
                        listViewTab.setAdapter((BaseAdapter) tabAdapter);
                        stopProgress();
                } else {
                    ITabAdapter tabAdapter = tabAdaptersCache.findAdapter(tab);
                    if (tab.getType() == Constants.TAB_AUTOMATIC ||
                            tab.getType() == Constants.TAB_ADHERENCE ||
                            tab.getType() == Constants.TAB_IQATAB ||
                            tab.getType() == Constants.TAB_REPORTING ||
                            tab.getType() == Constants.TAB_COMPOSITE_SCORE) {
                        tabAdapter.initializeSubscore();
                    }
                    ListView mQuestions = (ListView) SurveyActivity.this.findViewById(R.id.listView);
                    mQuestions.setAdapter((BaseAdapter) tabAdapter);
                    UnfocusScrollListener unfocusScrollListener = new UnfocusScrollListener();
                    mQuestions.setOnScrollListener(unfocusScrollListener);
                    stopProgress();
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }
    }


    /**
     * Adds actionbar to the activity
     */
    private void createActionBar() {
        Survey survey = Session.getSurvey();

        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        if (Utils.isPictureQuestion()) {
            OrgUnit orgUnit = survey.getOrgUnit();
            LayoutUtils.setActionBarText(actionBar, getApplicationContext().getResources().getString(R.string.organisation_unit), orgUnit.getName());

        } else {
            //FIXME: Shall we add the tab group?
            Program program = survey.getTabGroup().getProgram();
            LayoutUtils.setActionBarText(actionBar, survey.getOrgUnit().getName(), program.getName());
        }
    }


    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void createProgress() {
        if(Utils.isPictureQuestion()){
            content = (LinearLayout) this.findViewById(R.id.content_pictureapp);
            progressBar = (ProgressBar) findViewById(R.id.survey_progress_pictureapp);
        }
        else {
            content = (LinearLayout) this.findViewById(R.id.content);
            progressBar = (ProgressBar) findViewById(R.id.survey_progress);
        }
    }

    /**
     * Prepares the selected tab to be shown
     *
     * @param selectedTab
     * @return
     */
    private View prepareTab(Tab selectedTab) {
        LayoutInflater inflater = LayoutInflater.from(this);

        if (selectedTab.isCompositeScore()) {
            //Initialize scores x question not loaded yet
            List<Tab> notLoadedTabs = tabAdaptersCache.getNotLoadedTabs();
            ScoreRegister.initScoresForQuestions(Question.listAllByTabs(notLoadedTabs), Session.getSurvey());
        }
        ITabAdapter tabAdapter = tabAdaptersCache.findAdapter(selectedTab);

        return inflater.inflate(tabAdapter.getLayout(), content, false);
    }

    /**
     * Shows the special 'Score' tab
     */
    private void showGeneralScores() {
        LayoutInflater inflater = LayoutInflater.from(this);

        content.removeAllViews();
        View view = inflater.inflate(R.layout.scoretab, content, false);
        content.addView(view);

        List<ITabAdapter> adaptersList = tabAdaptersCache.list();
        Float avgClinical = 0F;
        Float avgRdt = 0F;
        Float avgOverall = 0F;
        for (ITabAdapter adapter : adaptersList) {
            updateViewInGeneralScores(adapter);
            avgClinical += valueForClinical(adapter);
            avgRdt += valueForRdt(adapter);
            avgOverall += valueForOverall(adapter);
        }

        avgClinical = avgClinical / 3;
        avgRdt = avgRdt / 3;
        avgOverall = (avgOverall + avgClinical + avgRdt) / 5;

        updateAvgInGeneralScores(R.id.clinicalAvg, avgClinical);
        updateAvgInGeneralScores(R.id.rdtAvg, avgRdt);
        updateAvgInGeneralScores(R.id.totalScore, avgOverall);
    }

    private void updateViewInGeneralScores(ITabAdapter adapter) {

        if (isNotAutoTabAdapterOrNull(adapter)) {
            return;
        }

        Float score = adapter.getScore();
        if (score == null) {
            return;
        }
        Tab tab;
        if(Utils.isPictureQuestion())
            tab = ((AutoTabAdapterPictureApp) adapter).getTab();
        else
            tab = ((AutoTabAdapter) adapter).getTab();
        int viewId = IDS_SCORES_IN_GENERAL_TAB[tab.getOrder_pos()];
        if (viewId != 0) {
            CustomTextView customTextView = ((CustomTextView) this.findViewById(viewId));
            customTextView.setText(Utils.round(score));
            LayoutUtils.trafficLight(customTextView, score, null);
        }
    }

    private Float valueForClinical(ITabAdapter adapter) {
        return valueForAvg(adapter, ORDER_TABS_AVG_CLINICAL);
    }

    private Float valueForRdt(ITabAdapter adapter) {
        return valueForAvg(adapter, ORDER_TABS_RDT);
    }

    private Float valueForOverall(ITabAdapter adapter) {
        return valueForAvg(adapter, ORDER_TABS_OVERALL);
    }

    /**
     * Returns the score of the tab inside the given adapter if the tab is relevant to the metric according to given array of positions.
     * It the tab is NOT relevant to that metric returns 0.
     *
     * @param adapter         Adapter whose tab is evaluated.
     * @param indexToConsider Arrays of positions to consider
     * @return The score of the tab or 0 if it doesnt apply for the metric.
     */
    private Float valueForAvg(ITabAdapter adapter, int[] indexToConsider) {
        if (isNotAutoTabAdapterOrNull(adapter)) {
            return 0F;
        }

        Float score = adapter.getScore();
        if (score == null) {
            return 0F;
        }
        Tab tab;
        if(Utils.isPictureQuestion())
            tab = ((AutoTabAdapterPictureApp) adapter).getTab();
        else
            tab = ((AutoTabAdapter) adapter).getTab();
        if (contains(indexToConsider, tab.getOrder_pos())) {
            return score;
        }
        return 0F;
    }

    private boolean contains(int[] array, int value) {
        boolean found = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                found = true;
                break;
            }
        }
        return found;
    }

    private boolean isNotAutoTabAdapterOrNull(ITabAdapter adapter) {
        return adapter == null || !(adapter instanceof AutoTabAdapter) || (adapter instanceof  AutoTabAdapterPictureApp);
    }

    private void updateAvgInGeneralScores(int viewId, Float score) {
        ((CustomTextView) this.findViewById(viewId)).setText(Utils.round(score));
        LayoutUtils.trafficLight(this.findViewById(viewId), score, null);
    }

    /**
     * Stops progress view and shows real form
     */
    private void stopProgress() {
        try{
        this.progressBar.setVisibility(View.GONE);
        if(!Utils.isPictureQuestion())
        this.spinner.setVisibility(View.VISIBLE);
        this.content.setVisibility(View.VISIBLE);

        }catch (Exception ex) { Log.e(TAG, ex.toString());}
    }

    private void startProgress() {
        this.content.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
        this.progressBar.setEnabled(true);
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    public void registerReceiver() {
        Log.d(TAG, "registerReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.PREPARE_SURVEY_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterReceiver() {
        Log.d(TAG, "unregisterReceiver");
        if (surveyReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    /**
     * Asks SurveyService for the current list of surveys
     */
    public void prepareSurveyInfo() {
        Log.d(TAG, "prepareSurveyInfo");
        Intent surveysIntent = new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PREPARE_SURVEY_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Reloads tabs info and notifies its adapter
     *
     * @param tabs
     */
    private void reloadTabs(List<Tab> tabs) {
        Log.d(TAG, "reloadTabs(" + tabs.size() + ")");

        this.tabsList.clear();
        this.tabsList.addAll(tabs);
        if (Utils.isPictureQuestion()) {
            new AsyncChangeTab(tabs.get(0)).execute((Void) null);
        } else {
            this.tabAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "reloadTabs(" + tabs.size() + ")..DONE");
    }

    @Subscribe
    public void onLogoutFinished(UiEvent uiEvent) {
        super.onLogoutFinished(uiEvent);
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
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.clearFocus();
                    // Remove the virtual keyboard from the screen
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }

    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            List<CompositeScore> compositeScores = (List<CompositeScore>) Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_COMPOSITE_SCORES);
            List<Tab> tabs = (List<Tab>) Session.popServiceValue(SurveyService.PREPARE_SURVEY_ACTION_TABS);
try {
    tabAdaptersCache.reloadAdapters(tabs, compositeScores);
    reloadTabs(tabs);
    stopProgress();
} catch(Exception ex) { Log.e(TAG, ex.toString());}
            if (!Utils.isPictureQuestion()) {
                // After loading first tab we start the individual services that preload the items for the rest of tabs
                preLoadItems();
            }
        }
    }

    /**
     * Inner class that resolves each Tab as it is required (lazy manner) instead of loading all of them at once.
     */
    private class TabAdaptersCache {

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
        private boolean compositeScoreTabShown = false;

        /**
         * Finds the right adapter according to the selected tab.
         * Tabs are lazy trying to speed up the first load
         *
         * @param tab Tab whose adapter is searched.
         * @return The right adapter to deal with that Tab
         */
        public ITabAdapter findAdapter(Tab tab) {
            ITabAdapter adapter = adapters.get(tab);
            if (adapter == null) {
                adapter = buildAdapter(tab);
                //The 'Score' tab has no adapter
                if (adapter != null) {
                    this.adapters.put(tab, adapter);
                }
            }
            return adapter;
        }

        public List<Tab> getNotLoadedTabs() {
            List<Tab> notLoadedTabs;
            if (Utils.isPictureQuestion()) {
                notLoadedTabs = new ArrayList<Tab>();
            } else {
                notLoadedTabs = new ArrayList<>();
            }

            //If has already been shown NOTHING to reload
            if (compositeScoreTabShown) {
                return notLoadedTabs;
            }

            compositeScoreTabShown = true;
            notLoadedTabs = new ArrayList<>(tabsList);
            Set<Tab> loadedTabs = adapters.keySet();
            notLoadedTabs.removeAll(loadedTabs);
            return notLoadedTabs;
        }

        /**
         * Resets the state of the cache.
         * Called form the receiver once data is ready.
         *
         * @param tabs
         * @param compositeScores
         */
        public void reloadAdapters(List<Tab> tabs, List<CompositeScore> compositeScores) {
            Tab firstTab = tabs.get(0);
            this.adapters.clear();
            if (Utils.isPictureQuestion()) {
                Log.d(TAG,firstTab.toString()+" AdapterTAB "+buildAdapter(firstTab).getAdapter().isEmpty()+"");
                this.adapters.put(firstTab, buildAdapter(firstTab));
            } else {
                this.adapters.put(firstTab, AutoTabAdapter.build(firstTab, SurveyActivity.this));
            }
            this.compositeScores = compositeScores;
        }

        /**
         * Returns the list of adapters.
         * Puts every adapter (for every tab) into the cache if is not already there.
         *
         * @return
         */
        public List<ITabAdapter> list() {
            //The cache only has loaded Tabs
            if (this.adapters.size() < tabsList.size()) {
                cacheAllTabs();
            }
            //Return full list of adapters
            if (Utils.isPictureQuestion()) {

                return new ArrayList<ITabAdapter>(this.adapters.values());
            } else {
                return new ArrayList<>(this.adapters.values());
            }

        }

        /**
         * Puts every adapter (for every tab) into the cache if is not already there.
         */
        public void cacheAllTabs() {
            for (Tab tab : tabsList) {
                findAdapter(tab);
            }
        }

        /**
         * Builds the right adapter for the given tab
         *
         * @param tab
         * @return
         */
        private ITabAdapter buildAdapter(Tab tab) {
            if (Utils.isPictureQuestion()) {
                Log.d(TAG,"Type: "+tab.getType());
                if (tab.isCompositeScore())
                    return new CompositeScoreAdapterPictureApp(this.compositeScores, SurveyActivity.this, R.layout.composite_score_tab, tab.getName());

                if (tab.isAdherenceTab()) {
                    Log.d(TAG, "Creating an Adherence Adapter");
                    return CustomAdherenceAdapterPictureApp.build(tab, SurveyActivity.this);
                }

                if (tab.isIQATab())
                    return CustomIQTABAdapter.build(tab, SurveyActivity.this);


                if (tab.isGeneralScore()) {
                    return null;
                }

                if (tab.isDynamicTab()) {
                    return new DynamicTabAdapter(tab, SurveyActivity.this);
                }
                return AutoTabAdapterPictureApp.build(tab, SurveyActivity.this);
            } else {
                switch (tab.getType()) {
                    case Constants.TAB_COMPOSITE_SCORE:
                        return CompositeScoreAdapter.build(tab, SurveyActivity.this);
                    case Constants.TAB_IQATAB:
                        return CustomIQTABAdapter.build(tab, SurveyActivity.this);
                    case Constants.TAB_ADHERENCE:
                        return CustomAdherenceAdapter.build(tab, SurveyActivity.this);
                    case Constants.TAB_REPORTING:
                        return CustomReportingAdapter.build(tab, SurveyActivity.this);
                }
                return AutoTabAdapter.build(tab, SurveyActivity.this);
            }
        }
    }
}

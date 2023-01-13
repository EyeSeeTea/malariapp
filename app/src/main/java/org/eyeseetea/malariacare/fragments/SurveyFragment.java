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


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;

import com.google.common.collect.Iterables;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.repositories.SurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.subscriber.DomainEventPublisher;
import org.eyeseetea.malariacare.domain.subscriber.DomainEventSubscriber;
import org.eyeseetea.malariacare.domain.subscriber.event.ValueChangedEvent;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;
import org.eyeseetea.malariacare.domain.usecase.SaveSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.adapters.general.TabArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.ITabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.presentation.presenters.surveys.SurveyPresenter;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ignac on 05/01/2016.
 */
public class SurveyFragment extends Fragment implements DomainEventSubscriber<ValueChangedEvent>, SurveyPresenter.View {
    private String TAG = ".SurveyFragment";


    ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
            new SurveyAnsweredRatioRepository();
    IAsyncExecutor asyncExecutor;
    IMainExecutor mainExecutor;
    SurveyAnsweredRatio mSurveyAnsweredRatio;

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

    private static final int[] ORDER_TABS_AVG_CLINICAL =
            {ORDER_C1_CLINICAL, ORDER_C2_CLINICAL, ORDER_C3_CLINICAL};
    private static final int[] ORDER_TABS_RDT = {ORDER_C1_RDT, ORDER_C2_RDT, ORDER_C3_RDT};
    private static final int[] ORDER_TABS_OVERALL =
            {ORDER_PROFILE, ORDER_FEEDBACK, ORDER_ENVIRONMENT};

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
    private List<TabDB> tabsList = new ArrayList<>();

    /**
     * List of all tabs
     */
    List<TabDB> allTabs;

    private TabAdaptersCache tabAdaptersCache = new TabAdaptersCache();

    /**
     * Adapter for the tabs actionSpinner
     */
    private TabArrayAdapter tabAdapter;

    /**
     * Progress text shown while loading
     */
    public static CustomTextView progressText;
    public static Iterator<String> messageIterator;
    private static ListView listView;
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

    String moduleName = Constants.FRAGMENT_FEEDBACK_KEY;

    SurveyPresenter surveyPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferencesState.getInstance().initalizateActivityDependencies();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }

        llLayout = (RelativeLayout) inflater.inflate(R.layout.survey, container, false);

        createMenu(moduleName);
        createProgress();
        createBackButton();
        DomainEventPublisher.instance().subscribe(this);

        initializeSurvey();

        initPresenter();

        return llLayout;
    }

    private void initPresenter() {
        surveyPresenter = DataFactory.INSTANCE.provideSurveyPresenter();

        surveyPresenter.attachView(this,moduleName);
    }

    private void initializeSurvey() {
        final SurveyDB survey = Session.getSurveyByModule(moduleName);

        if (survey == null) return;

        surveyAnsweredRatioRepository = new SurveyAnsweredRatioRepository();
        asyncExecutor = new AsyncExecutor();
        mainExecutor = new UIThreadExecutor();
        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase =
                new GetSurveyAnsweredRatioUseCase(surveyAnsweredRatioRepository,
                        mainExecutor, asyncExecutor);

        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        Log.d(getClass().getName(), "onComplete");
                        mSurveyAnsweredRatio = surveyAnsweredRatio;
                        tabAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void createBackButton() {
        ImageButton goback = (ImageButton) llLayout.findViewById(
                R.id.backToSentSurveys);
        goback.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          getActivity().onBackPressed();
                                      }
                                  }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Subscriber onDestroy");
        DomainEventPublisher.instance().unSubscribe(this);

        surveyPresenter.detachView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    public void nextProgressMessage() {
        if (messageIterator != null) {
            if (messageIterator.hasNext()) {
                progressText.setText(messageIterator.next());
            }
        }
    }

    private void createProgressMessages() {
        List<String> messagesList = new ArrayList<>();
        //// FIXME: 20/03/2017 it is a fake flow.
        messagesList.add(
                PreferencesState.getInstance().getContext().getString(R.string.survey_first_step));
        messagesList.add(
                PreferencesState.getInstance().getContext().getString(R.string.survey_second_step));
        messagesList.add(
                PreferencesState.getInstance().getContext().getString(R.string.survey_third_step));
        messagesList.add(
                PreferencesState.getInstance().getContext().getString(R.string.survey_fourth_step));
        messageIterator = Iterables.cycle(messagesList).iterator();
    }

    public void setModuleName(String simpleName) {
        this.moduleName = simpleName;
    }

    /**
     * Adds the actionSpinner and imagebutons for tabs
     */
    private void createMenu(final String moduleName) {

        Log.d(TAG, "createMenu");
        this.tabAdapter = new TabArrayAdapter(getActivity().getApplicationContext(), tabsList);
        spinner = (Spinner) llLayout.findViewById(R.id.tabSpinner);
        //If the actionSpinner is null, is a survey without header tabs)
        if (spinner != null) {
            //Invisible until info ready
            spinner.setVisibility(View.GONE);
            spinner.setAdapter(this.tabAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position,
                                           long id) {
                    Log.d(TAG, "onItemSelected..");
                    final TabDB selectedTab = (TabDB) spinner.getSelectedItem();
                    llLayout.findViewById(R.id.previous_tab).setAlpha(0f);
                    llLayout.findViewById(R.id.next_tab).setAlpha(0f);
                    new AsyncChangeTab(selectedTab).executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
                    Log.d(TAG, "onItemSelected(" + Thread.currentThread().getId() + ")..DONE");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            if (!PreferencesState.getInstance().isVerticalDashboard()) {
                tabPagination();
            }
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
                if (position < spinner.getAdapter().getCount()) {
                    setCurrentTab(position);
                }
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = currentTabPosition();
                position--;
                if (position >= 0) {
                    setCurrentTab(position);
                }
            }
        });

    }


    /**
     * set the current tab to the position
     */
    private void setCurrentTab(int position) {
        spinner.setSelection(position);
        final TabDB selectedTab = (TabDB) spinner.getSelectedItem();
        new AsyncChangeTab(selectedTab).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                (Void) null);
        Log.d(TAG, "onItemSelected(" + Thread.currentThread().getId() + ")..DONE");
    }

    private int currentTabPosition() {
        Log.d(TAG, "onItemSelect(" + spinner.getSelectedItemPosition() + ")");
        return spinner.getSelectedItemPosition();
    }

    private void preLoadItems() {
        for (TabDB tab : allTabs) {
            surveyPresenter.preLoadTabItems( tab.getId_tab(), moduleName);
        }
    }

    @Override
    public void handleEvent(final ValueChangedEvent valueChangedEvent) {
        Log.d(TAG, "handleEvent");
        mainExecutor.run(new Runnable() {
            @Override
            public void run() {
                runChartUpdate(valueChangedEvent);
            }
        });
    }

    private void runChartUpdate(final ValueChangedEvent valueChangedEvent) {
        final DoublePieChart doublePieChart =
                (DoublePieChart) DashboardActivity.dashboardActivity.getSupportActionBar
                        ().getCustomView().findViewById(
                        R.id.action_bar_chart);
        doublePieChart.setVisibility(View.VISIBLE);
        for (Question question : valueChangedEvent.getQuestions()) {
            if (question.isComputable()) {
                if (valueChangedEvent.getAction().equals(
                        ValueChangedEvent.Action.INSERT)) {
                    mSurveyAnsweredRatio.addQuestion(question.isCompulsory());
                } else if (valueChangedEvent.getAction().equals(
                        ValueChangedEvent.Action.DELETE)) {
                    mSurveyAnsweredRatio.removeQuestion(question.isCompulsory());
                } else if (valueChangedEvent.getAction().equals(
                        ValueChangedEvent.Action.TOGGLE)) {
                    mSurveyAnsweredRatio.fixTotalQuestion(question.isCompulsory(),
                            question.isCachedVisibility());
                    if (question.isRemoved()) {
                        mSurveyAnsweredRatio.removeQuestion(question.isCompulsory());
                    }
                }
            }
        }

        SaveSurveyAnsweredRatioUseCase saveSurveyAnsweredRatioUseCase =
                new SaveSurveyAnsweredRatioUseCase(
                        new SurveyAnsweredRatioRepository(), mainExecutor,
                        asyncExecutor);
        saveSurveyAnsweredRatioUseCase.execute(
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                    }

                    @Override
                    public void onComplete(
                            SurveyAnsweredRatio surveyAnsweredRatio) {
                        if (surveyAnsweredRatio != null) {
                            LayoutUtils.updateChart(mSurveyAnsweredRatio,
                                    doublePieChart);
                        }
                    }
                }, mSurveyAnsweredRatio);
    }

    @Override
    public Class<ValueChangedEvent> subscribedToEventType() {
        return ValueChangedEvent.class;
    }

    @Override
    public void showData( List<CompositeScoreDB> compositeScores, List<TabDB> tabs) {
        tabAdaptersCache.reloadAdapters(tabs, compositeScores);
        reloadTabs(tabs);
        stopProgress();

        allTabs = tabs;

        // After loading first tab we start the individual services that preload the items
        // for the rest of tabs
        preLoadItems();
    }

    @Override
    public void showNetworkError() {
        Log.e(this.getClass().getSimpleName(), "Network Error");
    }

    public class AsyncChangeTab extends AsyncTask<Void, Integer, View> {

        private TabDB tab;

        String module;

        public AsyncChangeTab(TabDB tab) {
            this.tab = tab;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //actionSpinner
            startProgress();
        }

        @Override
        protected View doInBackground(Void... params) {
            Log.d(TAG, "doInBackground(" + Thread.currentThread().getId() + ")..");
            View view = null;
            try {
                if (tab.isGeneralScore()) {
                    showGeneralScores();
                } else {
                    view = prepareTab(tab, moduleName);
                }
            } catch (Exception e) {
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
                listView = (ListView) llLayout.findViewById(R.id.listView);
                listView.setAdapter((BaseAdapter) tabAdapter);
                listView.setOnScrollListener(new UnfocusScrollListener());
                stopProgress();
                checkArrows();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Show and hide the arrows alpha=0f == transparent alpha 1f 100% visible
    private void checkArrows() {
        int position = currentTabPosition();
        if (position == 0) {
            llLayout.findViewById(R.id.previous_tab).setAlpha(0f);
        } else {
            llLayout.findViewById(R.id.previous_tab).setAlpha(1f);
        }
        if (position == spinner.getAdapter().getCount() - 1) {
            llLayout.findViewById(R.id.next_tab).setAlpha(0f);
        } else {
            llLayout.findViewById(R.id.next_tab).setAlpha(1f);
        }
    }

    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void createProgress() {
        content = (LinearLayout) llLayout.findViewById(R.id.content);
        progressBar = (ProgressBar) llLayout.findViewById(R.id.survey_progress);
        progressText = (CustomTextView) llLayout.findViewById(R.id.progress_text);
        createProgressMessages();
    }

    /**
     * Stops progress view and shows real form
     */
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    public void showProgress() {
        content.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setEnabled(true);
        progressText.setVisibility(View.VISIBLE);
    }

    /**
     * Prepares the selected tab to be shown
     */
    private View prepareTab(TabDB selectedTab, String module) {
        LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());

        if (selectedTab.isCompositeScore()) {
            //Initialize scores x question not loaded yet
            List<TabDB> notLoadedTabs = tabAdaptersCache.getNotLoadedTabs();
            ScoreRegister.initScoresForQuestions(QuestionDB.listAllByTabs(notLoadedTabs),
                    Session.getSurveyByModule(module), module);
        }
        ITabAdapter tabAdapter = tabAdaptersCache.findAdapter(selectedTab);

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
        TabDB tab = ((AutoTabAdapter) adapter).getTab();
        int viewId = IDS_SCORES_IN_GENERAL_TAB[tab.getOrder_pos()];
        if (viewId != 0) {
            CustomTextView customTextView = ((CustomTextView) llLayout.findViewById(viewId));
            customTextView.setText(AUtils.round(score,2));
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
     * Returns the score of the tab inside the given adapter if the tab is relevant to the metric
     * according to given array of positions.
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
        TabDB tab = ((AutoTabAdapter) adapter).getTab();
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
        return adapter == null || !(adapter instanceof AutoTabAdapter);
    }

    private void updateAvgInGeneralScores(int viewId, Float score) {
        ((CustomTextView) llLayout.findViewById(viewId)).setText(AUtils.round(score,2));
        LayoutUtils.trafficLight(llLayout.findViewById(viewId), score, null);
    }

    /**
     * Stops progress view and shows real form
     */
    private void stopProgress() {
        this.progressBar.setVisibility(View.INVISIBLE);
        if (this.spinner != null) {
            this.spinner.setVisibility(View.VISIBLE);
        }
        this.content.setVisibility(View.VISIBLE);

    }

    private void startProgress() {
        this.content.setVisibility(View.GONE);
        this.progressBar.setVisibility(View.VISIBLE);
        this.progressBar.setEnabled(true);
    }

    /**
     * Reloads tabs info and notifies its adapter
     */
    private void reloadTabs(List<TabDB> tabs) {
        Log.d(TAG, "reloadTabs(" + tabs.size() + ")");

        this.tabsList.clear();
        this.tabsList.addAll(tabs);
        this.tabAdapter.notifyDataSetChanged();

        Log.d(TAG, "reloadTabs(" + tabs.size() + ")..DONE");
    }

    /*
     * ScrollListener added to avoid bug ocurred when checkbox pressed in a listview after this
     * view is gone out from the focus
     * see more here: http://stackoverflow
     * .com/questions/7100555/preventing-catching-illegalargumentexception-parameter-must-be-a
     * -descendant-of
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
                View currentFocus = getActivity().getCurrentFocus();
                if (currentFocus != null) {
                    currentFocus.clearFocus();
                    // Remove the virtual keyboard from the screen
                    InputMethodManager imm =
                            (InputMethodManager) getActivity().getApplicationContext()
                                    .getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }

    }

    /**
     * Inner class that resolves each Tab as it is required (lazy manner) instead of loading all of
     * them at once.
     */
    private class TabAdaptersCache {

        /**
         * Cache of {tab: adapter} for each tab in the survey
         */
        private Map<TabDB, ITabAdapter> adapters = new HashMap<>();

        /**
         * List of composite scores of the current survey
         */
        private List<CompositeScoreDB> compositeScores;

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
        public ITabAdapter findAdapter(TabDB tab) {
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

        public List<TabDB> getNotLoadedTabs() {
            List<TabDB> notLoadedTabs = new ArrayList<>();
            //If has already been shown NOTHING to reload
            if (compositeScoreTabShown) {
                return notLoadedTabs;
            }

            compositeScoreTabShown = true;
            notLoadedTabs = new ArrayList<>(tabsList);
            Set<TabDB> loadedTabs = adapters.keySet();
            notLoadedTabs.removeAll(loadedTabs);
            return notLoadedTabs;
        }

        /**
         * Resets the state of the cache.
         * Called form the receiver once data is ready.
         */
        public void reloadAdapters(List<TabDB> tabs, List<CompositeScoreDB> compositeScores) {
            TabDB firstTab = tabs.get(0);
            this.adapters.clear();
            this.adapters.put(firstTab, AutoTabAdapter.build(firstTab, getActivity(),
                    Session.getSurveyByModule(moduleName).getId_survey(), moduleName));
            this.compositeScores = compositeScores;
        }

        /**
         * Returns the list of adapters.
         * Puts every adapter (for every tab) into the cache if is not already there.
         */
        public List<ITabAdapter> list() {
            //The cache only has loaded Tabs
            if (this.adapters.size() < tabsList.size()) {
                cacheAllTabs();
            }
            //Return full list of adapters
            return new ArrayList<>(this.adapters.values());

        }

        /**
         * Puts every adapter (for every tab) into the cache if is not already there.
         */
        public void cacheAllTabs() {
            for (TabDB tab : tabsList) {
                findAdapter(tab);
            }
        }

        /**
         * Builds the right adapter for the given tab
         */
        private ITabAdapter buildAdapter(TabDB tab) {
            return AutoTabAdapter.build(tab, getActivity(),
                    Session.getSurveyByModule(moduleName).getId_survey(), moduleName);
        }
    }
}

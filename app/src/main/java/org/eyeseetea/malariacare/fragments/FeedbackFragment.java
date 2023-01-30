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

import static org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.fragments.strategies.AFeedbackFragmentStrategy;
import org.eyeseetea.malariacare.fragments.strategies.FeedbackFragmentStrategy;
import org.eyeseetea.malariacare.layout.adapters.survey.FeedbackAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomButton;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;


public class FeedbackFragment extends Fragment implements IModuleFragment {

    public static final String TAG = ".FeedbackActivity";

    /**
     * Receiver of data from SurveyService
     */
    private SurveyReceiver surveyReceiver;

    /**
     * Progress dialog shown while loading
     */
    private RelativeLayout progressBarContainer;

    /**
     * Checkbox that toggle between all|failed questions
     */
    private CustomRadioButton chkFailed;

    /**
     * Checkbox that toggle between all|containing media questions
     */
    private CustomRadioButton chkMedia;
    /**
     * planAction that toggle between all|failed questions
     */
    private CustomButton planAction;

    /**
     * List view adapter for items
     */
    private FeedbackAdapter feedbackAdapter;

    /**
     * List view items
     */
    private ListView feedbackListView;


    private String moduleName;

    /**
     * Parent layout
     */
    RelativeLayout llLayout;

    AFeedbackFragmentStrategy mFeedbackFragmentStrategy;

    private static String SERVER_CLASSIFICATION = "ServerClassification";
    private ServerClassification serverClassification;

    public static FeedbackFragment newInstance(ServerClassification serverClassification) {
        FeedbackFragment fragment = new FeedbackFragment();

        Bundle args = new Bundle();
        args.putInt(SERVER_CLASSIFICATION, serverClassification.getCode());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        serverClassification = ServerClassification.Companion.get(
                getArguments().getInt(SERVER_CLASSIFICATION));

        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        llLayout = (RelativeLayout) inflater.inflate(R.layout.feedback, container, false);
        mFeedbackFragmentStrategy = new FeedbackFragmentStrategy();
        prepareUI(moduleName);
        //Starts the background service only one time
        startProgress();
        registerReceiver();
        prepareFeedbackInfo();
        return llLayout; // We must return the loaded Layout
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        List<Feedback> feedbackList = new ArrayList<>();
        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        startProgress();
        registerReceiver();
        loadDataIfExistsInMemory();
    }

    //If the feedback service finish on background all the necessary data is in memory
    private void loadDataIfExistsInMemory() {
        if (feedbackAdapter != null) {
            List<Feedback> feedbackList = (List<Feedback>) Session.popServiceValue(
                    PREPARE_FEEDBACK_ACTION_ITEMS);
            if (feedbackList != null && feedbackList.size() > 0) {
                loadItems(feedbackList);
            }
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver();
        super.onPause();
    }

    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void prepareUI(String module) {
        //Get progress
        progressBarContainer = llLayout.findViewById(
                R.id.survey_progress_container);

        //Set adapter and list
        feedbackAdapter = new FeedbackAdapter(getActivity(),
                Session.getSurveyByModule(module).getId_survey(), module);
        feedbackListView = llLayout.findViewById(R.id.feedbackListView);
        feedbackListView.setAdapter(feedbackAdapter);
        feedbackListView.setDivider(null);
        feedbackListView.setDividerHeight(0);

        //And checkbox listener
        chkFailed = llLayout.findViewById(R.id.chkFailed);
        chkFailed.setChecked(true);
        chkFailed.setOnClickListener(v -> {
                    feedbackAdapter.toggleOnlyFailed();
                    ((CustomRadioButton) v).setChecked(feedbackAdapter
                            .isOnlyFailed());
                }
        );
        chkMedia = llLayout.findViewById(R.id.chkMedia);
        chkMedia.setChecked(false);
        chkMedia.setOnClickListener(v -> {
                    feedbackAdapter.toggleOnlyMedia();
                    ((CustomRadioButton) v).setChecked(feedbackAdapter
                            .isOnlyMedia());
                }
        );
        planAction = llLayout.findViewById(R.id.action_plan);
        planAction.setOnClickListener(v -> DashboardActivity.dashboardActivity.openActionPlan()
        );
        ImageButton goback = llLayout.findViewById(
                R.id.backToSentSurveys);
        goback.setOnClickListener(v -> getActivity().onBackPressed()
        );

        //Set mainscore and color.
        SurveyDB survey = Session.getSurveyByModule(module);
        if (survey.hasMainScore()) {
            float average = survey.getMainScoreValue();
            CustomTextView item = llLayout.findViewById(R.id.feedback_total_score);
            item.setText(AUtils.round(average,2));
            int colorId = LayoutUtils.trafficColor(average);
            mFeedbackFragmentStrategy.setTotalPercentColor(item, colorId, getActivity());
        } else {
            CustomTextView item = llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("NaN"));
            float average = 0;
            int colorId = LayoutUtils.trafficColor(average);
            mFeedbackFragmentStrategy.setTotalPercentColor(item, colorId, getActivity());
        }

        renderHeaderByServerClassification(survey);
    }

    private void renderHeaderByServerClassification(SurveyDB survey) {
        CustomTextView competencyTextView = llLayout.findViewById(R.id.feedback_competency);

        if (serverClassification == ServerClassification.COMPETENCIES) {
            CompetencyScoreClassification classification =
                    CompetencyScoreClassification.get(
                            survey.getCompetencyScoreClassification());

            CompetencyUtils.setTextByCompetency(competencyTextView, classification);
            CompetencyUtils.setBackgroundByCompetency(competencyTextView, classification);
            CompetencyUtils.setTextColorByCompetency(competencyTextView, classification);
            competencyTextView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            String text = getString(R.string.quality_of_care);
            competencyTextView.setText(text);
        }
    }

    private void loadItems(List<Feedback> items) {
        this.feedbackAdapter.setItems(items);
        stopProgress();
    }

    /**
     * Stops progress view and shows real data
     */
    private void stopProgress() {
        this.progressBarContainer.setVisibility(View.GONE);
        this.feedbackListView.setVisibility(View.VISIBLE);
    }

    /**
     * Starts progress view, hiding list temporarily
     */
    private void startProgress() {
        this.feedbackListView.setVisibility(View.GONE);
        this.progressBarContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    public void registerReceiver() {
        Log.d(TAG, "registerReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver,
                    new IntentFilter(SurveyService.PREPARE_FEEDBACK_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterReceiver() {
        Log.d(TAG, "unregisterReceiver");
        if (surveyReceiver != null) {
            LocalBroadcastManager.getInstance(
                    getActivity().getApplicationContext()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    /**
     * Asks SurveyService for the current list of surveys
     */
    public void prepareFeedbackInfo() {
        Log.d(TAG, "prepareFeedbackInfo");
        Intent surveysIntent = new Intent(getActivity().getApplicationContext(),
                SurveyService.class);
        surveysIntent.putExtra(Constants.MODULE_KEY, moduleName);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PREPARE_FEEDBACK_ACTION);
        getActivity().getApplicationContext().startService(surveysIntent);
    }

    public void setModuleName(String simpleName) {
        this.moduleName = simpleName;
    }

    @Override
    public void reloadData() {
        if (feedbackAdapter != null) {
            List<Feedback> feedbackList = (List<Feedback>) Session.popServiceValue(
                    PREPARE_FEEDBACK_ACTION_ITEMS);
            loadItems(feedbackList);
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
            if (SurveyService.PREPARE_FEEDBACK_ACTION.equals(intent.getAction())) {
                List<Feedback> feedbackList = (List<Feedback>) Session.popServiceValue(
                        PREPARE_FEEDBACK_ACTION_ITEMS);
                loadItems(feedbackList);
            }
        }
    }

}

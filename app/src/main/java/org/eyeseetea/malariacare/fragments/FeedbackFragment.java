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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.layout.adapters.survey.FeedbackAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Created by ignac on 07/01/2016.
 */
public class FeedbackFragment extends Fragment {

    public static final String TAG = ".FeedbackActivity";

    /**
     * Receiver of data from SurveyService
     */
    private SurveyReceiver surveyReceiver;

    /**
     * Progress dialog shown while loading
     */
    private ProgressBar progressBar;

    /**
     * Checkbox that toggle between all|failed questions
     */
    private CustomRadioButton chkFailed;

    /**
     * List view adapter for items
     */
    private FeedbackAdapter feedbackAdapter;

    /**
     * List view items
     */
    private ListView feedbackListView;

    /**
     * Menu of the activity
     */
    private Menu menu;


    /**
     * Parent layout
     */
    RelativeLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentActivity    faActivity  = (FragmentActivity)    super.getActivity();
        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        llLayout = (RelativeLayout) inflater.inflate(R.layout.feedback, container, false);
        prepareUI();

        return llLayout; // We must return the loaded Layout
    }

    public static FeedbackFragment newInstance(int index) {
        FeedbackFragment f = new FeedbackFragment();

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
        prepareFeedbackInfo();
    }
    @Override
    public void onPause(){
        unregisterReceiver();
        super.onPause();
    }

    /**
     * Gets a reference to the progress view in order to stop it later
     */
    private void prepareUI(){
        //Get progress
        progressBar=(ProgressBar)llLayout.findViewById(R.id.survey_progress);

        //Set adapter and list
        feedbackAdapter=new FeedbackAdapter(getActivity(),Session.getSurvey().getId_survey());
        feedbackListView=(ListView)llLayout.findViewById(R.id.feedbackListView);
        feedbackListView.setAdapter(feedbackAdapter);

        //And checkbox listener
        chkFailed=(CustomRadioButton)llLayout.findViewById(R.id.chkFailed);
        chkFailed.setChecked(true);
        chkFailed.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             feedbackAdapter.toggleOnlyFailed();
                                             ((CustomRadioButton) v).setChecked(feedbackAdapter.isOnlyFailed());
                                         }
                                     }
        );
        CustomRadioButton goback=(CustomRadioButton)llLayout.findViewById(R.id.backToSentSurveys);
        goback.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          getActivity().onBackPressed();
                                      }
                                  }
        );
        
        //Set mainscore and color.
        Survey survey = Session.getSurvey();
        float average = survey.getMainScore();
        CustomTextView item= (CustomTextView)llLayout.findViewById(R.id.feedback_total_score);
        item.setText(String.format("%.1f%%", average));
        int colorId= LayoutUtils.trafficColor(average);
        item.setTextColor(getResources().getColor(colorId));
    }

    private void loadItems(List<Feedback> items){
        this.feedbackAdapter.setItems(items);
        stopProgress();
    }

    /**
     * Stops progress view and shows real data
     */
    private void stopProgress(){
        this.progressBar.setVisibility(View.GONE);
        this.feedbackListView.setVisibility(View.VISIBLE);
    }

    /**
     * Starts progress view, hiding list temporarily
     */
    private void startProgress(){
        this.feedbackListView.setVisibility(View.GONE);
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
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.PREPARE_FEEDBACK_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void  unregisterReceiver(){
        Log.d(TAG, "unregisterReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }

    /**
     * Asks SurveyService for the current list of surveys
     */
    public void prepareFeedbackInfo(){
        Log.d(TAG, "prepareFeedbackInfo");
        Intent surveysIntent=new Intent(getActivity().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PREPARE_FEEDBACK_ACTION);
        getActivity().getApplicationContext().startService(surveysIntent);
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive");
            List<Feedback> feedbackList=(List<Feedback>)Session.popServiceValue(SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS);
            loadItems(feedbackList);
        }
    }

}

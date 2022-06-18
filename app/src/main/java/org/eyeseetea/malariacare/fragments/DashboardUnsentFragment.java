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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.List;

public class DashboardUnsentFragment extends FiltersFragment{

    public static final String TAG = ".UnsentFragment";
    private SurveyReceiver surveyReceiver;

    private AssessmentUnsentAdapter adapter;

    private FloatingActionButton startButton;
    private TextView noSurveysText;

    private RecyclerView recyclerView;
    private View rootView;

    @Override
    protected void onFiltersChanged() {
        reloadInProgressSurveys();
    }

    @Override
    protected OrgUnitProgramFilterView.FilterType getFilterType() {
        return OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE;
    }

    @Override
    protected int getOrgUnitProgramFilterViewId() {
        return R.id.assess_org_unit_program_filter_view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        rootView =  inflater.inflate(R.layout.assess_listview, null);

        noSurveysText = rootView.findViewById(R.id.no_surveys);
        startButton = rootView.findViewById(R.id.start_button);

        initRecyclerView();

        return rootView;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    private void showOrHiddenButton(SurveyDB survey) {
        String orgUnitFilter = getSelectedOrgUnitUidFilter();
        String programFilter = getSelectedProgramUidFilter();

        if(orgUnitFilter == "" || programFilter == ""){
            startButton.setVisibility(View.VISIBLE);
            noSurveysText.setText(R.string.assess_no_surveys);
        }else if (survey != null ||
                !OrgUnitProgramRelationDB.existProgramAndOrgUnitRelation(programFilter, orgUnitFilter)){
            startButton.setVisibility(View.INVISIBLE);
            noSurveysText.setText(R.string.survey_not_assigned_facility);
        }else{
            startButton.setVisibility(View.VISIBLE);
            noSurveysText.setText(R.string.assess_no_surveys);
        }
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.unsentSurveyList);

        adapter = new AssessmentUnsentAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void reloadData() {
        super.reloadData();

        //Reload data using service
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    public void reloadToSend() {
        //Reload data using service
        Intent surveysIntent = new Intent(PreferencesState.getInstance().getContext().getApplicationContext()
                , SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD,
                SurveyService.ALL_COMPLETED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }

    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver,
                    new IntentFilter(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION));
        }
    }

    public void unregisterSurveysReceiver() {
        Log.d(TAG, "unregisterSurveysReceiver");
        if (surveyReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    public void reloadInProgressSurveys() {
        List<SurveyDB> surveysInProgressFromService = (List<SurveyDB>) Session.popServiceValue(
                SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION);
        if(surveysInProgressFromService==null){
            return;
        }
        reloadSurveys(getSurveysByOrgUnitAndProgram(surveysInProgressFromService));
    }

    private List<SurveyDB> getSurveysByOrgUnitAndProgram(
            List<SurveyDB> surveysInProgressFromService) {
        List<SurveyDB> filteredSurveys = new ArrayList<>();

        for (SurveyDB survey : surveysInProgressFromService) {
            if (surveyHasOrgUnitFilter(survey) && surveyHasProgramFilter(survey)) {
                filteredSurveys.add(survey);
            }
        }

        return filteredSurveys;
    }

    private boolean surveyHasOrgUnitFilter(SurveyDB survey){
        String orgUnitFilter = getSelectedOrgUnitUidFilter();

        return (orgUnitFilter.equals("") || survey.getOrgUnit().getUid().equals(orgUnitFilter));
    }

    private boolean surveyHasProgramFilter(SurveyDB survey){
        String programFilter = getSelectedProgramUidFilter();

        return (programFilter.equals("") || survey.getProgram().getUid().equals(programFilter));
    }


    public void reloadSurveys(List<SurveyDB> newListSurveys) {
        if (newListSurveys != null && this.adapter != null) {
            Log.d(TAG, "refreshScreen (Thread: " + Thread.currentThread().getId() + "): "
                    + newListSurveys.size());
            this.adapter.setSurveys(newListSurveys);
            SurveyDB surveyDB=null;
            if(newListSurveys.size()>0) {
                surveyDB =newListSurveys.get(0);
            }
            showOrHiddenButton(surveyDB);
            showOrHiddenList(newListSurveys.isEmpty());
        }
    }

    private void showOrHiddenList(boolean hasSurveys) {
        if(hasSurveys){
            noSurveysText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            noSurveysText.setVisibility(View.GONE);
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
            //Listening only intents from this method
            if (SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadInProgressSurveys();
            }
        }
    }


}
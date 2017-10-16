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

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardUnsentFragment extends ListFragment implements IModuleFragment {

    public static final String TAG = ".DetailsFragment";
    private SurveyReceiver surveyReceiver;
    private List<SurveyDB> surveys;
    protected AssessmentUnsentAdapter adapter;
    private static int selectedPosition = 0;
    DashboardActivity dashboardActivity;

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    public DashboardUnsentFragment() {
        this.surveys = new ArrayList();
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
        if (container == null) {
            return null;
        }

        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                        .findViewById(R.id.assess_org_unit_program_filter_view);

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(ProgramDB selectedProgramFilter) {
                        reloadInProgressSurveys();
                        saveCurrentFilters();
                    }

                    @Override
                    public void onOrgUnitFilterChanged(OrgUnitDB selectedOrgUnitFilter) {
                        reloadInProgressSurveys();
                        saveCurrentFilters();
                    }
                });

        return inflater.inflate(R.layout.assess_listview, null);
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(
                orgUnitProgramFilterView.getSelectedProgramFilter().getUid());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                orgUnitProgramFilterView.getSelectedOrgUnitFilter().getUid());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initListView();
        registerForContextMenu(getListView());
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    private void updateSelectedFilters() {
        if (orgUnitProgramFilterView != null) {
            String programUidFilter = PreferencesState.getInstance().getProgramUidFilter();
            String orgUnitUidFilter = PreferencesState.getInstance().getOrgUnitUidFilter();

            orgUnitProgramFilterView.changeSelectedFilters(programUidFilter, orgUnitUidFilter);
        }
    }

    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the
     * one in session is created.
     */
    private void initAdapter() {
        this.adapter = new AssessmentUnsentAdapter(this.surveys, getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dashboardActivity = (DashboardActivity) activity;
    }

    //Remove survey from the list and reload list.
    public void removeSurveyFromAdapter(SurveyDB survey) {
        adapter.remove(survey);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void reloadData(){
        updateSelectedFilters();

        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    public void reloadToSend(){
        //Reload data using service
        Intent surveysIntent=new Intent(getActivity(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_COMPLETED_SURVEYS_ACTION);
        getActivity().startService(surveysIntent);
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }

    /**
     * Checks if the given position points to a real survey instead of a footer or header of the listview.
     * @param position
     * @return true|false
     */
    private boolean isPositionASurvey(int position){
        return !isPositionFooter(position) && !isPositionHeader(position);
    }

    /**
     * Checks if the given position is the header of the listview instead of a real survey
     * @param position
     * @return true|false
     */
    private boolean isPositionHeader(int position){
        return position<=0;
    }

    /**
     * Checks if the given position is the footer of the listview instead of a real survey
     * @param position
     * @return true|false
     */
    private boolean isPositionFooter(int position){
        return position==(this.surveys.size()+1);
    }

    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView(){
        if(PreferencesState.getInstance().isVerticalDashboard()) {
            CustomTextView title = (CustomTextView) getActivity().findViewById(R.id.titleInProgress);
            title.setText(adapter.getTitle());
        }
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        //Discard clicks on header|footer (which is attendend on onNewSurvey via super)
        selectedPosition=position;
        if (isPositionASurvey(selectedPosition)) {
            final SurveyDB survey = (SurveyDB) adapter.getItem(selectedPosition - 1);
            dashboardActivity.onSurveySelected(survey);
        }
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if(surveyReceiver==null){
            surveyReceiver=new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver, new IntentFilter(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver(){
        Log.d(TAG, "unregisterSurveysReceiver");
        if(surveyReceiver!=null){
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver=null;
        }
    }
    public void reloadInProgressSurveys(){
        List<SurveyDB> surveysInProgressFromService = (List<SurveyDB>) Session.popServiceValue(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION);

        reloadSurveys(getSurveysByOrgUnitAndProgram(surveysInProgressFromService));
    }

    private List<SurveyDB> getSurveysByOrgUnitAndProgram(List<SurveyDB> surveysInProgressFromService) {
        List<SurveyDB> filteredSurveys = new ArrayList<>();

        for (SurveyDB survey:surveysInProgressFromService) {
            if (surveyHasOrgUnitFilter(survey) && surveyHasProgramFilter(survey)){
                filteredSurveys.add(survey);
            }
        }

        return filteredSurveys;
    }

    private boolean surveyHasOrgUnitFilter(SurveyDB survey) {
        OrgUnitDB orgUnitFilter = orgUnitProgramFilterView.getSelectedOrgUnitFilter();

        return survey.getOrgUnit().getUid().equals(orgUnitFilter.getUid())||
        orgUnitFilter.getName().equals(PreferencesState.getInstance().getContext().getString(
                R.string.filter_all_org_units));
    }

    private boolean surveyHasProgramFilter(SurveyDB survey) {
        ProgramDB programFilter = orgUnitProgramFilterView.getSelectedProgramFilter();

        return survey.getProgram().getUid().equals(programFilter.getUid())||
                programFilter.getName().equals(PreferencesState.getInstance().getContext().getString(
                        R.string.filter_all_org_assessments));
    }

    public void reloadSurveys(List<SurveyDB> newListSurveys){
        if(newListSurveys!=null) {
            Log.d(TAG, "refreshScreen (Thread: " + Thread.currentThread().getId() + "): " + newListSurveys.size());
            this.surveys.clear();
            this.surveys.addAll(newListSurveys);
            this.adapter.notifyDataSetChanged();
        }
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver{
        private SurveyReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadInProgressSurveys();
            }
        }
    }


}
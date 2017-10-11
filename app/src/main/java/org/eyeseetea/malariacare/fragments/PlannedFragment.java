/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.fragments;

import static org.eyeseetea.malariacare.DashboardActivity.dashboardActivity;

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

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.services.PlannedServiceBundle;
import org.eyeseetea.malariacare.layout.adapters.survey.PlannedAdapter;
import org.eyeseetea.malariacare.layout.dashboard.controllers.PlanModuleController;
import org.eyeseetea.malariacare.presentation.presenters.OrgUnitProgramFilterPresenter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.List;

/**
 * Created by ivan.arrizabalaga on 15/12/2015.
 */
public class PlannedFragment extends ListFragment implements IModuleFragment{
    public static final String TAG = ".PlannedFragment";

    private PlannedItemsReceiver plannedItemsReceiver;

    private PlannedAdapter adapter;


    private List<ProgramDB> programList;
    private List<OrgUnitDB> orgUnitList;

    private ProgramDB programFilter;
    List<PlannedItem> plannedItemList;
    public PlannedFragment() {

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

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    private void prepareUI(List<PlannedItem> plannedItemList) {
        this.adapter = new PlannedAdapter(plannedItemList,getActivity());
        this.setListAdapter(adapter);

        reloadFilter();
    }

    public void reloadFilter(){
        OrgUnitProgramFilterView orgUnitProgramFilterView = (OrgUnitProgramFilterView) getActivity()
                .findViewById(R.id.plan_org_unit_program_filter_view);

        ProgramDB selectedProgram = orgUnitProgramFilterView.getSelectedProgramFilter();

        if(selectedProgram!=null) {
            loadProgram(selectedProgram);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        //Loading...
        setListShown(false);
        //Listen for data
        registerPlannedItemsReceiver();
        super.onResume();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        unregisterPlannedItemsReceiver();
        super.onStop();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        unregisterPlannedItemsReceiver();

        super.onPause();
    }

    /**
     * Register a survey receiver to load plannedItems into the listadapter
     */
    private void registerPlannedItemsReceiver() {
        Log.d(TAG, "registerPlannedItemsReceiver");

        if (plannedItemsReceiver == null) {
            plannedItemsReceiver = new PlannedItemsReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(plannedItemsReceiver, new IntentFilter(SurveyService.PLANNED_SURVEYS_ACTION));
        }
    }
    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterPlannedItemsReceiver() {
        Log.d(TAG, "unregisterPlannedItemsReceiver");
        if (plannedItemsReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(plannedItemsReceiver);
            plannedItemsReceiver = null;
        }
    }

    @Override
    public void reloadData(){
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PLANNED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    public void loadProgram(ProgramDB program) {
        Log.d(TAG,"Loading program: "+program.getUid());
        programFilter=program;
        if(adapter!=null){
            adapter.applyFilter(programFilter);
            adapter.notifyDataSetChanged();
        }
        else {
            reloadData();
        }
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class PlannedItemsReceiver extends BroadcastReceiver {
        private PlannedItemsReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if(SurveyService.PLANNED_SURVEYS_ACTION.equals(intent.getAction())){
                PlannedServiceBundle plannedServiceBundle= (PlannedServiceBundle)Session.popServiceValue(SurveyService.PLANNED_SURVEYS_ACTION);

                prepareUI(plannedServiceBundle.getPlannedItems());

                setListShown(true);
                adapter.notifyDataSetChanged();
            }
        }
    }
}

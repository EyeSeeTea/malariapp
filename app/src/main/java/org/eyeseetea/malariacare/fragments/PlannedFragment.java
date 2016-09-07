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

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.database.utils.services.PlannedServiceBundle;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterOrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.PlannedAdapter;
import org.eyeseetea.malariacare.layout.dashboard.controllers.DashboardController;
import org.eyeseetea.malariacare.services.SurveyService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan.arrizabalaga on 15/12/2015.
 */
public class PlannedFragment extends ListFragment implements IModuleFragment{
    public static final String TAG = ".PlannedFragment";

    private PlannedItemsReceiver plannedItemsReceiver;

    private PlannedAdapter adapter;

    private List<PlannedItem> plannedItems;

    private Program programDefaultOption;
    private OrgUnit orgUnitDefaultOption;

    private List<Program> programList;
    private List<OrgUnit> orgUnitList;

    private Program programFilter;
    List<PlannedItem> plannedItemList;

    public PlannedFragment() {
        this.plannedItems = new ArrayList();
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        programDefaultOption = new Program(getResources().getString(R.string.filter_all_org_assessments).toUpperCase());
        orgUnitDefaultOption = new OrgUnit(getResources().getString(R.string.filter_all_org_assessments).toUpperCase());
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

    private void prepareUI() {
        this.adapter = new PlannedAdapter(this.plannedItems,getActivity());
        this.setListAdapter(adapter);

        final Spinner programSpinner = (Spinner) getActivity().findViewById(R.id.dashboard_planning_program);
        //Populate Program View DDL
        if(!programList.contains(programDefaultOption))
            programList.add(0, programDefaultOption);
        programSpinner.setAdapter(new FilterProgramArrayAdapter(getActivity(), programList));
        //Apply filter to listview
        programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner=((Spinner) parent);
                Program selectedProgram=position==0?null:(Program)spinner.getItemAtPosition(position);
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                if(programFilter!=null) {
                    adapter.applyFilter(programFilter);
                    adapter.notifyDataSetChanged();
                    programFilter = null;
                }
                else {
                    adapter.applyFilter(selectedProgram);
                }
                if(selectedProgram!=null)
                    DashboardActivity.dashboardActivity.onProgramSelected(selectedProgram);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner orgUnitSpinner = (Spinner) getActivity().findViewById(R.id.dashboard_planning_orgUnit);

        //Populate Program View DDL
        if(!orgUnitList.contains(orgUnitDefaultOption))
            orgUnitList.add(0, orgUnitDefaultOption);
        orgUnitSpinner.setAdapter(new FilterOrgUnitArrayAdapter(getActivity(), orgUnitList));
        //Apply filter to listview
        orgUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner=((Spinner) parent);
                OrgUnit selectedOrgUnit=position==0?null:(OrgUnit)spinner.getItemAtPosition(position);
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                if(selectedOrgUnit!=null && !selectedOrgUnit.getName().equals(getResources().getString(R.string.filter_all_org_assessments).toUpperCase()))
                    DashboardActivity.dashboardActivity.onOrgUnitSelected(selectedOrgUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
    public void reloadData(){        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PLANNED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    public void reloadPlannedItems() {
        if(adapter!=null && plannedItemList!=null){
        adapter.reloadItems(plannedItemList);
        setListShown(true);
        adapter.notifyDataSetChanged();
        }
    }

    public void loadProgram(Program program) {
        programFilter=program;
        if(adapter!=null){
            adapter.applyFilter(programFilter);
            reloadPlannedItems();
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
                if(plannedServiceBundle==null)
                    return;
                programList=(List<Program>) plannedServiceBundle.getModelList(Program.class.getName());
                orgUnitList=(List<OrgUnit>)plannedServiceBundle.getModelList(OrgUnit.class.getName());
                prepareUI();
                plannedItemList=plannedServiceBundle.getPlannedItems();
                reloadPlannedItems();
            }
        }
    }
}

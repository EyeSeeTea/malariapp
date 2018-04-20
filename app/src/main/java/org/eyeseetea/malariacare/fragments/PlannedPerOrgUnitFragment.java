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
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurveyByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.data.database.utils.services.PlannedServiceBundle;
import org.eyeseetea.malariacare.layout.adapters.dashboard.PlanningPerOrgUnitAdapter;
import org.eyeseetea.malariacare.services.PlannedSurveyService;
import org.eyeseetea.malariacare.views.CustomCheckBox;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 09/08/2016.
 */
public class PlannedPerOrgUnitFragment extends ListFragment {
    public static final String TAG = ".PlannedOrgUnitsF";

    private PlannedItemsReceiver plannedItemsReceiver;
    protected PlanningPerOrgUnitAdapter adapter;
    private static List<PlannedSurveyByOrgUnit> plannedSurveys;
    static ImageButton scheduleButton;
    CustomCheckBox selectAllCheckbox;
    String filterOrgUnitUid;

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    public PlannedPerOrgUnitFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferencesState.getInstance().initalizateActivityDependencies();
        this.plannedSurveys = new ArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (container == null) {
            return null;
        }

        orgUnitProgramFilterView = (OrgUnitProgramFilterView) getActivity()
                .findViewById(R.id.plan_org_unit_program_filter_view);
        return inflater.inflate(R.layout.plan_per_org_unit_listview, null);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }

    private void prepareUI(List<PlannedSurveyByOrgUnit> plannedItems) {
        int countOfCheckedSurveys=0;
        //Recover the plannedItem last status.
        if(plannedSurveys!=null && plannedSurveys.size()>1){
            for (PlannedSurveyByOrgUnit newPlannedSurveys:plannedItems) {
                reCheckCheckboxes(newPlannedSurveys);
                if(newPlannedSurveys.getChecked()) {
                    countOfCheckedSurveys++;
                }
            }
        }
        plannedSurveys=plannedItems;
        initAdapter(plannedItems);
        initScheduleButton();
        initListView();
        //checks the allSelect checkbox looking the reloaded surveys.
        if(plannedItems.size()==countOfCheckedSurveys){
            setSelectAllCheckboxAs(true,false);
        }
        else{
            setSelectAllCheckboxAs(false,false);
        }
        resetList();
    }

    private void reCheckCheckboxes(PlannedSurveyByOrgUnit newPlannedSurveys) {
        if (newPlannedSurveys.getSurvey() == null) return;
        for (PlannedSurveyByOrgUnit plannedSurvey: plannedSurveys){
            if (plannedSurvey.getSurvey() != null && plannedSurvey.getSurvey().getId_survey().equals(newPlannedSurveys.getSurvey().getId_survey())) {
                newPlannedSurveys.setChecked(plannedSurvey.getChecked());
            }
        }
    }

    private void setSelectAllCheckboxAs(final boolean value, final boolean isClicked) {
        selectAllCheckbox.post(new Runnable() {
            @Override
            public void run() {
                selectAllCheckbox.setChecked(value,isClicked);
            }
        });
    }

    private void initScheduleButton() {
        scheduleButton = (ImageButton) getActivity().findViewById(R.id.reschedule_button);
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SurveyDB> scheduleSurveys=new ArrayList<>();
                for(PlannedSurveyByOrgUnit plannedSurveyByOrgUnit:plannedSurveys){
                    if(plannedSurveyByOrgUnit.getChecked()) {
                        scheduleSurveys.add(plannedSurveyByOrgUnit.getSurvey());
                    }
                }

                if(scheduleSurveys.size()==0) return;


                new ScheduleListener(scheduleSurveys,adapter.getContext());
            }
        });
        disableScheduleButton();
    }

    public static void enableScheduleButton(){
        scheduleButton.setEnabled(true);
    }
    public static void disableScheduleButton(){
        scheduleButton.setEnabled(false);
    }

    public void resetList() {
        this.adapter.notifyDataSetChanged();
    }
    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView(){
        selectAllCheckbox=(CustomCheckBox) getActivity().findViewById(R.id.select_all_orgunits);
        selectAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                checkAll(isChecked);
            }
        });
    }

    private void checkAll(boolean value) {
        for(PlannedSurveyByOrgUnit plannedSurveyByOrgUnit:plannedSurveys){
                plannedSurveyByOrgUnit.setChecked(value);
        }
        this.adapter.setItems(plannedSurveys);
        this.adapter.notifyDataSetChanged();
        selectAllCheckbox.setChecked(value,false);
        if(value){
            enableScheduleButton();
        }else{
            disableScheduleButton();
        }
    }

    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the one in session is created.
     * @param plannedItems
     */
    private void initAdapter(List<PlannedSurveyByOrgUnit> plannedItems){
        this.adapter  = new PlanningPerOrgUnitAdapter(plannedItems, getActivity());
        setListAdapter(adapter);
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
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
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(plannedItemsReceiver, new IntentFilter(PlannedSurveyService.PLANNED_PER_ORG_UNIT_SURVEYS_ACTION));
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

    public void reloadData(){
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), PlannedSurveyService.class);
        surveysIntent.putExtra(PlannedSurveyService.SERVICE_METHOD, PlannedSurveyService.PLANNED_PER_ORG_UNIT_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(surveysIntent);
    }

    public static void reloadButtonState(boolean isChecked) {
        if(isChecked){
            enableScheduleButton();
            return;
        }
        for(PlannedSurveyByOrgUnit plannedSurveyByOrgUnit:plannedSurveys){
            if(plannedSurveyByOrgUnit.getChecked()) {
                enableScheduleButton();
                return;
            }
        }
        disableScheduleButton();
    }

    public void setOrgUnitFilter(String uid) {
        filterOrgUnitUid=uid;
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
            if(PlannedSurveyService.PLANNED_PER_ORG_UNIT_SURVEYS_ACTION.equals(intent.getAction())){
                PlannedServiceBundle plannedServiceBundle= (PlannedServiceBundle)Session.popServiceValue(PlannedSurveyService.PLANNED_PER_ORG_UNIT_SURVEYS_ACTION);
                List<PlannedSurveyByOrgUnit> items= new ArrayList<>();
                for(PlannedItem item: plannedServiceBundle.getPlannedItems()){
                    if(item instanceof PlannedSurvey && isNotFiltered(item)){
                        items.add(new PlannedSurveyByOrgUnit(((PlannedSurvey) item).getSurvey(),((PlannedSurvey) item).getHeader()));
                    }
                }
                prepareUI(items);
            }
        }

        private boolean isNotFiltered(PlannedItem item){
            return ((PlannedSurvey) item).getSurvey().getOrgUnit().getUid().equals(filterOrgUnitUid);
        }
    }
}
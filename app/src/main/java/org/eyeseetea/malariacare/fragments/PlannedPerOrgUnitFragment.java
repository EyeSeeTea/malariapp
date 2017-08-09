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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;

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
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomCheckBox;

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
    static Button scheduleButton;
    CustomCheckBox selectAllCheckbox;
    String filterOrgUnitUid;
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

        return super.onCreateView(inflater, container, savedInstanceState);
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
        setListShown(true);
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
                CustomCheckBox selectAllCheckbox=(CustomCheckBox) getView().findViewById(R.id.select_all_orgunits);
                selectAllCheckbox.setChecked(value,isClicked);
            }
        });
    }

    private void initScheduleButton() {
        scheduleButton = (Button) getActivity().findViewById(R.id.reschedule_button);
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
        scheduleButton.setBackgroundColor(ContextCompat.getColor(PreferencesState.getInstance().getContext(),R.color.dark_navy_blue));
    }
    public static void disableScheduleButton(){
        scheduleButton.setEnabled(false);
        scheduleButton.setBackgroundColor(ContextCompat.getColor(PreferencesState.getInstance().getContext(),R.color.common_plus_signin_btn_text_light_disabled));
    }

    public void resetList() {
        this.adapter.notifyDataSetChanged();
    }
    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView(){
        LayoutInflater inflater = LayoutInflater.from(PreferencesState.getInstance().getContext().getApplicationContext());
        View header = inflater.inflate(this.adapter.getHeaderLayout(), null, false);
        selectAllCheckbox=(CustomCheckBox) header.findViewById(R.id.select_all_orgunits);
        selectAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                checkAll(isChecked);
            }
        });
        ListView listView = getListView();
        if(listView.getHeaderViewsCount()==0)
            listView.addHeaderView(header);
        setListAdapter(adapter);
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

    public void reloadData(){
        //Reload data using service
        Intent surveysIntent=new Intent(PreferencesState.getInstance().getContext().getApplicationContext(), SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.PLANNED_SURVEYS_ACTION);
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
            if(SurveyService.PLANNED_SURVEYS_ACTION.equals(intent.getAction())){
                PlannedServiceBundle plannedServiceBundle= (PlannedServiceBundle)Session.popServiceValue(SurveyService.PLANNED_SURVEYS_ACTION);
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
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
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.database.utils.planning.PlannedServiceBundle;
import org.eyeseetea.malariacare.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.layout.adapters.dashboard.IDashboardAdapter;
import org.eyeseetea.malariacare.layout.adapters.dashboard.PlanningPerOrgUnitAdapter;
import org.eyeseetea.malariacare.services.SurveyService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan.arrizabalaga on 15/12/2015.
 */
public class PlannedPerOrgUnitFragment extends ListFragment {
    public static final String TAG = ".PlannedOrgUnitsF";

    private PlannedItemsReceiver plannedItemsReceiver;
    protected IDashboardAdapter adapter;
    private List<Survey> surveys;

    private Program programDefaultOption;
    private OrgUnit orgUnitDefaultOption;

    private List<Program> programList;
    private List<OrgUnit> orgUnitList;


    public PlannedPerOrgUnitFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        this.adapter = Session.getAdapterSent();
        this.surveys = new ArrayList();
        programDefaultOption = new Program(getResources().getString(R.string.filter_all_assessments).toUpperCase());
        orgUnitDefaultOption = new OrgUnit(getResources().getString(R.string.filter_all_assessments).toUpperCase());
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

    private void prepareUI(List<PlannedItem> plannedItems) {
        initAdapter(plannedItems);
        initListView();
        resetList();
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
        ListView listView = getListView();
        listView.setBackgroundColor(getResources().getColor(R.color.feedbackDarkBlue));
        if(listView.getHeaderViewsCount()==0)
            listView.addHeaderView(header);
        setListAdapter((BaseAdapter) adapter);
        Session.listViewSent = listView;
    }
    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the one in session is created.
     * @param plannedItems
     */
    private void initAdapter(List<PlannedItem> plannedItems){
        IDashboardAdapter adapterInSession = Session.getAdapterOrgUnit();
        if(adapterInSession == null){
            adapterInSession = new PlanningPerOrgUnitAdapter(plannedItems, getActivity());
        }else{
            adapterInSession = adapterInSession.newInstance(plannedItems, getActivity());
        }
        this.adapter = adapterInSession;
        Session.setAdapterOrgUnit(this.adapter);
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
                List<PlannedItem> items= new ArrayList<>();
                for(PlannedItem item: plannedServiceBundle.getPlannedItems())
                    if(item instanceof PlannedSurvey)
                        items.add(item);
                prepareUI(items);
            }
        }
    }
}

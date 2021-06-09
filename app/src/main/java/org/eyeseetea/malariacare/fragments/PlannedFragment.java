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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.services.PlannedServiceBundle;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.layout.adapters.survey.PlannedAdapter;
import org.eyeseetea.malariacare.services.PlannedSurveyService;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.List;

public class PlannedFragment extends FiltersFragment implements IModuleFragment {
    public static final String TAG = ".PlannedFragment";

    private PlannedItemsReceiver plannedItemsReceiver;

    private String programUidFilter;

    private View rootView;
    private RecyclerView plannedRecyclerView;
    private PlannedAdapter plannedAdapter;

    private static String SERVER_CLASSIFICATION = "ServerClassification";
    private ServerClassification serverClassification;

    public static PlannedFragment newInstance(ServerClassification serverClassification) {
        PlannedFragment fragment = new PlannedFragment();

        Bundle args = new Bundle();
        args.putInt(SERVER_CLASSIFICATION, serverClassification.getCode());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_plan, container, false);

        serverClassification = ServerClassification.Companion.get(
                getArguments().getInt(SERVER_CLASSIFICATION));

        initializeRecyclerView();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    private void refreshPlannedItems(List<PlannedItem> plannedItemList) {
        plannedAdapter.setItems(plannedItemList);

        reloadFilter();
    }

    private void initializeRecyclerView() {
        plannedRecyclerView = rootView.findViewById(R.id.planList);

        plannedAdapter = new PlannedAdapter(getActivity(), serverClassification);
        plannedRecyclerView.setAdapter(plannedAdapter);
    }


    private void reloadFilter() {
        loadProgram(getSelectedProgramUidFilter());

        if (plannedAdapter != null) {
            plannedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        //Listen for data
        registerPlannedItemsReceiver();
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        unregisterPlannedItemsReceiver();
        super.onStop();
    }

    @Override
    public void onPause() {
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
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(plannedItemsReceiver,
                    new IntentFilter(PlannedSurveyService.PLANNED_SURVEYS_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterPlannedItemsReceiver() {
        Log.d(TAG, "unregisterPlannedItemsReceiver");
        if (plannedItemsReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                    plannedItemsReceiver);
            plannedItemsReceiver = null;
        }
    }

    @Override
    protected void onFiltersChanged() {
        if (!getSelectedOrgUnitUidFilter().isEmpty()) {
            DashboardActivity.dashboardActivity.onOrgUnitSelected(getSelectedOrgUnitUidFilter());
        } else {
            reloadFilter();
        }
    }

    @Override
    protected OrgUnitProgramFilterView.FilterType getFilterType() {
        return OrgUnitProgramFilterView.FilterType.EXCLUSIVE;
    }

    @Override
    protected int getOrgUnitProgramFilterViewId() {
        return R.id.plan_org_unit_program_filter_view;
    }

    @Override
    public void reloadData() {
        super.reloadData();

        //Reload data using service
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                PlannedSurveyService.class);
        surveysIntent.putExtra(PlannedSurveyService.SERVICE_METHOD,
                PlannedSurveyService.PLANNED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    public void loadProgram(String programUid) {
        Log.d(TAG, "Loading program: " + programUid);
        programUidFilter = programUid;
        if (plannedAdapter != null) {
            plannedAdapter.applyFilter(programUidFilter);
            plannedAdapter.notifyDataSetChanged();
        } else {
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
            if (PlannedSurveyService.PLANNED_SURVEYS_ACTION.equals(intent.getAction())) {
                PlannedServiceBundle plannedServiceBundle =
                        (PlannedServiceBundle) Session.popServiceValue(
                                PlannedSurveyService.PLANNED_SURVEYS_ACTION);

                refreshPlannedItems(plannedServiceBundle.getPlannedItems());
            }
        }
    }
}

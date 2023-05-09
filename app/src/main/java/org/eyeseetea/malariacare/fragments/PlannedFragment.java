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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.adapters.survey.PlannedAdapter;
import org.eyeseetea.malariacare.presentation.presenters.surveys.PlannedSurveysPresenter;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.List;

public class PlannedFragment extends FiltersFragment implements IModuleFragment, PlannedSurveysPresenter.View {
    public static final String TAG = ".PlannedFragment";


    private String programUidFilter;

    private View rootView;
    private RecyclerView plannedRecyclerView;
    private PlannedAdapter plannedAdapter;

    private static String SERVER_CLASSIFICATION = "ServerClassification";
    private ServerClassification serverClassification;

    private PlannedSurveysPresenter presenter;

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
        initPresenter();

        return rootView;
    }

    private void initPresenter() {
        presenter = DataFactory.INSTANCE.providePlannedSurveysPresenter();

        presenter.attachView(this);
    }

    private void refreshPlannedItems(List<PlannedItem> plannedItemList) {
        plannedAdapter.setItems(plannedItemList);

        reloadFilter();
    }

    private void initializeRecyclerView() {
        plannedRecyclerView = rootView.findViewById(R.id.planList);

        plannedAdapter = new PlannedAdapter(getActivity(), serverClassification,  () -> {
            reloadData();
        });

        plannedRecyclerView.setAdapter(plannedAdapter);
    }


    private void reloadFilter() {
        loadProgram(getSelectedProgramUidFilter());

        if (plannedAdapter != null) {
            plannedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        presenter.detachView();

        super.onDestroy();
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

        if (presenter != null) {
            presenter.reload();
        }
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

    @Override
    public void showData(@NonNull List<PlannedItem> plannedItems) {
        refreshPlannedItems(plannedItems);
    }

    @Override
    public void showNetworkError() {
        Log.e(this.getClass().getSimpleName(), "Network Error");
    }
}

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
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurveyByOrgUnit;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.factories.DataFactory;
import org.eyeseetea.malariacare.layout.adapters.dashboard.PlanningPerOrgUnitAdapter;
import org.eyeseetea.malariacare.presentation.presenters.surveys.PlannedSurveysPresenter;
import org.eyeseetea.malariacare.views.CustomCheckBox;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.List;

public class PlannedPerOrgUnitFragment extends FiltersFragment implements IModuleFragment,PlannedSurveysPresenter.View  {
    public static final String TAG = ".PlannedOrgUnitsF";


    public interface Callback {
        void onItemCheckboxChanged();
    }

    private PlanningPerOrgUnitAdapter adapter;
    private List<PlannedSurveyByOrgUnit> plannedSurveys = new ArrayList<>();
    private ImageButton scheduleButton;
    private CustomCheckBox selectAllCheckbox;

    private View rootView;
    private RecyclerView recyclerView;

    private PlannedSurveysPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        PreferencesState.getInstance().initalizateActivityDependencies();
        this.plannedSurveys = new ArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.plan_per_org_unit_list, null);

        scheduleButton = rootView.findViewById(R.id.reschedule_button);
        selectAllCheckbox = rootView.findViewById(R.id.select_all_orgunits);

        initScheduleButton();
        initSelectAllCheckbox();
        initRecyclerView();
        initPresenter();

        return rootView;
    }

    private void initPresenter() {
        presenter = DataFactory.INSTANCE.providePlannedSurveysPresenter();

        presenter.attachView(this);
    }

    @Override
    protected void onFiltersChanged() {
        if (!getSelectedProgramUidFilter().isEmpty() || getSelectedOrgUnitUidFilter().isEmpty()) {
            DashboardActivity.dashboardActivity.onProgramSelected(getSelectedProgramUidFilter());
        } else {
            reloadData();
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
    public void showData(@NonNull List<PlannedItem> plannedItems) {
        List<PlannedSurveyByOrgUnit> items = new ArrayList<>();
        for (PlannedItem item : plannedItems) {
            if (item instanceof PlannedSurvey && isNotFiltered(item)) {
                items.add(new PlannedSurveyByOrgUnit(((PlannedSurvey) item).getSurvey(),
                        ((PlannedSurvey) item).getHeader()));
            }
        }

        refreshItems(items);
    }

    @Override
    public void showNetworkError() {
        Log.e(this.getClass().getSimpleName(), "Network Error");
    }

    private void refreshItems(List<PlannedSurveyByOrgUnit> plannedItems) {
        int countOfCheckedSurveys = 0;
        //Recover the plannedItem last status.
        if (plannedSurveys != null && plannedSurveys.size() > 1) {
            for (PlannedSurveyByOrgUnit newPlannedSurveys : plannedItems) {
                reCheckCheckboxes(newPlannedSurveys);
                if (newPlannedSurveys.getChecked()) {
                    countOfCheckedSurveys++;
                }
            }
        }
        plannedSurveys = plannedItems;
        adapter.setItems(plannedItems);

        //checks the allSelect checkbox looking the reloaded surveys.
        if (plannedItems.size() == countOfCheckedSurveys) {
            setSelectAllCheckboxAs(true, false);
        } else {
            setSelectAllCheckboxAs(false, false);
        }
        resetList();
    }

    private void refreshMenuDots() {
        int countOfPlannedSurveys = 0;
        for (PlannedSurveyByOrgUnit plannedSurveyByOrgUnit : plannedSurveys) {
            if (plannedSurveyByOrgUnit.getChecked()) {
                countOfPlannedSurveys++;
            }
        }
        for (PlannedSurveyByOrgUnit plannedSurveyByOrgUnit : plannedSurveys) {
            if (countOfPlannedSurveys >= 2) {
                plannedSurveyByOrgUnit.setHideMenu(true);
            } else {
                plannedSurveyByOrgUnit.setHideMenu(false);
            }
        }
    }

    private void reCheckCheckboxes(PlannedSurveyByOrgUnit newPlannedSurveys) {
        if (newPlannedSurveys.getSurvey() == null) return;
        for (PlannedSurveyByOrgUnit plannedSurvey : plannedSurveys) {
            if (plannedSurvey.getSurvey() != null
                    && plannedSurvey.getSurvey().getId_survey().equals(
                    newPlannedSurveys.getSurvey().getId_survey())) {
                newPlannedSurveys.setChecked(plannedSurvey.getChecked());
            }
        }
    }

    private void setSelectAllCheckboxAs(final boolean value, final boolean isClicked) {
        selectAllCheckbox.post(new Runnable() {
            @Override
            public void run() {
                selectAllCheckbox.setChecked(value, isClicked);
            }
        });
    }

    private void initScheduleButton() {
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SurveyDB> scheduleSurveys = new ArrayList<>();
                for (PlannedSurveyByOrgUnit plannedSurveyByOrgUnit : plannedSurveys) {
                    if (plannedSurveyByOrgUnit.getChecked()) {
                        scheduleSurveys.add(plannedSurveyByOrgUnit.getSurvey());
                    }
                }

                if (scheduleSurveys.size() == 0) return;


                new ScheduleListener(scheduleSurveys, getActivity(), () -> {
                    reloadData();
                });
            }
        });
        disableScheduleButton();
    }

    private void enableScheduleButton() {
        scheduleButton.setEnabled(true);
    }

    private void disableScheduleButton() {
        scheduleButton.setEnabled(false);
    }

    private void resetList() {
        refreshMenuDots();

        this.recyclerView.post(() -> PlannedPerOrgUnitFragment.this.adapter.notifyDataSetChanged());
    }

    private void initSelectAllCheckbox() {
        selectAllCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> checkAll(isChecked));
    }

    private void checkAll(boolean value) {
        for (PlannedSurveyByOrgUnit plannedSurveyByOrgUnit : plannedSurveys) {
            plannedSurveyByOrgUnit.setChecked(value);
        }
        this.adapter.setItems(plannedSurveys);
        this.adapter.notifyDataSetChanged();
        selectAllCheckbox.setChecked(value, false);
        if (value) {
            enableScheduleButton();
        } else {
            disableScheduleButton();
        }
        resetList();
    }


    @Override
    public void onDestroy() {
        presenter.detachView();

        super.onDestroy();
    }

    @Override
    public void reloadData() {
        super.reloadData();

        if (presenter != null) {
            presenter.reload();
        }
    }

    public void reloadButtonState() {
        for (PlannedSurveyByOrgUnit plannedSurveyByOrgUnit : plannedSurveys) {
            if (plannedSurveyByOrgUnit.getChecked()) {
                enableScheduleButton();
                return;
            }
        }
        disableScheduleButton();
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.planByOrgUnitList);

        this.adapter = new PlanningPerOrgUnitAdapter(getActivity(),
                () -> {
                    resetList();
                    reloadButtonState();
                }, () -> {
                    reloadData();
        });

        recyclerView.setAdapter(adapter);
    }

    private boolean isNotFiltered(PlannedItem item) {
        return ((PlannedSurvey) item).getSurvey().getOrgUnit().getUid().equals(
                getSelectedOrgUnitUidFilter());
    }
}
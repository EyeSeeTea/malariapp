/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.layout.dashboard.controllers;

import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

public class PlanModuleController extends ModuleController {
    private static final String TAG = ".PlanModuleCOntroller";
    PlannedPerOrgUnitFragment plannedOrgUnitsFragment;

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    public PlanModuleController(ModuleSettings moduleSettings) {
        super(moduleSettings);
        this.tabLayout = R.id.tab_plan_layout;
    }

    public static String getSimpleName() {
        return PlanModuleController.class.getSimpleName();
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        createFilters();
        orgUnitVisibility(View.GONE);
        programVisibility(View.VISIBLE);
        fragment = PlannedFragment.newInstance(server.getClassification());
    }

    private void createFilters() {

        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                        .findViewById(R.id.plan_org_unit_program_filter_view);

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                        new OrgUnitProgramFilterView.FilterChangedListener() {
                            @Override
                            public void onProgramFilterChanged(String programFilter) {
                                saveCurrentFilters();

                                DashboardActivity.dashboardActivity.onProgramSelected(programFilter);

                            }

                            @Override
                            public void onOrgUnitFilterChanged(String orgUnitFilter) {
                                saveCurrentFilters();

                                if (orgUnitFilter == ""){
                                    DashboardActivity.dashboardActivity.onProgramSelected(
                                            orgUnitProgramFilterView.getSelectedProgramFilter()
                                    );
                                }else {
                                    DashboardActivity.dashboardActivity.onOrgUnitSelected(
                                            orgUnitFilter);
                                }


                            }
                        });
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(
                orgUnitProgramFilterView.getSelectedProgramFilter());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                orgUnitProgramFilterView.getSelectedOrgUnitFilter());
    }

    public boolean isVisible() {
        if (PreferencesState.getInstance().isHidePlanningTab()) {
            DashboardActivity.dashboardActivity.findViewById(R.id.tab_plan_layout).setVisibility(
                    View.GONE);
        }
        return !PreferencesState.getInstance().isHidePlanningTab();
    }


    public void onOrgUnitSelected(String orgUnitUid) {
        Log.d(TAG, "onOrgUnitSelected");
        //hide plannedFragment layout and show plannedOrgUnitsFragment
        programVisibility(View.GONE);
        orgUnitVisibility(View.VISIBLE);

        if (plannedOrgUnitsFragment == null) {
            plannedOrgUnitsFragment = new PlannedPerOrgUnitFragment();
        }
        plannedOrgUnitsFragment.setOrgUnitFilter(orgUnitUid);
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(R.id.dashboard_planning_orgunit, plannedOrgUnitsFragment);
        ft.commit();
        plannedOrgUnitsFragment.reloadData();
    }


    public void onProgramSelected(String programUid) {
        Log.d(TAG, "onProgramSelected");
        if (DashboardActivity.dashboardActivity.findViewById(
                R.id.dashboard_planning_orgunit).getVisibility() == View.VISIBLE) {
            //hide plannedFragment layout and show plannedOrgUnitsFragment
            orgUnitVisibility(View.GONE);
            programVisibility(View.VISIBLE);


            if (fragment == null) {
                fragment = PlannedFragment.newInstance(server.getClassification());
            }

            FragmentTransaction ft = getFragmentTransaction();
            ft.replace(R.id.dashboard_planning_init, fragment);
            ft.commit();
            if (programUid != null) {
                ((PlannedFragment) fragment).reloadFilter();
            }
        } else {
            ((PlannedFragment) fragment).reloadFilter();
        }

    }

    private void programVisibility(int visibility) {
        DashboardActivity.dashboardActivity.findViewById(
                R.id.dashboard_planning_init).setVisibility(visibility);
    }

    private void orgUnitVisibility(int visibility) {
        DashboardActivity.dashboardActivity.findViewById(
                R.id.dashboard_planning_orgunit).setVisibility(visibility);
    }
}

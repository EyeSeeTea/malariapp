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

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterOrgUnitArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.filters.FilterProgramArrayAdapter;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.List;

public class PlanModuleController extends ModuleController {
    private static final String TAG = ".PlanModuleCOntroller";
    PlannedPerOrgUnitFragment plannedOrgUnitsFragment;

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    public PlanModuleController(ModuleSettings moduleSettings){
        super(moduleSettings);
        this.tabLayout=R.id.tab_plan_layout;
    }

    public static String getSimpleName(){
        return PlanModuleController.class.getSimpleName();
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        createFilters();
        orgUnitVisibility(View.GONE);
        programVisibility(View.VISIBLE);
        fragment= new PlannedFragment();
    }

    private void createFilters() {

        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                        .findViewById(R.id.plan_org_unit_program_filter_view);

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                        new OrgUnitProgramFilterView.FilterChangedListener() {
                            @Override
                            public void onProgramFilterChanged(ProgramDB programFilter) {
                                saveCurrentFilters();

                                DashboardActivity.dashboardActivity.onProgramSelected(programFilter);

                            }

                            @Override
                            public void onOrgUnitFilterChanged(OrgUnitDB orgUnitFilter) {
                                saveCurrentFilters();

                                if (orgUnitFilter.getName().equals(
                                        PreferencesState.getInstance().getContext().getResources()
                                                .getString(R.string.filter_all_org_units))){
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
                orgUnitProgramFilterView.getSelectedProgramFilter().getUid());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                orgUnitProgramFilterView.getSelectedOrgUnitFilter().getUid());
    }

    public boolean isVisible(){
        if(PreferencesState.getInstance().isHidePlanningTab())
            DashboardActivity.dashboardActivity.findViewById(R.id.tab_plan_layout).setVisibility(View.GONE);
        return !PreferencesState.getInstance().isHidePlanningTab();
    }


    public void onOrgUnitSelected(OrgUnitDB orgUnit) {
        Log.d(TAG, "onOrgUnitSelected");
        //hide plannedFragment layout and show plannedOrgUnitsFragment
        programVisibility(View.GONE);
        orgUnitVisibility(View.VISIBLE);

        if(plannedOrgUnitsFragment==null) {
            plannedOrgUnitsFragment = new PlannedPerOrgUnitFragment();
        }
        plannedOrgUnitsFragment.setOrgUnitFilter(orgUnit.getUid());
        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(R.id.dashboard_planning_orgunit, plannedOrgUnitsFragment);
        ft.commit();
        plannedOrgUnitsFragment.reloadData();
    }


    public void onProgramSelected(ProgramDB program) {
        Log.d(TAG, "onProgramSelected");
        if (DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).getVisibility() == View.VISIBLE) {
            //hide plannedFragment layout and show plannedOrgUnitsFragment
            orgUnitVisibility(View.GONE);
            programVisibility(View.VISIBLE);


            if (fragment == null)
                fragment = new PlannedFragment();

            FragmentTransaction ft = getFragmentTransaction();
            ft.replace(R.id.dashboard_planning_init, fragment);
            ft.commit();
            if(program!=null){
                ((PlannedFragment)fragment).reloadFilter();
            }
        }
        else
            ((PlannedFragment)fragment).reloadFilter();

    }

    private void programVisibility(int visibility) {
        DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_init).setVisibility(visibility);
        DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_programs_header).setVisibility(visibility);
    }

    private void orgUnitVisibility(int visibility) {
        DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).setVisibility(visibility);
        DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit_header).setVisibility(visibility);
    }
}

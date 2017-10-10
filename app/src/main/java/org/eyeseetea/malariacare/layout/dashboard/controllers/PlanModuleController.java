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

import java.util.List;

/**
 * Created by idelcano on 25/02/2016.
 */
public class PlanModuleController extends ModuleController {
    private static final String TAG = ".PlanModuleCOntroller";
    PlannedPerOrgUnitFragment plannedOrgUnitsFragment;
    CustomSpinner orgUnitSpinner;
    CustomSpinner programSpinner;
    private ProgramDB programDefaultOption;
    private OrgUnitDB orgUnitDefaultOption;

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
        programDefaultOption = new ProgramDB(PreferencesState.getInstance().getContext().getResources().getString(R.string.filter_all_org_assessments).toUpperCase());
        orgUnitDefaultOption = new OrgUnitDB(PreferencesState.getInstance().getContext().getResources().getString(R.string.filter_all_org_units).toUpperCase());

        orgUnitSpinner = (CustomSpinner) DashboardActivity.dashboardActivity.findViewById(R.id.spinner_orgUnit_filter);
        programSpinner = (CustomSpinner) DashboardActivity.dashboardActivity.findViewById(R.id.spinner_program_filter);
    }

    public boolean isVisible(){
        if(PreferencesState.getInstance().isHidePlanningTab())
            DashboardActivity.dashboardActivity.findViewById(R.id.tab_plan_layout).setVisibility(View.GONE);
        return !PreferencesState.getInstance().isHidePlanningTab();
    }

    public void prepareFilters(final List<ProgramDB> programList, final List<OrgUnitDB> orgUnitList) {
        //Populate Program View DDL
        if (!programList.contains(programDefaultOption))
            programList.add(0, programDefaultOption);
        programSpinner.setAdapter(new FilterProgramArrayAdapter(DashboardActivity.dashboardActivity, programList));
        //Apply filter to listview
        programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                CustomSpinner spinner = ((CustomSpinner) parent);
                ProgramDB selectedProgram = (ProgramDB) spinner.getItemAtPosition(position);

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                if(selectedProgram==null)
                    return;
                //Set orgUnit to "All org units"
                orgUnitSpinner.setSelection(0,true,true);
                DashboardActivity.dashboardActivity.onProgramSelected(selectedProgram);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Populate Program View DDL
        if(!orgUnitList.contains(orgUnitDefaultOption))
            orgUnitList.add(0, orgUnitDefaultOption);
        orgUnitSpinner.setAdapter(new FilterOrgUnitArrayAdapter(DashboardActivity.dashboardActivity, orgUnitList));
        //Apply filter to listview
        orgUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //Set programSpinner to "All assessments"
                CustomSpinner spinner=((CustomSpinner) parent);
                OrgUnitDB selectedOrgUnit=position==0?null:(OrgUnitDB)spinner.getItemAtPosition(position);

                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);

                if(selectedOrgUnit==null)
                    return;
                programSpinner.setSelection(0,true,true);
                DashboardActivity.dashboardActivity.onOrgUnitSelected(selectedOrgUnit);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

    public void clickOrgProgramSpinner() {
        programSpinner.performClick();
    }

    public void clickOrgUnitSpinner() {
        orgUnitSpinner.performClick();
    }
}

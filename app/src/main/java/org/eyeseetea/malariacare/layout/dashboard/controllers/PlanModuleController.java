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
import android.view.View;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;

/**
 * Created by idelcano on 25/02/2016.
 */
public class PlanModuleController extends ModuleController {
    PlannedPerOrgUnitFragment plannedOrgUnitsFragment;
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
        DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).setVisibility(View.GONE);
        fragment= new PlannedFragment();
    }

    public boolean isVisible(){
        if(PreferencesState.getInstance().isHidePlanningTab())
            DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).setVisibility(View.GONE);
        return !PreferencesState.getInstance().isHidePlanningTab();
    }

    public void onOrgUnitSelected(OrgUnit orgUnit) {
        //hide plannedFragment layout and show plannedOrgUnitsFragment
        if(DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).getVisibility()!=View.VISIBLE) {
            DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_init).setVisibility(View.GONE);
            DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).setVisibility(View.VISIBLE);
            plannedOrgUnitsFragment = new PlannedPerOrgUnitFragment();

            try {
                //fix some visual problems
                View vg = DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit);
                vg.invalidate();
            } catch (Exception e) {
            }

            FragmentTransaction ft = getFragmentTransaction();
            ft.replace(R.id.dashboard_planning_orgunit, plannedOrgUnitsFragment);
            ft.commit();
            plannedOrgUnitsFragment.loadOrgUnit(orgUnit);
        }
        else {
            plannedOrgUnitsFragment.loadOrgUnit(orgUnit);
        }

    }

    public void onProgramSelected(Program program) {
        if(DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_init).getVisibility()!=View.VISIBLE)
        {
            //hide plannedFragment layout and show plannedOrgUnitsFragment
            DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_init).setVisibility(View.VISIBLE);
            DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit).setVisibility(View.GONE);
            fragment = new PlannedFragment();

            try {
                //fix some visual problems
                View vg = DashboardActivity.dashboardActivity.findViewById(R.id.dashboard_planning_orgunit);
                vg.invalidate();
            } catch (Exception e) {
            }

            FragmentTransaction ft = getFragmentTransaction();
            ft.replace(R.id.dashboard_planning_init, fragment);
            ft.commit();
        }
        else{
            ((PlannedFragment) fragment).loadProgram(program);
        }
    }
}

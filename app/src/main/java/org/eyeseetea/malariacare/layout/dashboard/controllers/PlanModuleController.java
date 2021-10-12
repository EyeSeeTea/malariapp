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

import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.IModuleFragment;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.fragments.PlannedPerOrgUnitFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;

public class PlanModuleController extends ModuleController {
    private static final String TAG = ".PlanModuleCOntroller";

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

        fragment = PlannedFragment.newInstance(server.getClassification());
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

        fragment = new PlannedPerOrgUnitFragment();

        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(R.id.dashboard_planning_init, fragment);
        ft.commitAllowingStateLoss();

        if (orgUnitUid != null) {
            ((IModuleFragment) fragment).reloadData();
        }

    }

    public void onProgramSelected(String programUid) {
        Log.d(TAG, "onProgramSelected");

            fragment = PlannedFragment.newInstance(server.getClassification());

        FragmentTransaction ft = getFragmentTransaction();
        ft.replace(R.id.dashboard_planning_init, fragment);
        ft.commitAllowingStateLoss();
        if (programUid != null) {
            ((IModuleFragment) fragment).reloadData();
        }
    }
}

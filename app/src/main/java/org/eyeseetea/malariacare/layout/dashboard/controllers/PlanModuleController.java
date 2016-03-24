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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.PlannedFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;

/**
 * Created by idelcano on 25/02/2016.
 */
public class PlanModuleController extends ModuleController {

    public PlanModuleController(ModuleSettings moduleSettings){
        super(moduleSettings);
        this.tabLayout=R.id.tab_plan_layout;
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        fragment= new PlannedFragment();
    }

    public boolean isVisible(){
        return !PreferencesState.getInstance().isHidePlanningTab();
    }

    /**
     * Going back from planning -> confirms closing
     */
    public void onBackPressed(){
        super.onBackPressed();
    }

}

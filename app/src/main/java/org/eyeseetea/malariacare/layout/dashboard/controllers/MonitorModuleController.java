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

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.fragments.MonitorBySurveyActionsFragment;
import org.eyeseetea.malariacare.fragments.MonitorFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;

public class MonitorModuleController extends ModuleController {

    MonitorBySurveyActionsFragment monitorBySurveyActionsFragment;
    MonitorFragment monitoringByCalendar;

    public MonitorModuleController(ModuleSettings moduleSettings) {
        super(moduleSettings);
        this.tabLayout = R.id.tab_monitor_layout;
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);

        monitorBySurveyActionsFragment =
                MonitorBySurveyActionsFragment.newInstance();

        fragment = monitorBySurveyActionsFragment;
/*
        monitorFragment.setFilterType(moduleSettings.getMonitorFilter());
        fragment = monitorFragment;*/
    }

    public void openMonitoringByCalendar() {
        if (monitoringByCalendar == null) {
            monitoringByCalendar = new MonitorFragment();
            monitoringByCalendar.setFilterType(moduleSettings.getMonitorFilter());
        }

        fragment = monitoringByCalendar;

        replaceFragment(R.id.dashboard_charts_container, monitoringByCalendar);

        reloadData();
    }

    public void openMonitorByActions() {
        fragment = monitorBySurveyActionsFragment;

        replaceFragment(R.id.dashboard_charts_container, monitorBySurveyActionsFragment);

        reloadData();
    }
}

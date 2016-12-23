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

package org.eyeseetea.malariacare.layout.dashboard.config;

import android.app.Application;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO that holds the settings.json info that separates representational from behavioural code
 * Created by arrizabalaga on 16/03/16.
 */
public class AppSettings {
    /**
     * Reference to the dashboard settings
     */
    @JsonProperty("dashboard")
    DashboardSettings dashboardSettings;

    /**
     * Reference to the database settings
     */
    @JsonProperty("database")
    DatabaseSettings databaseSettings;

    public AppSettings(){}

    public DashboardSettings getDashboardSettings() {
        return dashboardSettings;
    }

    public void setDashboardSettings(DashboardSettings dashboardSettings) {
        this.dashboardSettings = dashboardSettings;
    }

    public DatabaseSettings getDatabaseSettings() {
        return databaseSettings;
    }

    public void setDatabaseSettings(DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;
    }

    @Override
    public String toString() {
        return "AppSettings{" +
                "dashboard=" + dashboardSettings +
                ", database=" + databaseSettings +
                '}';
    }
}

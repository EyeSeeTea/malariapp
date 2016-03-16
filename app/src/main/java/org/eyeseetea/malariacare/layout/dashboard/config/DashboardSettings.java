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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.eyeseetea.malariacare.layout.dashboard.deserializers.DashboardOrientationDeserializer;

import java.util.List;

/**
 * Created by arrizabalaga on 16/03/16.
 */
public class DashboardSettings {
    @JsonDeserialize(using = DashboardOrientationDeserializer.class)
    DashboardOrientation orientation;

    List<ModuleSettings> modules;

    public DashboardSettings(){

    }

    public DashboardOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DashboardOrientation orientation) {
        this.orientation = orientation;
    }

    public List<ModuleSettings> getModules() {
        return modules;
    }

    public void setModules(List<ModuleSettings> modules) {
        this.modules = modules;
    }

    @Override
    public String toString() {
        return "DashboardSettings{" +
                "orientation=" + orientation +
                ", modules=" + modules +
                '}';
    }
}

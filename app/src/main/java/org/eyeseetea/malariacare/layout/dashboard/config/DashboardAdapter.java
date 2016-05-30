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

/**
 * Created by idelcano on 16/05/2016.
 */
public enum DashboardAdapter {
    DYNAMIC("dynamic"),
    AUTOMATIC("automatic");

    private final String id;

    DashboardAdapter(final String id){
        this.id=id;
    }

    public String toString(){
        return id;
    }

    public static DashboardAdapter fromId(final String id){
        if(id==null){
            return null;
        }

        for(DashboardAdapter dashboardAdapter:DashboardAdapter.values()){
            if(id.equalsIgnoreCase(dashboardAdapter.id)){
                return dashboardAdapter;
            }
        }

        return null;
    }
}

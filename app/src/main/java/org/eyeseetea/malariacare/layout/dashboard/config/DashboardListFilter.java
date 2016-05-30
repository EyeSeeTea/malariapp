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
 * Represents the type of filter of survey list
 * Created by idelcano on 30/05/2016.
 */

public enum DashboardListFilter {
    LAST_FOR_ORG("lastForOU"),
    NONE("none");

    private final String id;

    DashboardListFilter(final String id){
        this.id=id;
    }

    public String toString(){
        return id;
    }

    public static DashboardListFilter fromId(final String id){
        if(id==null){
            return null;
        }

        for(DashboardListFilter dashboardListFilter : DashboardListFilter.values()){
            if(id.equalsIgnoreCase(dashboardListFilter.id)){
                return dashboardListFilter;
            }
        }

        return null;
    }
}
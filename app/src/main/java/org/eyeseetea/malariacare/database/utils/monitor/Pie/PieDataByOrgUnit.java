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

package org.eyeseetea.malariacare.database.utils.monitor.pie;

import org.eyeseetea.malariacare.database.model.OrgUnit;

/**
 * Created by idelcano on 23/08/2016.
 */
public class PieDataByOrgUnit extends PieDataBase {
    /**
     * Type of program for this chart
     */
    private OrgUnit orgUnit;


    /**
     * Constructor per OrgUnit
     * @param orgUnit
     */
    public PieDataByOrgUnit(OrgUnit orgUnit) {
        this.orgUnit=orgUnit;
    }
    public String toJSON(String tipChat){
        String pieTitle = String.format("%s (%s)", orgUnit.getName(), orgUnit.getId_org_unit());
        String json = String.format(JSONFORMAT, pieTitle, tipChat, orgUnit.getId_org_unit(), this.numA, this.numB, this.numC, orgUnit.getUid(), orgUnit.getUid());
        return json;
    }
}

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

package org.eyeseetea.malariacare.data.database.utils.monitor.facilities;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.HashMap;
import java.util.Map;

public class FacilityTableDataByOrgUnit extends FacilityTableDataBase {

    private static final String TAG = ".FacilityTableDataOU";
    Map<String, FacilityRowDataBase> rowData;

    public FacilityTableDataByOrgUnit(ProgramDB program, OrgUnitDB orgUnit,
            ServerClassification serverClassification) {
        super(serverClassification);
        this.title = AUtils.escapeQuotes(program.getName());
        this.uid = program.getUid();
        this.id = String.valueOf(program.getId_program());
        rowData = new HashMap<>();
    }

    public void addSurvey(SurveyDB survey) {
        OrgUnitDB orgUnit = survey.getOrgUnit();
        //Get facility row
        FacilityRowDataBase facilityRow = rowData.get(orgUnit.toString());
        //First time facility
        if (facilityRow == null) {
            facilityRow = new FacilityRowDataBase(AUtils.escapeQuotes(orgUnit.getName()),
                    this.serverClassification);
            rowData.put(orgUnit.toString(), facilityRow);
        }
        //Add survey
        facilityRow.addSurvey(survey);
    }

    public String getAsJSON() {
        return String.format("{title:'%s',months:%s,tables:%s,tableuid:'%s',id:'%s'}", title,
                getMonthsAsJSONArray(),
                getFacilitiesAsJSONArray(rowData),
                uid, id);
    }
}

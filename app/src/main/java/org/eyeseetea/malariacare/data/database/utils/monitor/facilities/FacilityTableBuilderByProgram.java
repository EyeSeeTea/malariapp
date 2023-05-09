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

import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeSetDataTablesPerProgram;

import android.webkit.WebView;

import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacilityTableBuilderByProgram {
    private final Map<String, OrgUnit> orgUnits;
    private final Map<String, Program> programs;
    private List<Survey> surveys;
    private Map<String, FacilityTableDataByProgram> facilityTableDataMap;

    public FacilityTableBuilderByProgram(List<Survey> surveys, Map<String, OrgUnit> orgUnits, Map<String, Program> programs) {
        this.surveys = surveys;
        this.orgUnits = orgUnits;
        this.programs = programs;
        this.facilityTableDataMap = new HashMap<>();
    }

    private void build(List<Survey> surveys,
                       ServerClassification serverClassification) {
        for (Survey survey : surveys) {

            //Get right table
            FacilityTableDataByProgram facilityTableData = facilityTableDataMap.get(
                    survey.getOrgUnitUId());

            OrgUnit orgUnit = orgUnits.get(survey.getOrgUnitUId());

            //Init entry first time of a program
            if (facilityTableData == null) {
                facilityTableData = new FacilityTableDataByProgram(orgUnit,
                        serverClassification);
                facilityTableDataMap.put(survey.getOrgUnitUId(), facilityTableData);
            }

            //Add survey to that table
            facilityTableData.addSurvey(survey, programs);
        }
    }

    public void addDataInChart(WebView webView,
                               ServerClassification serverClassification) {
        //Build tables
        build(surveys, serverClassification);
        //Inject tables in view
        for (Map.Entry<String, FacilityTableDataByProgram> tableEntry :
                facilityTableDataMap.entrySet()) {
            String cadena = tableEntry.getKey();
            FacilityTableDataByProgram facilityTableData = tableEntry.getValue();
            invokeSetDataTablesPerProgram(webView, cadena, facilityTableData.getAsJSON());
        }
    }
}

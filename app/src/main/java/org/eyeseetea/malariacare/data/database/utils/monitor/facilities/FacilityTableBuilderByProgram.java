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

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacilityTableBuilderByProgram {
    private List<SurveyDB> surveys;
    private Map<String, FacilityTableDataByProgram> facilityTableDataMap;

    public FacilityTableBuilderByProgram(List<SurveyDB> surveys) {
        this.surveys = surveys;
        this.facilityTableDataMap = new HashMap<>();
    }

    private void build(List<SurveyDB> surveys,
            ServerClassification serverClassification) {
        for (SurveyDB survey : surveys) {

            //Get right table
            FacilityTableDataByProgram facilityTableData = facilityTableDataMap.get(
                    survey.getOrgUnit().getUid());

            //Init entry first time of a program
            if (facilityTableData == null) {
                facilityTableData = new FacilityTableDataByProgram(survey.getOrgUnit(),
                        serverClassification);
                facilityTableDataMap.put(survey.getOrgUnit().getUid(), facilityTableData);
            }

            //Add survey to that table
            facilityTableData.addSurvey(survey);
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

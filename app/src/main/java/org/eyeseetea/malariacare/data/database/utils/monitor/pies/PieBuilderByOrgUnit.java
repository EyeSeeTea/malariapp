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

package org.eyeseetea.malariacare.data.database.utils.monitor.pies;

import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeSetOrgUnitPieData;
import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeSetProgramPieData;

import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieBuilderByOrgUnit {
    private Map<OrgUnitDB, PieDataByOrgUnit> pieTabGroupDataMap;
    private List<SurveyDB> surveys;
    private ServerClassification serverClassification;

    public PieBuilderByOrgUnit(List<SurveyDB> surveys, ServerClassification serverClassification) {
        this.surveys = surveys;
        this.serverClassification = serverClassification;
        pieTabGroupDataMap = new HashMap<>();
    }

    public void addDataInChart(WebView webView) {
        //Build entries
        List<PieDataByOrgUnit> entries = build(surveys);
        String json = buildJSONArray(entries);
        invokeSetOrgUnitPieData(webView, json);
        entries.clear();
    }

    private List<PieDataByOrgUnit> build(List<SurveyDB> surveys) {
        for (SurveyDB survey : surveys) {
            build(survey);
        }

        return new ArrayList(pieTabGroupDataMap.values());
    }

    private void build(SurveyDB survey) {
        //Get the program
        OrgUnitDB orgUnit = survey.getOrgUnit();

        //Get the entry for that program
        PieDataByOrgUnit pieTabGroupData = pieTabGroupDataMap.get(orgUnit);

        //First time no entry
        if (pieTabGroupData == null) {
            pieTabGroupData = new PieDataByOrgUnit(orgUnit);
            pieTabGroupDataMap.put(orgUnit, pieTabGroupData);
        }

        if (serverClassification == ServerClassification.COMPETENCIES) {
            pieTabGroupData.incCounterByCompetency(survey.getCompetencyScoreClassification());
        } else {
            pieTabGroupData.incCounterByScoring(survey.getMainScore().getScore());
        }
    }

    private String buildJSONArray(List<PieDataByOrgUnit> entries) {
        String arrayJSON = "[";
        int i = 0;
        for (PieDataByOrgUnit pieTabGroupData : entries) {
            String pieJSON = pieTabGroupData.toJSON(
                    PreferencesState.getInstance().getContext().getString(
                            R.string.dashboard_tip_pie_chart));
            arrayJSON += pieJSON;
            i++;
            if (i != entries.size()) {
                arrayJSON += ",";
            }
        }
        arrayJSON += "]";
        return arrayJSON;
    }
}

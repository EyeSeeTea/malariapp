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

import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeSetProgramPieData;

import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Program;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieBuilderByProgram {
    private final Map<String, Program> programs;
    private Map<String, PieDataByProgram> pieTabGroupDataMap;
    private List<Survey> surveys;
    private ServerClassification serverClassification;

    public PieBuilderByProgram(List<Survey> surveys, ServerClassification serverClassification,Map<String, Program> programs) {
        this.surveys = surveys;
        this.serverClassification = serverClassification;
        this.programs = programs;

        pieTabGroupDataMap = new HashMap<>();
    }


    /**
     * Adds calculated entries to the given webView
     */
    public void addDataInChart(WebView webView) {
        //Build entries
        List<PieDataByProgram> entries = build(surveys);
        String json = buildJSONArray(entries);
        invokeSetProgramPieData(webView, json);
        entries.clear();
    }

    private List<PieDataByProgram> build(List<Survey> surveys) {
        for (Survey survey : surveys) {
            build(survey);
        }

        return new ArrayList(pieTabGroupDataMap.values());
    }

    private void build(Survey survey) {
        //Get the program
        Program program = programs.get(survey.getProgramUId()) ;

        //Get the entry for that program
        PieDataByProgram pieTabGroupData = pieTabGroupDataMap.get(survey.getProgramUId());

        //First time no entry
        if (pieTabGroupData == null) {
            pieTabGroupData = new PieDataByProgram(program);
            pieTabGroupDataMap.put(survey.getProgramUId(), pieTabGroupData);
        }

        if (serverClassification == ServerClassification.COMPETENCIES) {
            pieTabGroupData.incCounterByCompetency(survey.getCompetency().getId());
        } else {
            pieTabGroupData.incCounterByScoring(survey.getScore().getScore());
        }
    }

    private String buildJSONArray(List<PieDataByProgram> entries) {
        String arrayJSON = "[";
        int i = 0;
        for (PieDataByProgram pieTabGroupData : entries) {
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

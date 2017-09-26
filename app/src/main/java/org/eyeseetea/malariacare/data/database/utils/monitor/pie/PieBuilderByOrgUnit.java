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

package org.eyeseetea.malariacare.data.database.utils.monitor.pie;

import android.content.Context;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 23/08/2016.
 */
public class PieBuilderByOrgUnit extends PieBuilderBase {
    public static final String JAVASCRIPT_UPDATE_CHARTS = "javascript:setOrgUnitPieData(%s)";

    /**
     * Map of entries per program
     */
    private Map<OrgUnitDB,PieDataByOrgUnit> pieTabGroupDataMap;
    /**
     * Default constructor
     *
     * @param surveys
     * @param context
     */
    public PieBuilderByOrgUnit(List<SurveyDB> surveys, Context context) {
        super(surveys, context);
        pieTabGroupDataMap=new HashMap<>();
    }
    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build entries
        List<PieDataByOrgUnit> entries=build(surveys);
        //Inyect entries in view
        injectDataInChart(webView, entries);
        buildJSONArray(entries);
        entries.clear();
    }
    private void build(SurveyDB survey) {
        //Get the program
        OrgUnitDB orgUnit=survey.getOrgUnit();

        //Get the entry for that program
        PieDataByOrgUnit pieTabGroupData = pieTabGroupDataMap.get(orgUnit);

        //First time no entry
        if(pieTabGroupData ==null){
            pieTabGroupData =new PieDataByOrgUnit(orgUnit);
            pieTabGroupDataMap.put(orgUnit, pieTabGroupData);
        }
        //Increment surveys for that month
        pieTabGroupData.incCounter(survey.getMainScore());
    }


    private List<PieDataByOrgUnit> build(List<SurveyDB> surveys) {
        for(SurveyDB survey:surveys){
            build(survey);
        }

        return new ArrayList(pieTabGroupDataMap.values());
    }
    private void injectDataInChart(WebView webView, List<PieDataByOrgUnit> entries) {
        //Build array JSON
        String json=buildJSONArray(entries);

        //Inyect in browser
        inyectInBrowser(webView, JAVASCRIPT_UPDATE_CHARTS, json);
    }

    private String buildJSONArray(List<PieDataByOrgUnit> entries){
        String arrayJSON="[";
        int i=0;
        for(PieDataByOrgUnit pieTabGroupData :entries){
            String pieJSON= pieTabGroupData.toJSON(context.getString(R.string.dashboard_tip_pie_chart));
            arrayJSON+=pieJSON;
            i++;
            if(i!=entries.size()){
                arrayJSON+=",";
            }
        }
        arrayJSON+="]";
        return arrayJSON;
    }
}

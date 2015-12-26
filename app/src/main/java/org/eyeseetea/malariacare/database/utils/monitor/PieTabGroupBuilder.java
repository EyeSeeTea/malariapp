/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.utils.monitor;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build that creates and inyects the info into the piecharts
 * Created by arrizabalaga on 9/10/15.
 */
public class PieTabGroupBuilder {

    public static final String JAVASCRIPT_UPDATE_CHARTS = "javascript:buildPieCharts(%s)";
    private static final String TAG=".PieTabGroupBuilder";
    public static final String JAVASCRIPT_SHOW = "javascript:rebuildTableFacilities()";

    /**
     * Required to inyect title according to current language
    */
    private Context context;

    /**
     * List of sent surveys
     */
    private List<Survey> surveys;

    /**
     * Map of entries per program
     */
    private Map<TabGroup,PieTabGroupData> pieTabGroupDataMap;

    /**
     * Default constructor
     */
    public PieTabGroupBuilder(List<Survey> surveys, Context context) {
        pieTabGroupDataMap = new HashMap<>();
        this.surveys = surveys;
        this.context = context;
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build entries
        List<PieTabGroupData> entries=build(surveys);
        //Inyect entries in view
        inyectDataInChart(webView,entries);
    }

    private List<PieTabGroupData> build(List<Survey> surveys) {
        for(Survey survey:surveys){
            build(survey);
        }

        return new ArrayList(pieTabGroupDataMap.values());
    }

    private void build(Survey survey) {
        //Get the program
        TabGroup tabGroup=survey.getTabGroup();

        //Get the entry for that program
        PieTabGroupData pieTabGroupData = pieTabGroupDataMap.get(tabGroup);

        //First time no entry
        if(pieTabGroupData ==null){
            pieTabGroupData =new PieTabGroupData(tabGroup);
            pieTabGroupDataMap.put(tabGroup, pieTabGroupData);
        }
        //Increment surveys for that month
        pieTabGroupData.incCounter(survey.getMainScore());
    }

    private void inyectDataInChart(WebView webView, List<PieTabGroupData> entries) {
        //Build array JSON
        String json=buildJSONArray(entries);

        //Inyect in browser
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_CHARTS, json);
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);
    }

    private String buildJSONArray(List<PieTabGroupData> entries){
        String arrayJSON="[";
        int i=0;
        for(PieTabGroupData pieTabGroupData :entries){
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

    public static void showPieTab(WebView webView){
        //Set chart title
        Log.d(TAG, JAVASCRIPT_SHOW);
        webView.loadUrl(String.format(JAVASCRIPT_SHOW));
    }

}

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
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds data for table of facilities
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityTableBuilder {

    public static final String JAVASCRIPT_UPDATE_TABLE = "javascript:buildTableFacilities(%d,%s)";
    private static final String TAG=".FacilityTableBuilder";
    public static final String JAVASCRIPT_SHOW = "javascript:renderPieCharts()";
    public static final String JAVASCRIPT_SET_GREEN = "javascript:setGreen(%s)";
    public static final String JAVASCRIPT_SET_YELLOW = "javascript:setYellow(%s)";
    public static final String JAVASCRIPT_SET_RED = "javascript:setRed(%s)";

    /**
     * Required to inyect title according to current language
     */
    private Context context;

    /**
     * List of sent surveys
     */
    private List<Survey> surveys;

    private Map<Program,FacilityTableData> facilityTableDataMap;


    /**
     * Default constructor
     */
    public FacilityTableBuilder(List<Survey> surveys, Context context) {
        this.surveys = surveys;
        this.context = context;
        this.facilityTableDataMap = new HashMap<>();
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build tables
        build(surveys);
        //Inyect tables in view
        for(Map.Entry<Program,FacilityTableData> tableEntry:facilityTableDataMap.entrySet()){
            Program program=tableEntry.getKey();
            FacilityTableData facilityTableData=tableEntry.getValue();
            inyectDataInChart(webView, program, facilityTableData);
        }

    }

    /**
     * Build table data from surveys
     * @param surveys
     * @return
     */
    private void build(List<Survey> surveys){
        for(Survey survey:surveys){
            //Current program
            Program program=survey.getProgram();

            //Get right table
            FacilityTableData facilityTableData=facilityTableDataMap.get(program);

            //Init entry first time of a tabgroup
            if(facilityTableData==null){
                facilityTableData=new FacilityTableData(program);
                facilityTableDataMap.put(program,facilityTableData);
            }

            //Add survey to that table
            facilityTableData.addSurvey(survey);
        }
    }

    private void inyectDataInChart(WebView webView, Program program,FacilityTableData facilityTableData) {
        //Build JSON data
        String json=facilityTableData.getAsJSON();

        //Inyect in browser
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_TABLE,program.getId_program(),json);
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);

    }

    public static void showFacilities(WebView webView) {
        Log.d(TAG, JAVASCRIPT_SHOW);
        webView.loadUrl(String.format(JAVASCRIPT_SHOW));
    }
    public static void setColor(WebView webView){
        //noinspection ResourceType
        String color=PreferencesState.getInstance().getContext().getResources().getString(R.color.lightGreen);
        String injectColor=String.format(JAVASCRIPT_SET_GREEN,"{color:'"+getHtmlCodeColor(color)+"'}");
        Log.d(TAG, injectColor);
        webView.loadUrl(injectColor);
        //noinspection ResourceType
        color=PreferencesState.getInstance().getContext().getResources().getString(R.color.darkRed);
        injectColor=String.format(JAVASCRIPT_SET_RED,"{color:'"+getHtmlCodeColor(color)+"'}");
        Log.d(TAG, injectColor);
        webView.loadUrl(injectColor);
        //noinspection ResourceType
        color=PreferencesState.getInstance().getContext().getResources().getString(R.color.assess_yellow);
        injectColor = String.format(JAVASCRIPT_SET_YELLOW,"{color:'"+getHtmlCodeColor(color)+"'}");
        Log.d(TAG,injectColor);
        webView.loadUrl(injectColor);
    }

    private static String getHtmlCodeColor(String color) {
        //remove the first two characters(about alpha color).
        String colorRRGGBB="#"+color.substring(3,9);
        return colorRRGGBB;
    }
}

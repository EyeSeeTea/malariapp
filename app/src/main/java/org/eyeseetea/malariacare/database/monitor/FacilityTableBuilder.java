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

package org.eyeseetea.malariacare.database.monitor;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;

import java.util.HashMap;
import java.util.List;

/**
 * Builds data for table of facilities
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityTableBuilder {

    public static final String JAVASCRIPT_UPDATE_TABLE = "javascript:buildTableFacilities(%s)";
    private static final String TAG=".FacilityTableBuilder";

    /**
     * Required to inyect title according to current language
     */
    private Context context;

    /**
     * List of sent surveys
     */
    private List<Survey> surveys;


    /**
     * Default constructor
     */
    public FacilityTableBuilder(List<Survey> surveys, Context context) {
        this.surveys = surveys;
        this.context = context;
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build entries
        FacilityTableData facilityTableData=build(surveys);
        //Inyect entries in view
        inyectDataInChart(webView,facilityTableData);
    }

    /**
     * Build table data from surveys
     * @param surveys
     * @return
     */
    private FacilityTableData build(List<Survey> surveys){
        FacilityTableData facilityTableData=new FacilityTableData(context.getString(R.string.dashboard_title_table_facilities));
        for(Survey survey:surveys){
            facilityTableData.addSurvey(survey);
        }
        return facilityTableData;
    }

    private void inyectDataInChart(WebView webView, FacilityTableData facilityTableData) {
        //Build JSON data
        String json=facilityTableData.getAsJSON();

        //Inyect in browser
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_TABLE,json);
        Log.d(TAG, json);
        webView.loadUrl(updateChartJS);

    }
}

package org.eyeseetea.malariacare.data.database.utils.monitor.allassessments;/*
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


import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 23/08/2016.
 */
public class SentSurveysBuilderByOrgUnit extends SentSurveysBuilderBase {
    private static final String TAG=".SentSurveysBuilderOU";
    /**
     * List of sent surveys
     */
    List<OrgUnitDB>  orgUnits;
    /**
     * Map of entries per month
     */
    private Map<String,EntrySentSurveysChartByOrgUnit> sentSurveysChartMap;
    /**
     * Default constructor
     *
     * @param surveyList
     * @param context
     * @param orgUnits
     */
    public SentSurveysBuilderByOrgUnit(List<SurveyDB> surveyList, Context context, List<OrgUnitDB> orgUnits) {
        super(surveyList, context);
        this.orgUnits=orgUnits;
        sentSurveysChartMap= new HashMap<>();
    }

    /**
     * Creates the list of entries for the last 6 months from 'now'
     * @return
     */
    private List<EntrySentSurveysChartByOrgUnit> takeLast6MonthsByOrgUnit() {
        List<EntrySentSurveysChartByOrgUnit> last6entries=new ArrayList<>();
        Date today = new Date();
        //Loop over last 6 months
        for(int i=0;i<MAX_MONTHS;i++){
            Date iMonth= minusMonth(today, i);
            String currentMonth= EntrySentSurveysChartByOrgUnit.getDateAsString(iMonth);
            EntrySentSurveysChartByOrgUnit entryMonth=sentSurveysChartMap.get(currentMonth+orgUnit.getUid());
            //No entry for this month ->0
            if(entryMonth==null){
                entryMonth=new EntrySentSurveysChartByOrgUnit(PreferencesState.getInstance().getMonitoringTarget(),iMonth, orgUnit);
            }
            //Whatever was calculated
            last6entries.add(0,entryMonth);
        }

        return last6entries;
    }
    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build entries
        for(OrgUnitDB orgUnit:orgUnits) {
            this.orgUnit = orgUnit;
            build(surveyList);
            //Take only 6 months from now
            List<EntrySentSurveysChartByOrgUnit> entries = takeLast6MonthsByOrgUnit();
            //Inyect entries in view
            injectDataInChart(webView, entries);
        }
    }
    /**
     * Inyects data into the sentSurveys chart
     * @param webView Android webView where data is inyected
     * @param entries List of entries for the chart
     */
    private void injectDataInChart(WebView webView, List<EntrySentSurveysChartByOrgUnit> entries){

        //Add data to the chart
        for(EntrySentSurveysChartByOrgUnit entry:entries){
            Log.d(TAG, entry.getEntryAsJS());
            webView.loadUrl(entry.getEntryAsJS());
        }
    }

    /**
     * Builds a list of entry points for the chart from the list of surveys By Org Unit
     * @param surveys List of sent surveys to create the list
     * @return
     */
    public void build(List<SurveyDB> surveys){
        for(SurveyDB survey:surveys){
            if(survey.getOrgUnit().equals(orgUnit)) {

                //Get the month for the survey (key)
                String month = EntrySentSurveysChartByOrgUnit.getDateAsString(survey.getCompletionDate());

                //Get the entry for that month
                EntrySentSurveysChartByOrgUnit entrySentSurveysChartByOrgUnit = sentSurveysChartMap.get(month+orgUnit.getUid());
                if (entrySentSurveysChartByOrgUnit == null) {
                    entrySentSurveysChartByOrgUnit = new EntrySentSurveysChartByOrgUnit(PreferencesState.getInstance().getMonitoringTarget(), survey.getCompletionDate(), orgUnit);
                    sentSurveysChartMap.put(month+orgUnit.getUid(), entrySentSurveysChartByOrgUnit);
                }
                entrySentSurveysChartByOrgUnit.incSent();
            }
        }
    }
}

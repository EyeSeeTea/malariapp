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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder to create the info required to show the line chart of sent surveys
 * Created by arrizabalaga on 7/10/15.
 */
public class SentSurveysBuilder {

    private static final String TAG=".SentSurveysBuilder";
    private static final int EXPECTED_SENT_SURVEYS_PER_MONTH=30;
    public static final String JAVASCRIPT_UPDATE_CHART = "javascript:updateChartTitle('titleSent','%s')";
    private static final int MAX_MONTHS=6;

    /**
     * Required to inyect title according to current language
     */
    private Context context;

    /**
     * List of sent surveys
     */
    private List<Survey> surveys;

    /**
     * Map of entries per month
     */
    private Map<String,EntrySentSurveysChart> sentSurveysChartMap;

    /**
     * Default constructor
     */
    public SentSurveysBuilder(List<Survey> surveys, Context context) {
        sentSurveysChartMap = new HashMap<>();
        this.surveys = surveys;
        this.context = context;
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build entries
        build(surveys);
        //Take only 6 months from now
        List<EntrySentSurveysChart> entries = takeLast6Months();
        //Inyect entries in view
        inyectDataInChart(webView,entries);
    }

    /**
     * Creates the list of entries for the last 6 months from 'now'
     * @return
     */
    private List<EntrySentSurveysChart> takeLast6Months() {
        List<EntrySentSurveysChart> last6entries=new ArrayList<>();
        Date today = new Date();
        //Loop over last 6 months
        for(int i=0;i<MAX_MONTHS;i++){
            Date iMonth= minusMonth(today, i);
            String currentMonth=EntrySentSurveysChart.getDateAsString(iMonth);
            EntrySentSurveysChart entryMonth=sentSurveysChartMap.get(currentMonth);
            //No entry for this month ->0
            if(entryMonth==null){
                entryMonth=new EntrySentSurveysChart(EXPECTED_SENT_SURVEYS_PER_MONTH,iMonth);
            }
            //Whatever was calculated
            last6entries.add(0,entryMonth);
        }

        return last6entries;
    }

    /**
     * Returns 'date' - 1 month
     * @param today
     * @return
     */
    private Date minusMonth(Date today, int numMonths){
        Calendar cal=Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.MONTH,-1*numMonths);
        return cal.getTime();
    }

    /**
     * Inyects data into the sentSurveys chart
     * @param webView Android webView where data is inyected
     * @param entries List of entries for the chart
     */
    private void inyectDataInChart(WebView webView, List<EntrySentSurveysChart> entries){
        //Set chart title
        inyectChartTitle(webView);

        //Add data to the chart
        for(EntrySentSurveysChart entry:entries){
            Log.d(TAG, entry.getEntryAsJS());
            webView.loadUrl(entry.getEntryAsJS());
        }
    }

    /**
     * Updates the title of the sent chart according to current language
     * @param webView
     */
    private void inyectChartTitle(WebView webView){
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_CHART,context.getString(R.string.dashboard_title_total_assessments));
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);
    }

    /**
     * Builds a list of entry points for the chart from the list of surveys
     * @param surveys List of sent surveys to create the list
     * @return
     */
    public void build(List<Survey> surveys){
        for(Survey survey:surveys){
            build(survey);
        }
    }

    /**
     * Builds an entry from a survey
     * @param survey
     * @return
     */
    private void build(Survey survey){
        //Get the month for the survey (key)
        String month=EntrySentSurveysChart.getDateAsString(survey.getCompletionDate());

        //Get the entry for that month
        EntrySentSurveysChart entrySentSurveysChart=sentSurveysChartMap.get(month);

        //First time no entry
        if(entrySentSurveysChart==null){
            entrySentSurveysChart=new EntrySentSurveysChart(EXPECTED_SENT_SURVEYS_PER_MONTH,survey.getCompletionDate());
            sentSurveysChartMap.put(month,entrySentSurveysChart);
        }
        //Increment surveys for that month
        entrySentSurveysChart.incSent();
    }
}

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder to create the info required to show the line chart of sent surveys
 * Created by arrizabalaga on 7/10/15.
 */
public class SentSurveysBuilder {

    private static final String TAG=".SentSurveysBuilder";
    private static final int EXPECTED_SENT_SURVEYS_PER_MONTH=10;
    public static final String JAVASCRIPT_UPDATE_CHART = "javascript:updateChartTitle('titleSent','%s')";

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
        List<EntrySentSurveysChart> entries=build(surveys);
        //Inyect entries in view
        inyectDataInChart(webView,entries);
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
    public List<EntrySentSurveysChart> build(List<Survey> surveys){
        for(Survey survey:surveys){
            build(survey);
        }
        List<EntrySentSurveysChart> orderedEntries=orderByDate(sentSurveysChartMap.values());
        return orderedEntries;
    }

    /**
     * Builds an entry from a survey
     * @param survey
     * @return
     */
    private EntrySentSurveysChart build(Survey survey){
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

        //Returns the entry
        return entrySentSurveysChart;
    }

    private List<EntrySentSurveysChart> orderByDate(Collection<EntrySentSurveysChart> unorderedCollection){
        List orderedList = new ArrayList(unorderedCollection);
        Collections.sort(orderedList);
        return orderedList;
    }
}

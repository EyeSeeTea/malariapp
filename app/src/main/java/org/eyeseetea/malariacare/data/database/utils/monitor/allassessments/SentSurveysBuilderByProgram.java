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

package org.eyeseetea.malariacare.data.database.utils.monitor.allassessments;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 23/08/2016.
 */
public class SentSurveysBuilderByProgram extends  SentSurveysBuilderBase {
    private static final String TAG=".SentSurveysBuilderP";
    /**
     * Map of entries per month
     */
    private Map<String,EntrySentSurveysChartByProgram> sentSurveysChartMap;
    /**
     * List of sent surveys
     */
    List<Program>  programs;
    /**
     * Default constructor
     *
     * @param surveyList
     * @param context
     * @param programs
     */
    public SentSurveysBuilderByProgram(List<Survey> surveyList, Context context, List<Program> programs) {
        super(surveyList, context);
        this.programs=programs;
        sentSurveysChartMap= new HashMap<>();
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView) {
        //Build entries
        for (Program program : programs) {
            this.program = program;
            build(surveyList);
            //Take only 6 months from now
            List<EntrySentSurveysChartByProgram> entries = takeLast6MonthsByProgram();
            //Inyect entries in view
            injectDataInChart(webView, entries);
        }
    }
    /**
     * Creates the list of entries for the last 6 months from 'now'
     * @return
     */
    private List<EntrySentSurveysChartByProgram> takeLast6MonthsByProgram() {
        List<EntrySentSurveysChartByProgram> last6entries=new ArrayList<>();
        Date today = new Date();
        //Loop over last 6 months
        for(int i=0;i<MAX_MONTHS;i++){
            Date iMonth= minusMonth(today, i);
            String currentMonth= EntrySentSurveysChartByProgram.getDateAsString(iMonth);
            EntrySentSurveysChartByProgram entryMonth=sentSurveysChartMap.get(currentMonth+program.getUid());
            //No entry for this month ->0
            if(entryMonth==null){
                entryMonth=new EntrySentSurveysChartByProgram(EXPECTED_SENT_SURVEYS_PER_MONTH,iMonth,program);
            }
            //Whatever was calculated
            last6entries.add(0,entryMonth);
        }

        return last6entries;
    }
    /**
     * Inyects data into the sentSurveys chart
     * @param webView Android webView where data is inyected
     * @param entries List of entries for the chart
     */
    private void injectDataInChart(WebView webView, List<EntrySentSurveysChartByProgram> entries){
        //Set chart title
        injectChartTitle(webView);

        //Add data to the chart
        for(EntrySentSurveysChartByProgram entry:entries){
            Log.d(TAG, entry.getEntryAsJS());
            webView.loadUrl(entry.getEntryAsJS());
        }
    }

    /**
     * Builds a list of entry points for the chart from the list of surveys
     * @param surveys List of sent surveys to create the list
     * @return
     */
    public void build(List<Survey> surveys){
        for(Survey survey:surveys){
            if(survey.getProgram().equals(program)) {

                //Get the month for the survey (key)
                String month = EntrySentSurveysChartByProgram.getDateAsString(survey.getCompletionDate());
                //Get the entry for that month
                EntrySentSurveysChartByProgram entrySentSurveysChartByProgram = sentSurveysChartMap.get(month+program.getUid());

                //First time no entry
                if (entrySentSurveysChartByProgram == null) {
                    entrySentSurveysChartByProgram = new  EntrySentSurveysChartByProgram(EXPECTED_SENT_SURVEYS_PER_MONTH, survey.getCompletionDate(), program);
                    sentSurveysChartMap.put(month+program.getUid(), entrySentSurveysChartByProgram);
                }
                entrySentSurveysChartByProgram.incSent();
            }
        }
    }
}

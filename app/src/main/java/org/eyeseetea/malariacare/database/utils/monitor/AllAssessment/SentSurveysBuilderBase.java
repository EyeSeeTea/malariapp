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

package org.eyeseetea.malariacare.database.utils.monitor.AllAssessment;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Builder to create the info required to show the line chart of sent surveys
 * Created by arrizabalaga on 7/10/15.
 */
public class SentSurveysBuilderBase {

    private static final String TAG=".SentSurveysBuilderBase";
    static final int EXPECTED_SENT_SURVEYS_PER_MONTH=30;
    public static final String JAVASCRIPT_UPDATE_CHART = "javascript:updateChartTitle('titleSent','%s')";
    public static final String JAVASCRIPT_SHOW = "javascript:showMainTable()";
    static final int MAX_MONTHS=6;

    /**
     * Required to inyect title according to current language
     */
    private Context context;

    Program  program;
    OrgUnit  orgUnit;

    List<Survey> surveyList;

    /**
     * Default constructor
     */
    public SentSurveysBuilderBase(List<Survey> surveyList, Context context) {
        this.surveyList=surveyList;
        this.context = context;
    }

    /**
     * Returns 'date' - 1 month
     * @param today
     * @return
     */
    Date minusMonth(Date today, int numMonths){
        Calendar cal=Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.MONTH, -1 * numMonths);
        return cal.getTime();
    }

    public static void showData(WebView webView){
        //Set chart title
        Log.d(TAG, JAVASCRIPT_SHOW);
        webView.loadUrl(String.format(JAVASCRIPT_SHOW));
    }
    /**
     * Updates the title of the sent chart according to current language
     * @param webView
     */
    void injectChartTitle(WebView webView){
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_CHART,context.getString(R.string.dashboard_title_total_assessments));
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);
    }

    /**
     * Builds an entry from a survey
     * @param survey
     * @return
     */
    private void build(Survey survey){
    }

    public static void init(List<Survey> surveysForGraphic, Activity activity, List<OrgUnit> orgUnits, List<Program> programs, WebView view) {
        new SentSurveysBuilderByOrgUnit(surveysForGraphic, activity,orgUnits).addDataInChart(view);
        new SentSurveysBuilderByProgram(surveysForGraphic, activity,programs).addDataInChart(view);
        //Show stats by program
        SentSurveysBuilderBase.showData(view);
    }
}

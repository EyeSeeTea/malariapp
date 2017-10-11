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

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

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
    public static final String JAVASCRIPT_SHOW = "javascript:showMainTableByProgram()";
    static final int MAX_MONTHS=6;

    /**
     * Required to inyect title according to current language
     */
    private Context context;

    ProgramDB program;
    OrgUnitDB orgUnit;

    List<SurveyDB> surveyList;

    /**
     * Default constructor
     */
    public SentSurveysBuilderBase(List<SurveyDB> surveyList, Context context) {
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
    public static void injectChartTitle(WebView webView){
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_CHART, PreferencesState.getInstance().getContext().getString(R.string.dashboard_title_total_assessments));
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);
    }
}

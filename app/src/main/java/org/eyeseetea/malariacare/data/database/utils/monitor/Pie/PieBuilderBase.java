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

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.data.database.model.Survey;

import java.util.List;

/**
 * Build that creates and inyects the info into the piecharts
 * Created by arrizabalaga on 9/10/15.
 */
public class PieBuilderBase {

    private static final String TAG=".PieBuilderBase";
    public static final String JAVASCRIPT_SHOW = "javascript:rebuildTableFacilities()";

    /**
     * Required to inyect title according to current language
    */
    Context context;

    /**
     * List of sent surveys
     */
    List<Survey> surveys;


    /**
     * Default constructor
     */
    public PieBuilderBase(List<Survey> surveys, Context context) {
        this.surveys = surveys;
        this.context = context;
    }


    void inyectInBrowser(WebView webView, String formatter, String json) {
        String updateChartJS=String.format(formatter, json);
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);
    }

    public static void showPieTab(WebView webView){
        //Set chart title
        Log.d(TAG, JAVASCRIPT_SHOW);
        webView.loadUrl(String.format(JAVASCRIPT_SHOW));
    }
}

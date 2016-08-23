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

package org.eyeseetea.malariacare.database.utils.monitor.Facility;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds data for table of facilities
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityTableBuilderBase {

    public static final String JAVASCRIPT_UPDATE_TABLE = "javascript:buildTableFacilities(%d,%s)";
    private static final String TAG=".FacilityTableBuilder";
    public static final String JAVASCRIPT_SHOW_PROGRAMS = "javascript:renderPieChartsByProgram()";
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
    List<Survey> surveys;


    /**
     * Default constructor
     */
    public FacilityTableBuilderBase(List<Survey> surveys, Context context) {
        this.surveys = surveys;
        this.context = context;
   }

    public static void showFacilities(WebView webView) {
        Log.d(TAG, JAVASCRIPT_SHOW_PROGRAMS);
        webView.loadUrl(String.format(JAVASCRIPT_SHOW_PROGRAMS));
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

    void inyectDataInChart(WebView webView, Long id, String json) {

        //Inyect in browser
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_TABLE,id,json);
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);

    }
}

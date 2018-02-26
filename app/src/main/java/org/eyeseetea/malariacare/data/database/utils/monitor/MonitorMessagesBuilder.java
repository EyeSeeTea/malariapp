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

package org.eyeseetea.malariacare.data.database.utils.monitor;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

/**
 * Created by arrizabalaga on 26/07/16.
 */
public class MonitorMessagesBuilder {

    private static final String TAG="MonitorMessagesBuilder";
    public static final String JSON_MAP = "{'assesmentUnderTaken':'%s','target': '%s','qualityOfCare': '%s','months':'%s'}";
    public static final String JAVASCRIPT_UPDATE_TABLE = "javascript:initContext(%s)";

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        String messagesJSON=buildJSON();
        String jsFunction= String.format(JAVASCRIPT_UPDATE_TABLE,messagesJSON);
        Log.d(TAG, jsFunction);
        webView.loadUrl(jsFunction);
    }

    private String buildJSON() {
        String assesmentUnderTaken = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_assessments);
        String target = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_target);
        String qualityOfCare = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_quality_of_care);
        String months = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_months);
        return String.format(JSON_MAP,assesmentUnderTaken,target,qualityOfCare,months);
    }


}

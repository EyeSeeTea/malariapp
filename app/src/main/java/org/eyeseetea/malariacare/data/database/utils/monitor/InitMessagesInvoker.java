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

import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeInitMessages;

import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

public class InitMessagesInvoker {

    public static final String JSON_MAP =
            "{'assesmentUnderTaken':'%s','target': '%s','qualityOfCare': '%s','months':'%s',"
                    + "'noSurveys':'%s','multipleEventLegend': '%s' }";

    public void invoke(WebView webView){
        String messagesJSON=buildJSON();
        invokeInitMessages(webView, messagesJSON);
    }

    private String buildJSON() {
        String assesmentUnderTaken = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_assessments);
        String target = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_target);
        String qualityOfCare = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_quality_of_care);
        String months = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_months);
        String noSurveys = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_no_surveys_to_show);
        String multipleEventLegend = PreferencesState.getInstance().getContext().getResources().getString(R.string.monitor_js_multiple_event_legend);
        return String.format(JSON_MAP, assesmentUnderTaken, target, qualityOfCare, months,
                noSurveys, multipleEventLegend);
    }


}

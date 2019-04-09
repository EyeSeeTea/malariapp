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

package org.eyeseetea.malariacare.data.database.utils.monitor.facilities;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.utils.CompetencyUtils;

import java.util.List;

/**
 * Builds data for table of facilities
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityTableBuilderBase {

    private static final String TAG = ".FacilityTableBuilder";
    public static final String JAVASCRIPT_SET_COMPETENT_COLOR = "javascript:setCompetentColor(%s)";
    public static final String JAVASCRIPT_SET_COMPETENT_IMPROVEMENT_COLOR =
            "javascript:setCompetentImprovementColor(%s)";
    public static final String JAVASCRIPT_SET_NOT_COMPETENT_COLOR =
            "javascript:setNotCompetentColor(%s)";
    public static final String JAVASCRIPT_SET_CLASSIFICATION = "javascript:setClassification(%s)";

    /**
     * List of sent surveys
     */
    List<SurveyDB> surveys;


    /**
     * Default constructor
     */
    public FacilityTableBuilderBase(List<SurveyDB> surveys) {
        this.surveys = surveys;
    }

    public static void setColor(WebView webView) {
        Context context = PreferencesState.getInstance().getContext();

        //noinspection ResourceType
        String color = context.getResources().getString(R.color.competency_competent_background_color);
        String injectColor = String.format(
                JAVASCRIPT_SET_COMPETENT_COLOR, "{color:'" + getHtmlCodeColor(color) + "'}");
        Log.d(TAG, injectColor);
        webView.loadUrl(injectColor);
        //noinspection ResourceType
        color = context.getResources().getString(R.color.competency_not_competent_background_color);
        injectColor = String.format(
                JAVASCRIPT_SET_NOT_COMPETENT_COLOR, "{color:'" + getHtmlCodeColor(color) + "'}");
        Log.d(TAG, injectColor);
        webView.loadUrl(injectColor);
        //noinspection ResourceType
        color = context.getResources().getString(R.color.competency_competent_improvement_background_color);
        injectColor = String.format(
                JAVASCRIPT_SET_COMPETENT_IMPROVEMENT_COLOR,
                "{color:'" + getHtmlCodeColor(color) + "'}");
        Log.d(TAG, injectColor);
        webView.loadUrl(injectColor);

        String injectClassification = String.format(JAVASCRIPT_SET_CLASSIFICATION,
                "{competentText:'" + CompetencyUtils.getTextByCompetency(
                        CompetencyScoreClassification.COMPETENT, context) + "'," +
                        "competentImprovementText:'" + CompetencyUtils.getTextByCompetency(
                        CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT, context) + "'," +
                        "notCompetentText:'" + CompetencyUtils.getTextByCompetency(
                        CompetencyScoreClassification.NOT_COMPETENT, context) + "'}");

        Log.d(TAG, injectClassification);
        webView.loadUrl(injectClassification);
    }

    private static String getHtmlCodeColor(String color) {
        //remove the first two characters(about alpha color).
        String colorRRGGBB = "#" + color.substring(3, 9);
        return colorRRGGBB;
    }
}

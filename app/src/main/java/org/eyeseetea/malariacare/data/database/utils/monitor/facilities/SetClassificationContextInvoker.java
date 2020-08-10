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

import static org.eyeseetea.malariacare.data.database.utils.monitor.JavascriptInvokerKt.invokeSetClassificationContext;

import android.content.Context;
import android.webkit.WebView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.utils.CompetencyUtils;

public class SetClassificationContextInvoker {
    public static final String SCORING_CLASSIFICATION_CONTEXT_JSON =
            "{\n"
            + "   scores : {\n"
            + "      high: %d,\n"
            + "      medium: %d,\n"
            + "      mediumFormatted: '%s',\n"
            + "      low: %d\n"
            + "   },\n"
            + "   colors: {\n"
            + "      a: '%s',\n"
            + "      b: '%s',\n"
            + "      c: '%s'\n"
            + "   }\n"
            + "}";
    public static final String COMPETENCIES_CLASSIFICATION_CONTEXT_JSON =
            "{\n"
            + "   texts : {\n"
            + "      competentText: '%s',\n"
            + "      competentImprovementText: '%s',\n"
            + "      notCompetentText: '%s',\n"
            + "      notAvailableText: '%s',\n"
            + "      competentAbbreviationText: '%s',\n"
            + "      competentImprovementAbbreviationText: '%s',\n"
            + "      notCompetentAbbreviationText: '%s',\n"
            + "      notAvailableAbbreviationText: '%s'\n"
            + "    },\n"
            + "    colors: {\n"
            + "        competentColor: '%s',\n"
            + "        competentImprovementColor: '%s',\n"
            + "        notCompetentColor: '%s',\n"
            + "        notAvailableColor: '%s'\n"
            + "    }\n"
            + "}";


    public static void invokeByScoring(WebView webView) {
        Context context = PreferencesState.getInstance().getContext();

        //noinspection ResourceType
        String colorA = context.getResources().getString(
                R.color.lightGreen);
        String colorB = context.getResources().getString(
                R.color.assess_yellow);
        String colorC = context.getResources().getString(
                R.color.darkRed);


        String injectColor = String.format(
                SCORING_CLASSIFICATION_CONTEXT_JSON,
                ScoreType.getMonitoringMinimalHigh(),
                ScoreType.getMonitoringMaximumMedium(),
                ScoreType.getMonitoringMediumPieFormat(),
                ScoreType.getMonitoringMaximumLow(),
                getHtmlCodeColor(colorA),
                getHtmlCodeColor(colorB),
                getHtmlCodeColor(colorC));

        invokeSetClassificationContext(webView, injectColor);
    }

    public static void invokeByCompetencies(WebView webView) {
        Context context = PreferencesState.getInstance().getContext();

        //noinspection ResourceType
        String competentColor = context.getResources().getString(
                R.color.competency_competent_background_color);
        String competentImprovementColor = context.getResources().getString(
                R.color.competency_competent_improvement_background_color);
        String notCompetentColor = context.getResources().getString(
                R.color.competency_not_competent_background_color);
        String notAvailableColor = context.getResources().getString(
                R.color.competency_not_available_background_color);

        String injectColor = String.format(
                COMPETENCIES_CLASSIFICATION_CONTEXT_JSON,
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.COMPETENT, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.NOT_COMPETENT, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.NOT_AVAILABLE, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.COMPETENT, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.NOT_COMPETENT, context),
                CompetencyUtils.getTextByCompetencyAbbreviation(
                        CompetencyScoreClassification.NOT_AVAILABLE, context),
                getHtmlCodeColor(competentColor),
                getHtmlCodeColor(competentImprovementColor),
                getHtmlCodeColor(notCompetentColor),
                getHtmlCodeColor(notAvailableColor));

        invokeSetClassificationContext(webView, injectColor);
    }

    private static String getHtmlCodeColor(String color) {
        try {
            //remove the first two characters(about alpha color).
            String colorRRGGBB = "#" + color.substring(3, 9);
            return colorRRGGBB;
        } catch (Exception e) {
            return "";
        }
    }
}

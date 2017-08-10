/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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

package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.content.Context;
import android.view.View;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AssessmentSentAdapter extends
        ADashboardAdapter {

    private static final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
    public static final String SCORE_FORMAT = "%.1f %%";

    public AssessmentSentAdapter(List<SurveyDB> items, Context context) {
        super(context);
        this.items = items;
        this.headerLayout = R.layout.assessment_sent_header;
        this.recordLayout = R.layout.assessment_sent_record;
        this.footerLayout = R.layout.assessment_sent_footer;
        if (PreferencesState.getInstance().isVerticalDashboard()) {
            this.title = context.getString(R.string.assessment_sent_title_header);
        }
    }

    /**
     * Determines whether to show facility or not according to:
     * - The previous survey belongs to the same one.
     */
    @Override
    protected boolean hasToShowFacility(int position, SurveyDB survey) {
        if (position == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void hideFacility(CustomTextView facilityName, CustomTextView surveyType) {
        facilityName.setVisibility(View.GONE);
    }

    @Override
    protected void showFacility(CustomTextView facilityName, CustomTextView surveyType,
            SurveyDB survey) {
        facilityName.setText(survey.getOrgUnit().getName());
    }

    @Override
    protected void decorateCustomColumns(SurveyDB survey, View rowView) {
        decorateSentDate(survey, rowView);
        decorateSentScore(survey, rowView);
    }

    private void decorateSentScore(SurveyDB survey, View rowView) {

        int colorId;
        String scoreText;
        if (survey.hasConflict()) {
            scoreText = (getContext().getResources().getString(
                    R.string.feedback_info_conflict)).toUpperCase();
            colorId = R.color.darkRed;
        } else {
            if (survey.hasMainScore()) {
                scoreText = String.format(SCORE_FORMAT, survey.getMainScore());
            } else {
                scoreText = "NaN";
            }
            colorId = getColorByScore(survey);
        }

        CustomTextView sentScore = (CustomTextView) rowView.findViewById(R.id.score);
        sentScore.setText(scoreText);
        sentScore.setTextColor(getContext().getResources().getColor(colorId));
    }

    private void decorateSentDate(SurveyDB survey, View rowView) {
        CustomTextView sentDate = (CustomTextView) rowView.findViewById(R.id.sentDate);
        sentDate.setText(decorateCompletionDate(survey));
    }

    private String decorateCompletionDate(SurveyDB survey) {
        Date completionDate = survey.getCompletionDate();
        return format.format(completionDate);
    }

    private int getColorByScore(SurveyDB survey) {
        return LayoutUtils.trafficColor(survey.hasMainScore() ? survey.getMainScore() : 0f);
    }


    /**
     * The orgunit background in DashboardSentFragment is always the same.
     */
    @Override
    protected View decorateBackground(int position, View rowView) {
        rowView.setBackgroundResource(R.drawable.background_odd_wo_border);

        return rowView;
    }
}
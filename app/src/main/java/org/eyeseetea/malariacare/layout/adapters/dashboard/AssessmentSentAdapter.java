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

import static org.eyeseetea.malariacare.DashboardActivity.dashboardActivity;

import android.content.Context;
import android.view.View;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.Date;
import java.util.List;

public class AssessmentSentAdapter extends
        ADashboardAdapter {

    public static final String SCORE_FORMAT = "%.1f %%";

    public AssessmentSentAdapter(List<SurveyDB> items, Context context) {
        super(context);
        this.items = items;
        this.recordLayout = R.layout.assessment_sent_record;
        this.footerLayout = R.layout.assessment_sent_footer;
        if (PreferencesState.getInstance().isVerticalDashboard()) {
            this.title = context.getString(R.string.assessment_sent_title_header);
        }
    }


    @Override
    protected void initMenu(final SurveyDB survey) {
        menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dashboardActivity.onFeedbackSelected(survey);
            }
        });
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
        CustomTextView competencyTextView = rowView.findViewById(R.id.score);

        String competencyText;

        if (survey.hasConflict()) {
            competencyText = (getContext().getResources().getString(
                    R.string.feedback_info_conflict)).toUpperCase();
            competencyTextView.setText(competencyText);
        } else {
            CompetencyScoreClassification classification =
                    CompetencyScoreClassification.get(
                            survey.getCompetencyScoreClassification());

            CompetencyUtils.setTextByCompetencyAbbreviation(competencyTextView, classification);
        }
    }

    private void decorateSentDate(SurveyDB survey, View rowView) {
        CustomTextView sentDate = (CustomTextView) rowView.findViewById(R.id.sentDate);
        sentDate.setText(decorateCompletionDate(survey));
    }

    private String decorateCompletionDate(SurveyDB survey) {
        Date completionDate = survey.getCompletionDate();
        DateParser dateParser = new DateParser();
        return dateParser.getEuropeanFormattedDate(completionDate);
    }

    private int getColorByScore(SurveyDB survey) {
        return LayoutUtils.trafficColor(survey.hasMainScore() ? survey.getMainScoreValue() : 0f);
    }


    /**
     * The orgunit background in DashboardSentFragment is always the same.
     */
    @Override
    protected View decorateBackground(int position, View rowView) {
        if (position == 0 || position % 2 == 0) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.white));
        } else {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.white_grey));
        }
        return rowView;
    }
}
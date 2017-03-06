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
import android.view.ViewGroup;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Adapter that represents a list of surveys in the dashboard.
 */
public abstract class ADashboardAdapter extends ABaseAdapter {

    public static final String COMPLETED_SURVEY_MARK = "* ";
    public static final String SENT_SURVEY_MARK = "- ";
    /**
     * List of surveys to show
     */
    List<Survey> items;

    /**
     * Counter that helps with background calculation
     */
    protected int backIndex = 0;

    public ADashboardAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List items) {
        this.items = (List<Survey>) items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Get current survey
        Survey survey = (Survey) getItem(position);

        //Inflate row with right padding
        View rowView = adjustRowPadding(parent);

        decorateCustomColumns(survey, rowView);

        //OrgUnit
        CustomTextView facilityName = (CustomTextView) rowView.findViewById(R.id.facility);

        //Program
        CustomTextView surveyType = (CustomTextView) rowView.findViewById(R.id.survey_type);


        // show facility name (or not) and write survey type name
        if (hasToShowFacility(position, survey)) {
            showFacility(facilityName, surveyType, survey);
        } else {
            hideFacility(facilityName, surveyType);
        }

        decorateSurveyType(surveyType, survey);
        rowView = decorateBackground(position, rowView);

        return rowView;
    }

    /**
     * Each specific adapter must program its differences using this method
     */
    protected abstract void decorateCustomColumns(Survey survey, View rowView);


    /**
     * Determines whether to show facility or not according to:
     * - The previous survey belongs to the same one.
     */
    protected abstract boolean hasToShowFacility(int position, Survey survey);


    protected abstract void hideFacility(CustomTextView facilityName, CustomTextView surveyType);

    protected abstract void showFacility(CustomTextView facilityName, CustomTextView surveyType,
            Survey survey);

    /**
     * Calculate proper background according to the following rule:
     * -Same orgunit same background
     */
    protected abstract View decorateBackground(int position, View rowView);


    /**
     * Each specific adapter must program its differences using this method
     */
    protected View adjustRowPadding(ViewGroup parent) {
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingDp = (int) (5 * density);

        // Get the row layout
        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        return rowView;
    }

    private CustomTextView decorateSurveyType(CustomTextView surveyType, Survey survey) {
        String surveyDescription;
        if (survey.isCompleted()) {
            surveyDescription = COMPLETED_SURVEY_MARK + survey.getProgram().getName();
        } else {
            surveyDescription = SENT_SURVEY_MARK + survey.getProgram().getName();
        }
        surveyType.setText(surveyDescription);
        return surveyType;
    }


    public View setBackgroundWithBorder(int position, View rowView) {
        if (!PreferencesState.getInstance().isVerticalDashboard() && (items.get(
                position).isCompleted() || items.get(position).isSent())) {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgroundsImprove(this.backIndex));
        } else {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(this.backIndex));
        }
        return rowView;
    }

    public View setBackground(int position, View rowView) {
        if (!PreferencesState.getInstance().isVerticalDashboard() && (items.get(
                position).isCompleted() || items.get(position).isSent())) {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgroundsImprove(this.backIndex));
        } else {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(this.backIndex));
        }
        return rowView;
    }
    /**
     * Returns the proper status value (% or ready to send) according to the level of completion of
     * the survey
     */
    protected String getStatus(Survey survey) {

        if (survey.isSent()) {
            return getContext().getString(R.string.dashboard_info_sent);
        }

        SurveyAnsweredRatio surveyAnsweredRatio = survey.getAnsweredQuestionRatio();

        if (surveyAnsweredRatio.isCompleted()) {
            return getContext().getString(R.string.dashboard_info_ready_to_upload);
        } else {
            if (!PreferencesState.getInstance().isVerticalDashboard()) {
                if (surveyAnsweredRatio.getTotalCompulsory() > 0) {
                    int value = Float.valueOf(
                            100 * surveyAnsweredRatio.getCompulsoryRatio()).intValue();
                    if (value >= 100) {
                        return getContext().getString(R.string.dashboard_info_ready_to_upload);
                    } else {
                        return String.format("%d", value);
                    }
                }
            }
            return String.format("%d",
                    Float.valueOf(100 * surveyAnsweredRatio.getRatio()).intValue());
        }
    }

    public void remove(Object item) {
        this.items.remove(item);
    }
}
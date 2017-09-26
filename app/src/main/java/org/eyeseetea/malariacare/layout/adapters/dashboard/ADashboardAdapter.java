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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatioCache;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
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
    List<SurveyDB> items;

    ImageView menuDots;

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
        this.items = (List<SurveyDB>) items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Get current survey
        SurveyDB survey = (SurveyDB) getItem(position);

        //Inflate row with right padding
        View rowView = adjustRowPadding(parent);

        decorateCustomColumns(survey, rowView);

        //OrgUnit
        CustomTextView facilityName = (CustomTextView) rowView.findViewById(R.id.facility);

        //Program
        CustomTextView surveyType = (CustomTextView) rowView.findViewById(R.id.survey_type);

        menuDots = (ImageView) rowView.findViewById(R.id.menu_dots);

        initMenu(survey);

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

    protected abstract void initMenu(SurveyDB survey);

    /**
     * Each specific adapter must program its differences using this method
     */
    protected abstract void decorateCustomColumns(SurveyDB survey, View rowView);


    /**
     * Determines whether to show facility or not according to:
     * - The previous survey belongs to the same one.
     */
    protected abstract boolean hasToShowFacility(int position, SurveyDB survey);


    protected abstract void hideFacility(CustomTextView facilityName, CustomTextView surveyType);

    protected abstract void showFacility(CustomTextView facilityName, CustomTextView surveyType,
            SurveyDB survey);

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

    private CustomTextView decorateSurveyType(CustomTextView surveyType, SurveyDB survey) {
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
    protected int getTotalStatus(SurveyDB survey) {

        if (survey.isSent()) {
            //return getContext().getString(R.string.dashboard_info_sent);
            return 0;
        }

        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase = new GetSurveyAnsweredRatioUseCase();
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                GetSurveyAnsweredRatioUseCase.RecoveryFrom.MEMORY_FIRST,
                new GetSurveyAnsweredRatioUseCase.Callback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatioResult) {
                        Log.d(getClass().getName(), "onComplete");
                    }
                });
        SurveyAnsweredRatio surveyAnsweredRatio = SurveyAnsweredRatioCache.get(survey.getId_survey());
        if (surveyAnsweredRatio.isCompleted()) {
            //return getContext().getString(R.string.dashboard_info_ready_to_upload);
            return 100;
        } else {
            //return String.format("%d",Float.valueOf(100 * surveyAnsweredRatio.getRatio()).intValue());
            return Float.valueOf(100 * surveyAnsweredRatio.getRatio()).intValue();
        }
    }


    /**
     * Returns the proper status value (% or ready to send) according to the level of completion of mandatory questions
     */
    protected int getMandatoryStatus(SurveyDB survey) {

        if (survey.isSent()) {
            //return getContext().getString(R.string.dashboard_info_sent);
            return 0;
        }

        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase = new GetSurveyAnsweredRatioUseCase();
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                GetSurveyAnsweredRatioUseCase.RecoveryFrom.MEMORY_FIRST,
                new GetSurveyAnsweredRatioUseCase.Callback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatioResult) {
                        Log.d(getClass().getName(), "onComplete");
                    }
                });
        SurveyAnsweredRatio surveyAnsweredRatio = SurveyAnsweredRatioCache.get(survey.getId_survey());
        if (surveyAnsweredRatio.getTotalCompulsory() > 0) {
            int value = Float.valueOf(100 * surveyAnsweredRatio.getCompulsoryRatio()).intValue();
            return  value;
        }
        else{
            return 100;
        }
    }

    public void remove(Object item) {
        this.items.remove(item);
    }
}
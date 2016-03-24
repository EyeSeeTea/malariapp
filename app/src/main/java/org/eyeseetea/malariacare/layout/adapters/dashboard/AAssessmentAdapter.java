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
import android.graphics.Color;
import android.text.Layout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class AAssessmentAdapter extends ADashboardAdapter implements IDashboardAdapter {

    protected int backIndex = 0;

    public AAssessmentAdapter() { }

    public AAssessmentAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.assessment_header;
        this.recordLayout = R.layout.assessment_record;
        this.footerLayout = R.layout.assessment_footer;
        if(PreferencesState.getInstance().isVerticalDashboard())
            this.title = context.getString(R.string.assessment_title_header);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Survey survey = (Survey) getItem(position);
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingDp = (int)(5 * density);

        // Get the row layout
        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

        // Org Unit Cell
        CustomTextView facilityName = (CustomTextView) rowView.findViewById(R.id.facility);
        CustomTextView surveyType = (CustomTextView) rowView.findViewById(R.id.survey_type);
        CustomTextView sentDate = (CustomTextView) rowView.findViewById(R.id.sentDate);
        CustomTextView sentScore = (CustomTextView) rowView.findViewById(R.id.score);


        if (sentDate != null){
            Date eventDate = survey.getCompletionDate();
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            sentDate.setText(format.format(eventDate));
            sentDate.setText(Utils.formatDate(eventDate));
            if(survey.hasConflict() && sentScore!=null){
                sentScore.setText((getContext().getResources().getString(R.string.feedback_info_conflict)).toUpperCase());
                sentScore.setTextColor(getContext().getResources().getColor(R.color.darkRed));
            }
            else if(sentScore!=null){
                // if(!PreferencesState.getInstance().isVerticalDashboard()){
                sentScore.setText(String.format("%.1f %%",survey.getMainScore()));
                int colorId=LayoutUtils.trafficColor(survey.getMainScore());
                sentScore.setTextColor(getContext().getResources().getColor(colorId));
            }
        } else {
            //Status Cell
            ((CustomTextView) rowView.findViewById(R.id.score)).setText(getStatus(survey));
        }

        // show facility name (or not) and write survey type name
        if (hasToShowFacility(position,survey)) {
            facilityName.setText(survey.getOrgUnit().getName());
        } else {
            facilityName.setVisibility(View.GONE);
        }

        String surveyDescription;
        if(survey.isCompleted())
            surveyDescription = "* " + survey.getTabGroup().getProgram().getName();
        else
            surveyDescription = "- " + survey.getTabGroup().getProgram().getName();
        surveyType.setText(surveyDescription);

        // check whether the following item belongs to the same org unit (to group the data related
        // to same org unit with the same background)
        if (position < (this.items.size()-1)) {
            if (this.items.get(position+1).getOrgUnit().equals((this.items.get(position)).getOrgUnit())){
                // show background without border and tell the system that next survey belongs to the same org unit, so its name doesn't need to be shown
                rowView=setBackground(position+1,rowView);
            } else {
                // show background with border and switch background for the next row
                rowView=setBackgroundWithBorder(position + 1, rowView);
                this.backIndex++;
            }
        }  else {
            //show background with border
            rowView=setBackgroundWithBorder(position, rowView);
        }

        return rowView;
    }

    /**
     * Determines whether to show facility or not according to:
     *  - The previous survey belongs to the same one.
     * @param position
     * @param survey
     * @return
     */
    private boolean hasToShowFacility(int position, Survey survey){
        if(position==0){
            return true;
        }

        Survey previousSurvey = this.items.get(position-1);
        return !survey.getOrgUnit().getId_org_unit().equals(previousSurvey.getOrgUnit().getId_org_unit());
    }

    private View setBackgroundWithBorder(int position, View rowView) {
        if(!PreferencesState.getInstance().isVerticalDashboard() && (items.get(position).isCompleted() || items.get(position).isSent())) {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgroundsImprove(this.backIndex));
        }
        else {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(this.backIndex));
        }
        return rowView;
    }

    private View setBackground(int position, View rowView) {
        if(!PreferencesState.getInstance().isVerticalDashboard() && (items.get(position).isCompleted() || items.get(position).isSent())) {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgroundsImprove(this.backIndex));
        }
        else {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(this.backIndex));
        }
        return rowView;
    }
    /**
     * Returns the proper status value (% or ready to send) according to the level of completion of the survey
     * @param survey
     * @return
     */
    private String getStatus(Survey survey){

        if(survey.isSent()){
            return getContext().getString(R.string.dashboard_info_sent);
        }

        SurveyAnsweredRatio surveyAnsweredRatio=survey.getAnsweredQuestionRatio();

        if (surveyAnsweredRatio.isCompleted()) {
            return getContext().getString(R.string.dashboard_info_ready_to_upload);
        } else {
            if (!PreferencesState.getInstance().isVerticalDashboard()){
                if (surveyAnsweredRatio.getTotalCompulsory() > 0) {
                    int value = Float.valueOf(100 * surveyAnsweredRatio.getCompulsoryRatio()).intValue();
                    if (value >= 100) {
                        return getContext().getString(R.string.dashboard_info_ready_to_upload);
                    } else
                        return String.format("%d", value);
                }
            }
            return String.format("%d", Float.valueOf(100*surveyAnsweredRatio.getRatio()).intValue());
        }
    }

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }

}
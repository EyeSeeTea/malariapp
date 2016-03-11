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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Adapter that represents a list of surveys in the dashboard.
 *
 */
public abstract class ADashboardAdapter extends BaseAdapter{

    public static final String COMPLETED_SURVEY_MARK = "* ";
    public static final String SENT_SURVEY_MARK = "- ";
    /**
     * List of surveys to show
     */
    List<Survey> items;

    /**
     * Reference to inflater (micro optimization)
     */
    protected LayoutInflater lInflater;

    /**
     * Context required to resolve strings
     */
    protected Context context;

    /**
     * The layout of the header
     */
    protected Integer headerLayout;

    /**
     * The layout of the footer
     */
    protected Integer footerLayout;

    /**
     * The layout of the record itself
     */
    protected Integer recordLayout;

    /**
     * Counter that helps with background calculation
     */
    protected int backIndex = 0;

    public ADashboardAdapter(Context context){
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
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

    public Integer getHeaderLayout() {
        return this.headerLayout;
    }

    public Integer getFooterLayout() {
        return footerLayout;
    }

    public Integer getRecordLayout() {
        return this.recordLayout;
    }

    protected Context getContext(){
        return this.context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Get current survey
        Survey survey = (Survey) getItem(position);

        //Inflate row with right padding
        View rowView = adjustRowPadding(parent);

        decorateCustomColumns(survey,rowView);

        //OrgUnit
        CustomTextView facilityName = (CustomTextView) rowView.findViewById(R.id.facility);

        //Program
        CustomTextView surveyType = (CustomTextView) rowView.findViewById(R.id.survey_type);

        // show facility name (or not) and write survey type name
        if (hasToShowFacility(position,survey)) {
            showFacility(facilityName, surveyType, survey);
        } else {
            hideFacility(facilityName,surveyType);
        }

        decorateSurveyType(surveyType,survey);
        rowView = decorateBackground(position, rowView);

        return rowView;
    }

    /**
     * Each specific adapter must program its differences using this method
     * @param survey
     * @param rowView
     */
    protected abstract void decorateCustomColumns(Survey survey, View rowView);


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
        //Different orgUnits -> has to show
        return !survey.getOrgUnit().getId_org_unit().equals(previousSurvey.getOrgUnit().getId_org_unit());
    }

    private View adjustRowPadding(ViewGroup parent) {
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingDp = (int)(5 * density);

        // Get the row layout
        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        return rowView;
    }

    private void hideFacility(CustomTextView facilityName, CustomTextView surveyType){
        facilityName.setVisibility(View.GONE);
        facilityName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0f));
        LinearLayout.LayoutParams linearLayout=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1f);
        int pixels =(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) getContext().getResources().getDimension(R.dimen.survey_row_marging), getContext().getResources().getDisplayMetrics());
        linearLayout.setMargins(0, pixels, 0, pixels);
        surveyType.setLayoutParams(linearLayout);
    }

    private void showFacility(CustomTextView facilityName, CustomTextView surveyType, Survey survey){
        facilityName.setText(survey.getOrgUnit().getName());
        facilityName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.5f));
        surveyType.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.5f));
    }

    private CustomTextView decorateSurveyType(CustomTextView surveyType, Survey survey){
        String surveyDescription;
        if(survey.isCompleted())
            surveyDescription = COMPLETED_SURVEY_MARK + survey.getTabGroup().getProgram().getName();
        else
            surveyDescription = SENT_SURVEY_MARK + survey.getTabGroup().getProgram().getName();
        surveyType.setText(surveyDescription);
        return surveyType;
    }

    /**
     * Calculate proper background according to the following rule:
     *  -Same orgunit same background
     *
     * @param position
     * @param rowView
     * @return
     */
    private View decorateBackground(int position, View rowView) {

        //Last survey
        if(position == (this.items.size()-1)){
            return setBackgroundWithBorder(position, rowView);
        }

        //Same orgUnit -> No border
        if (this.items.get(position+1).getOrgUnit().equals((this.items.get(position)).getOrgUnit())){
            return setBackground(position+1,rowView);
        }

        //Different orgUnit -> With border, next background switches
        rowView=setBackgroundWithBorder(position + 1, rowView);
        this.backIndex++;

        return rowView;
    }


    private View setBackgroundWithBorder(int position, View rowView) {
        if(items.get(position).isCompleted() || items.get(position).isSent()) {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgroundsImprove(this.backIndex));
        }
        else {
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(this.backIndex));
        }
        return rowView;
    }

    private View setBackground(int position, View rowView) {
        if(items.get(position).isCompleted() || items.get(position).isSent()) {
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
    protected String getStatus(Survey survey){

        if(survey.isSent()){
            return getContext().getString(R.string.dashboard_info_sent);
        }

        SurveyAnsweredRatio surveyAnsweredRatio=survey.getAnsweredQuestionRatio();

        if (surveyAnsweredRatio.isCompleted()) {
            return getContext().getString(R.string.dashboard_info_ready_to_upload);
        } else {
            if(surveyAnsweredRatio.getTotalCompulsory()>0) {
                int value=Float.valueOf(100 * surveyAnsweredRatio.getCompulsoryRatio()).intValue();
                if(value>=100){
                    return getContext().getString(R.string.dashboard_info_ready_to_upload);
                }
                else
                    return String.format("%d", value);
            }
            return String.format("%d", Float.valueOf(100*surveyAnsweredRatio.getRatio()).intValue());
        }
    }

}
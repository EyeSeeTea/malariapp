/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.dashboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.dialog.DialogDispatcher;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.FloatingButton;

import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class AssessmentAdapter extends BaseAdapter implements IDashboardAdapter {

    List<Survey> items;
    private LayoutInflater lInflater;
    private Context context;
    private Integer headerLayout;
    private Integer recordLayout;
    private String title;

    public AssessmentAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.assessment_header;
        this.recordLayout = R.layout.assessment_record;
        this.title = context.getString(R.string.assessment_title_header);
    }

    public AssessmentAdapter(List<Survey> items, Context context, Integer headerLayout, Integer recordLayout, String title) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.assessment_header;
        this.recordLayout = R.layout.assessment_record;
        this.title = title;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Survey survey = (Survey) getItem(position);

        View rowView = lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        // Org Unit Cell
        ((TextView) rowView.findViewById(R.id.facility)).setText(survey.getOrgUnit().getName());
        ((TextView) rowView.findViewById(R.id.survey_type)).setText("- " + survey.getProgram().getName());

        //Status Cell
        //FIXME: This bit needs to change when jose architecture is introduced because probably the save will be executed in a different way
        List<Integer> status = survey.getAnsweredQuestionRatio();

        if (status.get(0) == status.get(1)) {
            ((TextView) rowView.findViewById(R.id.score)).setText(getContext().getString(R.string.dashboard_info_ready_to_upload));
        } else {
            ((TextView) rowView.findViewById(R.id.score)).setText(String.format("%d", new Double(100 * (double) status.get(0) / (double) status.get(1)).intValue()));
        }

        //Tools Cell
        LinearLayout toolContainerView = (LinearLayout) rowView.findViewById(R.id.toolsContainer);

        TextView deleteTextView = new TextView(this.context);
        deleteTextView.setText(R.string.assessment_info_delete);
        deleteTextView.setTextColor(getContext().getResources().getColor(R.color.headerColor));
        deleteTextView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        deleteTextView.setOnClickListener(new AssessmentListener((Activity) this.context, survey, context.getString(R.string.assessment_info_delete)));
        toolContainerView.addView(deleteTextView);
        //FloatingButton addSurvey = new FloatingButton(this.context);
        //toolContainerView.addView(addSurvey);

        return rowView;
    }

    @Override
    public void setItems(List items) {
        this.items = (List<Survey>) items;
    }

    @Override
    public IDashboardAdapter newInstance(List items, Context context) {
        return new AssessmentAdapter((List<Survey>) items, context);
    }

    @Override
    public void setHeaderLayout(Integer headerLayout){
        this.headerLayout = headerLayout;
    }

    @Override
    public Integer getHeaderLayout() {
        return this.headerLayout;
    }

    @Override
    public void setRecordLayout(Integer recordLayout){
        this.recordLayout = recordLayout;
    }

    @Override
    public Integer getRecordLayout() {
        return this.recordLayout;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setContext(Context context){
        this.context = context;
    }

    @Override
    public Context getContext(){
        return this.context;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    private class AssessmentListener implements View.OnClickListener {

        private Survey survey;
        private String listenerOption; //One of edit, delete
        private Activity context;

        public AssessmentListener(Activity context, Survey survey, String listenerOption) {
            this.context = context;
            this.survey = survey;
            this.listenerOption = listenerOption;
        }

        public void onClick(View view) {
            if (listenerOption.equals(context.getString(R.string.assessment_info_delete))) {
                Session.setSurvey(survey);
                DialogDispatcher mf = DialogDispatcher.newInstance(view);
                mf.showDialog(context.getFragmentManager(), DialogDispatcher.DELETE_SURVEY_DIALOG);
            }
        }
    }
}
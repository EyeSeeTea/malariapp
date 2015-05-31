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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

import java.util.List;

public class AssessmentAdapter extends ADashboardAdapter implements IDashboardAdapter {

    public AssessmentAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.assessment_header;
        this.recordLayout = R.layout.assessment_record;
        this.title = context.getString(R.string.assessment_title_header);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Survey survey = (Survey) getItem(position);

        View rowView = this.lInflater.inflate(getRecordLayout(), parent, false);
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
    public IDashboardAdapter newInstance(List items, Context context) {
        return new AssessmentAdapter((List<Survey>) items, context);
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

                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.dialog_title_delete_survey))
                        .setMessage(context.getString(R.string.dialog_info_delete_survey))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Session.getSurvey().delete();

                                Intent intent = new Intent(context, DashboardActivity.class);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).create().show();
            }
        }
    }
}
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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;

import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class AssessmentAdapter extends BaseAdapter {

    List<Survey> items;
    private LayoutInflater lInflater;
    private final Context context;

    public AssessmentAdapter(List<Survey> items, Context context) {
        this.items = items;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Survey item = (Survey) getItem(position);

        View rowView = lInflater.inflate(R.layout.assessment_record, parent, false);

        ((TextView) rowView.findViewById(R.id.facility)).setText(item.getOrgUnit().getUid() + " - " + item.getOrgUnit().getName());
        ((TextView) rowView.findViewById(R.id.date)).setText(item.getEventDate());
        ((TextView) rowView.findViewById(R.id.score)).setText("25 %");
        ((TextView) rowView.findViewById(R.id.completed)).setText("25");
        ((TextView) rowView.findViewById(R.id.total)).setText("824");
 
		//FIXME: We need to add some logic. Depending on the status we will be showing different links

        LinearLayout toolContainerView = (LinearLayout) rowView.findViewById(R.id.toolsContainer);
        TextView editTextView = new TextView(this.context);
        editTextView.setText("Edit");
        editTextView.setTextColor(Color.parseColor("#1e506c"));
        editTextView.setTypeface(null, Typeface.BOLD);
        editTextView.setOnClickListener(new AssessmentListener((Activity) this.context, item, "edit"));
        toolContainerView.addView(editTextView);

        TextView deleteTextView = new TextView(this.context);
        deleteTextView.setText("Delete");
        deleteTextView.setTextColor(Color.parseColor("#1e506c"));
        deleteTextView.setTypeface(null, Typeface.BOLD);
        deleteTextView.setOnClickListener(new AssessmentListener((Activity) this.context, item, "delete"));
        toolContainerView.addView(deleteTextView);

        return rowView;
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
            if (listenerOption.equals("delete")){
                survey.delete();
                this.context.finish();
                this.context.startActivity(this.context.getIntent());
            }
            else if (listenerOption.equals("edit")){
                Session.setSurvey(survey);

                //Call Survey Activity
                Intent surveyIntent = new Intent(this.context, MainActivity.class);
                this.context.startActivity(surveyIntent);
            }
        }


    }
}
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

import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class FutureAssessmentPlanningAdapter extends ADashboardAdapter implements IDashboardAdapter {

    public FutureAssessmentPlanningAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.future_assessment_planning_header;
        this.footerLayout = null;
        this.recordLayout = R.layout.future_assessment_planning_record;
        this.title = context.getString(R.string.future_title_header);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Survey item = (Survey) getItem(position);

        View rowView = lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        ((TextView)rowView.findViewById(R.id.facility)).setText(item.getOrgUnit().getUid() + " - " + item.getOrgUnit().getName());
        ((TextView)rowView.findViewById(R.id.datePreviousAssessment)).setText("");
        ((TextView)rowView.findViewById(R.id.dueDate)).setText("23 Mar 2015");
        TextView action = ((TextView)rowView.findViewById(R.id.action));
        action.setText("Start \nReschedule");
        action.setTextColor(this.context.getResources().getColor(R.color.headerColor));
        action.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        return rowView;
    }

    @Override
    public IDashboardAdapter newInstance(List items, Context context) {
        return new FutureAssessmentPlanningAdapter((List<Survey>) items, context);
    }
}
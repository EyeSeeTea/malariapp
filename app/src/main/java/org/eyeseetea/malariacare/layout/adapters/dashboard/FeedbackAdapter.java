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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class FeedbackAdapter extends ADashboardAdapter implements IDashboardAdapter {

    public FeedbackAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.feedback_header;
        this.recordLayout = R.layout.feedback_record;
        this.title = context.getString(R.string.feedback_title_header);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        Survey item = (Survey) getItem(position);

        rowView = lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        ((TextView)rowView.findViewById(R.id.facility)).setText(item.getOrgUnit().getUid() + " - " + item.getOrgUnit().getName());
        SimpleDateFormat formattedDate = new SimpleDateFormat("dd MMM yyyy");
        ((TextView)rowView.findViewById(R.id.survey_type)).setText(item.getProgram().getName() + " \n\t " + formattedDate.format(item.getEventDate()));
        ((TextView)rowView.findViewById(R.id.status)).setText("FCM - 22 issues \n RDT - on site retraining \n Microscopy - 25 issues \n Work Environment - 20 issues");

        return rowView;
    }

    @Override
    public IDashboardAdapter newInstance(List items, Context context) {
        return new FeedbackAdapter((List<Survey>) items, context);
    }
}
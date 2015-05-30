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

import java.util.List;

public class PerformancePlanningAdapter extends ADashboardAdapter implements IDashboardAdapter{

    public PerformancePlanningAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.performance_planning_header;
        this.recordLayout = R.layout.performance_planning_record;
        this.title = context.getString(R.string.performance_title_header);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        Survey item = (Survey) getItem(position);

        rowView = lInflater.inflate(getRecordLayout(), parent, false);
        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        ((TextView)rowView.findViewById(R.id.survey_type)).setText("Jan");
        ((TextView)rowView.findViewById(R.id.target)).setText("10");
        ((TextView)rowView.findViewById(R.id.done)).setText("5");
        ((TextView)rowView.findViewById(R.id.achievement)).setText("50 %");

        return rowView;
    }

    @Override
    public IDashboardAdapter newInstance(List items, Context context) {
        return new PerformancePlanningAdapter((List<Survey>) items, context);
    }
}
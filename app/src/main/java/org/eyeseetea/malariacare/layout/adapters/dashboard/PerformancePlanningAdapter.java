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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class PerformancePlanningAdapter extends BaseAdapter implements IDashboardAdapter{

    List<Survey> items;
    private LayoutInflater lInflater;
    private final Context context;

    public PerformancePlanningAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;

        this.lInflater=LayoutInflater.from(context);
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
        View rowView = null;

        Survey item = (Survey) getItem(position);

        rowView = lInflater.inflate(R.layout.performance_planning_record, parent, false);
        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        ((TextView)rowView.findViewById(R.id.date)).setText("Jan");
        ((TextView)rowView.findViewById(R.id.target)).setText("10");
        ((TextView)rowView.findViewById(R.id.done)).setText("5");
        ((TextView)rowView.findViewById(R.id.achievement)).setText("50 %");

        return rowView;
    }

    @Override
    public BaseAdapter getAdapter() {
        return null;
    }

    @Override
    public ListFragment getFragment() {
        return null;
    }

    @Override
    public Integer getHeaderLayout() {
        return null;
    }

    @Override
    public Integer getRecordLayout() {
        return null;
    }
}
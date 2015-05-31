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
import android.widget.TableLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;

import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class DashboardAdapter extends BaseAdapter {

    List<Survey> items;
    List<IDashboardAdapter> adapters;
    List<View.OnClickListener> listeners;
    LayoutInflater lInflater;
    Context context;

    public DashboardAdapter(List<Survey> items, List<IDashboardAdapter> adapters, List<View.OnClickListener> listeners, Context context) {
        this.items = items;
        this.adapters = adapters;
        this.listeners = listeners;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return this.adapters.size();
    }

    @Override
    public Object getItem(int position) {
        return this.adapters.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = lInflater.inflate(R.layout.dashboard_row, parent, false);
        View header = lInflater.inflate(this.adapters.get(position).getHeaderLayout(), null, false);
        TableLayout table = (TableLayout) rowView.findViewById(R.id.dashboard_table);
        TextView title = (TextView) rowView.findViewById(R.id.title);
        title.setText(this.adapters.get(position).getTitle());
        TextView seeAll = (TextView) rowView.findViewById(R.id.see_all);
        seeAll.setOnClickListener(this.listeners.get(position));

        table.addView(header);


        for (int i=0; i<((BaseAdapter)adapters.get(position)).getCount(); i++) {
            View subRow = ((BaseAdapter) getItem(position)).getView(i, convertView, parent);
            if (subRow != null) table.addView(subRow);
        }

        return rowView;
    }
}
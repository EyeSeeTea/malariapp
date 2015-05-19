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

import java.io.Serializable;
import java.util.List;

/**
 * Created by Adrian on 22/04/2015.
 */
public class AnalyticsAdapter extends BaseAdapter implements IDashboardAdapter {

    List<Survey> items;
    private LayoutInflater lInflater;
    private Context context;
    private Integer headerLayout;
    private Integer recordLayout;
    private String title;

    public AnalyticsAdapter(List<Survey> items, Context context) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = R.layout.analytics_header;
        this.recordLayout = R.layout.analytics_record;
        this.title = context.getString(R.string.analytics_title_header);
    }

    public AnalyticsAdapter(List<Survey> items, Context context, Integer headerLayout, Integer recordLayout, String title) {
        this.items = items;
        this.context = context;
        this.lInflater = LayoutInflater.from(context);
        this.headerLayout = headerLayout;
        this.recordLayout = recordLayout;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Integer getHeaderLayout() {
        return headerLayout;
    }

    @Override
    public void setHeaderLayout(Integer headerLayout) {
        this.headerLayout = headerLayout;
    }

    @Override
    public Integer getRecordLayout() {
        return recordLayout;
    }

    @Override
    public void setRecordLayout(Integer recordLayout) {
        this.recordLayout = recordLayout;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}
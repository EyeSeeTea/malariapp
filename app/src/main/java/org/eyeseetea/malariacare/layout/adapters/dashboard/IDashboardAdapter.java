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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.eyeseetea.malariacare.database.model.Survey;

import java.io.Serializable;
import java.util.List;

public interface IDashboardAdapter extends Serializable {

    public void setContext(Context context);

    public Context getContext();

    public void setHeaderLayout(Integer headerLayout);

    public Integer getHeaderLayout();

    public void setRecordLayout(Integer recordLayout);

    public Integer getRecordLayout();

    public void setTitle(String title);

    public String getTitle();

    public View getView(int position, View convertView, ViewGroup parent);

    public void setItems(List items);

    public IDashboardAdapter newInstance(List items, Context context);
}

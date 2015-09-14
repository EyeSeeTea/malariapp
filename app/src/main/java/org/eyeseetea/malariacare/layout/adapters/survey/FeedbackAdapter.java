/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.eyeseetea.malariacare.database.feedback.Feedback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 14/09/15.
 */
public class FeedbackAdapter extends BaseAdapter {

    private List<Feedback> items;

    public FeedbackAdapter(){
        this(new ArrayList<Feedback>());
    }

    public FeedbackAdapter(List<Feedback> items){
        this.items=items;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * Reloads items into the adapter
     * @param newItems
     */
    public void setItems(List<Feedback> newItems){
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }

}

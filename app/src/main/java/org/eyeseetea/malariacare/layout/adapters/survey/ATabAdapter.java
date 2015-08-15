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

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ATabAdapter extends BaseAdapter implements  ITabAdapter{

    private int id_layout;
    private Tab tab;
    private LayoutInflater lInflater;
    private final Context context;

    //List of Headers and Questions. Each position contains an object to be showed in the listview
    private List<? extends BaseModel> items;


    public ATabAdapter(Tab tab, Context context, int id_layout){
        this.context = context;
        this.tab = tab;
        this.lInflater = LayoutInflater.from(context);
        this.items = Utils.preloadTabItems(tab);
        this.id_layout = id_layout;
    }

    /**
     * Flag that indicates if the current survey in session is already sent or not (it affects readonly settings)
     */
    private boolean readOnly = Session.getSurvey().isSent();

    @Override
    public BaseAdapter getAdapter() {
        return this;
    }

    @Override
    public int getLayout() {
        return id_layout;
    }

    @Override
    public Float getScore() {
        return null;
    }

    @Override
    public void initializeSubscore() {}

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
        return items.get(position).hashCode();
    }

    @Override
    public String getName() {
        return tab.getName();
    }


    public Context getContext(){
        return this.context;
    }

    public LayoutInflater getInflater(){
        return this.lInflater;
    }

    public boolean getReadOnly(){
        return readOnly;
    }

    public Tab getTab() {
        return this.tab;
    }

    public List<? extends BaseModel> getItems(){ return this.items; }
}

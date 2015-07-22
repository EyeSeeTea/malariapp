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

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import org.eyeseetea.malariacare.database.model.Tab;

import java.util.List;

/**
 * Created by Jose on 24/04/2015.
 */
// FIXME: this tab is apparently not going to be used anymore. This need to be removed in next refactor
public class ScoreTabAdapter implements ITabAdapter {

    int id_layout;
    String tab_name;
    List<Tab> tabs;
    private LayoutInflater lInflater;

    public ScoreTabAdapter(List<Tab> tabs, int id_layout, String tab_name) {
        this.tabs = tabs;
        this.id_layout = id_layout;
        this.tab_name = tab_name;
    }

    @Override
    public void initializeSubscore() {


    }

    @Override
    public BaseAdapter getAdapter() {
        return null;
    }

    @Override
    public Float getScore() {
        return null;
    }

    @Override
    public int getLayout() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }
}

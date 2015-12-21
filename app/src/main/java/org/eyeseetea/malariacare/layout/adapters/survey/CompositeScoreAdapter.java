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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Created by Jose on 21/04/2015.
 */
public class CompositeScoreAdapter extends ATabAdapter {

    String tab_name;

    public CompositeScoreAdapter(List<CompositeScore> items, Context context, int id_layout, Tab tab) {
        super(tab, context, id_layout);
        super.setItems(items);
    }

    public CompositeScoreAdapter(Tab tab, Context context, int id_layout) {
        super(tab, context, id_layout);
    }

    public static CompositeScoreAdapter build(Tab tab, Context context) {
        int layoutId;
        if(PreferencesState.isPictureQuestion())
            layoutId=R.layout.composite_score_header_pictureapp;
        else
            layoutId=R.layout.composite_score_tab;
        return new CompositeScoreAdapter(tab, context, layoutId);
    }
    @Override
    public void initializeSubscore() {

        ListView compositeScoreListView = (ListView) ((Activity) getContext()).findViewById(R.id.listView);
        ViewGroup header;
        int layoutId;
        if(PreferencesState.isPictureQuestion())
            layoutId=R.layout.composite_score_header_pictureapp;
        else
            layoutId=R.layout.composite_score_header;

        header  = (ViewGroup) getInflater().inflate(layoutId, compositeScoreListView, false);
        compositeScoreListView.addHeaderView(header);
    }

    @Override
    public String getName() {
        return tab_name;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        CompositeScore item = (CompositeScore) getItem(position);

        int layoutId;
        if(PreferencesState.isPictureQuestion())
            layoutId=R.layout.composite_scores_record_pictureapp;
        else
            layoutId=R.layout.composite_scores_record;

        rowView = getInflater().inflate(layoutId, parent, false);

        ((CustomTextView)rowView.findViewById(R.id.code)).setText(item.getHierarchical_code());
        ((CustomTextView)rowView.findViewById(R.id.label)).setText(item.getLabel());

        Float compositeScoreValue = ScoreRegister.getCompositeScore(item);

        if (compositeScoreValue == null)
            ((CustomTextView)rowView.findViewById(R.id.score)).setText(getContext().getString(R.string.number_zero));
        else
            ((CustomTextView)rowView.findViewById(R.id.score)).setText(Utils.round(compositeScoreValue));

        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        return rowView;
    }
}

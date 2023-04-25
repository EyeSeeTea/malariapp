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
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.views.CustomTextView;

/**
 * Created by Jose on 21/04/2015.
 */
public class CompositeScoreAdapter extends ATabAdapter {

    public CompositeScoreAdapter(TabDB tab, Context context, int id_layout, float idSurvey, String module) {
        super(tab, context, id_layout, idSurvey, module);
    }

    /**
     * Factory method to build a scored/non scored layout according to tab type.
     *
     * @param tab
     * @param context
     * @return
     */
    public static CompositeScoreAdapter build(TabDB tab, Context context, float idSurvey, String module) {
        return new CompositeScoreAdapter(tab, context, R.layout.composite_score_tab, idSurvey, module);
    }

    @Override
    public void initializeSubscore() {
        ListView compositeScoreListView = (ListView) ((Activity) getContext()).findViewById(R.id.listView);

        ViewGroup header = (ViewGroup) getInflater().inflate(R.layout.composite_score_header, compositeScoreListView, false);
        compositeScoreListView.addHeaderView(header);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = getInflater().inflate(R.layout.composite_scores_record, parent, false);

        CompositeScoreDB item = (CompositeScoreDB) getItem(position);

        ((CustomTextView)rowView.findViewById(R.id.code)).setText(item.getHierarchical_code());
        ((CustomTextView)rowView.findViewById(R.id.label)).setText(item.getLabel());

        Float compositeScoreValue = ScoreRegister.getCompositeScore(item, idSurvey, module);

        if (compositeScoreValue == null)
            ((CustomTextView)rowView.findViewById(R.id.score)).setText(getContext().getString(R.string.number_zero));
        else
            ((CustomTextView)rowView.findViewById(R.id.score)).setText(AUtils.round(compositeScoreValue,2));

        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        return rowView;
    }
}

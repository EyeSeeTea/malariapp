/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.layout.adapters.filters;

import android.content.Context;
import android.graphics.Typeface;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.layout.adapters.general.AddlArrayAdapter;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Created by idelcano on 13/04/2016.
 */
public class FilterTabGroupArrayAdapter extends AddlArrayAdapter<TabGroup> {

    public FilterTabGroupArrayAdapter(Context context, List<TabGroup> TabGroups) {
        super(context, TabGroups);
    }
    int count=0;
    @Override public void drawText(CustomTextView customTextView, TabGroup TabGroup) {
        if (customTextView.getmScale().equals(getContext().getString(R.string.font_size_system)))
            customTextView.setTextSize(20);
        customTextView.setText(TabGroup.getName());
        customTextView.setAlpha(1f);
        customTextView.setmDimension(getContext().getResources().getString(R.string.font_size_level4));
        customTextView.setmFontName(getContext().getResources().getString(R.string.medium_font_name));
        customTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        customTextView.setTextColor(getContext().getResources().getColor(R.color.white));
        customTextView.setBackgroundColor(getContext().getResources().getColor(R.color.assess_grey));
        customTextView.setPadding(customTextView.getPaddingLeft(), getContext().getResources().getDimensionPixelSize(R.dimen.filters_top_bottom_padding), customTextView.getPaddingRight(), getContext().getResources().getDimensionPixelSize(R.dimen.filters_top_bottom_padding));
        customTextView.setTypeface(null, Typeface.BOLD);
    }
}

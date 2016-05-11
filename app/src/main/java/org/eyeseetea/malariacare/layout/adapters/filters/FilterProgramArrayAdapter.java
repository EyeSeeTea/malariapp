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

package org.eyeseetea.malariacare.layout.adapters.filters;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Typeface;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.layout.adapters.general.AddlArrayAdapter;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Created by adrian on 30/04/15.
 */
public class FilterProgramArrayAdapter extends AddlArrayAdapter<Program> {

    public FilterProgramArrayAdapter(Context context, List<Program> programs) {
        super(context, programs);
    }
    int count=0;
    @Override public void drawText(CustomTextView customTextView, Program program) {
        if (customTextView.getmScale().equals(getContext().getString(R.string.font_size_system)))
            customTextView.setTextSize(20);
        customTextView.setText(program.getName());
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

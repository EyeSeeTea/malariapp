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

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedHeader;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItem;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.data.database.utils.planning.ScheduleListener;
import org.eyeseetea.malariacare.utils.AUtils;

import java.util.List;

public class PlannedStyleStrategy {
    private PlannedHeader mPlannedHeader;
    private TextView mTextView;
    private ImageView mImg;
    private int mColor;

    public PlannedStyleStrategy(PlannedHeader plannedHeader, TextView textView,
            ImageView img, int color) {
        mPlannedHeader = plannedHeader;
        mTextView = textView;
        mImg = img;
        mColor = color;
    }

    public int draw(PlannedHeader currentHeader) {
        Context context = PreferencesState.getInstance().getContext();
        if (mPlannedHeader.equals(currentHeader)) {
            mImg.setImageResource(R.drawable.ic_plan_media_arrow_up);
            mImg.setColorFilter(PreferencesState.getInstance().getContext().getResources().getColor(
                    R.color.white));
        } else {
            if (mPlannedHeader.getTitleHeader().contains(
                    context.getString(R.string.dashboard_title_planned_type_never))) {
                mColor = PreferencesState.getInstance().getContext().getResources().getColor(
                        R.color.white);
                Typeface font = Typeface.createFromAsset(context.getAssets(),
                        "fonts/" + context.getString(R.string.medium_font_name));
                mTextView.setTypeface(font);
            } else {
                mColor = PreferencesState.getInstance().getContext().getResources().getColor(
                        R.color.black);
            }
        }
        return mColor;
    }

    public static void drawQualityOfCare(View rowLayout, PlannedSurvey plannedSurvey) {
        TextView textView = (TextView) rowLayout.findViewById(R.id.planning_survey_qoc);
        textView.setText(plannedSurvey.getQualityOfCare());
    }

    public static String formatDate(Date date) {
        return AUtils.getEuropeanFormatedDate(date);
    }

    public static String getTitleHeader(String titleHeader, Integer counter) {
        return String.format("%s (%d)",titleHeader,counter);
    }
}
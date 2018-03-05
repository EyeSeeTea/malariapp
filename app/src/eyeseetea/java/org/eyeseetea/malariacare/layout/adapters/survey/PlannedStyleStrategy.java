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

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedHeader;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedSurvey;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

import java.util.Date;

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
        if (mPlannedHeader.equals(currentHeader)) {
            mImg.setRotation(180);
        }
        Context context = PreferencesState.getInstance().getContext();
            mColor = PreferencesState.getInstance().getContext().getResources().getColor(
                    R.color.white);
        return mColor;
    }

    public static void drawQualityOfCare(View rowLayout, PlannedSurvey plannedSurvey) {
        DoubleRectChart doubleRectChart = (DoubleRectChart) rowLayout.findViewById(R.id.planning_survey_qoc);
        float score;
        try{
            score = Float.parseFloat(plannedSurvey.getQualityOfCare());
        }catch (NumberFormatException e){
            score = 0;
        }
        int color = LayoutUtils.trafficColor(score);
        String scoreText;
        if(plannedSurvey.getQualityOfCare().equals("-") || plannedSurvey.getQualityOfCare().equals("NaN")){
            scoreText = "-";
        }else {
            scoreText = plannedSurvey.getQualityOfCare() + ".0";
        }
        if(scoreText.equals("-")){
            doubleRectChart.createNaNDoubleRectChart(scoreText,
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.nan_color),ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.white));
        }else {
            doubleRectChart.createDoubleRectChart(scoreText, (int) score,
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(), color),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.white),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.black),
                    ContextCompat.getColor(PreferencesState.getInstance().getContext(),
                            R.color.white));
        }
    }

    public static String formatDate(Date date) {
        return AUtils.getEuropeanFormatedDateWithShortYear(date);
    }

    public static String getTitleHeader(String titleHeader, Integer counter) {
        return String.format("%s",titleHeader,counter);
    }

    public static void drawActionButtonTint(ImageButton actionButton) {
    }

    public static View getViewByPlannedSurveyHeader(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LinearLayout rowLayout = (LinearLayout) inflater.inflate(R.layout.planning_survey_header_row,
                parent, false);
        return rowLayout;
    }

    public static void drawNumber(LinearLayout rowLayout, Integer counter) {
        TextView textView = (TextView) rowLayout.findViewById(R.id.planning_number);
        textView.setText(counter+"");
    }

    public static DoubleRectChart loadDoubleRectChart(RelativeLayout rootView) {
        return (DoubleRectChart) rootView.findViewById(R.id.scoreChart);
    }
}
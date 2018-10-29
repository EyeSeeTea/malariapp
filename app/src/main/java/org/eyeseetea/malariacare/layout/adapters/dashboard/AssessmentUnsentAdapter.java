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

import static android.view.View.GONE;

import static org.eyeseetea.malariacare.DashboardActivity.dashboardActivity;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.repositories.SurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.strategies.AssessmentUnsentAdapterCosmeticsStrategy;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;

import java.util.ArrayList;
import java.util.List;

public class AssessmentUnsentAdapter extends ADashboardAdapter {

    public AssessmentUnsentAdapter(List<SurveyDB> items, Context context) {
        super(context);
        this.items = items;
        this.recordLayout = R.layout.assessment_unsent_record;

    }
    @Override
    protected void initMenu(final SurveyDB survey) {
        menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dashboardActivity.onAssessSelected(survey);
            }
        });
    }

    @Override
    protected void decorateCustomColumns(final SurveyDB survey, View rowView) {
        final DoublePieChart doublePieChart =
                (DoublePieChart) rowView.findViewById(R.id.double_pie_chart);

        ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
                new SurveyAnsweredRatioRepository();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();
        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase =
                new GetSurveyAnsweredRatioUseCase(surveyAnsweredRatioRepository, mainExecutor, asyncExecutor);
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                        Log.d(getClass().getName(), "nextProgressMessage");
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        Log.d(getClass().getName(), "onComplete");

                        if (surveyAnsweredRatio != null) {
                            doublePieChart.createDoublePie(surveyAnsweredRatio.getMandatoryStatus(),
                                    surveyAnsweredRatio.getTotalStatus());
                        }
                    }
                });
    }


    protected void createPie(PieChart mChart, int percentage,
            int highColor, int middleColor, int lowColor) {
        Log.d("percentage", "percentage: " + percentage);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(true);


        mChart.setTransparentCircleColor(Color.RED);
        mChart.setTransparentCircleAlpha(255);

        mChart.setHoleRadius(0f);
        mChart.setTransparentCircleRadius(0f);

        mChart.setDrawCenterText(false);

        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        setData(mChart, percentage, highColor, middleColor, lowColor);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);
    }

    private void setData(PieChart mChart, int percentage,
            int highColor, int middleColor, int lowColor) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their
        // position around the center of
        // the chart.
        if (percentage == 0) {
            percentage++;
        }
        entries.add(new PieEntry((float) percentage));
        entries.add(new PieEntry((float) (100 - percentage)));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        if (percentage > 90) {
            colors.add(highColor);
        } else if (percentage > 50) {
            colors.add(middleColor);
        } else {
            colors.add(lowColor);
        }


        colors.add(Color.TRANSPARENT);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.TRANSPARENT);

        //hide legend
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    @Override
    protected boolean hasToShowFacility(int position, SurveyDB survey) {
        if (position == 0) {
            return true;
        }

        SurveyDB previousSurvey = this.items.get(position - 1);
        return survey.getOrgUnit().getId_org_unit() != previousSurvey.getOrgUnit().getId_org_unit();
    }

    @Override
    protected void hideFacility(CustomTextView facilityName, CustomTextView surveyType) {
        facilityName.setVisibility(GONE);
        facilityName.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0f));
        LinearLayout.LayoutParams linearLayout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1f);
        surveyType.setLayoutParams(linearLayout);
    }

    @Override
    protected void showFacility(CustomTextView facilityName, CustomTextView surveyType,
            SurveyDB survey) {
        facilityName.setText(survey.getOrgUnit().getName());
        facilityName.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.5f));
        surveyType.setLayoutParams(
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0.5f));
    }

    @Override
    protected void decorateSurveyChart(View rowView, SurveyDB survey) {
        AssessmentUnsentAdapterCosmeticsStrategy assessmentUnsentAdapterCosmeticsStrategy =
                new AssessmentUnsentAdapterCosmeticsStrategy();
        assessmentUnsentAdapterCosmeticsStrategy.decorateUnsentSurveyChart(rowView, survey);
    }

    /**
     * Calculate proper background according to the following rule:
     * -Same orgunit same background
     */
    @Override
    protected View decorateBackground(int position, View rowView) {
        if(position==0 || position%2==0){
            rowView.setBackgroundColor(context.getResources().getColor(R.color.white));
        }else{
            rowView.setBackgroundColor(context.getResources().getColor(R.color.white_grey));
        }
        return rowView;
    }

}
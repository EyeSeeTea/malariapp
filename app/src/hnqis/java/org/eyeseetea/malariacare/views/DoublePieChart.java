package org.eyeseetea.malariacare.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.eyeseetea.malariacare.R;

import java.util.ArrayList;

public class DoublePieChart extends FrameLayout {
    private PieChart centerPie, outsidePie;

    public DoublePieChart(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        inflate(context, R.layout.double_pie_chart, this);
        centerPie = (PieChart) findViewById(R.id.internal_chart);
        outsidePie = (PieChart) findViewById(R.id.external_chart);

        int[] attrsArray = new int[]{
                android.R.attr.layout_width,
                android.R.attr.layout_height
        };
        TypedArray ta = context.obtainStyledAttributes(attributeSet, attrsArray);
        int layout_width = ta.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
        int layout_height = ta.getDimensionPixelSize(1, ViewGroup.LayoutParams.MATCH_PARENT);

        outsidePie.setLayoutParams(new LayoutParams(layout_width, layout_height));
        centerPie.setLayoutParams(
                new LayoutParams((int) (layout_width * 0.8), (int) (layout_height * 0.8),
                        Gravity.CENTER));

    }

    public void createDoublePie(int internalPercentage, int externalPercentage) {
        createPie(outsidePie, externalPercentage);
        createPie(centerPie, internalPercentage);
    }

    protected void createPie(PieChart mChart, int percentage) {
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

        setData(mChart, percentage);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);
    }

    private void setData(PieChart mChart, int percentage) {

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
            colors.add(Color.GREEN);
        } else if (percentage > 50) {
            colors.add(Color.YELLOW);
        } else {
            colors.add(Color.RED);
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
}

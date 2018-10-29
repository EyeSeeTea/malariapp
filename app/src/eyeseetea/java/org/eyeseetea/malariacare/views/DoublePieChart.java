package org.eyeseetea.malariacare.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
    private View doublePieContainer;
    private int highColor, middleColor, lowColor,
            mandatoryHighColor, mandatoryMiddleColor, mandatoryLowColor;
    private static final int HIGH_VALUE = 90;
    private static final int MIDDLE_VALUE = 50;

    public DoublePieChart(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        inflate(context, R.layout.double_pie_chart, this);

        centerPie = (PieChart) findViewById(R.id.internal_chart);
        outsidePie = (PieChart) findViewById(R.id.external_chart);
        doublePieContainer = (View) findViewById(R.id.double_pie_container);

        highColor = ResourcesCompat.getColor(
                getContext().getResources(), R.color.ratio_questions_high, null);
        middleColor = ResourcesCompat.getColor(
                getContext().getResources(), R.color.ratio_questions_middle, null);
        lowColor = ResourcesCompat.getColor(
                getContext().getResources(), R.color.ratio_questions_low, null);
        mandatoryHighColor = ResourcesCompat.getColor(
                getContext().getResources(), R.color.ratio_mandatory_questions_high, null);
        mandatoryMiddleColor = ResourcesCompat.getColor(
                getContext().getResources(), R.color.ratio_mandatory_questions_middle, null);
        mandatoryLowColor = ResourcesCompat.getColor(
                getContext().getResources(), R.color.ratio_mandatory_questions_low, null);

        int[] attrsArray = new int[]{
                android.R.attr.layout_width,
                android.R.attr.layout_height
        };
        TypedArray ta = context.obtainStyledAttributes(attributeSet, attrsArray);
        int layout_width = ta.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
        int layout_height = ta.getDimensionPixelSize(1, ViewGroup.LayoutParams.MATCH_PARENT);

        outsidePie.setLayoutParams(new LayoutParams(layout_width, layout_height));

        centerPie.setLayoutParams(
                new LayoutParams((int) (layout_width * 0.70), (int) (layout_height * 0.70),
                        Gravity.CENTER));

    }

    public void createDoublePie(final int internalPercentage, int externalPercentage) {
        createPie(outsidePie, externalPercentage, highColor, middleColor, lowColor, true);
        createPie(centerPie, internalPercentage, mandatoryHighColor, mandatoryMiddleColor, mandatoryLowColor, false);
        final ImageView mandatoryCheck = (ImageView) doublePieContainer.findViewById(
                R.id.completed_mandatory_check);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (internalPercentage == 100) {
                    mandatoryCheck.setVisibility(View.VISIBLE);
                } else {
                    mandatoryCheck.setVisibility(GONE);
                }
            }
        }, 1400);
    }

    protected void createPie(PieChart mChart, int percentage,
            int highColor, int middleColor, int lowColor, boolean hole) {
        Log.d("percentage", "percentage: " + percentage);
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(hole);

        mChart.setHoleColor(Color.TRANSPARENT);

        float radio = 0;
        if(hole) {
            radio = 70f;
        }
        mChart.setHoleRadius(radio);
        mChart.setDrawCenterText(false);

        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        setData(mChart, percentage, highColor, middleColor, lowColor);

        mChart.animateY(100, Easing.EasingOption.EaseInOutQuad);
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

        if (percentage > HIGH_VALUE) {
            colors.add(highColor);
        } else if (percentage > MIDDLE_VALUE) {
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
        mChart.setHoleColor(getColor(getContext(), R.color.transparent));
        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private OnClickListener listener;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(listener != null) listener.onClick(this);
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            if(listener != null) listener.onClick(this);
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public static int getMandatoryColorByPercentage(int percentage, @NonNull Context context) {
        int highColor =  getMandatoryHighColor(context);

        int middleColor = getMandatoryMiddleColor(context);

        int lowColor  = getMandatoryLowColor(context);

        return getColorByPercentage(percentage, highColor,
                middleColor,
                lowColor);
    }

    public static int getOverAllColorByPercentage(int percentage, @NonNull Context context) {
        int highColor =  getDarkHighColor(context);

        int middleColor = getDarkMiddleColor(context);

        int lowColor  = getLowColor(context);

        return getColorByPercentage(percentage, highColor,
                middleColor,
                lowColor);
    }

    private static int getColorByPercentage(int percentage, int highColor, int middleColor,
            int lowColor) {
        int colorByPercentage;
        if (percentage >= HIGH_VALUE) {
            colorByPercentage = highColor;
        } else if (percentage >= MIDDLE_VALUE) {
            colorByPercentage = middleColor;
        } else {
            colorByPercentage = lowColor;
        }
        return colorByPercentage;
    }

    private static int getDarkHighColor(@NonNull Context context){
        return  getColor(context,R.color.ratio_mandatory_questions_high);
    }

    private static int getDarkMiddleColor(@NonNull Context context){
        return  getColor(context,R.color.ratio_mandatory_questions_middle);
    }

    private static int getLowColor(@NonNull Context context){
        return  getColor(context,R.color.ratio_questions_low);
    }

    private static int getMandatoryHighColor(@NonNull Context context){
        return  getColor(context,R.color.ratio_mandatory_questions_high);
    }
    private static int getMandatoryMiddleColor(@NonNull Context context){
        return  getColor(context,R.color.ratio_mandatory_questions_middle);
    }

    private static int getMandatoryLowColor(@NonNull Context context){
        return  getColor(context,R.color.ratio_mandatory_questions_low);
    }

    private static int getColor(@NonNull Context context, @ColorRes int color){
        return  ResourcesCompat.getColor(
                context.getResources(), color, null);
    }
}

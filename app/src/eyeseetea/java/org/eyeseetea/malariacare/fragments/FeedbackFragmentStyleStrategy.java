package org.eyeseetea.malariacare.fragments;

import android.content.Context;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

public class FeedbackFragmentStyleStrategy {
    public static void drawScore(RelativeLayout llLayout, SurveyDB survey, Context context) {
        DoubleRectChart doubleRectChart = (DoubleRectChart)llLayout.findViewById(R.id.feedback_total_score);
        LayoutUtils.drawScore(survey.getMainScore(), doubleRectChart);
    }
}

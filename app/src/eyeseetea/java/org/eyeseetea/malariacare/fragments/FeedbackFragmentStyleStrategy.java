package org.eyeseetea.malariacare.fragments;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.feedback.CompositeScoreFeedback;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

public class FeedbackFragmentStyleStrategy {
    public static void drawScore(RelativeLayout llLayout, SurveyDB survey, Context context) {
        DoubleRectChart doubleRectChart = (DoubleRectChart) llLayout.findViewById(R.id.feedback_total_score);
        LayoutUtils.drawScore(survey.getMainScore(), doubleRectChart);
    }

    public static void changeBackgroundColor(View view, CompositeScoreFeedback feedback) {
    }

    public static void drawFeedbackScore(View rowLayout, CompositeScoreFeedback feedback, float idSurvey, String module) {
        DoubleRectChart doubleRectChart = (DoubleRectChart) rowLayout.findViewById(R.id.feedback_total_score);
        LayoutUtils.drawScore(feedback.getScore(idSurvey, module), doubleRectChart);
    }
}

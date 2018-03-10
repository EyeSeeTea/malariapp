package org.eyeseetea.malariacare.strategies;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.feedback.CompositeScoreFeedback;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.layout.adapters.survey.FeedbackAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;
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

    public static void showFilters(View view, final FeedbackAdapter feedbackAdapter) {
        //And checkbox listener
        View button = view.findViewById(R.id.filters_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showFeedbackFilters(view.getContext(), feedbackAdapter);
            }
        });
    }

    public static void showImproveFilter(View view,
            final DashboardSentFragment dashboardSentFragment) {
        View button = view.findViewById(R.id.more_options);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.showImproveFilter(view.getContext(), dashboardSentFragment);
            }
        });
    }


    public static void loadArrow(LinearLayout rowLayout) {
        View view=rowLayout.findViewById(R.id.feedback_question_arrow);
        view.setVisibility(View.INVISIBLE);
    }

    public static void toggleArrow(LinearLayout rowLayout, boolean visible) {
        ImageView imageView = (ImageView) rowLayout.findViewById(R.id.feedback_question_arrow);
        if(imageView!=null && visible) {
            imageView.setRotation(270f);
        }else{
            imageView.setRotation(0f);
        }
    }
}

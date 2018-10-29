package org.eyeseetea.malariacare.strategies;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.feedback.CompositeScoreFeedback;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.layout.adapters.survey.FeedbackAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

public class FeedbackFragmentStyleStrategy {
    public static void drawScore(RelativeLayout llLayout, SurveyDB survey, Context context) {
        if (survey.hasMainScore()) {
            float average = survey.getMainScore().getScore();
            CustomTextView item = (CustomTextView) llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("%.1f%%", average));
            int colorId = LayoutUtils.trafficColor(average);
            item.setTextColor(context.getResources().getColor(colorId));
        } else {
            CustomTextView item = (CustomTextView) llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("NaN"));
            float average = 0;
            int colorId = LayoutUtils.trafficColor(average);
            item.setTextColor(context.getResources().getColor(colorId));
        }
    }

    public static void changeBackgroundColor(View view, CompositeScoreFeedback feedback) {
        view.findViewById(R.id.cs_header).setBackgroundResource(feedback.getBackgroundColor());

        ImageView imageView = (ImageView)view.findViewById(R.id.feedback_image);

        imageView.setBackgroundResource(feedback.getBackgroundColor());
    }

    public static void drawFeedbackScore(View rowLayout, CompositeScoreFeedback feedback, float idSurvey, String module) {
        TextView textView=(TextView)rowLayout.findViewById(R.id.feedback_score_label);

        if(!PreferencesState.getInstance().isVerticalDashboard()){
            ScoreType scoreType = new ScoreType(feedback.getScore(idSurvey, module));
            if(scoreType.getClassification() == ScoreType.Classification.LOW) {
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed));
            }else if(scoreType.getClassification() == ScoreType.Classification.MEDIUM) {
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.amber));
            }else if(scoreType.getClassification() == ScoreType.Classification.HIGH) {
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.lightGreen));
            }
        }
        textView.setText(feedback.getPercentageAsString(idSurvey, module));
    }


    public static void showFilters(View view,
            final FeedbackAdapter feedbackAdapter) {
        //And checkbox listener
        CustomRadioButton chkFailed = (CustomRadioButton) view.findViewById(R.id.chkFailed);
        chkFailed.setChecked(true);
        chkFailed.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             feedbackAdapter.toggleOnlyFailed(true);
                                             ((CustomRadioButton) v).setChecked(feedbackAdapter
                                                     .isOnlyFailed());
                                         }
                                     }
        );
        CustomRadioButton chkMedia = (CustomRadioButton) view.findViewById(R.id.chkMedia);
        chkMedia.setChecked(false);
        chkMedia.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            feedbackAdapter.toggleOnlyMedia(true);
                                            ((CustomRadioButton) v).setChecked(feedbackAdapter
                                                    .isOnlyMedia());
                                        }
                                    }
        );
    }

    public static void showImproveFilter(View view, DashboardSentFragment dashboardSentFragment) {
    }

    public static void loadArrow(LinearLayout rowLayout) {
    }

    public static void toggleArrow(LinearLayout rowLayout, boolean visible) {
    }
}
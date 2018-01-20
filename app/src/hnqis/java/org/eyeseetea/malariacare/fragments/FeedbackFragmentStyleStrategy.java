package org.eyeseetea.malariacare.fragments;

import android.content.Context;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.data.database.utils.feedback.CompositeScoreFeedback;
import android.view.View;

public class FeedbackFragmentStyleStrategy {
    public static void drawScore(RelativeLayout llLayout, SurveyDB survey, Context context) {
        if (survey.hasMainScore()) {
            float average = survey.getMainScore();
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
        FeedbackFragmentStyleStrategy.changeBackgroundColor(rowLayout, feedback);

        imageView.setBackgroundResource(feedback.getBackgroundColor());
    }

    public static void drawFeedbackScore(View rowLayout, CompositeScoreFeedback feedback, float idSurvey, String module) {
        TextView textView=(TextView)rowLayout.findViewById(R.id.feedback_score_label);

        if(!PreferencesState.getInstance().isVerticalDashboard()){
            if(feedback.getScore(idSurvey, module)< Constants.MAX_RED)
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed));
            else if(feedback.getScore(idSurvey, module)< Constants.MAX_AMBER)
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.amber));
            else
                textView.setTextColor(PreferencesState.getInstance().getContext().getResources().getColor(R.color.lightGreen));
        }
        textView.setText(feedback.getPercentageAsString(idSurvey, module));
    }


    public static void showFilters(View view, CustomRadioButton chkFailed, CustomRadioButton chkMedia, final FeedbackAdapter feedbackAdapter) {
        //And checkbox listener
        chkFailed = (CustomRadioButton) view.findViewById(R.id.chkFailed);
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
        chkMedia = (CustomRadioButton) view.findViewById(R.id.chkMedia);
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
}

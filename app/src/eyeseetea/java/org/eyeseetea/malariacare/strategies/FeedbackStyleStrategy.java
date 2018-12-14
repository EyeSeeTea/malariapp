package org.eyeseetea.malariacare.strategies;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.feedback.QuestionFeedback;

public class FeedbackStyleStrategy {
    public static void drawRowResult(LinearLayout rowLayout, QuestionFeedback feedback, Context context) {
        if(feedback.hasGrade()) {
            TextView textView = (TextView) rowLayout.findViewById(R.id.feedback_score_label);
            textView.setText(context.getString(feedback.getGrade()));
            textView.setTextColor(context.getResources().getColor(feedback.getColor()));
        }
    }
}

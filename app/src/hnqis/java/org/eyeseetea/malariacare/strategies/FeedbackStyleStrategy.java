package org.eyeseetea.malariacare.strategies;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.feedback.QuestionFeedback;

public class FeedbackStyleStrategy {
    public static void drawRowResult(LinearLayout rowLayout, QuestionFeedback feedback, Context context) {
        ImageView imageView = (ImageView) rowLayout.findViewById(R.id.feedback_score_label_img);
        TextView textView = (TextView) rowLayout.findViewById(R.id.feedback_score_label);
        textView.setVisibility(View.GONE);
        if(feedback.hasGrade()) {
            imageView.setVisibility(View.VISIBLE);
            int grade = feedback.getGrade();
            if(grade==R.string.feedback_info_passed){
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pass));
            }else if(grade==R.string.feedback_info_failed){
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_fail));
            }else{
                imageView.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(context.getString(grade));
                textView.setTextColor(context.getResources().getColor(feedback.getColor()));
            }
        }else{
            imageView.setVisibility(View.INVISIBLE);
        }
    }
}

package org.eyeseetea.malariacare.layout.adapters.survey.strategies;

import android.content.Context;
import android.widget.TextView;

public class FeedbackAdapterStrategy extends AFeedbackAdapterStrategy {
    @Override
    public void setPercentColor(TextView percentText, int color, Context context) {
        percentText.setBackgroundColor(context.getResources().getColor(color));
    }
}

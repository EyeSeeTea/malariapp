package org.eyeseetea.malariacare.fragments.strategies;

import android.content.Context;
import android.widget.TextView;

public class FeedbackFragmentStrategy extends AFeedbackFragmentStrategy {

    @Override
    public void setTotalPercentColor(TextView totalPercent, int color, Context context) {
        totalPercent.setBackgroundColor(context.getResources().getColor(color));
    }
}

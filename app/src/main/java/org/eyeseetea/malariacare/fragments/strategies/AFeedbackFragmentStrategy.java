package org.eyeseetea.malariacare.fragments.strategies;

import android.content.Context;
import android.widget.TextView;

public abstract class AFeedbackFragmentStrategy {
    public void setTotalPercentColor(TextView totalPercent, int color, Context context) {
        totalPercent.setTextColor(context.getResources().getColor(color));
    }

}

package org.eyeseetea.malariacare.layout.adapters.survey.strategies;

import android.content.Context;
import android.widget.TextView;

public class AFeedbackAdapterStrategy {
    public void setPercentColor(TextView percentText, int color, Context context) {
        percentText.setTextColor(context.getResources().getColor(color));
    }
}

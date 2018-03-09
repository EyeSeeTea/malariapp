package org.eyeseetea.malariacare.strategies;

import android.support.design.widget.FloatingActionButton;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

public class PlanActionStyleStrategy {
    public static void fabIcons(FloatingActionButton fabComplete, Integer status) {
        if (status.equals(Constants.SURVEY_IN_PROGRESS)) {
            fabComplete.setImageResource(R.drawable.ic_action_uncheck);
        } else if (status == Constants.SURVEY_SENT) {
            fabComplete.setImageResource(R.drawable.ic_double_check);
        } else {
            fabComplete.setImageResource(R.drawable.ic_action_check);
        }
    }

    public static DoubleRectChart loadDoubleRectChart(RelativeLayout rootView) {
        return null;
    }
}

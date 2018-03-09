package org.eyeseetea.malariacare.strategies;

import android.support.design.widget.FloatingActionButton;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.utils.Constants;

public class PlanActionStyleStrategy {
    public static void fabIcons(FloatingActionButton fabComplete, Integer status) {
        if (status.equals(Constants.SURVEY_IN_PROGRESS)) {
            mFabComplete.setImageResource(R.drawable.ic_action_uncheck);
        } else if (status == Constants.SURVEY_SENT) {
            mFabComplete.setImageResource(R.drawable.ic_double_check);
        } else {
            mFabComplete.setImageResource(R.drawable.ic_action_check);
        }
    }

    public static DoubleRectChart loadDoubleRectChart(RelativeLayout rootView) {
        return null;
    }
}

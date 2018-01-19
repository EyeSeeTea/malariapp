package org.eyeseetea.malariacare.fragments;

import android.support.design.widget.FloatingActionButton;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.utils.Constants;

public class PlanActionStyleStrategy {
    public static void fabIcons(FloatingActionButton fabComplete, Integer status) {
        if (status.equals(Constants.SURVEY_IN_PROGRESS)) {
            fabComplete.setImageResource(R.drawable.ic_action_uncheck);
        } else if (status == Constants.SURVEY_SENT) {
            fabComplete.setImageResource(R.drawable.ic_double_check);
        }else {
            fabComplete.setImageResource(R.drawable.ic_action_check);
        }
    }
}

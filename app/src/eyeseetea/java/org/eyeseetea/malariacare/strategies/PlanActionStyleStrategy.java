package org.eyeseetea.malariacare.strategies;

import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

public class PlanActionStyleStrategy {
    public static void fabIcons(FloatingActionButton fabComplete, ObservationStatus status) {
        if (status.equals(ObservationStatus.IN_PROGRESS)) {
            fabComplete.setImageResource(R.drawable.vct_action_uncheck);
        } else if (status.equals(ObservationStatus.SENT)) {
            fabComplete.setImageResource(R.drawable.vct_double_check);
        }else {
            fabComplete.setImageResource(R.drawable.vct_action_check);
        }
    }

    public static DoubleRectChart loadDoubleRectChart(RelativeLayout rootView) {
        return (DoubleRectChart) rootView.findViewById(R.id.scoreChart);
    }

    public static void disableShare(FloatingActionButton fabShare) {
        Drawable disabledShare= ResourcesCompat.getDrawable(PreferencesState.getInstance().getContext().getResources(), R.drawable.ic_disabled_share, null);
        fabShare.setImageDrawable(disabledShare);
    }

    public static void enableShare(FloatingActionButton fabShare) {
        Drawable enableShare= ResourcesCompat.getDrawable(PreferencesState.getInstance().getContext().getResources(), R.drawable.ic_share, null);
        fabShare.setImageDrawable(enableShare);
    }
}

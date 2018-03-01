package org.eyeseetea.malariacare.fragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.eyeseetea.malariacare.R;

public class DashboardSentFragmentStrategy {

    public static void createOptionsDialog(View view,
            View.OnClickListener onClickListener) {
        ImageView extras = (ImageView) view.findViewById(
                R.id.more_options);
        extras.setOnClickListener(onClickListener);
    }

    public static void hideFilterSubHeader(View view) {
        ViewGroup extras = (ViewGroup) view.findViewById(
                R.id.extra_options);
        extras.setVisibility(View.GONE);
    }
}

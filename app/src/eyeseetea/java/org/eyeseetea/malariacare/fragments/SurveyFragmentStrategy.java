package org.eyeseetea.malariacare.fragments;

import android.widget.ListView;

class SurveyFragmentStrategy {
    public static void modifyListviewBorder(ListView listView) {
        listView.setDivider(null);
        listView.setDividerHeight(0);
    }
}

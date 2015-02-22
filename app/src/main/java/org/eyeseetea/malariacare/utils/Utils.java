package org.eyeseetea.malariacare.utils;

import android.view.View;

/**
 * Created by Jose on 22/02/2015.
 */
public class Utils {

    public static View findParentRecursively(View view, int targetId) {
        if (view.getId() == targetId) {
            return view;
        }
        View parent = (View) view.getParent();
        if (parent == null) {
            return null;
        }
        return findParentRecursively(parent, targetId);
    }
}

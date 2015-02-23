package org.eyeseetea.malariacare.utils;

import android.view.View;

import java.util.Arrays;
import java.util.List;

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

    public static View findParentRecursively(View view, List<Integer> targetIds){
        int i = 0;
        while (i<targetIds.size()){
            if (view.getId() == targetIds.get(i)){
                return view;
            }
            i++;
        }
        View parent = (View) view.getParent();
        if (parent == null){
            return null;
        }
        return findParentRecursively(parent, targetIds);
    }
}

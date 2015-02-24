package org.eyeseetea.malariacare.utils;

import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.data.Question;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jose on 22/02/2015.
 */
public class Utils {

    // Given a View, this method climbs the tree searching the target ID
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

    // Given a View, this method climbs the tree searching one of the target ID
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

    // Given a View, this method search down in the tree to find the corresponding child View to a given question
    public static View findChildRecursively(View startingView, Question question) {
        ViewGroup startingViewGroup = (ViewGroup) startingView;
        int i = 0;
        int count = startingViewGroup.getChildCount();
        while(i<count){
            View child = startingViewGroup.getChildAt(i);
            if (child.getTag().equals(question)){
                return child;
            }
            child = findChildRecursively(child, question);
            if (child != null){
                return child;
            }
            i++;
        }
        return null;
    }

}

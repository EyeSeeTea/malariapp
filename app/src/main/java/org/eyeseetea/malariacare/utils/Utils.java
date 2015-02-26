package org.eyeseetea.malariacare.utils;

import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.data.Question;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jose on 22/02/2015.
 */
public class Utils {

    static final int numberOfDecimals = 2; // Number of decimals outputs will have

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
        int i = 0;
        while(i<((ViewGroup)startingView).getChildCount()){
            View child = ((ViewGroup)startingView).getChildAt(i);
            if (child == null) return null;
            if (child.getTag() != null && child.getTag().equals(question)){
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

    public static String round(float base, int decimalPlace){
        BigDecimal bd = new BigDecimal(Float.toString(base));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_DOWN);
        return Float.toString(bd.floatValue());
    }

    public static String round(float base){
        return round(base, Utils.numberOfDecimals);
    }
}

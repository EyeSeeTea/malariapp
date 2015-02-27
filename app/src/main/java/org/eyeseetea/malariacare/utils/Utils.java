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
        View toReturn;
        if (startingView != null && (startingView.getTag() != null) && (startingView.getTag() instanceof Question) && ((((Question) startingView.getTag()).getId()).longValue() == (question.getId()).longValue())) {
            return startingView;
        } else {
            int childrenCount;
            if (startingView instanceof ViewGroup) childrenCount = ((ViewGroup) startingView).getChildCount();
            else childrenCount = 0;
            if (childrenCount > 0)
                for (int i = 0; i < childrenCount; i++) {
                    toReturn = findChildRecursively(((ViewGroup) startingView).getChildAt(i), question);
                    if (toReturn != null) return toReturn;
                }
        }
        return null;
    }

    public static void setVisible(View startingView, Question question){
        View childView = findChildRecursively(startingView, question);
        if (childView != null){
            ((View)childView.getParent().getParent()).setVisibility(View.VISIBLE);
            ((View)childView.getParent()).setVisibility(View.VISIBLE);
            ((View)childView).setVisibility(View.VISIBLE);
        }
    }

    public static void setInvisible(View startingView, Question question){
        View childView = findChildRecursively(startingView, question);
        if (childView != null){
            ((View)childView.getParent().getParent()).setVisibility(View.GONE);
            ((View)childView.getParent()).setVisibility(View.GONE);
            ((View)childView).setVisibility(View.GONE);
        }
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

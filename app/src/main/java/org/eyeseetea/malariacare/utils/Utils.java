package org.eyeseetea.malariacare.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.orm.SugarRecord;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.Question;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        if (startingView != null && (startingView.getTag(R.id.QuestionTag) != null) && (startingView.getTag(R.id.QuestionTag) instanceof Question) && ((((Question) startingView.getTag(R.id.QuestionTag)).getId()).longValue() == (question.getId()).longValue())) {
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

    public static void toggleVisible(View childView, int visibility){
        ((View)childView.getParent().getParent()).setVisibility(visibility);
        ((View)childView.getParent()).setVisibility(visibility);
        ((View)childView).setVisibility(visibility);

        if (childView instanceof Spinner) {
            ((Spinner) childView).setSelection(0);
        }
        else{
            ((EditText) childView).setText("");
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

    public static List<Integer> getLayoutIds(){
        List<Integer> ids = new ArrayList<Integer>();
        for(TabConfiguration tabsLayout : MainActivity.getTabsLayouts()){
            ids.add(tabsLayout.getTabId());
        }
        return ids;
    }

    public static List<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            List<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        List<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            List<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    public static boolean isHeaderEmpty(List<Question> parentList, List<Question> childrenList){
        boolean isContained;
        for (Question child : childrenList){
            isContained = false;
            if (child.getQuestion() == null) return false;
            for (Question parent : parentList) {
                if (child.getId().equals(parent.getId())){
                    isContained = true;
                    break;
                }
            }
            if (!isContained)return false;
        }
        return true;
    }
}

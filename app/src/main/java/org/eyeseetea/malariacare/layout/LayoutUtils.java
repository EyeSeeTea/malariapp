package org.eyeseetea.malariacare.layout;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.models.ReportingResults;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.TabConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 22/02/2015.
 */
public class LayoutUtils {

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

    // Put a View visibility to one of the constants from View class [View.VISIBLE | View.INVISIBLE | View.GONE]
    public static void toggleVisible(View childView, int visibility){
        ((View)childView.getParent().getParent()).setVisibility(visibility);
        ((View)childView.getParent()).setVisibility(visibility);
        ((View)childView).setVisibility(visibility);

        resetComponent(childView);
    }

    // Given a View, clears the content (putting dropdown lists to its first position and resetting text fields)
    public static void resetComponent(View childView) {
        if (childView instanceof Spinner) {
            ((Spinner) childView).setSelection(0);
        }
        else{
            ((EditText) childView).setText("");
        }
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

    // Searchs for every children that contain the given tag. If tag equals null search for every view with the key. Otherwise, checks key equals the object tag
    public static List<View> getChildrenByTag(ViewGroup root, int key, Object tag){
        List<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getChildrenByTag((ViewGroup) child, key, tag));
            }

            Object tagObj = child.getTag(key);
            if (tagObj != null){
                if (tag != null) {
                    if (tagObj.equals(tag)) {
                        views.add(child);
                    }
                } else {
                    views.add(child);
                }
            }
        }
        return views;
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

    public static List<ReportingResults> addReportingQuestions()
    {
        List<ReportingResults> results=new ArrayList<ReportingResults>();

        results.add(new ReportingResults(Constants.REPORTING_Q1));
        results.add(new ReportingResults(Constants.REPORTING_Q2));
        results.add(new ReportingResults(Constants.REPORTING_Q3));
        results.add(new ReportingResults(Constants.REPORTING_Q4));
        results.add(new ReportingResults(Constants.REPORTING_Q5));
        results.add(new ReportingResults(Constants.REPORTING_Q6));
        results.add(new ReportingResults(Constants.REPORTING_Q7));
        results.add(new ReportingResults(Constants.REPORTING_Q8));
        results.add(new ReportingResults(Constants.REPORTING_Q9));
        results.add(new ReportingResults(Constants.REPORTING_Q10));

        return results;
    }

}

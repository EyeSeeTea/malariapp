package org.eyeseetea.malariacare.layout.utils;

import android.graphics.Color;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.configuration.TabConfiguration;
import org.eyeseetea.malariacare.layout.score.NumDenRecord;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 22/02/2015.
 */
public class LayoutUtils {

    public static final int [] rowBackgrounds = {R.drawable.background_even, R.drawable.background_odd};

    // Given a index, this method return a background color
    public static int calculateBackgrounds(int index) {
        return LayoutUtils.rowBackgrounds[index % LayoutUtils.rowBackgrounds.length];
    }

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

    public static void toggleVisibleChildren(int position, Spinner spinner, Question triggeredQuestion, SparseArray<NumDenRecord> numDenRecordMap) {
        View parent = LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab));
        for (Question childQuestion : triggeredQuestion.getQuestionChildren()) {
            View childView = LayoutUtils.findChildRecursively(parent, childQuestion);
            if (position == 1) { //FIXME: There must be a smarter way for saying "if the user selected yes"
                LayoutUtils.toggleVisible(childView, View.VISIBLE);
                ((View) ((View) childView).getTag(R.id.HeaderViewTag)).setVisibility(View.VISIBLE);
                numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(childQuestion, 0F, childQuestion.getDenominator_w());
            } else {
                LayoutUtils.toggleVisible(childView, View.GONE);
                if (LayoutUtils.isHeaderEmpty(triggeredQuestion.getQuestionChildren(), childQuestion.getHeader().getQuestions())) {
                    ((View) ((View) childView).getTag(R.id.HeaderViewTag)).setVisibility(View.GONE);
                }
                numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).deleteRecord(childQuestion);
            }
        }
    }

    // Put a View visibility to one of the constants from View class [View.VISIBLE | View.INVISIBLE | View.GONE]
    public static void toggleVisible(View childView, int visibility){
        if (childView instanceof Spinner) {
            ((ViewGroup) childView.getParent().getParent().getParent()).setVisibility(visibility);
        }
        else{
            ((View) childView.getParent().getParent()).setVisibility(visibility);
        }
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

    // Reset values from Score tab
    public static void resetScores(ViewGroup root){

        for (TabConfiguration tabConfiguration: LayoutConfiguration.getTabsConfiguration().values()){

            // First reset the scores TextViews
            if(tabConfiguration.getScoreFieldId() != null) ((TextView)root.findViewById(tabConfiguration.getScoreFieldId())).setText("0.0");
            if(tabConfiguration.getScoreAvgFieldId() != null) ((TextView)root.findViewById(tabConfiguration.getScoreAvgFieldId())).setText("0.0");

            // Then for Custom tabs we search for TextViews with id == R.id.score and set them to 0
            if(!tabConfiguration.isAutomaticTab() && tabConfiguration.getScoreFieldId() != null){
                LinearLayout tabLayout = (LinearLayout)root.findViewById(tabConfiguration.getTabId());
                List<View> tables = getTableChildren(tabLayout);
                for (View table: tables){
                    for (int i=0; i<((TableLayout)table).getChildCount(); i++){
                        TextView scoreText = (TextView)((TableLayout)table).getChildAt(i).findViewById(R.id.score);
                        if (scoreText != null) scoreText.setText("0");
                        // FIXME: we should rename this scoreValue ids to score, but subtotal updating relies on finding only one score id in the tab, so it's not straight-forward
                        scoreText = (TextView)((TableLayout)table).getChildAt(i).findViewById(R.id.scoreValue);
                        if (scoreText != null) scoreText.setText("0");
                    }
                }
                List<View> edits = getEditChildren(tabLayout);
                for (View edit: edits){
                    ((EditText)edit).setText("");
                }

                // Reset subscore layout components
                GridLayout subscore = (GridLayout)(tabLayout.getChildAt(1));
                LinearLayout subscoreContainer = (LinearLayout)((ViewGroup)((ViewGroup)subscore.getChildAt(0)).getChildAt(1)).getChildAt(0);
                TextView score = (TextView)((ViewGroup)subscoreContainer.getChildAt(0)).getChildAt(0);
                TextView percentageSymbol = (TextView)((ViewGroup)subscoreContainer.getChildAt(0)).getChildAt(1);
                TextView cualitiveScore = (TextView)((ViewGroup)subscoreContainer.getChildAt(1)).getChildAt(0);
                setScore(0.0F, score, percentageSymbol, cualitiveScore);
            }
        }
    }

    public static void setScore(float score, View scoreView, View percentageView, View cualitativeView){
        LayoutUtils.trafficLight(scoreView, score, cualitativeView);
        if (percentageView != null) LayoutUtils.trafficLight(percentageView, score, null);
        ((TextView)scoreView).setText(Utils.round(score));
    }

    public static void setScore(float score, View scoreView){
        setScore(score, scoreView, null, null);
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

    // Searchs for every children that is instance of TableLayout.
    public static List<View> getTableChildren(ViewGroup root){
        List<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getTableChildren((ViewGroup) child));
            }

            if (child != null) {
                if (child instanceof TableLayout) {
                    views.add(child);
                }
            }
        }
        return views;
    }

    // Searchs for every children that is instance of EditText.
    public static List<View> getEditChildren(ViewGroup root){
        List<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getEditChildren((ViewGroup) child));
            }

            if (child != null) {
                if (child instanceof EditText) {
                    views.add(child);
                }
            }
        }
        return views;
    }

    // Searchs for every children that contain the given tag.
    // If tag equals null search for every view with the key. Otherwise, checks key equals the object tag
    // If key equals null doesn't retrieve a key but it calls to getTag() assuming only one tag is set
    public static List<View> getChildrenByTag(ViewGroup root, Integer key, Object tag){
        List<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getChildrenByTag((ViewGroup) child, key, tag));
            }

            Object tagObj = null;
            if ( key == null) tagObj = child.getTag();
            else tagObj = child.getTag(key);
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

    // Depending on a score sets the first view color (0<x<50:poor ; 50<x<80:fare ; 80<x<100:good)
    // If a second view is given, it also writes the text good, fare or given there
    public static void trafficLight(View view, float score, View textView){
        if(score < 50.0F){ // poor
            ((TextView)view).setTextColor(Color.parseColor("#FF0000")); // red
            if(textView != null) {
                ((TextView)textView).setTextColor(Color.parseColor("#FF0000")); // red
                ((TextView)textView).setText(view.getContext().getResources().getString(R.string.poor));
            }
        } else if (score < 80.0F){ // fare
            ((TextView)view).setTextColor(Color.parseColor("#FF8000")); // amber
            if(textView != null) {
                ((TextView)textView).setTextColor(Color.parseColor("#FF8000")); // amber
                ((TextView)textView).setText(view.getContext().getResources().getString(R.string.fare));
            }
        } else {
            ((TextView)view).setTextColor(Color.parseColor("#40FF00")); // green
            if(textView != null) { // good
                ((TextView)textView).setTextColor(Color.parseColor("#40FF00")); // green
                ((TextView)textView).setText(view.getContext().getResources().getString(R.string.good));
            }
        }
    }
}

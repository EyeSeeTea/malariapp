/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 22/02/2015.
 */
public class LayoutUtils {

    public static final int [] rowBackgrounds = {R.drawable.background_even, R.drawable.background_odd};
    public static final int [] rowBackgroundsNoBorder = {R.drawable.background_even_wo_border, R.drawable.background_odd_wo_border};

    // Given a index, this method return a background color
    public static int calculateBackgrounds(int index) {
        return LayoutUtils.rowBackgrounds[index % LayoutUtils.rowBackgrounds.length];
    }

    // Given an index, this method returns a background color but without a cell border
    public static int calculateBackgroundsNoBorder(int index) {
        return LayoutUtils.rowBackgroundsNoBorder[index % LayoutUtils.rowBackgroundsNoBorder.length];
    }



    /**
     * Calculates de proper background according to an score
     * @param score
     * @return
     */
    public static int trafficDrawable(float score){
        if(score<Survey.MAX_RED){
            return R.drawable.circle_shape_red;
        }

        if(score<Survey.MAX_AMBER){
            return R.drawable.circle_shape_amber;
        }

        return R.drawable.circle_shape_green;
    }

    public static void trafficView(Context context, float score, View view){
        Drawable circleShape=context.getResources().getDrawable(trafficDrawable(score));
        if(android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            view.setBackground(circleShape);
        }else {
            view.setBackgroundDrawable(circleShape);
        }
    }



    // Used to setup the usual actionbar with the logo and the app name
    public static void setActionBarLogo(ActionBar actionBar){
        if(Utils.isPictureQuestion()){
            //// FIXME: 08/12/2015
            //actionBar.setLogo(R.drawable.pictureapp_logo);
            //actionBar.setIcon(R.drawable.pictureapp_logo);
            actionBar.setLogo(R.drawable.qualityapp_logo);
            actionBar.setIcon(R.drawable.qualityapp_logo);
        }
        else{
            actionBar.setLogo(R.drawable.qualityapp_logo);
            actionBar.setIcon(R.drawable.qualityapp_logo);
        }
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    }

    // Used to put the org unit name and the kind of survey instead of the app name
    public static void setActionBarText(ActionBar actionBar, String title, String subtitle){
        actionBar.setDisplayUseLogoEnabled(false);
        // Uncomment in case of we want the logo out
        // actionBar.setLogo(null);
        // actionBar.setIcon(null);
        actionBar.setTitle(title);
        actionBar.setSubtitle(subtitle);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
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
        if (startingView != null && (startingView.getTag(R.id.QuestionTag) != null) && (startingView.getTag(R.id.QuestionTag) instanceof Question) && ((((Question) startingView.getTag(R.id.QuestionTag)).getId_question()).longValue() == (question.getId_question()).longValue())) {
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

    public static void toggleVisibleChildren(int position, Spinner spinner, Question triggeredQuestion) {
        View parent = LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab));
        for (Question childQuestion : triggeredQuestion.getQuestionChildren()) {
            View childView = LayoutUtils.findChildRecursively(parent, childQuestion);
            View headerView = ((View) ((View) childView).getTag(R.id.HeaderViewTag));
            if (position == 1) { //FIXME: There must be a smarter way for saying "if the user selected yes"
                LayoutUtils.toggleVisible(childView, View.VISIBLE);
                if (headerView != null) headerView.setVisibility(View.VISIBLE);
                ScoreRegister.addRecord(childQuestion, 0F, childQuestion.getDenominator_w());
            } else {
                LayoutUtils.toggleVisible(childView, View.GONE);
                if (LayoutUtils.isHeaderEmpty(triggeredQuestion.getQuestionChildren(), childQuestion.getHeader().getQuestions())) {
                    if (headerView != null) headerView.setVisibility(View.GONE);
                }
                ScoreRegister.deleteRecord(childQuestion);
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
        else if (childView instanceof EditText){
            ((EditText) childView).setText("");
            Question triggeredQuestion = (Question) ((EditText)childView).getTag(R.id.QuestionTag);
            if (triggeredQuestion != null) {
                Value value = triggeredQuestion.getValueBySession();
                if (value != null) {
                    value.setValue("");
                    value.save();
                }
            }
        }
    }

    public static void setScore(float score, View scoreView, View percentageView, View cualitativeView){
        LayoutUtils.trafficLight(scoreView, score, cualitativeView);
        if (percentageView != null) LayoutUtils.trafficLight(percentageView, score, null);
        ((CustomTextView)scoreView).setText(Utils.round(score));
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
                if (child.getId_question().equals(parent.getId_question())){
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
    public static void trafficLight(View view, float score, View textCard){
        //Suppose it is 'Good' && Green
        int color=view.getContext().getResources().getColor(R.color.green);
        String tag=view.getContext().getResources().getString(R.string.good);
        if (score < Survey.MAX_AMBER){
            color= view.getContext().getResources().getColor(R.color.amber);
            tag=view.getContext().getResources().getString(R.string.fair);
        }
        if (score < Survey.MAX_RED){
            color= view.getContext().getResources().getColor(R.color.red);
            tag=view.getContext().getResources().getString(R.string.poor);
        }
        //Change color for number
        ((CustomTextView)view).setTextColor(color);
        //Change color& text for qualitative score
        if(textCard != null) {
            ((CustomTextView)textCard).setTextColor(color); // red
            ((CustomTextView)textCard).setText(tag);
        }
    }
    public static int getNumberOfQuestionParentsHeader(Header header) {
        int result = 0;

        List<Question> list =  header.getQuestions();

        for (Question question : list)
            if (question.hasChildren())
                result = result + 1;

        return result;
    }


}

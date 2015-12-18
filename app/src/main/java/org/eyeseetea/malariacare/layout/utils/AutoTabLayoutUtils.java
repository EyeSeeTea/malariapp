/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.utils;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.common.primitives.Booleans;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by adrian on 15/08/15.
 */
public class AutoTabLayoutUtils {

    private static final String TAG = ".ATLayoutUtils";

    //Store the Views references for each row (to avoid many calls to getViewById)
    public static class ViewHolder {
        //Label
        public CustomTextView statement;

        // Main component in the row: Spinner, EditText or RadioGroup
        public View component;

        public CustomTextView num;
        public CustomTextView denum;
        public int type;

        /**
         * Fixes a bug in older apis where a RadioGroup cannot find its children by id
         * @param id
         * @return
         */
        public CustomRadioButton findRadioButtonById(int id){
            //No component -> done
            if (component==null || ! (component instanceof RadioGroup)){
                return null;
            }

            //Modern api -> delegate in its method
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
                return (CustomRadioButton)component.findViewById(id);
            }

            //Find button manually
            for(int i=0;i<((RadioGroup) component).getChildCount();i++){
                View button=((RadioGroup) component).getChildAt(i);
                if(button.getId()==id){
                    return (CustomRadioButton) button;
                }
            }
            return null;
        }
    }

    //Store the views references for each view in the footer
    public static class ScoreHolder {
        public CustomTextView subtotalscore;
        public CustomTextView score;
        public CustomTextView totalNum;
        public CustomTextView totalDenum;
        public CustomTextView qualitativeScore;
    }

    /**
     * Enables/Disables input view according to the state of the survey.
     * Sent surveys cannot be modified.
     *
     * @param view
     */
    public static void updateReadOnly(View view, boolean readOnly) {
        if (view == null) {
            return;
        }

        if (view instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) view;
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(!readOnly);
            }
        } else {
            view.setEnabled(!readOnly);
        }
    }

    /**
     * Checks if given question should be hidden according to the current survey or not.
     * @param question
     * @return
     */
    public static boolean isHidden(Question question) {
        return question.isHiddenBySurvey(Session.getSurvey());
    }

    /**
     * Given a desired position (that means, the position shown in the screen) of an element, get the
     * real position (that means, the position in the stored items list taking into account the hidden
     * elements)
     * @param position
     * @return the real position in the elements list
     */
    public static int getRealPosition(int position, LinkedHashMap<BaseModel, Boolean> elementInvisibility, List<? extends BaseModel> items){
        int hElements = getHiddenCountUpTo(position, elementInvisibility);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (elementInvisibility.get(items.get(position + diff))) i--;
        }
        return (position + diff);
    }

    /**
     * Get the number of elements that are hidden until a given position
     * @param position
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    private static int getHiddenCountUpTo(int position, LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        boolean [] upper = Arrays.copyOfRange(Booleans.toArray(elementInvisibility.values()), 0, position + 1);
        int hiddens = Booleans.countTrue(upper);
        return hiddens;
    }

    public static View initialiseDropDown(int position, ViewGroup parent, Question question, ViewHolder viewHolder, LayoutInflater lInflater, Context context) {
        View rowView;
        rowView = initialiseView(R.layout.ddl, parent, question, viewHolder, position, lInflater);

        initialiseScorableComponent(rowView, viewHolder);

        // In case the option is selected, we will need to show num/dems
        List<Option> optionList = new ArrayList<>(question.getAnswer().getOptions());
        optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
        Spinner spinner = (Spinner) viewHolder.component;
        spinner.setAdapter(new OptionArrayAdapter(context, optionList));
        return rowView;
    }


    public static View initialiseView(int resource, ViewGroup parent, Question question, ViewHolder viewHolder, int position, LayoutInflater lInflater) {
        View rowView = lInflater.inflate(resource, parent, false);
        if (question.hasChildren())
            rowView.setBackgroundResource(R.drawable.background_parent);
        else
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        viewHolder.component = rowView.findViewById(R.id.answer);
        viewHolder.statement = (CustomTextView) rowView.findViewById(R.id.statement);
        viewHolder.statement.setText(question.getForm_name());

        return rowView;
    }

    public static void initialiseScorableComponent(View rowView, ViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (CustomTextView) rowView.findViewById(R.id.num);
        viewHolder.denum = (CustomTextView) rowView.findViewById(R.id.den);

        configureViewByPreference(viewHolder);
    }


    /**
     * Set visibility of numerators and denominators depending on the user preference selected in the settings activity
     *
     * @param viewHolder view that holds the component to be more efficient
     */
    private static void configureViewByPreference(AutoTabLayoutUtils.ViewHolder viewHolder) {
        int visibility = View.GONE;
        float statementWeight = 0.65f;
        float componentWeight = 0.35f;
        float numDenWeight = 0.0f;

        if (PreferencesState.getInstance().isShowNumDen()) {
            visibility = View.VISIBLE;
            statementWeight = 0.45f;
            componentWeight = 0.25f;
            numDenWeight = 0.15f;
        }

        viewHolder.num.setVisibility(visibility);
        viewHolder.denum.setVisibility(visibility);
        ((RelativeLayout) viewHolder.statement.getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, statementWeight));
        ((RelativeLayout) viewHolder.component.getParent().getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, componentWeight));
        ((RelativeLayout) viewHolder.num.getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, numDenWeight));
        ((RelativeLayout) viewHolder.denum.getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, numDenWeight));
    }

    /**
     * Decide whether we need or not to hide this header (if every question inside is hidden)
     * @param header header that
     * @return true if every header question is hidden, false otherwise
     */
    public static boolean hideHeader(Header header, LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        // look in every question to see if every question is hidden. In case one cuestion is not hidden, we return false
        for (Question question : header.getQuestions()) {
            if (!elementInvisibility.get(question)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the number of elements that are hidden
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    public static int getHiddenCount(LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        // using Guava library and its Booleans utility class
        return Booleans.countTrue(Booleans.toArray(elementInvisibility.values()));
    }
    private boolean checkMatches(Question question) {
        boolean match = true;

        List<Question> relatives = question.getRelatives();

        if (relatives.size() > 0) {

            Option option = ReadWriteDB.readOptionAnswered(relatives.get(0));

            if (option == null) match = false;

            for (int i = 1; i < relatives.size() && match; i++) {
                Option currentOption = ReadWriteDB.readOptionAnswered(relatives.get(i));

                if (currentOption == null) match = false;
                else
                    match = match && (Float.compare(option.getFactor(), currentOption.getFactor()) == 0);
            }

        }

        return match;
    }

    public static boolean autoFillAnswer(AutoTabLayoutUtils.ViewHolder viewHolder, AutoTabLayoutUtils.ScoreHolder scoreHolder, Question question, float totalNum, float totalDenum, Context context, LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        //FIXME Yes|No are 'hardcoded' here by using options 0|1
        int option=question.isTriggered(Session.getSurvey())?0:1;

        //Select option according to trigger
        return itemSelected(viewHolder, scoreHolder, question, question.getAnswer().getOptions().get(option), totalNum, totalDenum, context, elementInvisibility);
    }

    /**
     * Do the logic after a DDL option change
     * @param viewHolder private class that acts like a cache to quickly access the different views
     * @param question the question that changes his value
     * @param option the option that has been selected
     */

    public static boolean itemSelected(AutoTabLayoutUtils.ViewHolder viewHolder, AutoTabLayoutUtils.ScoreHolder scoreHolder, Question question, Option option, float totalNum, float totalDenum, Context context, LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        boolean refreshTab = false;

        // Write option to DB
        ReadWriteDB.saveValuesDDL(question, option);
        Float num;
        Float denum;
        if(Utils.isPictureQuestion()) {
            num = ScoreRegister.calcNum(question);
            denum = ScoreRegister.calcDenum(question);
        }
        else{
            num=totalNum;
            denum=totalDenum;
        }
        recalculateScores(viewHolder, question, num, denum);
        if(Utils.isPictureQuestion()) {
            if (question.hasChildren()) {
                toggleChildrenVisibility(question, elementInvisibility, num, denum);
            }
        }
        else{
        // If parent relation found, toggle Children Spinner Visibility
            // If question has question-option, refresh the tab
            if (question.hasChildren() || question.hasQuestionOption()) {
                if (question.hasChildren())
                    toggleChildrenVisibility(question, elementInvisibility, num, denum);
                refreshTab = true;
            }
        }
        updateScore(scoreHolder, num, denum, context);
        return refreshTab;
    }
    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    private static void recalculateScores(AutoTabLayoutUtils.ViewHolder viewHolder, Question question, float totalNum, float totalDenum) {
        Float num = ScoreRegister.calcNum(question);
        Float denum = ScoreRegister.calcDenum(question);

        viewHolder.num.setText(num.toString());
        viewHolder.denum.setText(denum.toString());

        List<Float> numdenum = ScoreRegister.getNumDenum(question);

        if (numdenum != null) {
            totalNum = totalNum - numdenum.get(0);
            totalDenum = totalDenum - numdenum.get(1);
        }

        totalNum = totalNum + num;
        totalDenum = totalDenum + denum;

        ScoreRegister.addRecord(question, num, denum);
    }

    public static void updateScore(ScoreHolder scoreHolder, float totalNum, float totalDenum, Context context) {
        scoreHolder.totalNum.setText(Float.toString(totalNum));
        scoreHolder.totalDenum.setText(Float.toString(totalDenum));
        if (totalDenum != 0) {
            Float score = 100 * (totalNum / totalDenum);
            LayoutUtils.trafficLight(scoreHolder.score, score, scoreHolder.qualitativeScore);
            scoreHolder.score.setText(Utils.round(100 * (totalNum / totalDenum)) + " % ");
        }
        if (totalDenum == 0 && totalNum == 0) {
            scoreHolder.score.setText(context.getString(R.string.number_zero_percentage));
        }
    }

    /**
     * Given a question, make visible or invisible their children. In case all children in a header
     * became invisible, that header is also hidden
     * @param question the question whose children we want to show/hide
     */
    private static void toggleChildrenVisibility(Question question, LinkedHashMap<BaseModel, Boolean> elementInvisibility, float totalNum, float totalDenum) {
        List<Question> children = question.getChildren();
        Question cachedQuestion = null;
        Survey survey=Session.getSurvey();
        boolean visible;

        for (Question child : children) {
            Header childHeader = child.getHeader();
            visible=!child.isHiddenBySurvey(survey);
            elementInvisibility.put(child, !visible);
            if (!visible) {
                List<Float> numdenum = ScoreRegister.getNumDenum(child);
                if (numdenum != null) {
                    // update scores
                    totalDenum = totalDenum - numdenum.get(1);
                    totalNum = totalNum - numdenum.get(0);
                    ScoreRegister.deleteRecord(child);
                }
                ReadWriteDB.deleteValue(child); // when we hide a question, we remove its value
                // little cache to avoid double checking same
                if(cachedQuestion == null || (cachedQuestion.getHeader().getId_header() != child.getHeader().getId_header()))
                    elementInvisibility.put(childHeader, AutoTabLayoutUtils.hideHeader(childHeader, elementInvisibility));
            } else {
                Float denum = ScoreRegister.calcDenum(child);
                totalDenum = totalDenum + denum;
                ScoreRegister.addRecord(child, 0F, denum);
                elementInvisibility.put(childHeader, false);
            }
            cachedQuestion = question;
        }
    }

    public static void initScoreQuestion(Question question, float totalNum, float totalDenum) {

        if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST || question.getAnswer().getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getAnswer().getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Float num = ScoreRegister.calcNum(question);
            Float denum = ScoreRegister.calcDenum(question);

            if(!Utils.isPictureQuestion()) {
                num = totalNum + num;
                denum = totalDenum + denum;
            }

            ScoreRegister.addRecord(question, num, denum);
        }


  }
    public static void createRadioGroupComponent(Question question, ViewHolder viewHolder, int orientation, LayoutInflater lInflater, Context context) {
        ((RadioGroup) viewHolder.component).setOrientation(orientation);

        for (Option option : question.getAnswer().getOptions()) {
            CustomRadioButton button;
            int layoutId;
            if(Utils.isPictureQuestion())
                layoutId=R.layout.uncheckeable_radiobutton_pictureapp;
            else
                layoutId=R.layout.uncheckeable_radiobutton;

            button= (CustomRadioButton) lInflater.inflate(layoutId, null);
            button.setOption(option);
            button.updateProperties(PreferencesState.getInstance().getScale(), context.getString(R.string.font_size_level1), context.getString(R.string.medium_font_name));
            ((RadioGroup) viewHolder.component).addView(button);
        }
    }


}

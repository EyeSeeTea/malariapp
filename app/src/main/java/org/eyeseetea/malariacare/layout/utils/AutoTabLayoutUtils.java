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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;

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
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomEditText;
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

        private List<View> columnComponents;

        public CustomTextView num;
        public CustomTextView denum;

        public ViewHolder(){
            columnComponents = new ArrayList<>();
        }

        public ViewHolder(View component){
            this();
            this.component=component;
        }

        public void addColumnComponent(View component){
            this.columnComponents.add(component);
        }

        public View getColumnComponent(int position){
            if(position > (this.columnComponents.size()-1)){
                return null;
            }

            return this.columnComponents.get(position);
        }

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


        public void setText(String text){
            if(component==null || !(component instanceof CustomEditText)){
                return;
            }

            ((CustomEditText)component).setText(text);
        }

        public void setNumText(String text){
            if(num==null){
                return;
            }

            num.setText(text);
        }

        public void setDenumText(String text){
            if(denum==null){
                return;
            }
            denum.setText(text);
        }

        public void setSpinnerSelection(int position){
            if(component==null || !(component instanceof Spinner)){
                return;
            }

            ((Spinner) component).setSelection(position);
        }

        public void setRadioChecked(Option option){
            if(component==null || !(component instanceof RadioGroup)){
                return;
            }
            ((CustomRadioButton) component.findViewWithTag(option)).setChecked(true);
        }

        public void setSwitchOption(Option option) {
            if(component==null || !(component instanceof Switch)){
                return;
            }

            boolean isChecked=false;
            String switchText ="";
            if(option!=null){
                isChecked = Boolean.valueOf(option.getCode());
                switchText = option.getName();
            }

            Switch switchButton = (Switch)component;
            switchButton.setChecked(isChecked);
            switchButton.setText(switchText);
        }
    }

    /**
     * Static class just to get the answer about the children deletion inside a listener
     */
    public static class QuestionVisibility{
        public static Question question;
        public static AutoTabInVisibilityState inVisibilityState;
        public static AutoTabAdapter adapter;
        public static Option option;
    }

    //Store the views references for each view in the footer
    public static class ScoreHolder {
        public CustomTextView subtotalscore;
        public CustomTextView score;
        public CustomTextView totalNum;
        public CustomTextView totalDenum;
        public CustomTextView qualitativeScore;
    }

    public static void updateReadOnly(ViewHolder viewHolder,QuestionRow questionRow, boolean readonly){
        if(viewHolder==null || questionRow==null){
            return;
        }

        for(int i=0;i<questionRow.sizeColumns();i++){
            View component = viewHolder.getColumnComponent(i);
            Question question = questionRow.getQuestions().get(i);

            updateReadOnly(component,question,readonly);
        }
    }

    /**
     * Enables/Disables input view according to the state of the survey.
     * Sent surveys cannot be modified.
     *
     * @param view
     */
    public static void updateReadOnly(View view, Question question, boolean readonly) {
        if (view == null || question==null) {
            return;
        }

        if(question.getOutput()==Constants.DROPDOWN_LIST_DISABLED){
            readonly=true;
        }

        if (view instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) view;
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(!readonly);
            }
        } else {
            view.setEnabled(!readonly);
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
     * A question row is hidden if the first question is hidden
     * @param questionRow
     * @return
     */
    public static boolean isHidden(QuestionRow questionRow){
        if(questionRow==null || questionRow.sizeColumns()==0){
            return true;
        }

        Question question = questionRow.getFirstQuestion();

        return isHidden(question);
    }

    /**
     * Given a desired position (that means, the position shown in the screen) of an element, get the
     * real position (that means, the position in the stored items list taking into account the hidden
     * elements)
     * @param position
     * @return the real position in the elements list
     */
    public static int getRealPosition(int position, LinkedHashMap<Object, Boolean> elementInvisibility, List<? extends BaseModel> items){
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
    private static int getHiddenCountUpTo(int position, LinkedHashMap<Object, Boolean> elementInvisibility) {
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

        if(question.getCompulsory()){
            int red = PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed);
            String appNameColorString = String.format("%X", red).substring(2);
            Spanned spannedQuestion= Html.fromHtml(String.format("<font color=\"#%s\"><b>", appNameColorString) + "*  " + "</b></font>" + question.getForm_name());
            viewHolder.statement.setText(spannedQuestion);
        }
        else
            viewHolder.statement.setText(question.getForm_name());

        return rowView;
    }

    public static void initialiseScorableComponent(View rowView, ViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (CustomTextView) rowView.findViewById(R.id.num);
        viewHolder.denum = (CustomTextView) rowView.findViewById(R.id.den);

        configureViewByPreference(viewHolder);
    }

    public static void createRadioGroupComponent(Question question, ViewHolder viewHolder, int orientation, LayoutInflater lInflater, Context context) {
        ((RadioGroup) viewHolder.component).setOrientation(orientation);

        for (Option option : question.getAnswer().getOptions()) {
            CustomRadioButton button = (CustomRadioButton) lInflater.inflate(R.layout.uncheckeable_radiobutton, null);
            button.setOption(option);
            button.updateProperties(PreferencesState.getInstance().getScale(), context.getString(R.string.font_size_level1), context.getString(R.string.medium_font_name));
            ((RadioGroup) viewHolder.component).addView(button);
        }
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
     * Get the number of elements that are hidden
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    public static int getHiddenCount(LinkedHashMap<Object, Boolean> elementInvisibility) {
        // using Guava library and its Booleans utility class
        return Booleans.countTrue(Booleans.toArray(elementInvisibility.values()));
    }

    public static boolean autoFillAnswer(AutoTabLayoutUtils.ViewHolder viewHolder, AutoTabLayoutUtils.ScoreHolder scoreHolder, Question question, float totalNum, float totalDenum, Context context, AutoTabInVisibilityState inVisibilityState, AutoTabAdapter adapter) {
        //FIXME Yes|No are 'hardcoded' here by using options 0|1
        int option=question.isTriggered(Session.getSurvey())?0:1;

        //Select option according to trigger
        return itemSelected(viewHolder, scoreHolder, question, question.getAnswer().getOptions().get(option), totalNum, totalDenum, context, inVisibilityState, adapter);
    }

    /**
     * Do the logic after a DDL option change
     * @param viewHolder private class that acts like a cache to quickly access the different views
     * @param question the question that changes his value
     * @param option the option that has been selected
     */
    public static boolean itemSelected(final AutoTabLayoutUtils.ViewHolder viewHolder, AutoTabLayoutUtils.ScoreHolder scoreHolder, Question question, Option option, float totalNum, float totalDenum, Context context, AutoTabInVisibilityState inVisibilityState, final AutoTabAdapter adapter) {
        boolean refreshTab = false;

        if (!question.hasChildren()) {
            // Write option to DB
            ReadWriteDB.saveValuesDDL(question, option);
            recalculateScores(viewHolder, question);
        }

        // If parent relation found, toggle Children Spinner Visibility
        // If question has question-option, refresh the tab
        if (question.hasChildren() || question.hasQuestionOption()){
            if (question.hasChildren()) {
                QuestionVisibility.question = question;
                QuestionVisibility.inVisibilityState = inVisibilityState;
                QuestionVisibility.adapter = adapter;
                QuestionVisibility.option = option;

                if (isRemovingValuesFromChildren(question)) {
                    new AlertDialog.Builder(context)
                            .setTitle(null)
                            .setMessage(context.getString(R.string.dialog_deleting_children))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    saveAndExpandChildren(viewHolder);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.notifyDataSetChanged();
                                }
                            }).create().show();
                } else{
                    saveAndExpandChildren(viewHolder);
                }
            }
            refreshTab = true;
        }

        return refreshTab;
    }

    /**
     * Looks for a children question with value (that is going to be removed due to a parent question 'No')
     * @param question
     * @return
     */
    private static boolean isRemovingValuesFromChildren(Question question){
        for (Question childQuestion: question.getChildren()){
            if (childQuestion.getValueBySession()!=null && childQuestion.getOutput()!=Constants.DROPDOWN_LIST_DISABLED){
                return true;
            }
        }
        return false;
    }

    public static void saveAndExpandChildren(ViewHolder viewHolder){
        // Write option to DB
        ReadWriteDB.saveValuesDDL(QuestionVisibility.question, QuestionVisibility.option);
        recalculateScores(viewHolder, QuestionVisibility.question);
        toggleChildrenVisibility();
        QuestionVisibility.adapter.notifyDataSetChanged();
    }

    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    private static void recalculateScores(AutoTabLayoutUtils.ViewHolder viewHolder, Question question) {
        Float num = ScoreRegister.calcNum(question);
        Float denum = ScoreRegister.calcDenum(question);

        viewHolder.setNumText(num.toString());
        viewHolder.setDenumText(denum.toString());

        ScoreRegister.addRecord(question, num, denum);
    }

    /**
     * Given a question, make visible or invisible their children. In case all children in a header
     * became invisible, that header is also hidden
     */
    private static void toggleChildrenVisibility() {
        Question question = QuestionVisibility.question;
        AutoTabInVisibilityState inVisibilityState = QuestionVisibility.inVisibilityState;
        List<Question> children = question.getChildren();
        Question cachedQuestion = null;
        Survey survey=Session.getSurvey();
        boolean visible;

        for (Question childQuestion : children) {
            Header childHeader = childQuestion.getHeader();
            visible=!childQuestion.isHiddenBySurvey(survey);
            inVisibilityState.updateVisibility(childQuestion,visible);
            if (!visible) {
                List<Float> numdenum = ScoreRegister.getNumDenum(childQuestion);
                if (numdenum != null) {
                    ScoreRegister.deleteRecord(childQuestion);
                }
                ReadWriteDB.deleteValue(childQuestion); // when we hide a question, we remove its value
                // little cache to avoid double checking same
                if(cachedQuestion == null || (cachedQuestion.getHeader().getId_header() != childQuestion.getHeader().getId_header())) {
                    inVisibilityState.updateHeaderVisibility(childHeader);
                }
            } else {
                Float denum = ScoreRegister.calcDenum(childQuestion);
                ScoreRegister.addRecord(childQuestion, 0F, denum);
                inVisibilityState.setInvisible(childHeader,false);
            }
            cachedQuestion = question;
        }
    }

    public static void initScoreQuestion(Question question) {

        if (question.getOutput() == Constants.DROPDOWN_LIST
                || question.getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Float num = ScoreRegister.calcNum(question);
            Float denum = ScoreRegister.calcDenum(question);
            ScoreRegister.addRecord(question, num, denum);
        }
    }

    public static void initScoreQuestion(QuestionRow questionRow){
        for(Question question: questionRow.getQuestions()){
            initScoreQuestion(question);
        }
    }
}

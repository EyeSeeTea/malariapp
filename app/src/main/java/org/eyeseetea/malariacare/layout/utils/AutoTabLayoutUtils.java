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
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.common.primitives.Booleans;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Media;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
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

    //Store the views references for each view in the footer
    public static class ScoreHolder {
        public CustomTextView subtotalscore;
        public CustomTextView score;
        public CustomTextView totalNum;
        public CustomTextView totalDenum;
        public CustomTextView qualitativeScore;
    }

    public static void updateReadOnly(AutoTabViewHolder viewHolder, QuestionRow questionRow, boolean readonly){
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

    public static View initialiseDropDown(int position, ViewGroup parent, Question question, AutoTabViewHolder viewHolder, LayoutInflater lInflater, Context context) {
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

    public static View initialiseView(int resource, ViewGroup parent, Question question, AutoTabViewHolder viewHolder, int position, LayoutInflater lInflater) {
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

    public static void initialiseScorableComponent(View rowView, AutoTabViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (CustomTextView) rowView.findViewById(R.id.num);
        viewHolder.denum = (CustomTextView) rowView.findViewById(R.id.den);

        configureViewByPreference(viewHolder);
    }

    public static void createRadioGroupComponent(Question question, AutoTabViewHolder viewHolder, int orientation, LayoutInflater lInflater, Context context) {
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
    private static void configureViewByPreference(AutoTabViewHolder viewHolder) {
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

    public static void autoFillAnswer(AutoTabViewHolder viewHolder, Question question, Context context, AutoTabInVisibilityState inVisibilityState, AutoTabAdapter adapter, float idSurvey, String module) {
        //FIXME Yes|No are 'hardcoded' here by using options 0|1
        int optionPosition=question.isTriggered(idSurvey)?0:1;

        //Build selected item
        Option option=question.getAnswer().getOptions().get(optionPosition);
        AutoTabSelectedItem autoTabSelectedItem = new AutoTabSelectedItem(adapter,inVisibilityState, idSurvey,module).buildSelectedItem(question,option,viewHolder,idSurvey,module);

        //Select that item to force related switch
        itemSelected(autoTabSelectedItem, idSurvey, module);
    }

    /**
     * Do the logic after a DDL option change
     * @param autoTabSelectedItem
     */
    public static void itemSelected(final AutoTabSelectedItem autoTabSelectedItem, final float idSurvey, final String module) {

        Question question = autoTabSelectedItem.getQuestion();
        Option option = autoTabSelectedItem.getOption();
        Context context = autoTabSelectedItem.getContext();
        final AutoTabViewHolder viewHolder = autoTabSelectedItem.getViewHolder();

        //No children -> Save, check scores, done.
        if (!question.hasChildren()) {
            // Write option to DB
            ReadWriteDB.saveValuesDDL(question, option, module);
            recalculateScores(viewHolder, question, idSurvey,module);
            return;
        }

        //Children -> Save, Expand|Collapse, Notify, ..

        //No children answers will be deleted -> Save, Expand|Collapse
        if (!isRemovingValuesFromChildren(question, module)) {
            saveAndExpandChildren(autoTabSelectedItem, idSurvey, module);
            return;
        }

        //Children answers will be deleted -> Confirm -> Save, Expand|Collapse
        new AlertDialog.Builder(context)
                .setTitle(null)
                .setMessage(context.getString(R.string.dialog_deleting_children))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        saveAndExpandChildren(autoTabSelectedItem, idSurvey, module);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        autoTabSelectedItem.notifyDataSetChanged();
                    }
                }).create().show();
    }

    /**
     * Looks for a children question with value (that is going to be removed due to a parent question 'No')
     * @param question
     * @return
     */
    private static boolean isRemovingValuesFromChildren(Question question, String module){
        for (Question childQuestion: question.getChildren()){
            if (childQuestion.getValueBySession(module)!=null && childQuestion.getOutput()!=Constants.DROPDOWN_LIST_DISABLED){
                return true;
            }
        }
        return false;
    }

    public static void saveAndExpandChildren(AutoTabSelectedItem autoTabSelectedItem, float idSurvey, String module){
        //Save value
        ReadWriteDB.saveValuesDDL(autoTabSelectedItem.getQuestion(), autoTabSelectedItem.getOption(), module);
        //Recalculate score
        recalculateScores(autoTabSelectedItem.getViewHolder(), autoTabSelectedItem.getQuestion(), idSurvey, module);
        //Toggle children
        autoTabSelectedItem.toggleChildrenVisibility(idSurvey, module);
        //Notify adapter
        autoTabSelectedItem.notifyDataSetChanged();
    }

    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    private static void recalculateScores(AutoTabViewHolder viewHolder, Question question, float idSurvey, String module) {
        Float num = ScoreRegister.calcNum(question, idSurvey);
        Float denum = ScoreRegister.calcDenum(question, idSurvey);

        viewHolder.setNumText(num.toString());
        viewHolder.setDenumText(denum.toString());

        ScoreRegister.addRecord(question, num, denum, idSurvey, module);
    }

    public static void initScoreQuestion(Question question, float idSurvey, String module) {

        if (question.getOutput() == Constants.DROPDOWN_LIST
                || question.getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Float num = ScoreRegister.calcNum(question, idSurvey);
            Float denum = ScoreRegister.calcDenum(question, idSurvey);
            ScoreRegister.addRecord(question, num, denum, idSurvey, module);
        }
    }

    public static void initScoreQuestion(QuestionRow questionRow, float idSurvey, String module){
        for(Question question: questionRow.getQuestions()){
            initScoreQuestion(question, idSurvey, module);
        }
    }
}

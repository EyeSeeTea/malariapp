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

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.AutoTabInVisibilityState;
import org.eyeseetea.malariacare.layout.utils.AutoTabLayoutUtils;
import org.eyeseetea.malariacare.layout.utils.AutoTabSelectedItem;
import org.eyeseetea.malariacare.layout.utils.AutoTabViewHolder;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.layout.utils.QuestionRow;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;

import java.util.ArrayList;
import java.util.List;


public class AutoTabAdapter extends ATabAdapter {

    private final static String TAG=".AutoTabAdapter";

    final AutoTabLayoutUtils.ScoreHolder scoreHolder = new AutoTabLayoutUtils.ScoreHolder();

    float totalDenum;

    /**
     * Reference to the visibility state of items
     */
    private final AutoTabInVisibilityState inVisibilityState;

    /**
     * Factory that holds common info between selected items
     */
    private final AutoTabSelectedItem autoTabSelectedItemFactory;

    public AutoTabAdapter(Tab tab, Context context, int id_layout) {
        super(tab, context, id_layout);

        this.inVisibilityState = new AutoTabInVisibilityState();
        this.autoTabSelectedItemFactory = new AutoTabSelectedItem(this,this.inVisibilityState);

        // Initialize the elementInvisibility HashMap by reading all questions and headers and decide
        // whether or not they must be visible
        for (int i = 0; i < getItems().size(); i++) {
            Object item = getItems().get(i);

            //Header are visible by default
            if (item instanceof Header) {
                inVisibilityState.initVisibility((Header) item);
            }

            //Question might be visible or not (according to parent values)
            if (item instanceof Question) {
                boolean visible = inVisibilityState.initVisibility((Question) item);
                if (visible){
                    AutoTabLayoutUtils.initScoreQuestion((Question) item);
                }else{
                    ScoreRegister.addRecord((Question) item, 0F, ScoreRegister.calcDenum((Question) item));
                }
                inVisibilityState.updateHeaderVisibility((Question) item);
            }

            //QuestionRow visibility equals first Question
            if (item instanceof QuestionRow){
                boolean visible = inVisibilityState.initVisibility((QuestionRow)item);
                if (visible){
                    AutoTabLayoutUtils.initScoreQuestion((QuestionRow) item);
                }else{
                    ScoreRegister.addQuestionRowRecords((QuestionRow) item);
                }
                inVisibilityState.updateHeaderVisibility((QuestionRow) item);
            }
        }
    }

    /**
     * Factory method to build a scored/non scored layout according to tab type.
     *
     * @param tab
     * @param context
     * @return
     */
    public static AutoTabAdapter build(Tab tab, Context context) {
        int idLayout = tab.getType() == Constants.TAB_AUTOMATIC_NON_SCORED ? R.layout.form_without_score : R.layout.form_with_score;
        return new AutoTabAdapter(tab, context, idLayout);
    }

    /**
     * Do every initialization related to subscore needed before showing a tab
     */
    @Override
    public void initializeSubscore() {
        initializeScoreViews();
        setSubScoreVisibility();
        initializeDenum();
    }

    /**
     * Store subscore bar views in a private class to later access them quickly
     */
    private void initializeScoreViews() {
        scoreHolder.score = (CustomTextView) ((Activity) getContext()).findViewById(R.id.score);
        scoreHolder.totalDenum = (CustomTextView) ((Activity) getContext()).findViewById(R.id.totalDen);
        scoreHolder.totalNum = (CustomTextView) ((Activity) getContext()).findViewById(R.id.totalNum);
        scoreHolder.subtotalscore = (CustomTextView) ((Activity) getContext()).findViewById(R.id.subtotalScoreText);
        scoreHolder.qualitativeScore = (CustomTextView) ((Activity) getContext()).findViewById(R.id.qualitativeScore);
    }

    /**
     * set subscore bar visibility depending on the show/hide num/dems user settings
     */
    private void setSubScoreVisibility(){
        ViewGroup subscoreBar = (ViewGroup) ((Activity)getContext()).findViewById(R.id.subscore_bar);
        subscoreBar.setVisibility(View.GONE);
    }


    private void initializeDenum() {
        float result = 0;
        int number_items = getItems().size();

        if (totalDenum == 0) {
            for (int i = 0; i < number_items; i++) {
                Object item=getItems().get(i);
                if ( item instanceof Question && inVisibilityState.isVisible(item)) {
                    Question question = (Question) item;
                    if (question.getOutput() == Constants.DROPDOWN_LIST) {
                        result = result + ScoreRegister.calcDenum(question);
                    }
                }
            }
            totalDenum = result;

        }
    }

    @Override
    public int getCount() {
        return (getItems().size() - inVisibilityState.countInvisible());
    }

    @Override
    public Object getItem(int position) {
        return getItems().get(inVisibilityState.getRealPosition(position,getItems()));
    }

    @Override
    public long getItemId(int position) {
        Object obj=getItem(position);
        return obj.hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        Question question;
        AutoTabViewHolder viewHolder = new AutoTabViewHolder();

        if (item instanceof Question) {
            question = (Question) item;
            rowView = getView(position, parent, rowView, question, viewHolder);

            //Put current value in the component
            setValues(viewHolder, question);

            //Disables component if survey has already been sent (except match spinner that are always disabled)
            AutoTabLayoutUtils.updateReadOnly(viewHolder.component, question, getReadOnly());

        } else if(item instanceof Header){
            rowView = getInflater().inflate(R.layout.headers, parent, false);
            viewHolder.statement = (CustomTextView) rowView.findViewById(R.id.headerName);
            viewHolder.statement.setText(((Header) item).getName());
        }else{
            QuestionRow questionRow = (QuestionRow)item;
            rowView = getRowView(position,parent,questionRow,viewHolder);
            //Put current values in components
            setValues(viewHolder,questionRow);
            //Disable components whenever required
            AutoTabLayoutUtils.updateReadOnly(viewHolder,questionRow,getReadOnly());
        }

        return rowView;
    }

    private View getRowView(int position,ViewGroup parent,QuestionRow questionRow, AutoTabViewHolder viewHolder){
        View rowView = getInflater().inflate(R.layout.question_row,parent,false);
        if(questionRow.isCustomTabTableHeader()){
            getViewTableHeader((LinearLayout) rowView, questionRow);
        }else{
            getViewTableContent((LinearLayout)rowView,questionRow, viewHolder);
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));
        }
        return  rowView;
    }

    private View getView(int position, ViewGroup parent, View rowView, Question question, AutoTabViewHolder viewHolder) {
        //FIXME This should be moved into its own class (Ex: ViewHolderFactory.getView(item))
        switch (question.getOutput()) {

            case Constants.LONG_TEXT:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.longtext, parent, question, viewHolder, position, getInflater());
                //Add main component and listener
                ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(question));
                break;
            case Constants.NO_ANSWER:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.label, parent, question, viewHolder, position, getInflater());
                break;
            case Constants.POSITIVE_INT:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer, parent, question, viewHolder, position, getInflater());
                //Add main component, set filters and listener
                ((CustomEditText) viewHolder.component).setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS),new MinMaxInputFilter(1, null)});
                ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(question));
                break;
            case Constants.INT:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer, parent, question, viewHolder, position, getInflater());
                //Add main component, set filters and listener
                ((CustomEditText) viewHolder.component).setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)});
                ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(question));
                break;
            case Constants.DATE:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.date, parent, question, viewHolder, position, getInflater());
                //Add main component and listener
                ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(question));
                break;

            case Constants.SHORT_TEXT:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.shorttext, parent, question, viewHolder, position, getInflater());
                //Add main component and listener
                ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(question));
                break;

            case Constants.DROPDOWN_LIST:
                rowView = AutoTabLayoutUtils.initialiseDropDown(position, parent, question, viewHolder, getInflater(), getContext());
                // Initialise Listener
                ((Spinner) viewHolder.component).setOnItemSelectedListener(new SpinnerListener(question, viewHolder));
                break;
            case Constants.DROPDOWN_LIST_DISABLED:
                rowView = AutoTabLayoutUtils.initialiseDropDown(position, parent, question, viewHolder, getInflater(), getContext());
                // Initialise value depending on match question
                AutoTabLayoutUtils.autoFillAnswer(viewHolder, question, getContext(), inVisibilityState, this);
                break;
            case Constants.RADIO_GROUP_HORIZONTAL:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());
                AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);
                AutoTabLayoutUtils.createRadioGroupComponent(question, viewHolder, LinearLayout.HORIZONTAL, getInflater(), getContext());
                //Add Listener
                ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder));
                break;
            case Constants.RADIO_GROUP_VERTICAL:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());
                AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);
                AutoTabLayoutUtils.createRadioGroupComponent(question, viewHolder, LinearLayout.VERTICAL, getInflater(), getContext());
                //Add Listener
                ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder));
                break;
            case Constants.SWITCH_BUTTON:
                rowView = AutoTabLayoutUtils.initialiseView(R.layout.switchbutton, parent, question, viewHolder, position, getInflater());
                ((Switch)viewHolder.component).setOnCheckedChangeListener(new SwitchButtonListener(question,viewHolder));

            default:
                break;
        }
        return rowView;
    }

    private View getViewTableHeader(LinearLayout row, QuestionRow questionRow){
        row.setWeightSum(1f);
        float columnWeight=questionRow.sizeColumns()/1f;
        for(Question question:questionRow.getQuestions()){
            addTextViewToRow(row,question,columnWeight, R.drawable.background_header,
                    getContext().getResources().getColor(R.color.white),
                    getContext().getString(R.string.condensed_font_name),
                    getContext().getString(R.string.font_size_level1));
        }
        return row;
    }

    private View getViewTableContent(LinearLayout row,QuestionRow questionRow, AutoTabViewHolder viewHolder){
        row.setWeightSum(1f);
        float columnWeight=questionRow.sizeColumns()/1f;
        for(Question question:questionRow.getQuestions()){
            CustomEditText customEditText;
            Spinner spinner;
            //Create view for columm
            switch (question.getOutput()){
                case Constants.NO_ANSWER:
                    addTextViewToRow(row,question,columnWeight);
                    viewHolder.addColumnComponent(null);
                    break;
                case Constants.DATE:
                case Constants.LONG_TEXT:
                case Constants.SHORT_TEXT:
                    customEditText= addEditViewToRow(row,question,columnWeight);
                    customEditText.addTextChangedListener(new TextViewListener(question));
                    viewHolder.addColumnComponent(customEditText);
                    break;
                case Constants.INT:
                    customEditText= addEditIntViewToRow(row, question, columnWeight);
                    customEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)});
                    customEditText.addTextChangedListener(new TextViewListener(question));
                    viewHolder.addColumnComponent(customEditText);
                    break;
                case Constants.POSITIVE_INT:
                    customEditText= addEditIntViewToRow(row, question, columnWeight);
                    customEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS), new MinMaxInputFilter(1, null)});
                    customEditText.addTextChangedListener(new TextViewListener(question));
                    viewHolder.addColumnComponent(customEditText);
                    break;
                case Constants.DROPDOWN_LIST:
                    spinner = addSpinnerViewToRow(row,question,columnWeight);
                    spinner.setOnItemSelectedListener(new SpinnerListener(question, new AutoTabViewHolder(spinner)));
                    viewHolder.addColumnComponent(spinner);
                    break;
                case Constants.DROPDOWN_LIST_DISABLED:
                    spinner = addSpinnerViewToRow(row,question,columnWeight);
                    spinner.setOnItemSelectedListener(new SpinnerListener(question, new AutoTabViewHolder(spinner)));
                    AutoTabLayoutUtils.autoFillAnswer(new AutoTabViewHolder(spinner), question, getContext(), inVisibilityState, this);
                    viewHolder.addColumnComponent(spinner);
                    break;
                case Constants.RADIO_GROUP_HORIZONTAL:
                case Constants.RADIO_GROUP_VERTICAL:
                    RadioGroup radioGroup = addRadioGroupViewToRow(row,question,columnWeight);
                    radioGroup.setOnCheckedChangeListener(new RadioGroupListener(question, new AutoTabViewHolder(radioGroup)));
                    viewHolder.addColumnComponent(radioGroup);
                    break;
                case Constants.SWITCH_BUTTON:
                    Switch switchButton = addSwitchViewToRow(row,question,columnWeight);
                    switchButton.setOnCheckedChangeListener(new SwitchButtonListener(question,viewHolder));
                    viewHolder.addColumnComponent(switchButton);
            }
        }
        return row;
    }

    private void addTextViewToRow(LinearLayout rowLayout, Question question, float columnWeight, Integer background, Integer fontColor, String fontName, String fontDimension){
        rowLayout.setWeightSum(columnWeight);
        CustomTextView textView = new CustomTextView(getContext());
        textView.setText(question.getForm_name());
        textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
        rowLayout.addView(textView);
        // Style customization
        if (background != null) rowLayout.setBackgroundResource(background);
        if (fontColor != null) textView.setTextColor(fontColor);
        if (fontName != null) textView.setmFontName(fontName);
        if (fontDimension != null) textView.setmDimension(fontDimension);
        if (PreferencesState.getInstance().getScale().equals(getContext().getString(R.string.font_size_system)))
            textView.setTextSize(getContext().getResources().getDimension(R.dimen.small_medium_text_size));
        else
            textView.setTextSize(PreferencesState.getInstance().getFontSize(PreferencesState.getInstance().getScale(), textView.getmDimension()));
    }

    private void addTextViewToRow(LinearLayout rowLayout, Question question, float columnWeight){
        addTextViewToRow(rowLayout, question, columnWeight, null, null, null, null);
    }

    private CustomEditText addEditViewToRow(LinearLayout rowLayout, Question question, float columnWeight){
        //TODO Add the right widget, style, setvalues, readonly, ...
        rowLayout.setWeightSum(columnWeight);
        CustomEditText editText = new CustomEditText(getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        rowLayout.addView(editText);
        return editText;
    }

    private CustomEditText addEditIntViewToRow(LinearLayout rowLayout, Question question, float columnWeight){
        CustomEditText customEditText=addEditViewToRow(rowLayout,question,columnWeight);
        customEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        return customEditText;
    }

    private Spinner addSpinnerViewToRow (LinearLayout rowLayout, Question question, float columnWeight) {
        rowLayout.setWeightSum(columnWeight);
        Spinner spinner = new Spinner(getContext(), Spinner.MODE_DROPDOWN);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP){
            spinner.setBackgroundTintMode(PorterDuff.Mode.ADD);
            spinner.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.headerColor)));
        }
        //this colors de arrow
//        spinner.getBackground().setColorFilter(getContext().getResources().getColor(R.color.headerColor), PorterDuff.Mode.SRC_ATOP);

        rowLayout.addView(spinner);
        List<Option> optionList = new ArrayList<>(question.getAnswer().getOptions());
        optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
        spinner.setAdapter(new OptionArrayAdapter(getContext(), optionList));
        return spinner;
    }

    private RadioGroup addRadioGroupViewToRow (LinearLayout rowLayout, Question question, float columnWeight) {
        rowLayout.setWeightSum(columnWeight);
        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        rowLayout.addView(radioGroup);

        int radioGroupOrientation=question.getOutput()==Constants.RADIO_GROUP_VERTICAL?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL;
        AutoTabLayoutUtils.createRadioGroupComponent(question, new AutoTabViewHolder(radioGroup), radioGroupOrientation, getInflater(), getContext());

        return radioGroup;
    }

    private Switch addSwitchViewToRow(LinearLayout rowLayout, Question question, float columnWeight){
        rowLayout.setWeightSum(columnWeight);
        Switch switchButton = new Switch(getContext());
        rowLayout.addView(switchButton);
        return switchButton;
    }

    public void setValues(AutoTabViewHolder viewHolder, QuestionRow questionRow) {
        for(int i=0;i<questionRow.sizeColumns();i++){
            View component = viewHolder.getColumnComponent(i);
            Question question = questionRow.getQuestions().get(i);
            setValues(component,question);
        }
    }

    public void setValues(View component, Question question){
        if(component==null || question==null){
            return;
        }
        setValues(new AutoTabViewHolder(component),question);
    }

    public void setValues(AutoTabViewHolder viewHolder, Question question) {
        if(viewHolder==null || question==null){
            return;
        }

        switch (question.getOutput()) {
            case Constants.DATE:
            case Constants.SHORT_TEXT:
            case Constants.INT:
            case Constants.LONG_TEXT:
            case Constants.POSITIVE_INT:
                viewHolder.setText(ReadWriteDB.readValueQuestion(question));
                break;
            case Constants.DROPDOWN_LIST:
            case Constants.DROPDOWN_LIST_DISABLED:
                viewHolder.setSpinnerSelection(ReadWriteDB.readPositionOption(question));
                List<Float> numdenum = ScoreRegister.getNumDenum(question);
                if (numdenum != null) {
                    viewHolder.setNumText(Float.toString(numdenum.get(0)));
                    viewHolder.setDenumText(Float.toString(numdenum.get(1)));
                } else {
                    viewHolder.setNumText(getContext().getString(R.string.number_zero));
                    viewHolder.setDenumText(Float.toString(ScoreRegister.calcDenum(question)));
                    viewHolder.setSpinnerSelection(0);
                }

                break;
            case Constants.RADIO_GROUP_HORIZONTAL:
            case Constants.RADIO_GROUP_VERTICAL:
                //FIXME: it is almost the same as the previous case
                Value value = question.getValueBySession();
                List<Float> numdenumradiobutton = ScoreRegister.getNumDenum(question);
                if (numdenumradiobutton == null) { //FIXME: this avoid app crash when onResume
                    break;
                }
                if (value != null) {
                    viewHolder.setRadioChecked(value.getOption());
                    viewHolder.setNumText(Float.toString(numdenumradiobutton.get(0)));
                    viewHolder.setDenumText(Float.toString(numdenumradiobutton.get(1)));
                } else {
                    viewHolder.setNumText(getContext().getString(R.string.number_zero));
                    viewHolder.setDenumText(Float.toString(ScoreRegister.calcDenum(question)));
                }
                break;
            case Constants.SWITCH_BUTTON:
                Option option = findOptionBySession(question);
                viewHolder.setSwitchOption(option);
                break;
            default:
                break;
        }
    }

    /**
     * Returns the option selected for the given question and boolean value
     * @param question
     * @param isChecked
     * @return
     */
    public static Option findSwitchOption(Question question,boolean isChecked){
        String expectedCode=String.valueOf(isChecked);
        for(Option option:question.getAnswer().getOptions()){
            if(expectedCode.equals(option.getCode())){
                return option;
            }
        }
        return null;
    }

    /**
     * Returns the selected option of the given question (for the survey in session)
     * @param question
     * @return
     */
    private Option findOptionBySession(Question question){
        Value value = question.getValueBySession();

        //real value -> real option
        if(value!=null){
            return value.getOption();
        }

        //Default 'falsy' option
        return findSwitchOption(question,false);
    }

    //////////////////////////////////////
    /////////// LISTENERS ////////////////
    //////////////////////////////////////
    private class TextViewListener implements TextWatcher {
        private boolean viewCreated;
        private Question question;

        public TextViewListener(Question question) {
            this.viewCreated = false;
            this.question = question;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!viewCreated){
                viewCreated=true;
                return;
            }
            ReadWriteDB.saveValuesText(question, s.toString());
        }
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        private boolean viewCreated;
        private AutoTabViewHolder viewHolder;
        private Question question;

        public SpinnerListener(Question question, AutoTabViewHolder viewHolder) {
            this.viewCreated = false;
            this.question = question;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if(!viewCreated){
                viewCreated = true;
                return;
            }

            Option selectedOption=(Option) ((Spinner) viewHolder.component).getItemAtPosition(pos);
            AutoTabSelectedItem autoTabSelectedItem = autoTabSelectedItemFactory.buildSelectedItem(question,selectedOption,viewHolder);
            AutoTabLayoutUtils.itemSelected(autoTabSelectedItem);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        private AutoTabViewHolder viewHolder;
        private Question question;

        public RadioGroupListener(Question question, AutoTabViewHolder viewHolder) {
            this.question = question;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(!group.isShown()){
                return;
            }

            Option selectedOption = new Option(Constants.DEFAULT_SELECT_OPTION);
            if (checkedId != -1) {
                CustomRadioButton customRadioButton = this.viewHolder.findRadioButtonById(checkedId);
                selectedOption = (Option) customRadioButton.getTag();
            }
            AutoTabSelectedItem autoTabSelectedItem = autoTabSelectedItemFactory.buildSelectedItem(question,selectedOption,viewHolder);
            AutoTabLayoutUtils.itemSelected(autoTabSelectedItem);
        }
    }

    public class SwitchButtonListener implements CompoundButton.OnCheckedChangeListener{

        private AutoTabViewHolder viewHolder;
        private Question question;

        public SwitchButtonListener(Question question, AutoTabViewHolder viewHolder) {
            this.question = question;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(!buttonView.isShown()){
                return;
            }

            //Take option
            Option selectedOption=findSwitchOption(question, isChecked);
            if(selectedOption==null){
                return;
            }
            ((Switch)viewHolder.component).setText(selectedOption.getName());
            AutoTabSelectedItem autoTabSelectedItem = autoTabSelectedItemFactory.buildSelectedItem(question,selectedOption,viewHolder);
            AutoTabLayoutUtils.itemSelected(autoTabSelectedItem);
        }
    }

}
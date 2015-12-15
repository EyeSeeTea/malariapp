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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.common.primitives.Booleans;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.AutoTabLayoutUtils;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.UncheckeableRadioButton;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Jose on 21/04/2015.
 */
public class AutoTabAdapter extends ATabAdapter {


    private final static String TAG=".AutoTabAdapter";

    //List of Headers and Questions. Each position contains an object to be showed in the listview
    List<Object> items;

    final AutoTabLayoutUtils.ScoreHolder scoreHolder = new AutoTabLayoutUtils.ScoreHolder();

    float totalNum = 0;
    float totalDenum;

    // The length of this arrays is the same that the items list. Each position indicates if the item
    // on this position is hidden (true) or visible (false)

    private final LinkedHashMap<BaseModel, Boolean> elementInvisibility = new LinkedHashMap<>();
    //private final LinkedHashMap<Object, Boolean> elementInvisibility = new LinkedHashMap<>();

    /**
     * Flag that indicates if the current survey in session is already sent or not (it affects readonly settings)
     */
    private boolean readOnly;

    AutoTabLayoutUtils.ViewHolder viewHolder = new AutoTabLayoutUtils.ViewHolder();

    public AutoTabAdapter(Tab tab, Context context) {
        this(tab, context, R.layout.form_with_score);
        if(Utils.isPictureQuestion()){
            this.items = Utils.convertTabToArray(tab);

            // Initialize the elementInvisibility HashMap by reading all questions and headers and decide
            // whether or not they must be visible
            for (int i = 0; i < getItems().size(); i++) {
                BaseModel item = getItems().get(i);
                if (item instanceof Header)
                    elementInvisibility.put((BaseModel)item, true);
                if (item instanceof Question) {
                    boolean hidden = isHidden((Question) item);
                    elementInvisibility.put(item, hidden);
                    if (!(hidden)) initScoreQuestion((Question) item);
                    else
                        ScoreRegister.addRecord((Question) item, 0F, ScoreRegister.calcDenum((Question) item));
                    Header header = ((Question) item).getHeader();
                    boolean headerVisibility = elementInvisibility.get(header);
                    elementInvisibility.put(header, headerVisibility && elementInvisibility.get(item));
                }
            }
            this.readOnly = Session.getSurvey().isSent();
        }
    }

    public AutoTabAdapter(Tab tab, Context context, int id_layout) {
        super(tab, context, id_layout);
        // Initialize the elementInvisibility HashMap by reading all questions and headers and decide
        // whether or not they must be visible
        if(!Utils.isPictureQuestion()) {
            for (int i = 0; i < getItems().size(); i++) {
                BaseModel item = getItems().get(i);
                if (item instanceof Header)
                    elementInvisibility.put(item, true);
                if (item instanceof Question) {
                    boolean hidden = AutoTabLayoutUtils.isHidden((Question) item);
                    elementInvisibility.put(item, hidden);
                    if (!(hidden)) AutoTabLayoutUtils.initScoreQuestion((Question) item, totalNum, totalDenum);
                    else ScoreRegister.addRecord((Question) item, 0F, ScoreRegister.calcDenum((Question) item));
                    Header header = ((Question) item).getHeader();
                    boolean headerVisibility = elementInvisibility.get(header);
                    elementInvisibility.put(header, headerVisibility && elementInvisibility.get(item));
                }
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
        int idLayout;
        if(Utils.isPictureQuestion()) {
            idLayout= tab.getType() == Constants.TAB_AUTOMATIC_NON_SCORED ? R.layout.form_without_score_pictureapp : R.layout.form_with_score;
        }
        else{
            idLayout = tab.getType() == Constants.TAB_AUTOMATIC_NON_SCORED ? R.layout.form_without_score : R.layout.form_with_score;
        }
        return new AutoTabAdapter(tab, context, idLayout);
    }

    @Override
    public Float getScore() {
        if (totalDenum != 0)
            return 100 * (totalNum / totalDenum);
        else return 0F;
    }

    /**
     * Do every initialization related to subscore needed before showing a tab
     */
    @Override
    public void initializeSubscore() {
        initializeScoreViews();
        setSubScoreVisibility();
        initializeDenum();
        updateScore();
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
        int visibility = (PreferencesState.getInstance().isShowNumDen()) ? View.VISIBLE : View.GONE;
        subscoreBar.setVisibility(visibility);
    }


    public void updateScore() {
        scoreHolder.totalNum.setText(Float.toString(totalNum));
        scoreHolder.totalDenum.setText(Float.toString(totalDenum));
        if (totalDenum != 0) {
            Float score = 100 * (totalNum / totalDenum);
            LayoutUtils.trafficLight(scoreHolder.score, score, scoreHolder.qualitativeScore);
            scoreHolder.score.setText(Utils.round(100 * (totalNum / totalDenum)) + " % ");
        }
        if (totalDenum == 0 && totalNum == 0) {
            scoreHolder.score.setText(getContext().getString(R.string.number_zero_percentage));
        }
    }

    private void initializeDenum() {
        float result = 0;
        int number_items = getItems().size();

        if (totalDenum == 0) {
            for (int i = 0; i < number_items; i++) {
                if (getItems().get(i) instanceof Question && !elementInvisibility.get(getItems().get(i))) {
                    Question question = (Question) getItems().get(i);
                    if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST)
                        result = result + ScoreRegister.calcDenum((Question) getItems().get(i));
                }

            }
            totalDenum = result;

        }
    }

    @Override
    public int getCount() {
        return (getItems().size() - AutoTabLayoutUtils.getHiddenCount(elementInvisibility));
    }

    @Override
    public Object getItem(int position) {
        return getItems().get(AutoTabLayoutUtils.getRealPosition(position, elementInvisibility, getItems()));
    }

    @Override
    public long getItemId(int position) {
        return getItems().get(AutoTabLayoutUtils.getRealPosition(position, elementInvisibility, getItems())).hashCode();
    }

    private void initScoreQuestion(Question question) {

        if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST || question.getAnswer().getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getAnswer().getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Float num = ScoreRegister.calcNum(question);
            Float denum = ScoreRegister.calcDenum(question);

            totalNum = totalNum + num;
            totalDenum = totalDenum + denum;

            ScoreRegister.addRecord(question, num, denum);
        }

    }

    /**
     * Get the number of elements that are hidden
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    private int getHiddenCount() {
        // using Guava library and its Booleans utility class
        return Booleans.countTrue(Booleans.toArray(elementInvisibility.values()));
    }

    /**
     * Get the number of elements that are hidden until a given position
     * @param position
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    private int getHiddenCountUpTo(int position) {
        boolean [] upper = Arrays.copyOfRange(Booleans.toArray(elementInvisibility.values()), 0, position + 1);
        int hiddens = Booleans.countTrue(upper);
        return hiddens;
    }

    /**
     * Given a desired position (that means, the position shown in the screen) of an element, get the
     * real position (that means, the position in the stored items list taking into account the hidden
     * elements)
     * @param position
     * @return the real position in the elements list
     */
    private int getRealPosition(int position){
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (elementInvisibility.get(items.get(position + diff))) i--;
        }
        return (position + diff);
    }

    /**
     * Decide whether we need or not to hide this header (if every question inside is hidden)
     * @param header header that
     * @return true if every header question is hidden, false otherwise
     */
    public boolean hideHeader(Header header) {
        // look in every question to see if every question is hidden. In case one cuestion is not hidden, we return false
        for (Question question : header.getQuestions()) {
            if (!elementInvisibility.get(question)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Given a question, make visible or invisible their children. In case all children in a header
     * became invisible, that header is also hidden
     * @param question the question whose children we want to show/hide
     * @param visible true for make them visible, false for invisible
     */
    private void toggleChildrenVisibility(Question question, boolean visible) {
        List<Question> children = question.getQuestionChildren();
        Question cachedQuestion = null;

        for (Question child : children) {
            Header childHeader = child.getHeader();
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
                    elementInvisibility.put(childHeader, hideHeader(childHeader));
            } else {
                Float denum = ScoreRegister.calcDenum(child);
                totalDenum = totalDenum + denum;
                ScoreRegister.addRecord(child, 0F, denum);
                elementInvisibility.put(childHeader, false);
            }
            cachedQuestion = question;
        }
        notifyDataSetChanged();
    }

    public void setValues(AutoTabLayoutUtils.ViewHolder viewHolder, Question question) {

        switch (question.getAnswer().getOutput()) {
            case Constants.DATE:
            case Constants.SHORT_TEXT:
            case Constants.INT:
            case Constants.LONG_TEXT:
            case Constants.POSITIVE_INT:
            case Constants.PHONE:
                ((CustomEditText) viewHolder.component).setText(ReadWriteDB.readValueQuestion(question));
                break;
            case Constants.NUMERIC_PICKER:
                try {
                    ((NumberPicker) viewHolder.component).setValue(Integer.valueOf(ReadWriteDB.readValueQuestion(question)));
                }catch(Exception e){}
                break;
            case Constants.DROPDOWN_LIST:
            case Constants.IMAGES_2:
            case Constants.IMAGES_4:
            case Constants.IMAGES_6:
                //Images_2 == case Constants.DROPDOWN_LIST_DISABLED:
                ((Spinner) viewHolder.component).setSelection(ReadWriteDB.readPositionOption(question));

                List<Float> numdenum = ScoreRegister.getNumDenum(question);
                if (numdenum != null) {
                    viewHolder.num.setText(Float.toString(numdenum.get(0)));
                    viewHolder.denum.setText(Float.toString(numdenum.get(1)));
                } else {
                    viewHolder.num.setText(getContext().getString(R.string.number_zero));
                    viewHolder.denum.setText(Float.toString(ScoreRegister.calcDenum(question)));
                    ((Spinner) viewHolder.component).setSelection(0);
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
                        if(Utils.isPictureQuestion()) {
                            ((UncheckeableRadioButton) viewHolder.component.findViewWithTag(value.getOption())).setChecked(true);
                        }else {
                            ((CustomRadioButton) viewHolder.component.findViewWithTag(value.getOption())).setChecked(true);
                        }
                        viewHolder.num.setText(Float.toString(numdenumradiobutton.get(0)));
                        viewHolder.denum.setText(Float.toString(numdenumradiobutton.get(1)));
                    } else {
                        viewHolder.num.setText(getContext().getString(R.string.number_zero));
                        viewHolder.denum.setText(Float.toString(ScoreRegister.calcDenum(question)));
                    }
                break;
            default:
                break;
        }
    }

    private boolean isHidden(Question question) {
        Question parent;
        boolean hidden = false;

        if ((parent = question.getQuestion()) != null) {
            if (parent.getValueBySession() == null)
                hidden = true;
        }

        return hidden;
    }

    private void resetTotalNumDenum(Question question) {
        List<Float> numdenum = ScoreRegister.getNumDenum(question);

        if (numdenum != null) {
            totalNum = totalNum - numdenum.get(0);
            totalDenum = totalDenum - numdenum.get(1);
        }
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

    private void autoFillAnswer(AutoTabLayoutUtils.ViewHolder viewHolder, Question question) {

        viewHolder.component.setEnabled(false);

        if (checkMatches(question))
            itemSelected(viewHolder, question, question.getAnswer().getOptions().get(0));
        else
            itemSelected(viewHolder, question, question.getAnswer().getOptions().get(1));

    }

    /**
     * Do the logic after a DDL option change
     * @param viewHolder private class that acts like a cache to quickly access the different views
     * @param question the question that changes his value
     * @param option the option that has been selected
     * Fixme it need be in the AutoTabLayoutUtils
     */
    private void itemSelected(AutoTabLayoutUtils.ViewHolder viewHolder, Question question, Option option) {
        // Write option to DB
        ReadWriteDB.saveValuesDDL(question, option);

        recalculateScores(viewHolder, question);

        if (question.hasChildren()) {
            toggleChildrenVisibility(question, option.isActiveChildren());
        }

        updateScore();
    }

    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    private void recalculateScores(AutoTabLayoutUtils.ViewHolder viewHolder, Question question) {
        Float num = ScoreRegister.calcNum(question);
        Float denum = ScoreRegister.calcDenum(question);

        viewHolder.num.setText(num.toString());
        viewHolder.denum.setText(denum.toString());

        resetTotalNumDenum(question);

        totalNum = totalNum + num;
        totalDenum = totalDenum + denum;

        ScoreRegister.addRecord(question, num, denum);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        Debug.startMethodTracing("auto_getview");
        View rowView = null;

        final Object item = getItem(position);
        Question question;
        AutoTabLayoutUtils.ViewHolder viewHolder = new AutoTabLayoutUtils.ViewHolder();

        if (item instanceof Question) {
            question = (Question) item;

            //FIXME This should be moved into its own class (Ex: ViewHolderFactory.getView(item))
            switch (question.getAnswer().getOutput()) {

                case Constants.LONG_TEXT:
                    if(Utils.isPictureQuestion())
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.longtext_picureapp, parent, question, viewHolder, position, getInflater());
                    else
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.longtext, parent, question, viewHolder, position, getInflater());
                    //Add main component and listener
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.NO_ANSWER:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.label, parent, question, viewHolder, position, getInflater());
                    break;
                case Constants.POSITIVE_INT:
                    if(Utils.isPictureQuestion())
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer_pictureapp, parent, question, viewHolder, position, getInflater());
                    else
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer, parent, question, viewHolder, position, getInflater());

                    //Add main component, set filters and listener
                    ((CustomEditText) viewHolder.component).setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS),new MinMaxInputFilter(1, null)});
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.INT:
                case Constants.PHONE:
                    if(Utils.isPictureQuestion())
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer_pictureapp, parent, question, viewHolder, position, getInflater());
                    else
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer, parent, question, viewHolder, position, getInflater());
                    //Add main component, set filters and listener
                    ((CustomEditText) viewHolder.component).setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)});
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.NUMERIC_PICKER:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.numeric_picker, parent, question, viewHolder, position, getInflater());
                    final NumberPicker numericPicker=(NumberPicker)rowView.findViewById(R.id.answer);
                    //Without setMinValue, setMaxValue, setValue in this order, the setValue is not displayed in the screen.
                    numericPicker.setMinValue(1);
                    numericPicker.setMaxValue(99999);//change for...?
                    final Question numericPickerQuestion=question;

                    //Add main component, set filters and listener
                    ((NumberPicker) viewHolder.component).setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                        boolean viewCreated = false;

                        @Override
                        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                            if (viewCreated) {
                                ReadWriteDB.saveValuesText(numericPickerQuestion, String.valueOf(newVal));
                            } else {
                                viewCreated = true;
                            }
                        }
                    });
                    break;
                case Constants.DATE:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.date, parent, question, viewHolder, position, getInflater());
                    //Add main component and listener
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.SHORT_TEXT:
                    if(Utils.isPictureQuestion())
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.shorttext_pictureapp, parent, question, viewHolder, position, getInflater());
                    else
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.shorttext, parent, question, viewHolder, position, getInflater());

                    //Add main component and listener
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.DROPDOWN_LIST:
                    if(Utils.isPictureQuestion()) {
                        createGraphicOutput(position, parent, question, viewHolder, getInflater(), getContext());
                    }
                    else{
                        rowView = AutoTabLayoutUtils.initialiseDropDown(position, parent, question, viewHolder, getInflater(), getContext());
                        // Initialise Listener
                        ((Spinner) viewHolder.component).setOnItemSelectedListener(new SpinnerListener(false, question, viewHolder));
                    }
                    break;
                case Constants.IMAGES_2:
                    //Fixme
                    //case Constants.DROPDOWN_LIST_DISABLED:
                    //in the malariapp component is a DropDown_list_disabled
                    if(Utils.isPictureQuestion()) {
                        createGraphicOutput(position, parent, question, viewHolder, getInflater(), getContext());
                    }
                    else{
                        rowView = AutoTabLayoutUtils.initialiseDropDown(position, parent, question, viewHolder, getInflater(), getContext());
                        // Initialise value depending on match question
                        AutoTabLayoutUtils.autoFillAnswer(viewHolder, scoreHolder, question, totalNum, totalDenum, getContext(), elementInvisibility);
                        break;
                    }
                case Constants.IMAGES_4:
                case Constants.IMAGES_6:
                    createGraphicOutput(position, parent, question, viewHolder, getInflater(), getContext());
                    break;
                case Constants.RADIO_GROUP_HORIZONTAL:
                    if(Utils.isPictureQuestion()) {
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());

                        AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);

                       createRadioGroupComponent(question, viewHolder, LinearLayout.HORIZONTAL);
                    }
                    else{
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());
                        AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);
                        AutoTabLayoutUtils.createRadioGroupComponent(question, viewHolder, LinearLayout.HORIZONTAL, getInflater(), getContext());
                        //Add Listener
                        ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder));
                    }
                    break;
                case Constants.RADIO_GROUP_VERTICAL:
                    if(Utils.isPictureQuestion()){
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());

                        AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);

                        createRadioGroupComponent(question, viewHolder, LinearLayout.VERTICAL);
                    }
                    else {
                        rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());
                        AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);
                        AutoTabLayoutUtils.createRadioGroupComponent(question, viewHolder, LinearLayout.HORIZONTAL, getInflater(), getContext());
                        //Add Listener
                        ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder));
                    }
                    break;

                default:
                    break;
            }

            //Put current value in the component
            setValues(viewHolder, question);
            if(Utils.isPictureQuestion()) {
                //Disables component if survey has already been sent
                updateReadOnly(viewHolder.component);
            }
            else{
                //Disables component if survey has already been sent (except match spinner that are always disabled)
                if(question.getAnswer().getOutput()==Constants.DROPDOWN_LIST_DISABLED){
                    AutoTabLayoutUtils.updateReadOnly(viewHolder.component, true);
                }else{
                    AutoTabLayoutUtils.updateReadOnly(viewHolder.component, getReadOnly());
                }
            }
        } else {
            rowView = getInflater().inflate(R.layout.headers, parent, false);
            viewHolder.statement = (CustomTextView) rowView.findViewById(R.id.headerName);
            viewHolder.statement.setText(((Header) item).getName());

        }

//        Debug.stopMethodTracing();
        return rowView;
    }

    private void createGraphicOutput(int position, ViewGroup parent, Question question, AutoTabLayoutUtils.ViewHolder viewHolder, LayoutInflater inflater, Context context) {

        View rowView = AutoTabLayoutUtils.initialiseView(R.layout.ddl, parent, question, this.viewHolder, position, getInflater());

        AutoTabLayoutUtils.initialiseScorableComponent(rowView, this.viewHolder);

        // In case the option is selected, we will need to show num/dems
        List<Option> optionList = question.getAnswer().getOptions();
        optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
        Spinner spinner = (Spinner) this.viewHolder.component;
        spinner.setAdapter(new OptionArrayAdapter(getContext(), optionList));

        //Add Listener
        if (!question.hasRelatives())
            ((Spinner) this.viewHolder.component).setOnItemSelectedListener(new SpinnerListener(false, question, this.viewHolder));
        else
            autoFillAnswer(this.viewHolder, question);
    }

    /**
     * Enables/Disables input view according to the state of the survey.
     * Sent surveys cannot be modified.
     *
     * @param view
     */
    private void updateReadOnly(View view) {
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

    private void createRadioGroupComponent(Question question, AutoTabLayoutUtils.ViewHolder viewHolder, int orientation) {
        ((RadioGroup) viewHolder.component).setOrientation(orientation);

        for (Option option : question.getAnswer().getOptions()) {
            UncheckeableRadioButton button = (UncheckeableRadioButton) getInflater().inflate(R.layout.uncheckeable_radiobutton_pictureapp, null);
            button.setOption(option);
            button.updateProperties(PreferencesState.getInstance().getScale(), this.getContext().getString(R.string.font_size_level1), this.getContext().getString(R.string.medium_font_name));
            ((RadioGroup) viewHolder.component).addView(button);
        }

        //Add Listener
        ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder));
    }

    /**
     * Set visibility of numerators and denominators depending on the user preference selected in the settings activity
     *
     * @param viewHolder view that holds the component to be more efficient
     */
    private void configureViewByPreference(AutoTabLayoutUtils.ViewHolder viewHolder) {
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

    //////////////////////////////////////
    /////////// LISTENERS ////////////////
    //////////////////////////////////////
    private class TextViewListener implements TextWatcher {
        private boolean viewCreated;
        private Question question;

        public TextViewListener(boolean viewCreated, Question question) {
            this.viewCreated = viewCreated;
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
            if (viewCreated) {
                ReadWriteDB.saveValuesText(question, s.toString());
            } else {
                viewCreated = true;
            }
        }
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        private boolean viewCreated;
        private AutoTabLayoutUtils.ViewHolder viewHolder;
        private Question question;

        public SpinnerListener(boolean viewCreated, Question question, AutoTabLayoutUtils.ViewHolder viewHolder) {
            this.viewCreated = viewCreated;
            this.question = question;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (viewCreated) {
                itemSelected(viewHolder, question, (Option) ((Spinner) viewHolder.component).getItemAtPosition(pos));
                if (!Utils.isPictureQuestion())
                    notifyDataSetChanged();
            } else {
                viewCreated = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        private AutoTabLayoutUtils.ViewHolder viewHolder;
        private Question question;

        public RadioGroupListener(Question question, AutoTabLayoutUtils.ViewHolder viewHolder) {
            this.question = question;
            this.viewHolder = viewHolder;
        }


        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(!group.isShown()){
                return;
            }

            Option option = new Option(Constants.DEFAULT_SELECT_OPTION);
            if (checkedId != -1) {
                if(Utils.isPictureQuestion()) {
                    UncheckeableRadioButton uncheckeableRadioButton = (UncheckeableRadioButton) (this.viewHolder.component).findViewById(checkedId);
                    option = (Option) uncheckeableRadioButton.getTag();
                }
                else{
                    CustomRadioButton customRadioButton = this.viewHolder.findRadioButtonById(checkedId);
                    option = (Option) customRadioButton.getTag();
                }
            }
            if(Utils.isPictureQuestion()) {
                itemSelected(viewHolder, question, option);
            }else{
                if (AutoTabLayoutUtils.itemSelected(viewHolder, scoreHolder, question, option, totalNum, totalDenum, getContext(), elementInvisibility))
                    notifyDataSetChanged();
            }
        }
    }

}
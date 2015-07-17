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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.common.primitives.Booleans;

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
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.EditCard;
import org.eyeseetea.malariacare.views.TextCard;
import org.eyeseetea.malariacare.views.UncheckeableRadioButton;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Jose on 21/04/2015.
 */
public class AutoTabAdapter extends BaseAdapter implements ITabAdapter {

    private final static String TAG=".AutoTabAdapter";

    //List of Headers and Questions. Each position contains an object to be showed in the listview
    List<Object> items;
    Tab tab;

    LayoutInflater lInflater;

    final ScoreHolder scoreHolder = new ScoreHolder();

    float totalNum = 0;
    float totalDenum;

    private final Context context;

    // The length of this arrays is the same that the items list. Each position indicates if the item
    // on this position is hidden (true) or visible (false)

    private final LinkedHashMap<Object, Boolean> elementInvisibility = new LinkedHashMap<>();

    int id_layout;

    /**
     * Flag that indicates if the current survey in session is already sent or not (it affects readonly settings)
     */
    private boolean readOnly;

    //Store the Views references for each row (to avoid many calls to getViewById)
    static class ViewHolder {
        //Label
        public TextCard statement;

        // Main component in the row: Spinner, EditText or RadioGroup
        public View component;

        public TextCard num;
        public TextCard denum;
        public int type;
    }

    //Store the views references for each view in the footer
    static class ScoreHolder {
        public TextCard subtotalscore;
        public TextCard score;
        public TextCard totalNum;
        public TextCard totalDenum;
        public TextCard qualitativeScore;
    }

    public AutoTabAdapter(Tab tab, Context context) {
        this.lInflater = LayoutInflater.from(context);
        this.items = Utils.convertTabToArray(tab);
        this.context = context;
        this.id_layout = R.layout.form_with_score;
        this.tab = tab;

        // Initialize the elementInvisibility HashMap by reading all questions and headers and decide
        // whether or not they must be visible
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof Header)
                elementInvisibility.put(item, true);
            if (item instanceof Question) {
                boolean hidden = isHidden((Question) item);
                elementInvisibility.put(item, hidden);
                if (!(hidden)) initScoreQuestion((Question) item);
                else ScoreRegister.addRecord((Question) item, 0F, ScoreRegister.calcDenum((Question) item));
                Header header = ((Question) item).getHeader();
                boolean headerVisibility = elementInvisibility.get(header);
                elementInvisibility.put(header, headerVisibility && elementInvisibility.get(item));
            }
        }

        this.readOnly = Session.getSurvey().isSent();
    }

    public AutoTabAdapter(Tab tab, Context context, int id_layout) {
        this(tab, context);
        this.id_layout = id_layout;
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

    public Tab getTab() {
        return this.tab;
    }

    @Override
    public BaseAdapter getAdapter() {
        return this;
    }

    @Override
    public int getLayout() {
        return id_layout;
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
        scoreHolder.score = (TextCard) ((Activity) context).findViewById(R.id.score);
        scoreHolder.totalDenum = (TextCard) ((Activity) context).findViewById(R.id.totalDen);
        scoreHolder.totalNum = (TextCard) ((Activity) context).findViewById(R.id.totalNum);
        scoreHolder.subtotalscore = (TextCard) ((Activity) context).findViewById(R.id.subtotalScoreText);
        scoreHolder.qualitativeScore = (TextCard) ((Activity) context).findViewById(R.id.qualitativeScore);
    }

    /**
     * set subscore bar visibility depending on the show/hide num/dems user settings
     */
    private void setSubScoreVisibility(){
        ViewGroup subscoreBar = (ViewGroup) ((Activity)context).findViewById(R.id.subscore_bar);
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
            scoreHolder.score.setText(this.context.getString(R.string.number_zero_percentage));
        }
    }

    private void initializeDenum() {
        float result = 0;
        int number_items = items.size();

        if (totalDenum == 0) {
            for (int i = 0; i < number_items; i++) {
                if (items.get(i) instanceof Question && !elementInvisibility.get(items.get(i))) {
                    Question question = (Question) items.get(i);
                    if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST)
                        result = result + ScoreRegister.calcDenum((Question) items.get(i));
                }

            }
            totalDenum = result;

        }
    }

    @Override
    public String getName() {
        return tab.getName();
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
        boolean [] upper = Arrays.copyOfRange(Booleans.toArray(elementInvisibility.values()), 0, position+1);
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

    @Override
    public int getCount() {
        return (items.size() - getHiddenCount());
    }

    @Override
    public Object getItem(int position) {
        return items.get(getRealPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return items.get(getRealPosition(position)).hashCode();
    }

    /**
     * Given a question, make visible or invisible their children. In case all children in a header
     * became invisible, that header is also hidden
     * @param question the question whose children we want to show/hide
     * @param visible true for make them visible, false for invisible
     */
    private void
    toggleChildrenVisibility(Question question, boolean visible) {
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
                if(cachedQuestion == null || (cachedQuestion.getHeader().getId() != child.getHeader().getId()))
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

    public void setValues(ViewHolder viewHolder, Question question) {

        switch (question.getAnswer().getOutput()) {
            case Constants.DATE:
            case Constants.SHORT_TEXT:
            case Constants.INT:
            case Constants.LONG_TEXT:
            case Constants.POSITIVE_INT:
                ((EditCard) viewHolder.component).setText(ReadWriteDB.readValueQuestion(question));
                break;
            case Constants.DROPDOWN_LIST:

                ((Spinner) viewHolder.component).setSelection(ReadWriteDB.readPositionOption(question));

                List<Float> numdenum = ScoreRegister.getNumDenum(question);
                if (numdenum != null) {
                    viewHolder.num.setText(Float.toString(numdenum.get(0)));
                    viewHolder.denum.setText(Float.toString(numdenum.get(1)));
                } else {
                    viewHolder.num.setText(this.context.getString(R.string.number_zero));
                    viewHolder.denum.setText(Float.toString(ScoreRegister.calcDenum(question)));
                    ((Spinner) viewHolder.component).setSelection(0);
                }

                break;
            case Constants.RADIO_GROUP_HORIZONTAL:
            case Constants.RADIO_GROUP_VERTICAL:
                //FIXME: it is almost the same as the previous case
                Value value = question.getValueBySession();
                List<Float> numdenumradiobutton = ScoreRegister.getNumDenum(question);
                if (value != null) {
                    ((UncheckeableRadioButton) viewHolder.component.findViewWithTag(value.getOption())).setChecked(true);

                    viewHolder.num.setText(Float.toString(numdenumradiobutton.get(0)));
                    viewHolder.denum.setText(Float.toString(numdenumradiobutton.get(1)));
                } else {
                    viewHolder.num.setText(this.context.getString(R.string.number_zero));
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

    private void autoFillAnswer(ViewHolder viewHolder, Question question) {

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
     */
    private void itemSelected(ViewHolder viewHolder, Question question, Option option) {
        // Write option to DB
        ReadWriteDB.saveValuesDDL(question, option);

        recalculateScores(viewHolder, question);

        if (question.hasChildren()) {
            toggleChildrenVisibility(question, option.is(context.getString(R.string.yes)));
        }

        updateScore();
    }

    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    private void recalculateScores(ViewHolder viewHolder, Question question) {
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
        ViewHolder viewHolder = new ViewHolder();

        if (item instanceof Question) {
            question = (Question) item;

            //FIXME This should be moved into its own class (Ex: ViewHolderFactory.getView(item))
            switch (question.getAnswer().getOutput()) {

                case Constants.LONG_TEXT:
                    rowView = initialiseView(R.layout.longtext, parent, question, viewHolder, position);

                    //Add main component and listener
                    ((EditCard) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.NO_ANSWER:
                    rowView = initialiseView(R.layout.label, parent, question, viewHolder, position);
                    break;
                case Constants.POSITIVE_INT:
                    rowView = initialiseView(R.layout.integer, parent, question, viewHolder, position);

                    //Add main component, set filters and listener
                    ((EditCard) viewHolder.component).setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(Constants.MAX_INT_CHARS),
                            new MinMaxInputFilter(1, null)
                    });
                    ((EditCard) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.INT:
                    rowView = initialiseView(R.layout.integer, parent, question, viewHolder, position);

                    //Add main component, set filters and listener
                    ((EditCard) viewHolder.component).setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)
                    });
                    ((EditCard) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.DATE:
                    rowView = initialiseView(R.layout.date, parent, question, viewHolder, position);

                    //Add main component and listener
                    ((EditCard) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.SHORT_TEXT:
                    rowView = initialiseView(R.layout.shorttext, parent, question, viewHolder, position);

                    //Add main component and listener
                    ((EditCard) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.DROPDOWN_LIST:
                    rowView = initialiseView(R.layout.ddl, parent, question, viewHolder, position);

                    initialiseScorableComponent(rowView, viewHolder);

                    // In case the option is selected, we will need to show num/dems
                    List<Option> optionList = question.getAnswer().getOptions();
                    optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
                    Spinner spinner = (Spinner) viewHolder.component;
                    spinner.setAdapter(new OptionArrayAdapter(context, optionList));

                    //Add Listener
                    if (!question.hasRelatives())
                        ((Spinner) viewHolder.component).setOnItemSelectedListener(new SpinnerListener(false, question, viewHolder));
                    else
                        autoFillAnswer(viewHolder, question);
                    break;
                case Constants.RADIO_GROUP_HORIZONTAL:
                    rowView = initialiseView(R.layout.radio, parent, question, viewHolder, position);

                    initialiseScorableComponent(rowView, viewHolder);

                    createRadioGroupComponent(question, viewHolder, LinearLayout.HORIZONTAL);
                    break;
                case Constants.RADIO_GROUP_VERTICAL:
                    rowView = initialiseView(R.layout.radio, parent, question, viewHolder, position);

                    initialiseScorableComponent(rowView, viewHolder);

                    createRadioGroupComponent(question, viewHolder, LinearLayout.VERTICAL);
                    break;

                default:
                    break;
            }

            //Put current value in the component
            setValues(viewHolder, question);
            //Disables component if survey has already been sent
            updateReadOnly(viewHolder.component);
        } else {
            rowView = lInflater.inflate(R.layout.headers, parent, false);
            viewHolder.statement = (TextCard) rowView.findViewById(R.id.headerName);
            viewHolder.statement.setText(((Header) item).getName());

        }

//        Debug.stopMethodTracing();
        return rowView;
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

    private View initialiseView(int resource, ViewGroup parent, Question question, ViewHolder viewHolder, int position) {
        View rowView = lInflater.inflate(resource, parent, false);
        if (question.hasChildren())
            rowView.setBackgroundResource(R.drawable.background_parent);
        else
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        viewHolder.component = rowView.findViewById(R.id.answer);
        viewHolder.statement = (TextCard) rowView.findViewById(R.id.statement);
        viewHolder.statement.setText(question.getForm_name());

        return rowView;
    }

    private void initialiseScorableComponent(View rowView, ViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (TextCard) rowView.findViewById(R.id.num);
        viewHolder.denum = (TextCard) rowView.findViewById(R.id.den);

        configureViewByPreference(viewHolder);
    }

    private void createRadioGroupComponent(Question question, ViewHolder viewHolder, int orientation) {
        ((RadioGroup) viewHolder.component).setOrientation(orientation);

        for (Option option : question.getAnswer().getOptions()) {
            UncheckeableRadioButton button = (UncheckeableRadioButton) lInflater.inflate(R.layout.uncheckeable_radiobutton, null);
            button.setOption(option);
            button.updateProperties(PreferencesState.getInstance().getScale(), this.context.getString(R.string.font_size_level1), this.context.getString(R.string.medium_font_name));
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
    private void configureViewByPreference(ViewHolder viewHolder) {
        int visibility = View.GONE;
        float statementWeight = 0.7f;
        float componentWeight = 0.3f;
        float numDenWeight = 0.0f;

        if (PreferencesState.getInstance().isShowNumDen()) {
            visibility = View.VISIBLE;
            statementWeight = 0.5f;
            componentWeight = 0.2f;
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
        private ViewHolder viewHolder;
        private Question question;

        public SpinnerListener(boolean viewCreated, Question question, ViewHolder viewHolder) {
            this.viewCreated = viewCreated;
            this.question = question;
            this.viewHolder = viewHolder;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (viewCreated) {
                itemSelected(viewHolder, question, (Option) ((Spinner) viewHolder.component).getItemAtPosition(pos));
                //if (question.belongsToMasterQuestions())
                  //  notifyDataSetChanged();
            } else {
                viewCreated = true;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        private ViewHolder viewHolder;
        private Question question;

        public RadioGroupListener(Question question, ViewHolder viewHolder) {
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
                UncheckeableRadioButton uncheckeableRadioButton = (UncheckeableRadioButton) (this.viewHolder.component).findViewById(checkedId);
                option = (Option) uncheckeableRadioButton.getTag();
            }
            itemSelected(viewHolder, question, option);
        }
    }

}
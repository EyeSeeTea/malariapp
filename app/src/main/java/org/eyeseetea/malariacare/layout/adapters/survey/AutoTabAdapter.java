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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

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
import org.eyeseetea.malariacare.views.UncheckeableRadioButton;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;

import java.util.List;

/**
 * Created by Jose on 21/04/2015.
 */
public class AutoTabAdapter extends BaseAdapter implements ITabAdapter {

    //List of Headers and Questions. Each position contains an object to be showed in the listview
    List<Object> items;
    Tab tab;

    LayoutInflater lInflater;

    final ScoreHolder scoreHolder = new ScoreHolder();

    float totalNum = 0;
    float totalDenum;

    private final Context context;

    //The length of this arrays is the same that the items list. Each position indicates if the item on this position is visible or not
    private final boolean[] hidden;

    int id_layout;

    //Store the Views references for each row (to avoid many calls to getViewById)
    static class ViewHolder {
        //Label
        public TextView statement;
//        public Spinner spinner;
//        public EditText answer;
//        public RadioGroup radioGroup;

        // Main component in the row: Spinner, EditText or RadioGroup
        public View component;

        public TextView num;
        public TextView denum;
        public int type;
    }

    //Store the views references for each view in the footer
    static class ScoreHolder {
        public TextView subtotalscore;
        public TextView score;
        public TextView totalNum;
        public TextView totalDenum;
        public TextView qualitativeScore;
    }

    public AutoTabAdapter(Tab tab, Context context) {
        this.lInflater = LayoutInflater.from(context);
        this.items = Utils.convertTabToArray(tab);
        this.context = context;
        this.id_layout = R.layout.form_with_score;
        this.tab = tab;

        hidden = new boolean[items.size()];

        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof Header)
                hidden[i] = true;
            if (item instanceof Question) {
                if (!(hidden[i] = isHidden((Question) item))) {
                    initScoreQuestion((Question) item);
                } else ScoreRegister.addRecord((Question) item, 0F, calcDenum((Question) item));
                hidden[items.indexOf(((Question) item).getHeader())] = hidden[items.indexOf(((Question) item).getHeader())]
                        && hidden[i];
            }
        }
    }

    public AutoTabAdapter(Tab tab, Context context, int id_layout) {
        this(tab, context);
        this.id_layout = id_layout;
    }

    /**
     * Factory method to build a scored/non scored layout according to tab type.
     * @param tab
     * @param context
     * @return
     */
    public static AutoTabAdapter build(Tab tab, Context context){
        int idLayout=tab.getType()==Constants.TAB_AUTOMATIC_NON_SCORED?R.layout.form_without_score:R.layout.form_with_score;
        return new AutoTabAdapter(tab, context, idLayout);
    }

    public Tab getTab(){
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

    @Override
    public void initializeSubscore() {
        initializeScoreViews();
        initializeDenum();
        updateScore();
    }

    private void initializeScoreViews() {
        scoreHolder.score = (TextView) ((Activity) context).findViewById(R.id.score);
        scoreHolder.totalDenum = (TextView) ((Activity) context).findViewById(R.id.totalDen);
        scoreHolder.totalNum = (TextView) ((Activity) context).findViewById(R.id.totalNum);
        scoreHolder.subtotalscore = (TextView) ((Activity) context).findViewById(R.id.subtotalScoreText);
        scoreHolder.qualitativeScore = (TextView) ((Activity) context).findViewById(R.id.qualitativeScore);
        RelativeLayout space = (RelativeLayout) (((Activity) context).findViewById(R.id.space));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        int visibility=View.GONE;
        float weightAccumulatedTextParent=0.0f;
        float weightSpace=0.45f;
        float weightNumDen=0.0f;

        if (PreferencesState.getInstance().isShowNumDen()) {
            visibility=View.VISIBLE;
            weightAccumulatedTextParent=0.25f;
            weightSpace=0f;
            weightNumDen=0.1f;
        }

        scoreHolder.totalDenum.setVisibility(visibility);
        scoreHolder.totalNum.setVisibility(visibility);
        (((Activity) context).findViewById(R.id.accumulatedText)).setVisibility(visibility);

        ((RelativeLayout) (((Activity) context).findViewById(R.id.accumulatedText)).getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, weightAccumulatedTextParent));
        space.setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, weightSpace));
        ((RelativeLayout) scoreHolder.totalNum.getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, weightNumDen));
        ((RelativeLayout) scoreHolder.totalDenum.getParent()).setLayoutParams(new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT, weightNumDen));
    }

    @Override
    public String getName() {
        return tab.getName();
    }

    public void updateScore() {
        scoreHolder.totalNum.setText(Float.toString(totalNum));
        scoreHolder.totalDenum.setText(Float.toString(totalDenum));
        if (totalDenum != 0) {
            Float score = 100 * (totalNum / totalDenum);
            LayoutUtils.trafficLight(scoreHolder.score, score, scoreHolder.qualitativeScore);
            scoreHolder.score.setText(Utils.round(100 * (totalNum / totalDenum))+" % ");
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
                if (items.get(i) instanceof Question && !hidden[i]) {
                    Question question = (Question) items.get(i);
                    if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST)
                        result = result + calcDenum((Question) items.get(i));
                }

            }
            totalDenum = result;

        }
    }

    private void initScoreQuestion(Question question) {

        if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST || question.getAnswer().getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getAnswer().getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Float num = calcNum(question);
            Float denum = calcDenum(question);

            totalNum = totalNum + num;
            totalDenum = totalDenum + denum;

            ScoreRegister.addRecord(question, num, denum);
        }

    }

    private int getHiddenCount() {
        int count = 0;

        for (int i = 0; i < items.size(); i++)
            if (hidden[i]) count++;

        return count;
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;

        for (int i = 0; i <= location; i++)
            if (hidden[i]) count++;

        return count;
    }

    private int getRealPosition(int position) {
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (hidden[position + diff]) i--;
        }
        return (position + diff);
    }

    public boolean isHeaderHide(Header h) {
        boolean hide = true;

        for (Question question : h.getQuestions()) {
            if (!hidden[items.indexOf(question)]) {
                hide = false;
                break;
            }
        }
        return hide;
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

    private float calcNum(Question question) {
        float result = 0;
        if (question.getValueBySession() != null) {
            Option op = question.getOptionBySession();
            result = question.getNumerator_w() * op.getFactor();
        }

        return result;
    }

    private float calcDenum(Question question) {
        float result = 0;

        if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST || question.getAnswer().getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getAnswer().getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Option option = question.getOptionBySession();
            if (option != null) {
                return calcDenum(option.getFactor(), question);
            } else {
                result = calcDenum(0, question);
            }
        }

        return result;
    }

    private float calcDenum(float factor, Question question) {
        float result = 0;
        float num = question.getNumerator_w();
        float denum = question.getDenominator_w();

        if (num == denum)
            result = denum;
        if (num == 0 && denum != 0)
            result = factor * denum;

        return result;
    }

    private void updateQuestionsVisibility(Question question, boolean show) {
        List<Question> children = question.getQuestionChildren();

        for (Question child : children) {
            hidden[items.indexOf(child)] = !show;
            if (!show) {
                List<Float> numdenum = ScoreRegister.getNumDenum(child);
                if (numdenum != null) {
                    totalDenum = totalDenum - numdenum.get(1);
                    totalNum = totalNum - numdenum.get(0);
                    ScoreRegister.deleteRecord(child);
                }
                ReadWriteDB.resetValue(child);
                hidden[items.indexOf(child.getHeader())] = isHeaderHide(child.getHeader());
            } else {
                Float denum = calcDenum(child);
                totalDenum = totalDenum + denum;
                ScoreRegister.addRecord(child, 0F, denum);
                hidden[items.indexOf(child.getHeader())] = false;
            }
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
                ((EditText) viewHolder.component).setText(ReadWriteDB.readValueQuestion(question));
                break;
            case Constants.DROPDOWN_LIST:

                ((Spinner) viewHolder.component).setSelection(ReadWriteDB.readPositionOption(question));

                List<Float> numdenum = ScoreRegister.getNumDenum(question);
                if (numdenum != null) {
                    viewHolder.num.setText(Float.toString(numdenum.get(0)));
                    viewHolder.denum.setText(Float.toString(numdenum.get(1)));
                } else {
                    viewHolder.num.setText(this.context.getString(R.string.number_zero));
                    viewHolder.denum.setText(Float.toString(calcDenum(question)));
                    ((Spinner) viewHolder.component).setSelection(0);
                }

                break;
            case Constants.RADIO_GROUP_HORIZONTAL:
            case Constants.RADIO_GROUP_VERTICAL:
                //FIXME: it is almost the same as the previous case
                Value value = question.getValueBySession();
                List<Float> numdenumradiobutton = ScoreRegister.getNumDenum(question);
                if (value != null) {
                    ((RadioButton)  viewHolder.component.findViewWithTag(value.getOption())).setChecked(true);

                    viewHolder.num.setText(Float.toString(numdenumradiobutton.get(0)));
                    viewHolder.denum.setText(Float.toString(numdenumradiobutton.get(1)));
                } else {
                    viewHolder.num.setText(this.context.getString(R.string.number_zero));
                    viewHolder.denum.setText(Float.toString(calcDenum(question)));
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

        ((Spinner) viewHolder.component).setEnabled(false);

        if (checkMatches(question))
            itemSelected(viewHolder, question, question.getAnswer().getOptions().get(0));
        else
            itemSelected(viewHolder, question, question.getAnswer().getOptions().get(1));

    }

    private void itemSelected(ViewHolder viewHolder, Question question, Option option) {

        ReadWriteDB.saveValuesDDL(question, option);

        recalculateScores(viewHolder, question);

        if (question.hasChildren()) {

            if (option.getName().equals(this.context.getString(R.string.yes)))
                updateQuestionsVisibility(question, true);
            else
                updateQuestionsVisibility(question, false);

        }

        updateScore();

    }

    private void recalculateScores(ViewHolder viewHolder, Question question) {
        Float num = calcNum(question);
        Float denum = calcDenum(question);

        viewHolder.num.setText(num.toString());
        viewHolder.denum.setText(denum.toString());

        resetTotalNumDenum(question);

        totalNum = totalNum + num;
        totalDenum = totalDenum + denum;

        ScoreRegister.addRecord(question, num, denum);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        Question question;
        ViewHolder viewHolder = new ViewHolder();

        if (item instanceof Header) {
            rowView = lInflater.inflate(R.layout.headers, parent, false);
            viewHolder.statement = (TextView) rowView.findViewById(R.id.headerName);
            viewHolder.statement.setText(((Header) item).getName());
        } else {

            question = (Question) item;

            //FIXME This should be moved into its own class (Ex: ViewHolderFactory.getView(item))
            switch (question.getAnswer().getOutput()) {

                case Constants.LONG_TEXT:
                    rowView = initialiseView(R.layout.longtext, parent, question, viewHolder, position);

                    //Add main component and listener
                    ((EditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.NO_ANSWER:
                    rowView = initialiseView(R.layout.label, parent, question, viewHolder, position);
                    break;
                case Constants.POSITIVE_INT:
                    rowView = initialiseView(R.layout.integer, parent, question, viewHolder, position);

                    //Add main component, set filters and listener
                    ((EditText) viewHolder.component).setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(Constants.MAX_INT_CHARS),
                            new MinMaxInputFilter(1,null)
                    });
                    ((EditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.INT:
                    rowView = initialiseView(R.layout.integer, parent, question, viewHolder, position);

                    //Add main component, set filters and listener
                    ((EditText) viewHolder.component).setFilters(new InputFilter[]{
                            new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)
                    });
                    ((EditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.DATE:
                    rowView = initialiseView(R.layout.date, parent, question, viewHolder, position);

                    //Add main component and listener
                    ((EditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.SHORT_TEXT:
                    rowView = initialiseView(R.layout.shorttext, parent, question, viewHolder, position);

                    //Add main component and listener
                    ((EditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
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

            setValues(viewHolder, question);
        }

        return rowView;
    }

    private View initialiseView(int resource, ViewGroup parent, Question question, ViewHolder viewHolder, int position) {
        View rowView = lInflater.inflate(resource, parent, false);
        if (question.hasChildren())
            rowView.setBackgroundResource(R.drawable.background_parent);
        else
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        viewHolder.component = rowView.findViewById(R.id.answer);
        viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
        viewHolder.statement.setText(question.getForm_name());

        return rowView;
    }

    private void initialiseScorableComponent(View rowView, ViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (TextView) rowView.findViewById(R.id.num);
        viewHolder.denum = (TextView) rowView.findViewById(R.id.den);

        configureViewByPreference(viewHolder);
    }

    private void createRadioGroupComponent(Question question, ViewHolder viewHolder, int orientation) {
        ((RadioGroup) viewHolder.component).setOrientation(orientation);

        for (Option option : question.getAnswer().getOptions()) {
            // FIXME: here we need to provide the attrs values for adapting the view
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
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
     * @param viewHolder view that holds the component to be more efficient
     */
    private void configureViewByPreference(ViewHolder viewHolder) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        int visibility=View.GONE;
        float statementWeight=0.7f;
        float componentWeight=0.3f;
        float numDenWeight=0.0f;

        if(PreferencesState.getInstance().isShowNumDen()){
            visibility=View.VISIBLE;
            statementWeight=0.5f;
            componentWeight=0.2f;
            numDenWeight=0.15f;
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
                if (question.belongsToMasterQuestions())
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
        private ViewHolder viewHolder;
        private Question question;

        public RadioGroupListener(Question question, ViewHolder viewHolder) {
            this.question = question;
            this.viewHolder = viewHolder;
        }


        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            if (group.isShown()) {

                Option option = new Option(Constants.DEFAULT_SELECT_OPTION);
                if (checkedId != -1) {
                    RadioButton radioButton = (RadioButton) ((RadioGroup) this.viewHolder.component).findViewById(checkedId);
                    option = (Option) radioButton.getTag();
                }
                itemSelected(viewHolder, question, option);
            }

        }
    }

}
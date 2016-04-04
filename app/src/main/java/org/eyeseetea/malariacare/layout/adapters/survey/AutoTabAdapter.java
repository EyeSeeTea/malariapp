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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.AutoTabLayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.MinMaxInputFilter;

import java.util.LinkedHashMap;
import java.util.List;


public class AutoTabAdapter extends ATabAdapter {

    private final static String TAG=".AutoTabAdapter";

    final AutoTabLayoutUtils.ScoreHolder scoreHolder = new AutoTabLayoutUtils.ScoreHolder();

    float totalNum = 0;
    float totalDenum;

    // The length of this arrays is the same that the items list. Each position indicates if the item
    // on this position is hidden (true) or visible (false)
    private final LinkedHashMap<BaseModel, Boolean> elementInvisibility = new LinkedHashMap<>();

    public AutoTabAdapter(Tab tab, Context context) {
        this(tab, context, R.layout.form_with_score);
    }

    public AutoTabAdapter(Tab tab, Context context, int id_layout) {
        super(tab, context, id_layout);

        // Initialize the elementInvisibility HashMap by reading all questions and headers and decide
        // whether or not they must be visible
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
        //int visibility = (PreferencesState.getInstance().isShowNumDen()) ? View.VISIBLE : View.GONE;
        subscoreBar.setVisibility(View.GONE);
    }


    private void initializeDenum() {
        float result = 0;
        int number_items = getItems().size();

        if (totalDenum == 0) {
            for (int i = 0; i < number_items; i++) {
                if (getItems().get(i) instanceof Question && !elementInvisibility.get(getItems().get(i))) {
                    Question question = (Question) getItems().get(i);
                    if (question.getOutput() == Constants.DROPDOWN_LIST)
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        Question question;
        AutoTabLayoutUtils.ViewHolder viewHolder = new AutoTabLayoutUtils.ViewHolder();

        if (item instanceof Question) {
            question = (Question) item;

            //FIXME This should be moved into its own class (Ex: ViewHolderFactory.getView(item))
            switch (question.getOutput()) {

                case Constants.LONG_TEXT:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.longtext, parent, question, viewHolder, position, getInflater());
                    //Add main component and listener
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.NO_ANSWER:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.label, parent, question, viewHolder, position, getInflater());
                    break;
                case Constants.POSITIVE_INT:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer, parent, question, viewHolder, position, getInflater());
                    //Add main component, set filters and listener
                    ((CustomEditText) viewHolder.component).setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS),new MinMaxInputFilter(1, null)});
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.INT:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.integer, parent, question, viewHolder, position, getInflater());
                    //Add main component, set filters and listener
                    ((CustomEditText) viewHolder.component).setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)});
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;
                case Constants.DATE:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.date, parent, question, viewHolder, position, getInflater());
                    //Add main component and listener
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.SHORT_TEXT:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.shorttext, parent, question, viewHolder, position, getInflater());
                    //Add main component and listener
                    ((CustomEditText) viewHolder.component).addTextChangedListener(new TextViewListener(false, question));
                    break;

                case Constants.DROPDOWN_LIST:
                    rowView = AutoTabLayoutUtils.initialiseDropDown(position, parent, question, viewHolder, getInflater(), getContext());
                    // Initialise Listener
                    ((Spinner) viewHolder.component).setOnItemSelectedListener(new SpinnerListener(false, question, viewHolder, this));
                    break;
                case Constants.DROPDOWN_LIST_DISABLED:
                    rowView = AutoTabLayoutUtils.initialiseDropDown(position, parent, question, viewHolder, getInflater(), getContext());
                    // Initialise value depending on match question
                    AutoTabLayoutUtils.autoFillAnswer(viewHolder, scoreHolder, question, totalNum, totalDenum, getContext(), elementInvisibility, this);
                    break;
                case Constants.RADIO_GROUP_HORIZONTAL:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());
                    AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);
                    AutoTabLayoutUtils.createRadioGroupComponent(question, viewHolder, LinearLayout.HORIZONTAL, getInflater(), getContext());
                    //Add Listener
                    ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder, this));
                    break;
                case Constants.RADIO_GROUP_VERTICAL:
                    rowView = AutoTabLayoutUtils.initialiseView(R.layout.radio, parent, question, viewHolder, position, getInflater());
                    AutoTabLayoutUtils.initialiseScorableComponent(rowView, viewHolder);
                    AutoTabLayoutUtils.createRadioGroupComponent(question, viewHolder, LinearLayout.VERTICAL, getInflater(), getContext());
                    //Add Listener
                    ((RadioGroup) viewHolder.component).setOnCheckedChangeListener(new RadioGroupListener(question, viewHolder, this));
                    break;

                default:
                    break;
            }

            //Put current value in the component
            setValues(viewHolder, question);

            //Disables component if survey has already been sent (except match spinner that are always disabled)
            if(question.getOutput()==Constants.DROPDOWN_LIST_DISABLED){
                AutoTabLayoutUtils.updateReadOnly(viewHolder.component, true);
            }else{
                AutoTabLayoutUtils.updateReadOnly(viewHolder.component, getReadOnly());
            }

        } else {
            rowView = getInflater().inflate(R.layout.headers, parent, false);
            viewHolder.statement = (CustomTextView) rowView.findViewById(R.id.headerName);
            viewHolder.statement.setText(((Header) item).getName());

        }

        return rowView;
    }

    public void setValues(AutoTabLayoutUtils.ViewHolder viewHolder, Question question) {

        switch (question.getOutput()) {
            case Constants.DATE:
            case Constants.SHORT_TEXT:
            case Constants.INT:
            case Constants.LONG_TEXT:
            case Constants.POSITIVE_INT:
                ((CustomEditText) viewHolder.component).setText(ReadWriteDB.readValueQuestion(question));
                break;
            case Constants.DROPDOWN_LIST:
            case Constants.DROPDOWN_LIST_DISABLED:

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
                    if(value.getOption()==null){
                        Log.d(TAG, "Error loading value survey:" + value.getSurvey().getId_survey()+" Value:"+value.toString());
                        break;
                    }
                    ((CustomRadioButton) viewHolder.component.findViewWithTag(value.getOption())).setChecked(true);

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
        private AutoTabAdapter adapter;

        public SpinnerListener(boolean viewCreated, Question question, AutoTabLayoutUtils.ViewHolder viewHolder, AutoTabAdapter adapter) {
            this.viewCreated = viewCreated;
            this.question = question;
            this.viewHolder = viewHolder;
            this.adapter = adapter;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (viewCreated) {
                if (AutoTabLayoutUtils.itemSelected(viewHolder, scoreHolder, question, (Option) ((Spinner) viewHolder.component).getItemAtPosition(pos), totalNum, totalDenum, getContext(), elementInvisibility, adapter))
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
        private AutoTabAdapter adapter;

        public RadioGroupListener(Question question, AutoTabLayoutUtils.ViewHolder viewHolder, AutoTabAdapter adapter) {
            this.question = question;
            this.viewHolder = viewHolder;
            this.adapter = adapter;
        }


        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(!group.isShown()){
                return;
            }

            Option option = new Option(Constants.DEFAULT_SELECT_OPTION);
            if (checkedId != -1) {
                CustomRadioButton customRadioButton = this.viewHolder.findRadioButtonById(checkedId);
                option = (Option) customRadioButton.getTag();
            }
            AutoTabLayoutUtils.itemSelected(viewHolder, scoreHolder, question, option, totalNum, totalDenum, getContext(), elementInvisibility, adapter);
        }
    }

}
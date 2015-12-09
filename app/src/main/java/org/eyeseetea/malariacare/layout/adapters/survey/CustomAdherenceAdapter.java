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
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

/**
 * Created by Jose on 24/04/2015.
 */
public class CustomAdherenceAdapter extends ATabAdapter {

    boolean visible = false;

    float denum = 20;
    float num = 0;

    int position_secondheader = 0;

    int []scores;

    static class ViewHolder {
        public CustomTextView number;
        public CustomEditText patientID;
        public Spinner gender;
        public CustomEditText age;
        public Spinner testResutl;
    }

    static class ViewHolder2 {
        public CustomTextView number;
        public CustomEditText patientID;
        public CustomTextView testResult;
        public Spinner act;
        public CustomTextView score;
    }

    @Override
    public Float getScore() {
        return num/denum;
    }

    @Override
    public void initializeSubscore() {
        ListView lAdapter = (ListView) ((Activity) getContext()).findViewById(R.id.listView);

        ViewGroup header = (ViewGroup) getInflater().inflate(R.layout.adherencetab_header0_pictureapp, lAdapter, false);
        lAdapter.addHeaderView(header);

        final Switch visibility = (Switch) ((Activity) getContext()).findViewById(R.id.visibilitySwitch);

        visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    visible = true;
                else visible = false;

                notifyDataSetChanged();
            }
        });

    }

    public CustomAdherenceAdapter(Tab tab, Context context) {
        super(tab, context, R.layout.form_custom);

        if (getItems().size()> 0)
            position_secondheader = (int) ((Header) getItems().get(0)).getNumberOfQuestionParents() +1 ;

        Log.d("Second header", position_secondheader + "");

        scores = new int[position_secondheader];

        for (int i=0;i<position_secondheader; i++) {
            if (getItems().get(i) instanceof Question) {
                Question testResult = ((Question) getItems().get(i)).getChildren().get(3);
                ScoreRegister.addRecord(testResult, ScoreRegister.calcNum(testResult), ScoreRegister.calcDenum(testResult));
            }
        }

        for (int i = position_secondheader; i < getItems().size(); i++) {
            if (getItems().get(i) instanceof Question) {
                Question act = ((Question) getItems().get(i)).getChildren().get(2);
                ScoreRegister.addRecord(act, ScoreRegister.calcNum(act), ScoreRegister.calcDenum(act));
                calcScore((Question) getItems().get(i));
            }
        }

    }

    /**
     * Factory method to build a CustomAdherenceTab.
     * @param tab
     * @param context
     * @return
     */
    public static CustomAdherenceAdapter build(Tab tab, Context context){
        return new CustomAdherenceAdapter(tab, context);
    }

    class Bool {
        public boolean value;

        public Bool(boolean value) {
            this.value=value;
        }
    }

    @Override
    public int getCount() {
        if (visible)
            return getItems().size();
        else return 0;
    }

    private void setValues(ViewHolder viewHolder, Question question) {
        viewHolder.number.setText(question.getForm_name());
        viewHolder.patientID.setText(ReadWriteDB.readValueQuestion(question.getChildren().get(0)));
        viewHolder.age.setText(ReadWriteDB.readValueQuestion(question.getChildren().get(2)));
        viewHolder.gender.setSelection(ReadWriteDB.readPositionOption(question.getChildren().get(1)));
        viewHolder.testResutl.setSelection(ReadWriteDB.readPositionOption(question.getChildren().get(3)));
    }

    private void setValues2(ViewHolder2 viewHolder, Question question) {
        calcScore(question);
        viewHolder.score.setText(String.valueOf(scores[getItems().indexOf(question) - position_secondheader]));
        viewHolder.patientID.setText(ReadWriteDB.readValueQuestion(question.getChildren().get(0)));
        viewHolder.number.setText(question.getForm_name());
        viewHolder.testResult.setText(question.getChildren().get(1).getForm_name());
        viewHolder.act.setSelection(ReadWriteDB.readPositionOption(question.getChildren().get(2)));

    }

    private void calcScore(Question question) {

        Question act = question.getChildren().get(2);
        Question test = question.getChildren().get(1);

        Value value = act.getValueBySession();

        if (value != null) {

            List optList = act.getAnswer().getOptions();
            optList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

            int pos = optList.indexOf(value.getOption());


            if (test.getForm_name().equals(getContext().getString(R.string.adherence_info_rdt_positive))
                    || test.getForm_name().equals(getContext().getString(R.string.adherence_info_rdt_negative))) {
                if (pos == 1) {
                    num = num - scores[getItems().indexOf(question) - position_secondheader] + 1;
                    scores[getItems().indexOf(question) - position_secondheader] = 1;
                } else {
                    num = num - scores[getItems().indexOf(question) - position_secondheader];
                    scores[getItems().indexOf(question) - position_secondheader] = 0;
                }
            } else {
                if (test.getForm_name().equals(getContext().getString(R.string.adherence_info_microscopy_positive))
                        || test.getForm_name().equals(getContext().getString(R.string.adherence_info_microscopy_negative))) {
                    if (pos == 2) {
                        num = num - scores[getItems().indexOf(question) - position_secondheader] + 1;
                        scores[getItems().indexOf(question) - position_secondheader] = 1;
                    } else {
                        num = num - scores[getItems().indexOf(question) - position_secondheader];
                        scores[getItems().indexOf(question) - position_secondheader] = 0;
                    }
                }

            }
        }
        else {
            scores[getItems().indexOf(question) - position_secondheader] = 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        final Bool viewCreated = new Bool(false);

        final Question question;

        if (position < position_secondheader) {
            if (item instanceof Header)
                rowView = getInflater().inflate(R.layout.adherencetab_header1_pictureapp, parent, false);
            else {

                question = (Question) item;

                final ViewHolder viewHolder = new ViewHolder();

                rowView = getInflater().inflate(R.layout.pharmacy_register, parent, false);
                viewHolder.number = (CustomTextView) rowView.findViewById(R.id.number);
                viewHolder.gender = (Spinner) rowView.findViewById(R.id.gender);
                viewHolder.age = (CustomEditText) rowView.findViewById(R.id.age);
                viewHolder.patientID = (CustomEditText) rowView.findViewById(R.id.patientId);
                viewHolder.testResutl = (Spinner) rowView.findViewById(R.id.testResults);

                List<Option> optionList = question.getChildren().get(1).getAnswer().getOptions();
                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder.gender.setAdapter(new OptionArrayAdapter(getContext(),optionList));

                optionList = question.getChildren().get(3).getAnswer().getOptions();

                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder.testResutl.setAdapter(new OptionArrayAdapter(getContext(), optionList));

                rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

                viewHolder.age.addTextChangedListener(new TextWatcher() {

                    Bool viewCreated = new Bool(false);

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (viewCreated.value) {
                            ReadWriteDB.saveValuesText(question.getChildren().get(2), s.toString());
                        } else viewCreated.value = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                viewHolder.patientID.addTextChangedListener(new TextWatcher() {

                    Bool viewCreated = new Bool(false);

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if (viewCreated.value) {
                            ReadWriteDB.saveValuesText(question.getChildren().get(0), s.toString());
                        } else viewCreated.value = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                viewHolder.gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    Bool viewCreated = new Bool(false);

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (viewCreated.value)
                            ReadWriteDB.saveValuesDDL(question.getChildren().get(1), (Option) viewHolder.gender.getItemAtPosition(position));
                        else viewCreated.value = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                viewHolder.testResutl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (viewCreated.value) {
                            Question testResult = question.getChildren().get(3);
                            ReadWriteDB.saveValuesDDL(testResult, (Option) viewHolder.testResutl.getItemAtPosition(position));
                            ScoreRegister.addRecord(testResult, ScoreRegister.calcNum(testResult), ScoreRegister.calcDenum(testResult));
                        }
                        else viewCreated.value = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                setValues(viewHolder, question);
            }

        }


        else {

            if (item instanceof Header)
                rowView = getInflater().inflate(R.layout.adherencetab_header2_pictureapp, parent, false);
            else {

                question = (Question) item;

                final ViewHolder2 viewHolder2 = new ViewHolder2();

                rowView = getInflater().inflate(R.layout.pharmacy_register2, parent, false);

                viewHolder2.number = (CustomTextView) rowView.findViewById(R.id.number);
                viewHolder2.patientID = (CustomEditText) rowView.findViewById(R.id.patientId);
                viewHolder2.testResult = (CustomTextView) rowView.findViewById(R.id.testResult);
                viewHolder2.act = (Spinner) rowView.findViewById(R.id.act1);
                viewHolder2.score = (CustomTextView) rowView.findViewById(R.id.scoreValue);

                List<Option> optionList = ((Question) item).getChildren().get(2).getAnswer().getOptions();
                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder2.act.setAdapter(new OptionArrayAdapter(getContext(), optionList));

                viewHolder2.patientID.addTextChangedListener(new TextWatcher() {

                    Bool viewCreated = new Bool(false);

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (viewCreated.value)
                            ReadWriteDB.saveValuesText(question.getChildren().get(0), s.toString());

                        else viewCreated.value = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                viewHolder2.act.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        if (viewCreated.value) {

                            Question act = question.getChildren().get(2);
                            ReadWriteDB.saveValuesDDL(act, (Option) viewHolder2.act.getItemAtPosition(pos));
                            calcScore(question);
                            viewHolder2.score.setText(Integer.toString(scores[getItems().indexOf(question) - position_secondheader]));
                            ScoreRegister.addRecord(act, ScoreRegister.calcNum(act), ScoreRegister.calcDenum(act));
                        } else
                            viewCreated.value = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                setValues2(viewHolder2, question);
            }

        }
        return rowView;
    }

}

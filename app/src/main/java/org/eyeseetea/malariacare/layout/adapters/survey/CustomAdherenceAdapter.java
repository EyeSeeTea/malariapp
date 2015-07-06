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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

/**
 * Created by Jose on 24/04/2015.
 */
public class CustomAdherenceAdapter extends BaseAdapter implements ITabAdapter {

    private List<Object> items;
    Tab tab;

    private LayoutInflater lInflater;

    boolean visible = false;

    //final ScoreHolder scoreHolder = new ScoreHolder();

    float denum = 20;
    float num = 0;

    private final Context context;

    int position_secondheader = 0;

    int id_layout;
    int []scores;

    static class ViewHolder {
        public TextView number;
        public EditText patientID;
        public Spinner gender;
        public EditText age;
        public Spinner testResutl;
    }

    static class ViewHolder2 {
        public TextView number;
        public EditText patientID;
        public TextView testResult;
        public Spinner act;
        public TextView score;
    }

    /*static class ScoreHolder {
        public TextView scoreText;
        public TextView score;
        public TextView cualitativeScore;
    }*/

    private void initializeScoreViews() {
       /* scoreHolder.score = (TextView) ((Activity) context).findViewById(R.id.score);
        scoreHolder.cualitativeScore = (TextView) ((Activity) context).findViewById(R.id.qualitativeScore);
        scoreHolder.scoreText = (TextView) ((Activity) context).findViewById(R.id.subtotalScoreText);*/
    }

    public void updateScore() {
        //scoreHolder.score.setText(Utils.round(num / denum));
    }

    @Override
    public Float getScore() {
        return num/denum;
    }

    @Override
    public String getName() {
        return tab.getName();
    }

    @Override
    public void initializeSubscore() {
        initializeScoreViews();
        updateScore();

        ListView lAdapter = (ListView) ((Activity) context).findViewById(R.id.listView);

        ViewGroup header = (ViewGroup) lInflater.inflate(R.layout.adherencetab_header0, lAdapter, false);
        lAdapter.addHeaderView(header);

        final Switch visibility = (Switch) ((Activity) context).findViewById(R.id.visibilitySwitch);

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
        this.lInflater=LayoutInflater.from(context);
        this.items=Utils.convertTabToArrayCustom(tab);
        this.id_layout = R.layout.form_custom;
        this.context=context;

        if (items.size()> 0)
            position_secondheader = LayoutUtils.getNumberOfQuestionParentsHeader((Header) items.get(0)) +1 ;

        Log.d("Second header", position_secondheader + "");

        scores = new int[position_secondheader];

        for (int i=0;i<position_secondheader; i++) {
            if (items.get(i) instanceof Question) {
                Question testResult = ((Question) items.get(i)).getQuestionChildren().get(3);
                ScoreRegister.addRecord(testResult, ScoreRegister.calcNum(testResult), ScoreRegister.calcDenum(testResult));
            }
        }

        for (int i = position_secondheader; i < items.size(); i++) {
            if (items.get(i) instanceof Question) {
                Question act = ((Question) items.get(i)).getQuestionChildren().get(2);
                ScoreRegister.addRecord(act, ScoreRegister.calcNum(act), ScoreRegister.calcDenum(act));
                calcScore((Question) items.get(i));
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

    private void resetScores() {
        for (int i=0; i<scores.length; i++)
            scores[i] = 0;
    }

    class Bool {
        public boolean value;

        public Bool(boolean value) {
            this.value=value;
        }
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
    public int getCount() {
        if (visible)
            return items.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }


    private void setValues(ViewHolder viewHolder, Question question) {
        viewHolder.number.setText(question.getForm_name());
        viewHolder.patientID.setText(ReadWriteDB.readValueQuestion(question.getQuestionChildren().get(0)));
        viewHolder.age.setText(ReadWriteDB.readValueQuestion(question.getQuestionChildren().get(2)));
        viewHolder.gender.setSelection(ReadWriteDB.readPositionOption(question.getQuestionChildren().get(1)));
        viewHolder.testResutl.setSelection(ReadWriteDB.readPositionOption(question.getQuestionChildren().get(3)));
    }

    private void setValues2(ViewHolder2 viewHolder, Question question) {
        calcScore(question);
        viewHolder.score.setText(String.valueOf(scores[items.indexOf(question) - position_secondheader]));
        viewHolder.patientID.setText(ReadWriteDB.readValueQuestion(question.getQuestionChildren().get(0)));
        viewHolder.number.setText(question.getForm_name());
        viewHolder.testResult.setText(question.getQuestionChildren().get(1).getForm_name());
        viewHolder.act.setSelection(ReadWriteDB.readPositionOption(question.getQuestionChildren().get(2)));

    }

    private void calcScore(Question question) {

        Question act = question.getQuestionChildren().get(2);
        Question test = question.getQuestionChildren().get(1);

        Value value = act.getValueBySession();

        if (value != null) {

            List optList = act.getAnswer().getOptions();
            optList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

            int pos = optList.indexOf(value.getOption());


            if (test.getForm_name().equals(this.context.getString(R.string.adherence_info_rdt_positive))
                    || test.getForm_name().equals(this.context.getString(R.string.adherence_info_rdt_negative))) {
                if (pos == 1) {
                    num = num - scores[items.indexOf(question) - position_secondheader] + 1;
                    scores[items.indexOf(question) - position_secondheader] = 1;
                } else {
                    num = num - scores[items.indexOf(question) - position_secondheader];
                    scores[items.indexOf(question) - position_secondheader] = 0;
                }
            } else {
                if (test.getForm_name().equals(this.context.getString(R.string.adherence_info_microscopy_positive))
                        || test.getForm_name().equals(this.context.getString(R.string.adherence_info_microscopy_negative))) {
                    if (pos == 2) {
                        num = num - scores[items.indexOf(question) - position_secondheader] + 1;
                        scores[items.indexOf(question) - position_secondheader] = 1;
                    } else {
                        num = num - scores[items.indexOf(question) - position_secondheader];
                        scores[items.indexOf(question) - position_secondheader] = 0;
                    }
                }

            }
        }
        else {
            scores[items.indexOf(question) - position_secondheader] = 0;
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
                rowView = lInflater.inflate(R.layout.adherencetab_header1, parent, false);
            else {

                question = (Question) item;

                final ViewHolder viewHolder = new ViewHolder();

                rowView = lInflater.inflate(R.layout.pharmacy_register, parent, false);
                viewHolder.number = (TextView) rowView.findViewById(R.id.number);
                viewHolder.gender = (Spinner) rowView.findViewById(R.id.gender);
                viewHolder.age = (EditText) rowView.findViewById(R.id.age);
                viewHolder.patientID = (EditText) rowView.findViewById(R.id.patientId);
                viewHolder.testResutl = (Spinner) rowView.findViewById(R.id.testResults);

                List<Option> optionList = question.getQuestionChildren().get(1).getAnswer().getOptions();
                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder.gender.setAdapter(new OptionArrayAdapter(context,optionList));

                optionList = question.getQuestionChildren().get(3).getAnswer().getOptions();

                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder.testResutl.setAdapter(new OptionArrayAdapter(context, optionList));

                rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

                viewHolder.age.addTextChangedListener(new TextWatcher() {

                    Bool viewCreated = new Bool(false);

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (viewCreated.value) {
                            ReadWriteDB.saveValuesText(question.getQuestionChildren().get(2), s.toString());
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
                            ReadWriteDB.saveValuesText(question.getQuestionChildren().get(0), s.toString());
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
                            ReadWriteDB.saveValuesDDL(question.getQuestionChildren().get(1), (Option) viewHolder.gender.getItemAtPosition(position));
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
                            Question testResult = question.getQuestionChildren().get(3);
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
                rowView = lInflater.inflate(R.layout.adherencetab_header2, parent, false);
            else {

                question = (Question) item;

                final ViewHolder2 viewHolder2 = new ViewHolder2();

                rowView = lInflater.inflate(R.layout.pharmacy_register2, parent, false);

                viewHolder2.number = (TextView) rowView.findViewById(R.id.number);
                viewHolder2.patientID = (EditText) rowView.findViewById(R.id.patientId);
                viewHolder2.testResult = (TextView) rowView.findViewById(R.id.testResult);
                viewHolder2.act = (Spinner) rowView.findViewById(R.id.act1);
                viewHolder2.score = (TextView) rowView.findViewById(R.id.scoreValue);

                List<Option> optionList = ((Question) item).getQuestionChildren().get(2).getAnswer().getOptions();
                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder2.act.setAdapter(new OptionArrayAdapter(context, optionList));

                viewHolder2.patientID.addTextChangedListener(new TextWatcher() {

                    Bool viewCreated = new Bool(false);

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (viewCreated.value)
                            ReadWriteDB.saveValuesText(question.getQuestionChildren().get(0), s.toString());

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

                            Question act = question.getQuestionChildren().get(2);
                            ReadWriteDB.saveValuesDDL(act, (Option) viewHolder2.act.getItemAtPosition(pos));
                            calcScore(question);
                            viewHolder2.score.setText(Integer.toString(scores[items.indexOf(question) - position_secondheader]));
                            ScoreRegister.addRecord(act, ScoreRegister.calcNum(act), ScoreRegister.calcDenum(act));

                            updateScore();
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

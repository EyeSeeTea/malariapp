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

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.EditCard;
import org.eyeseetea.malariacare.views.TextCard;

import java.util.List;

/**
 * Created by Jose on 11/04/2015.
 */
public class CustomIQTABAdapter extends BaseAdapter implements ITabAdapter {

    private List<Object> items;
    Tab tab;

    private LayoutInflater lInflater;

    private final Context context;

    //final ScoreHolder scoreHolder = new ScoreHolder();

    int number_rows_section;

    int id_layout;

    int[] results;

    static class ViewHolder {
        public TextCard number;
        public Spinner spinner;
        public EditCard parasites;
        public Spinner species;
    }

    static class ViewHolder2 {
        public TextCard number;
        public TextCard result;
    }

    /*static class ScoreHolder {
        public TextCard scoreText;
        public TextCard score;
        public TextCard cualitativeScore;
    }*/

    public CustomIQTABAdapter(Tab tab, Context context) {
        this.lInflater = LayoutInflater.from(context);
        this.items = Utils.convertTabToArrayCustom(tab);
        this.context = context;
        this.id_layout = R.layout.form_custom;

        if (items.size()>0)
            number_rows_section = LayoutUtils.getNumberOfQuestionParentsHeader((Header) items.get(0))+1;

        for (int i = 0; i < 2 * number_rows_section; i++) {
            Object item = items.get(i);
            if (item instanceof Question)
                calculateMatch((Question) item);


        }

        for (int i = 2 * number_rows_section; i<items.size();i++) {
            Object item = items.get(i);
            if (item instanceof Question) {
                Question result = ((Question) item).getQuestionChildren().get(0);
                ScoreRegister.addRecord(result, ScoreRegister.calcNum(result), ScoreRegister.calcDenum(result));
            }

        }

    }

    /**
     * Factory method to build a CustomIQTABAdapter.
     * @param tab
     * @param context
     * @return
     */
    public static CustomIQTABAdapter build(Tab tab, Context context){
        return new CustomIQTABAdapter(tab, context);
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
    public String getName() {
        return tab.getName();
    }

    private void initializeScoreViews() {
        /*scoreHolder.score = (TextCard) ((Activity) context).findViewById(R.id.score);
        scoreHolder.cualitativeScore = (TextCard) ((Activity) context).findViewById(R.id.qualitativeScore);
        scoreHolder.scoreText = (TextCard) ((Activity) context).findViewById(R.id.subtotalScoreText);*/
    }

    private void resetResults() {
        for (int i = 0; i < results.length; i++)
            results[i] = 0;
    }

    public void updateScore() {
        //scoreHolder.score.setText(Utils.round(num / denum));
    }

    public void initializeSubscore() {
        initializeScoreViews();
        updateScore();
    }

    @Override
    public Float getScore() {

        List<Float> numdenum = ScoreRegister.calculateGeneralScore(tab);

        Float num = numdenum.get(0);
        Float denum = numdenum.get(1);

        return num/denum;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    class Bool {
        public boolean value;

        public Bool(boolean value) {
            this.value = value;
        }
    }

    public void calculateMatch(Question question) {
        int simetric_position;
        int result_position;

        int position = items.indexOf(question);

        Question q1, q2;

        if (position > number_rows_section) {
            simetric_position = position - number_rows_section;
            result_position = position - number_rows_section - 1;
        } else {
            simetric_position = position + number_rows_section;
            result_position = position - 1;
        }

        q1 = ((Question) items.get(position)).getQuestionChildren().get(0);
        q2 = ((Question) items.get(simetric_position)).getQuestionChildren().get(0);

        Question questionAnswer =  (Question) items.get(2*number_rows_section+result_position+1);
        Question testResult = questionAnswer.getQuestionChildren().get(0);

        if (q1.getValueBySession() != null && q2.getValueBySession() != null &&
                q1.getValueBySession().getOption().equals(q2.getValueBySession().getOption())) {

            ReadWriteDB.saveValuesDDL(testResult,  testResult.getAnswer().getOptions().get(0));
            ScoreRegister.addRecord(testResult, ScoreRegister.calcNum(testResult), ScoreRegister.calcNum(testResult));

        } else {

            ScoreRegister.addRecord(testResult, 0F, ScoreRegister.calcDenum(testResult));

            if (q1.getValueBySession() != null && q2.getValueBySession() != null) {
                ReadWriteDB.saveValuesDDL(testResult,  testResult.getAnswer().getOptions().get(1));
            }
            else
                ReadWriteDB.deleteValue(testResult);
        }
        notifyDataSetChanged();
    }

    private void setValues(ViewHolder viewHolder, Question question) {
        viewHolder.number.setText(question.getForm_name());
        viewHolder.species.setSelection(ReadWriteDB.readPositionOption(question.getQuestionChildren().get(2)));
        viewHolder.parasites.setText(ReadWriteDB.readValueQuestion(question.getQuestionChildren().get(1)));
        viewHolder.spinner.setSelection(ReadWriteDB.readPositionOption(question.getQuestionChildren().get(0)));
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Question parasites;
        final Question test;
        final Question species;
        final Question question;

        if (position < 2*number_rows_section) {

            final Object item = getItem(position);
            final ViewHolder viewHolder = new ViewHolder();

            if (item instanceof Header) {
                if (position == 0)
                    rowView = lInflater.inflate(R.layout.iqtabheader1, parent, false);
                else
                    rowView = lInflater.inflate(R.layout.iqtabheader2, parent, false);
            } else {

                question = (Question) item;

                rowView = lInflater.inflate(R.layout.iqatab_record, parent, false);
                viewHolder.number = (TextCard) rowView.findViewById(R.id.number);
                viewHolder.spinner = (Spinner) rowView.findViewById(R.id.testRes);
                viewHolder.parasites = (EditCard) rowView.findViewById(R.id.parasites);
                viewHolder.species = (Spinner) rowView.findViewById(R.id.species);

                List<Option> optionList = ((Question) item).getQuestionChildren().get(0).getAnswer().getOptions();
                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder.spinner.setAdapter(new OptionArrayAdapter(context, optionList));

                optionList = ((Question) item).getQuestionChildren().get(2).getAnswer().getOptions();
                optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                viewHolder.species.setAdapter(new OptionArrayAdapter(context, optionList));


                test = question.getQuestionChildren().get(0);
                parasites = question.getQuestionChildren().get(1);
                species = question.getQuestionChildren().get(2);

                rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

                viewHolder.parasites.addTextChangedListener(new TextWatcher() {

                    Bool viewCreated = new Bool(false);
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (viewCreated.value) {
                            ReadWriteDB.saveValuesText(parasites, s.toString());
                        } else viewCreated.value = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });


                viewHolder.species.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    Bool viewCreated = new Bool(false);

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        if (viewCreated.value) {
                            ReadWriteDB.saveValuesDDL(species, (Option) viewHolder.species.getItemAtPosition(pos));
                        } else viewCreated.value = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    Bool viewCreated = new Bool(false);
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        if (viewCreated.value) {
                            ReadWriteDB.saveValuesDDL(test, (Option) viewHolder.spinner.getItemAtPosition(pos));
                            calculateMatch(question);
                            updateScore();
                        } else viewCreated.value = true;

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                setValues(viewHolder, question);


            }

        } else {

            final ViewHolder2 viewHolder2 = new ViewHolder2();


            if (position == 2 * number_rows_section) {
                rowView = lInflater.inflate(R.layout.iqtabheader3, parent, false);
            } else {
                rowView = lInflater.inflate(R.layout.iqatab_results, parent, false);

                Question questionResult = (Question) getItem(position);
                Question testResult = questionResult.getQuestionChildren().get(0);


                viewHolder2.number = (TextCard) rowView.findViewById(R.id.number_result);
                viewHolder2.result = (TextCard) rowView.findViewById(R.id.matches);


                viewHolder2.number.setText(String.valueOf(questionResult.getForm_name()));

                Option option = ReadWriteDB.readOptionAnswered(testResult);

                if (option!=null && option.getName().equals("Yes"))
                    viewHolder2.result.setText("1");
                else
                    viewHolder2.result.setText("0");


            }
        }
        return rowView;
    }
}

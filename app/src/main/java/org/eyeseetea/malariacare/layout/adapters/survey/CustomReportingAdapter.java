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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

/**
 * Created by Jose on 12/04/2015.
 */
public class CustomReportingAdapter extends BaseAdapter implements ITabAdapter {

    private List<Object> items;

    private LayoutInflater lInflater;

    String tabName;

    final ScoreHolder scoreHolder = new ScoreHolder();

    final RowValues[] values;
    private final Context context;

    int id_layout;

    float denum = 0;
    float num = 0;

    class RowValues {
        public String statement;
        public String register;
        public String report;
        public int score;
    }

    static class ViewHolder {
        public TextView statement;
        public EditText register;
        public EditText report;
        public TextView score;
    }

    static class ScoreHolder {
        public TextView scoreText;
        public TextView tabName;
        public TextView score;
        public TextView cualitativeScore;
    }

    public void updateScore() {
        scoreHolder.score.setText(Utils.round(num / denum));
    }

    public CustomReportingAdapter(List<Object> items, Context context, int id_layout, String tabName) {
        this.lInflater=LayoutInflater.from(context);
        this.items=items;
        this.context=context;
        this.id_layout = id_layout;
        this.tabName = tabName;

        values = new RowValues[items.size()];

        for (int i = 0; i<items.size(); i++){
            Object item = items.get(i);
            if (item instanceof Question) {
                Question question = (Question) item;
                if (question.hasChildren())
                    values[i] = readValues(question);
            }
        }

        //initializeSubscore();
    }

    @Override
    public BaseAdapter getAdapter() {
        return this;
    }

    @Override
    public int getLayout() {
        return id_layout;
    }

    private void initializeScoreViews() {
        scoreHolder.score = (TextView) ((Activity) context).findViewById(R.id.score);
        scoreHolder.cualitativeScore = (TextView) ((Activity) context).findViewById(R.id.cualitativeScore);
        scoreHolder.scoreText = (TextView) ((Activity) context).findViewById(R.id.subtotalScoreText);
        scoreHolder.tabName = (TextView) ((Activity) context).findViewById(R.id.tabName);
        scoreHolder.tabName.setText(tabName);
    }

    @Override
    public Float getScore() {
        return num/denum;
    }

    @Override
    public String getName() {
        return tabName;
    }

    @Override
    public void initializeSubscore() {
        initializeScoreViews();
        updateScore();
    }

    class Bool {
        public boolean value;

        public Bool(boolean value) {
            this.value=value;
        }
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

    private void setValues(ViewHolder viewHolder, RowValues rowValues) {
        viewHolder.statement.setText(rowValues.statement);
        viewHolder.register.setText(rowValues.register);
        viewHolder.report.setText(rowValues.report);
        viewHolder.score.setText(String.valueOf(rowValues.score));
    }

    private int areEquals(String answer1, String answer2) {
        int result = 0;

        if (answer1!=null && !answer1.equals(""))
            if (answer1.equals(answer2))
                result=1;

        return result;
    }

    private RowValues readValues(Question question) {

        RowValues rowValues = new RowValues();

        rowValues.statement = question.getQuestionChildren().get(0).getForm_name();
        rowValues.register = ReadWriteDB.readValueQuestion(question.getQuestionChildren().get(1));
        rowValues.report = ReadWriteDB.readValueQuestion(question.getQuestionChildren().get(2));
        rowValues.score = areEquals(rowValues.register, rowValues.report);

        if (rowValues.score == 1)
            num = num + 1;

        denum = denum + 1;

        return rowValues;
    }

    private void textEntered(TextView score, RowValues rowValues) {

        num = num - rowValues.score;

        rowValues.score = areEquals(rowValues.register, rowValues.report);

        if (rowValues.score == 1) {
            num = num +1;
            score.setText(this.context.getString(R.string.custom_info_one));
        }
        else score.setText(this.context.getString(R.string.number_zero));

        updateScore();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        final RowValues rowValues;
        final ViewHolder viewHolder = new ViewHolder();
        final Question register;
        final Question report;

        if (values[items.indexOf(item)]==null)
            values[items.indexOf(item)] = new RowValues();

        rowValues = values [items.indexOf(item)];

        if (item instanceof Header) {
            if (position==0)
                rowView = lInflater.inflate(R.layout.reportingtab, parent, false);
            else
                rowView = lInflater.inflate(R.layout.reporting_record, parent, false);
        }

        else
        {
            rowView = lInflater.inflate(R.layout.reporting_record2, parent, false);
            viewHolder.statement = (TextView) rowView.findViewById(R.id.reportingQuestion);
            viewHolder.report = (EditText) rowView.findViewById(R.id.monthlyReport);
            viewHolder.register = (EditText) rowView.findViewById(R.id.register);
            viewHolder.score = (TextView) rowView.findViewById(R.id.scoreValue);

            register = ((Question)item).getQuestionChildren().get(1);
            report = ((Question)item).getQuestionChildren().get(2);

            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));


            viewHolder.register.addTextChangedListener(new TextWatcher() {

                Bool viewCreated = new Bool(false);
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (viewCreated.value) {
                        rowValues.register = s.toString();
                        ReadWriteDB.saveValuesText(register, s.toString());
                        textEntered(viewHolder.score, rowValues);

                    }
                    else viewCreated.value = true;
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            viewHolder.report.addTextChangedListener(new TextWatcher() {

                Bool viewCreated = new Bool(false);
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (viewCreated.value) {
                        rowValues.report = s.toString();
                        ReadWriteDB.saveValuesText(report, s.toString());
                        textEntered(viewHolder.score, rowValues);
                    }
                    else viewCreated.value = true;
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            setValues(viewHolder, rowValues);


        }

        return rowView;
    }
}

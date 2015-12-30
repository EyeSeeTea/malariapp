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
import android.view.View;
import android.view.ViewGroup;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomTextView;

/**
 * Created by Jose on 12/04/2015.
 */
public class CustomReportingAdapter extends ATabAdapter {

    static class ViewHolder {
        public CustomTextView statement;
        public CustomEditText register;
        public CustomEditText report;
        public CustomTextView score;
    }

    /**
     * Factory method to build a CustomReportingTab.
     * @param tab
     * @param context
     * @return
     */
    public static CustomReportingAdapter build(Tab tab, Context context){
        return new CustomReportingAdapter(tab, context);
    }

    public CustomReportingAdapter(Tab tab, Context context) {
        super(tab, context, R.layout.form_custom);
    }

    @Override
    public Float getScore() {
        return 0F;
    }

    class Bool {
        public boolean value;

        public Bool(boolean value) {
            this.value=value;
        }
    }

    private void setValues(ViewHolder viewHolder, Question question) {

        Question questionRegister = question.getChildren().get(0);
        Question questionReport = question.getChildren().get(1);

        viewHolder.statement.setText(question.getForm_name());
        viewHolder.register.setText(ReadWriteDB.readValueQuestion(questionRegister));
        viewHolder.report.setText(ReadWriteDB.readValueQuestion(questionReport));

        setScoreColumn(viewHolder.score, questionRegister, questionReport);

    }

    private boolean areEquals(String answer1, String answer2) {
        boolean result = false;

        if (answer1!=null && !answer1.equals(""))
            if (answer1.equals(answer2))
                result=true;

        return result;
    }


    private void setScoreColumn(CustomTextView score, Question questionRegister, Question questionReport) {

        String register = ReadWriteDB.readValueQuestion(questionRegister);
        String report =  ReadWriteDB.readValueQuestion(questionReport);

        if (areEquals(register, report))
            score.setText(R.string.custom_info_one);
        else
            score.setText(R.string.number_zero);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        final ViewHolder viewHolder = new ViewHolder();

        final Question question;
        final Question questionRegister;
        final Question questionReport;

        if (item instanceof Header) {
            if (position==0)
                rowView = getInflater().inflate(R.layout.reportingtab, parent, false);
            else
                rowView = getInflater().inflate(R.layout.reporting_record, parent, false);
        }

        else
        {
            rowView = getInflater().inflate(R.layout.reporting_record2, parent, false);
            viewHolder.statement = (CustomTextView) rowView.findViewById(R.id.reportingQuestion);
            viewHolder.report = (CustomEditText) rowView.findViewById(R.id.monthlyReport);
            viewHolder.register = (CustomEditText) rowView.findViewById(R.id.register);
            viewHolder.score = (CustomTextView) rowView.findViewById(R.id.scoreValue);

            question = (Question) item;

            questionRegister = question.getChildren().get(0);
            questionReport = question.getChildren().get(1);

            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));
            viewHolder.register.addTextChangedListener(new TextWatcher() {

                Bool viewCreated = new Bool(false);
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (viewCreated.value) {
                        ReadWriteDB.saveValuesText(questionRegister, s.toString());
                        setScoreColumn(viewHolder.score, questionRegister, questionReport);
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
                        ReadWriteDB.saveValuesText(questionReport, s.toString());
                        setScoreColumn(viewHolder.score, questionRegister, questionReport);
                    }
                    else viewCreated.value = true;
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            setValues(viewHolder, question);

        }

        return rowView;
    }
}

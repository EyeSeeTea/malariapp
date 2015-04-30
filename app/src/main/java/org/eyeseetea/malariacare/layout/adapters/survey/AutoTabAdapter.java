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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

/**
 * Created by Jose on 21/04/2015.
 */
public class AutoTabAdapter extends BaseAdapter implements ITabAdapter {

    //List of Headers and Questions. Each position contains an object to be showed in the listview
    List<Object> items;

    LayoutInflater lInflater;

    final ScoreHolder scoreHolder = new ScoreHolder();

    String tabName;

    float totalNum = 0;
    float totalDenum;
    float score;

    private final Context context;

    //The length of this arrays is the same that the items list. Each position indicates if the item on this position is visible
    //or not
    private final boolean[] hidden;

    int id_layout;

    //Store the Views references for each row (to avoid many calls to getViewById)
    static class ViewHolder {
        public TextView statement;
        public Spinner spinner;
        public EditText answer;
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
        public TextView cualitativeScore;
        public TextView tabName;
    }

    public AutoTabAdapter(List<Object> items, Context context, int id_layout, String tabName) {
        this.lInflater = LayoutInflater.from(context);
        this.items = items;
        this.context = context;
        this.id_layout = id_layout;
        this.tabName = tabName;

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
    public void initialize() {
        initializeScoreViews();
        initializeDenum();
        updateScore();
    }

    private void initializeScoreViews() {
        scoreHolder.score = (TextView) ((Activity) context).findViewById(R.id.score);
        scoreHolder.totalDenum = (TextView) ((Activity) context).findViewById(R.id.totalDen);
        scoreHolder.totalNum = (TextView) ((Activity) context).findViewById(R.id.totalNum);
        scoreHolder.subtotalscore = (TextView) ((Activity) context).findViewById(R.id.subtotalScoreText);
        scoreHolder.cualitativeScore = (TextView) ((Activity) context).findViewById(R.id.cualitativeScore);
        scoreHolder.tabName = (TextView) ((Activity) context).findViewById(R.id.tabName);
        scoreHolder.tabName.setText(tabName);
    }

    @Override
    public String getName() {
        return tabName;
    }

    public void updateScore() {
        scoreHolder.totalNum.setText(Float.toString(totalNum));
        scoreHolder.totalDenum.setText(Float.toString(totalDenum));
        if (totalDenum != 0) {
            Float score = 100 * (totalNum / totalDenum);
            LayoutUtils.trafficLight(scoreHolder.score, score, scoreHolder.cualitativeScore);
            scoreHolder.score.setText(Utils.round(100 * (totalNum / totalDenum)));
        }
        if (totalDenum == 0 && totalNum == 0) {
            scoreHolder.score.setText("0");
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

        if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST) {

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

    class Bool {
        public boolean value;

        public Bool(boolean value) {
            this.value = value;
        }
    }

    private float calcNum(Question question) {
        float result = 0;
        if (question.getValueBySession() != null) {
            Option op = ReadWriteDB.readOption(question);
            result = question.getNumerator_w() * op.getFactor();
        }

        return result;
    }

    private float calcNum(float factor, Question question) {
        return factor * question.getNumerator_w();
    }

    private float calcDenum(Question question) {
        float result = 0;

        if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST) {

            if (question.getValueBySession() != null) {
                Option op = ReadWriteDB.readOption(question);
                return calcDenum(op.getFactor(), question);
            } else result = calcDenum(0, question);
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
                viewHolder.answer.setText(ReadWriteDB.readValueQuestion(question));
                break;
            case Constants.DROPDOWN_LIST:
                viewHolder.spinner.setSelection(ReadWriteDB.readPositionOption(question));

                List<Float> numdenum = ScoreRegister.getNumDenum(question);
                if (numdenum != null) {
                    viewHolder.num.setText(Float.toString(numdenum.get(0)));
                    viewHolder.denum.setText(Float.toString(numdenum.get(1)));
                } else {
                    viewHolder.num.setText("0");
                    viewHolder.denum.setText(Float.toString(calcDenum(question)));
                    viewHolder.spinner.setSelection(0);
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

    private void itemSelected(ViewHolder viewHolder, Question question, Option option) {

        ReadWriteDB.saveValuesDDL(question, option);

        Float num = calcNum(question);
        Float denum = calcDenum(question);

        viewHolder.num.setText(num.toString());
        viewHolder.denum.setText(denum.toString());

        resetTotalNumDenum(question);

        totalNum = totalNum + num;
        totalDenum = totalDenum + denum;

        ScoreRegister.addRecord(question, num, denum);

        if (question.hasChildren()) {

            if (option.getName().equals("Yes"))
                updateQuestionsVisibility(question, true);
            else
                updateQuestionsVisibility(question, false);

        }

        updateScore();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        final Object item = getItem(position);
        final Bool viewCreated = new Bool(false);
        final Question question;
        final ViewHolder viewHolder = new ViewHolder();

        if (item instanceof Header) {
            rowView = lInflater.inflate(R.layout.headers, parent, false);
            viewHolder.statement = (TextView) rowView.findViewById(R.id.headerName);
            viewHolder.statement.setText(((Header) item).getName());
        } else {

            question = (Question) item;

            switch (question.getAnswer().getOutput()) {

                case Constants.LONG_TEXT:
                    rowView = lInflater.inflate(R.layout.longtext, parent, false);
                    viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
                    viewHolder.answer = (EditText) rowView.findViewById(R.id.answer);
                    break;

                case Constants.NO_ANSWER:
                    rowView = lInflater.inflate(R.layout.label, parent, false);
                    viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
                    break;

                case Constants.POSITIVE_INT:
                case Constants.INT:
                    rowView = lInflater.inflate(R.layout.integer, parent, false);
                    viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
                    viewHolder.answer = (EditText) rowView.findViewById(R.id.answer);
                    break;

                case Constants.DATE:
                    rowView = lInflater.inflate(R.layout.date, parent, false);
                    viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
                    viewHolder.answer = (EditText) rowView.findViewById(R.id.answer);
                    break;

                case Constants.SHORT_TEXT:
                    rowView = lInflater.inflate(R.layout.shorttext, parent, false);
                    viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
                    viewHolder.answer = (EditText) rowView.findViewById(R.id.answer);
                    break;

                case Constants.DROPDOWN_LIST:
                    rowView = lInflater.inflate(R.layout.ddl, parent, false);
                    viewHolder.statement = (TextView) rowView.findViewById(R.id.statement);
                    viewHolder.spinner = (Spinner) rowView.findViewById(R.id.answer);
                    viewHolder.num = (TextView) rowView.findViewById(R.id.num);
                    viewHolder.denum = (TextView) rowView.findViewById(R.id.den);

                    Spinner answers = (Spinner) rowView.findViewById(R.id.answer);

                    List<Option> optionList = question.getAnswer().getOptions();
                    optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));

                    ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, optionList);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

                    answers.setAdapter(adapter);

                    break;

                default:
                    break;
            }

            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));
            if (question.hasChildren())
                rowView.setBackgroundResource(R.drawable.background_parent);

            if (question.getAnswer().getOutput() == Constants.DROPDOWN_LIST) {

                viewHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        if (viewCreated.value) {
                            itemSelected(viewHolder, question, (Option) viewHolder.spinner.getItemAtPosition(pos));
                        } else viewCreated.value = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            } else if (question.getAnswer().getOutput() != Constants.NO_ANSWER) {
                viewHolder.answer.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if (viewCreated.value)
                            ReadWriteDB.saveValuesText(question, s.toString());
                        else viewCreated.value = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
            viewHolder.statement.setText(question.getForm_name());
            setValues(viewHolder, question);
        }

        return rowView;
    }


}

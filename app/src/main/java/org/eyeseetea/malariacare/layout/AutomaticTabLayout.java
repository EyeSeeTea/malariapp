package org.eyeseetea.malariacare.layout;

import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.listeners.AutomaticTabListeners;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.score.ScoreUtils;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

public class AutomaticTabLayout {


    public static void generateAutomaticTab(MainActivity mainActivity, Tab tab, LayoutInflater inflater, GridLayout layoutParent, Option defaultOption) {
        //Iterator for background (odd and even)
        int iterBacks = 0;

        Log.i(".Layout", "Generate Headers");
        for (Header header: tab.getHeaders()){
            // First we introduce header text according to the template
            //Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(R.layout.headers, layoutParent, false);

            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setBackgroundResource(R.drawable.background_header);
            headerText.setText(header.getName());
            //Set Visibility to false until we check if it has any question visible
            headerView.setVisibility(View.GONE);
            layoutParent.addView(headerView);


            //Log.i(".Layout", "Reader questions for header " + header.toString());
            for (Question question : header.getQuestions()){
                View questionView = null;
                // Check previous existing value
                Value value = question.getValueBySession();

                // The statement is present in every kind of question
                switch(question.getAnswer().getOutput()){
                    case Constants.DROPDOWN_LIST:
                        questionView = inflater.inflate(R.layout.ddl, layoutParent, false);
                        questionView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));

                        TextView statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(R.id.QuestionTag, question);
                        dropdown.setTag(R.id.HeaderViewTag, headerView);
                        dropdown.setTag(R.id.NumeratorViewTag, questionView.findViewById(R.id.num));
                        dropdown.setTag(R.id.DenominatorViewTag, questionView.findViewById(R.id.den));
                        dropdown.setTag(R.id.Tab, LayoutConfiguration.getTabsConfiguration().get(tab).getTabId());
                        dropdown.setTag(R.id.QuestionTypeTag, Constants.DROPDOWN_LIST);

                        float numeratorF = ScoreUtils.calculateNum(question, defaultOption);
                        float denominatorF = ScoreUtils.calculateDen(question, defaultOption);

                        ScoreRegister.addRecord(question, numeratorF, denominatorF);
                        denominator.setText(Utils.round(denominatorF));
                        // If the question hasn't children we hide the question
                        if (!question.hasParent()) {
                            if (question.hasChildren()) questionView.setBackgroundResource(R.drawable.background_parent);
                            headerView.setVisibility(View.VISIBLE);
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        AutomaticTabListeners.createDropDownListener(tab, dropdown, mainActivity);

                        List<Option> optionList = question.getAnswer().getOptions();
                        optionList.add(0, defaultOption);
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
                        dropdown.setAdapter(adapter);

                        // In case value existed previously
                        if (value != null) dropdown.setSelection(optionList.indexOf(value.getOption()));
                        break;
                    case Constants.INT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.integer, Constants.INT);
                        EditText answer = ((EditText)(questionView.findViewById(R.id.answer)));
                        answer.setFilters(new InputFilter[] { new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)});
                        answer.setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.LONG_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.longtext, Constants.LONG_TEXT);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.SHORT_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.shorttext, Constants.SHORT_TEXT);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.DATE:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.date, Constants.DATE);
                        ((EditText)(questionView.findViewById(R.id.answer))).setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.POSITIVE_INT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.integer, Constants.POSITIVE_INT);
                        EditText answerPos = ((EditText)(questionView.findViewById(R.id.answer)));
                        answerPos.setFilters(new InputFilter[] { new InputFilter.LengthFilter(Constants.MAX_INT_CHARS)});
                        answerPos.setTag(R.id.QuestionTag, question);
                        if (value != null) ((EditText)(questionView.findViewById(R.id.answer))).setText(value.getValue());
                        AutomaticTabListeners.createTextListener(tab, (EditText)questionView.findViewById(R.id.answer), mainActivity);
                        break;
                    case Constants.NO_ANSWER:
                        questionView = inflater.inflate(R.layout.noanswer, layoutParent, false);
                        questionView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));
                        TextView noStatement = (TextView) questionView.findViewById(R.id.statement);
                        noStatement.setText(question.getForm_name());
                        noStatement.setTag(R.id.QuestionTag, question);
                        noStatement.setTag(R.id.HeaderViewTag, headerView);
                        noStatement.setTag(R.id.QuestionTypeTag, Constants.NO_ANSWER);
                        break;
                }
                if (questionView != null) layoutParent.addView(questionView);
                iterBacks++;
            }
        }
    }

    private static View getView(int iterBacks, LayoutInflater inflater, GridLayout layoutParent, View headerView, Question question, Integer componentType, int questionType) {
        View questionView = inflater.inflate(componentType, layoutParent, false);
        questionView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));
        TextView statement = (TextView) questionView.findViewById(R.id.statement);
        statement.setText(question.getForm_name());
        EditText answerI = (EditText) questionView.findViewById(R.id.answer);
        answerI.setTag(R.id.QuestionTag, question);
        answerI.setTag(R.id.HeaderViewTag, headerView);
        answerI.setTag(R.id.QuestionTypeTag, questionType);

        // If the question has children, we load the denominator, else we hide the question
        if (!question.hasParent()) {
            //set header to visible
            headerView.setVisibility(View.VISIBLE);
        } else {
            questionView.setVisibility(View.GONE);
        }
        return questionView;
    }
}

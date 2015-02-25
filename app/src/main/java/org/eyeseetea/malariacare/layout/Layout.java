package org.eyeseetea.malariacare.layout;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Score;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.List;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {

    static final Score scores=new Score();

    // This method fill in a tab layout
    public static int insertTab(MainActivity mainActivity, Tab tab, int parent, boolean withScore, int childlayout) {

        int child = -1;
        // This layout inflater is for joining other layouts
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This layout is for the tab content (questions)
        LinearLayout layoutGrandParent = (LinearLayout) mainActivity.findViewById(parent);
        layoutGrandParent.setTag(tab);
        ScrollView layoutParentScroll = (ScrollView) layoutGrandParent.getChildAt(0);
        GridLayout layoutParent = (GridLayout) layoutParentScroll.getChildAt(0);

        // Given the layoutGrandParent, we use the addTag method to associate the tab object, being able to instanciate it later

        if (childlayout != -1){
            child = childlayout;
            View scoreView = inflater.inflate(child, layoutParent, false);
            layoutParent.addView(scoreView);
            return childlayout;
        }

        scores.addTabScore(parent);


        int total_denum = 0;
        // We do this to have a default value in the ddl
        Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);

        Log.i(".Layout", "Get View For Tab");
        TabHost tabHost = (TabHost)mainActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        Log.i(".Layout", "Generate Tab");
        String name = tab.getName();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(Long.toString(tab.getId())); // Here we set the tag, we'll use later to move between tabs
        tabSpec.setIndicator(name);
        tabSpec.setContent(parent);
        tabHost.addTab(tabSpec);

        Log.i(".Layout", "Generate Headers");
        List<Header> headers = tab.getHeaders();
        for (Header header: headers){
            // First we introduce header text according to the template
            child = R.layout.headers;
            //Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(child, layoutParent, false);
            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setText(header.getName());
            layoutParent.addView(headerView);

            //Log.i(".Layout", "Reader questions for header " + header.toString());
            List<Question> questionList = header.getQuestions();
            for (Question question : questionList){
                // The statement is present in every kind of question
                TextView statement;
                switch(question.getAnswer().getOutput()){
                    case Constants.DROPDOWN_LIST:
                        child = R.layout.ddl;
                        View questionView = inflater.inflate(child, layoutParent, false);

                        statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        statement.setTag(parent);
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);
                        // If the question has children, we load the denominator, else we hide the question
                        if (question.hasChildren()) {
                            questionView.setBackgroundColor(Color.parseColor("#d3ffce"));
                            denominator.setText(Float.toString(question.getDenominator_w()));
                            // After loading the denominator we increase the subtotal denominator value
                            scores.addValueDenominator(parent, question.getDenominator_w());
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(question);

                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Spinner spinner = (Spinner)parentView;
                                Option triggeredOption = (Option)spinner.getItemAtPosition(position);
                                Question triggeredQuestion = (Question)spinner.getTag();
                                TextView numeratorView = (TextView) Utils.findParentRecursively(spinner,R.id.ddl).findViewById(R.id.num);
                                TextView denominatorView = (TextView) Utils.findParentRecursively(spinner,R.id.ddl).findViewById(R.id.den);
                                TextView statementView=(TextView) Utils.findParentRecursively(spinner,R.id.ddl).findViewById(R.id.statement);
                                TextView partialScoreView = (TextView) Utils.findParentRecursively(spinner,MainActivity.getLayoutIds()).findViewById(R.id.score);
                                TextView numSubtotal = (TextView)((LinearLayout) Utils.findParentRecursively(spinner,MainActivity.getLayoutIds())).findViewById(R.id.total_num);
                                TextView denSubtotal = (TextView)((LinearLayout) Utils.findParentRecursively(spinner,MainActivity.getLayoutIds())).findViewById(R.id.total_den);


                                if (triggeredOption.getName() != null && triggeredOption.getName() != Constants.DEFAULT_SELECT_OPTION) { // This is for capture the user selection
                                    // First we do the calculus
                                    Float numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();
                                    Log.i(".Layout", "numerator: " + numerator);
                                    Float denominator=new Float(0);
                                    Float oldNumerator=new Float(0.0);
                                    Float oldDenominator=new Float(0.0);


                                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w())==0) {
                                        denominator=triggeredQuestion.getDenominator_w();
                                        Log.i(".Layout", "denominator: " + denominator);
                                    }
                                    else {
                                        if (triggeredQuestion.getNumerator_w().compareTo(new Float(0))==0 && triggeredQuestion.getDenominator_w().compareTo(new Float(0))!=0) {
                                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();
                                            Log.i(".Layout", "denominator: " + denominator);
                                        }
                                    }

                                    if (numeratorView.getText().toString()!="") oldNumerator = Float.parseFloat(numeratorView.getText().toString());
                                    if (denominatorView.getText().toString()!="") oldDenominator = Float.parseFloat(denominatorView.getText().toString());


                                    scores.resetValuesNumDenum((Integer)statementView.getTag(),oldNumerator,oldDenominator);
                                    scores.addValuesNumDenum((Integer)statementView.getTag(), numerator, denominator);

                                    // If the option is changed to positive numerator and has children, we need to recalculate the denominator taking those children into account and make children visible again
                                    if (numerator != 0.0F && triggeredQuestion.hasChildren()){
                                        View father = Utils.findParentRecursively(spinner, MainActivity.getLayoutIds());
                                        View son;
                                        for (Question childQuestion: triggeredQuestion.getQuestionChildren()){
                                            son = Utils.findChildRecursively(father, childQuestion);
                                            ((View)son.getParent().getParent()).setVisibility(View.VISIBLE);
                                            scores.addValueDenominator((Integer)statementView.getTag(), childQuestion.getDenominator_w());
                                        }
                                    }

                                    numSubtotal.setText(Float.toString(scores.getNumerator((Integer)statementView.getTag())));
                                    denSubtotal.setText(Float.toString(scores.getDenominator((Integer)statementView.getTag())));

                                }
                                else{ // This is for capturing the event when the user leaves the dropdown list without selecting any option
                                    numeratorView.setText(Float.toString(0.0F));
                                    denominatorView.setText(Float.toString(0.0F));
                                }
                                View tabView;
                                // We update numerator in the subtotal score layout
                                numSubtotal.setText(Float.toString(scores.getNumerator((Integer) statementView.getTag())));


                                //denSubtotal.setText(Float.toString(scores.getDenominator((Integer) statementView.getTag())));
                                partialScoreView.setText(Float.toString(scores.getPercent((Integer) statementView.getTag())));
                                // Then we set the score in the Score tab
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // your code here
                            }

                        });

                        List<Option> optionList = question.getAnswer().getOptions();
                        optionList.add(0, defaultOption);
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        dropdown.setAdapter(adapter);
                        layoutParent.addView(questionView);
                        break;
                    case Constants.INT:
                        child = R.layout.integer;
                        View questionIntView = inflater.inflate(child, layoutParent, false);
                        statement = (TextView) questionIntView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerI = (EditText) questionIntView.findViewById(R.id.answer);
                        layoutParent.addView(questionIntView);
                        break;
                    case Constants.LONG_TEXT:
                        child = R.layout.longtext;
                        View questionLTView = inflater.inflate(child, layoutParent, false);
                        statement = (TextView) questionLTView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerLT = (EditText) questionLTView.findViewById(R.id.answer);
                        layoutParent.addView(questionLTView);
                        break;
                    case Constants.SHORT_TEXT:
                        child = R.layout.shorttext;
                        View questionSTView = inflater.inflate(child, layoutParent, false);
                        statement = (TextView) questionSTView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerST = (EditText) questionSTView.findViewById(R.id.answer);
                        layoutParent.addView(questionSTView);
                        break;
                    case Constants.SHORT_DATE: case Constants. LONG_DATE:
                        child = R.layout.date;
                        View questionSDView = inflater.inflate(child, layoutParent, false);
                        statement = (TextView) questionSDView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerSD = (EditText) questionSDView.findViewById(R.id.answer);
                        layoutParent.addView(questionSDView);
                        break;
                }

            }
        }

        if (withScore) {
            // This layout is for showing the accumulated score
            GridLayout layoutParentScore = (GridLayout) layoutGrandParent.getChildAt(1);
            Log.i(".Layout", "Grandpa layout children: " + layoutGrandParent.getChildCount());
            child = R.layout.subtotal_num_dem;
            View subtotalView = inflater.inflate(child, layoutParentScore, false);
            TextView total_num_text = (TextView) subtotalView.findViewById(R.id.total_num);
            total_num_text.setText("0.0");
            TextView total_den_text = (TextView) subtotalView.findViewById(R.id.total_den);
            total_den_text.setText(Float.toString(scores.getDenominator(parent)));
            layoutParentScore.addView(subtotalView);
            Log.i(".Layout", "after generated tab: " + scores.getNumerator(parent) + " " + scores.getDenominator(parent));
        }

        return child;
    }
}



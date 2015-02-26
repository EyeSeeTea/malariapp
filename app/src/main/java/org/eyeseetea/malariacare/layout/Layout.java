package org.eyeseetea.malariacare.layout;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import org.eyeseetea.malariacare.utils.NumDenRecord;
import org.eyeseetea.malariacare.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {

    //static final Score scores=new Score();
    static Map<Integer, NumDenRecord> numDenRecordMap = new HashMap<Integer, NumDenRecord>();
    static final int [] backgrounds = {R.drawable.background_even, R.drawable.background_odd};

    // This method fill in a tab layout
    public static int insertTab(MainActivity mainActivity, Tab tab, int parent, boolean withScore, int childlayout) {

        int child = -1;
        int iterBacks = 0;
        // This layout inflater is for joining other layouts
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This layout is for the tab content (questions)
        LinearLayout layoutGrandParent = (LinearLayout) mainActivity.findViewById(parent);
        layoutGrandParent.setTag(tab);
        ScrollView layoutParentScroll = (ScrollView) layoutGrandParent.getChildAt(0);
        GridLayout layoutParent = (GridLayout) layoutParentScroll.getChildAt(0);

        // Given the layoutGrandParent, we use the addTag method to associate the tab object, being able to instanciate it later


        numDenRecordMap.put(parent, new NumDenRecord());

        BigDecimal decimalNumber;
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

        if (childlayout != -1){
            child = childlayout;
            View scoreView = inflater.inflate(child, layoutParent, false);
            layoutParent.addView(scoreView);
            return childlayout;
        }

        Log.i(".Layout", "Generate Headers");
        List<Header> headers = tab.getHeaders();
        for (Header header: headers){
            // First we introduce header text according to the template
            child = R.layout.headers;
            //Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(child, layoutParent, false);

            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setBackgroundResource(R.drawable.background_header);
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
                        questionView.setBackgroundResource(backgrounds[iterBacks%backgrounds.length]);

                        statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        statement.setTag(parent);
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);
                        // If the question has children, we load the denominator, else we hide the question
                        if (!question.hasParent()) {
                            if (question.hasChildren()) {
                                questionView.setBackgroundResource(R.drawable.background_parent);
                            }
                            denominator.setText(Utils.round(question.getDenominator_w()));

                            numDenRecordMap.get(parent).addRecord(question, 0F, question.getDenominator_w());
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(question);

                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Spinner spinner = (Spinner)parentView;
                                RelativeLayout spinnerFather = (RelativeLayout)spinner.getParent();
                                Option triggeredOption = (Option)spinner.getItemAtPosition(position);

                                Question triggeredQuestion = (Question)spinner.getTag();
                                TextView numeratorView = (TextView) Utils.findParentRecursively(spinner,R.id.ddl).findViewById(R.id.num);
                                TextView denominatorView = (TextView) Utils.findParentRecursively(spinner,R.id.ddl).findViewById(R.id.den);
                                TextView statementView=(TextView) Utils.findParentRecursively(spinner,R.id.ddl).findViewById(R.id.statement);
                                TextView partialScoreView = (TextView) Utils.findParentRecursively(spinner,MainActivity.getLayoutIds()).findViewById(R.id.score);
                                TextView numSubtotal = (TextView)((LinearLayout) Utils.findParentRecursively(spinner,MainActivity.getLayoutIds())).findViewById(R.id.total_num);
                                TextView denSubtotal = (TextView)((LinearLayout) Utils.findParentRecursively(spinner,MainActivity.getLayoutIds())).findViewById(R.id.total_den);
                                BigDecimal decimalNumber;
                                Float numerator, denominator;

                                if (triggeredOption.getName() != null && triggeredOption.getName() != Constants.DEFAULT_SELECT_OPTION) { // This is for capture the user selection
                                    // First we do the calculus
                                    numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();
                                    Log.i(".Layout", "numerator: " + numerator);
                                    denominator=new Float(0.0F);

                                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w())==0) {
                                        denominator = triggeredQuestion.getDenominator_w();
                                        Log.i(".Layout", "denominator: " + denominator);
                                    }
                                    else {
                                        if (triggeredQuestion.getNumerator_w().compareTo(new Float(0.0F))==0 && triggeredQuestion.getDenominator_w().compareTo(new Float(0.0F))!=0) {
                                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();
                                            Log.i(".Layout", "denominator: " + denominator);
                                        }
                                    }

                                    numDenRecordMap.get((Integer)statementView.getTag()).addRecord(triggeredQuestion, numerator, denominator);


                                    // If the option is changed to positive numerator and has children, we need to recalculate the denominator taking those children into account and make children visible again
                                    if (triggeredQuestion.hasChildren()){
                                        View parent = Utils.findParentRecursively(spinner, MainActivity.getLayoutIds());
                                        View child;
                                        for (Question childQuestion: triggeredQuestion.getQuestionChildren()){
                                            child = Utils.findChildRecursively(parent, childQuestion);
                                            ((View)child.getParent().getParent()).setVisibility(View.VISIBLE);
                                            if (numerator != 0.0F) {
                                                numDenRecordMap.get((Integer)statementView.getTag()).addRecord(childQuestion, 0F, childQuestion.getDenominator_w());
                                            }
                                            else{
                                                numDenRecordMap.get((Integer)statementView.getTag()).deleteRecord(childQuestion);
                                            }
                                        }
                                    }

                                    numeratorView.setText(Utils.round(numerator));
                                    denominatorView.setText(Utils.round(denominator));

                                }
                                else{
                                // This is for capturing the event when the user leaves the dropdown list without selecting any option
                                    numerator = new Float(0.0F);
                                    denominator = triggeredQuestion.getDenominator_w();
                                    if (selectedItemView != null) {
                                        numeratorView.setText(Utils.round(numerator));
                                        denominatorView.setText(Utils.round(denominator));
                                    }
                                    numDenRecordMap.get((Integer)statementView.getTag()).addRecord(triggeredQuestion, numerator, denominator);
                                }


                                List<Float> numDenSubTotal = numDenRecordMap.get((Integer)statementView.getTag()).calculateNumDenTotal();

                                if (numSubtotal != null && denSubtotal != null && partialScoreView != null) {
                                    numSubtotal.setText(Utils.round(numDenSubTotal.get(0)));
                                    denSubtotal.setText(Utils.round(numDenSubTotal.get(1)));
                                    partialScoreView.setText(Utils.round((numDenSubTotal.get(0) / numDenSubTotal.get(1)) * 100));
                                }

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
                        questionIntView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                        statement = (TextView) questionIntView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerI = (EditText) questionIntView.findViewById(R.id.answer);
                        layoutParent.addView(questionIntView);
                        break;
                    case Constants.LONG_TEXT:
                        child = R.layout.longtext;
                        View questionLTView = inflater.inflate(child, layoutParent, false);
                        questionLTView.setBackgroundResource(backgrounds[iterBacks%backgrounds.length]);
                        statement = (TextView) questionLTView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerLT = (EditText) questionLTView.findViewById(R.id.answer);
                        layoutParent.addView(questionLTView);
                        break;
                    case Constants.SHORT_TEXT:
                        child = R.layout.shorttext;
                        View questionSTView = inflater.inflate(child, layoutParent, false);
                        questionSTView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                        statement = (TextView) questionSTView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerST = (EditText) questionSTView.findViewById(R.id.answer);
                        layoutParent.addView(questionSTView);
                        break;
                    case Constants.SHORT_DATE: case Constants. LONG_DATE:
                        child = R.layout.date;
                        View questionSDView = inflater.inflate(child, layoutParent, false);
                        questionSDView.setBackgroundResource(backgrounds[iterBacks%backgrounds.length]);
                        statement = (TextView) questionSDView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerSD = (EditText) questionSDView.findViewById(R.id.answer);
                        layoutParent.addView(questionSDView);
                        break;
                }
                iterBacks++;
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
            List<Float> numDenSubTotal = numDenRecordMap.get(parent).calculateNumDenTotal();
            total_den_text.setText(Utils.round(numDenSubTotal.get(1)));
            layoutParentScore.addView(subtotalView);
            Log.i(".Layout", "after generated tab: " + numDenSubTotal.get(0) + " " + numDenSubTotal.get(1));
        }

        return child;
    }
}



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

import java.util.List;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {
    public static int insertTab(MainActivity mainActivity, Tab tab, int parent, int scoreTab) {
        GridLayout layoutParent = (GridLayout) mainActivity.findViewById(parent);
        LinearLayout layoutScoreTab = (LinearLayout) mainActivity.findViewById(scoreTab);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int child = -1;
        int total_denum = 0;
        // We do this to have a default value in the ddl
        Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);

        Log.i(".Layout", "Get View For Tab");
        TabHost tabHost = (TabHost)mainActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        Log.i(".Layout", "Generate Tab");
        String name = tab.getName();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Question tab");
        tabSpec.setIndicator(name);
        tabSpec.setContent(parent);
        tabHost.addTab(tabSpec);

        Log.i(".Layout", "Generate Headers");
        List<Header> headers = tab.getHeaders();
        for (Header header: headers){
            // First we introduce header text according to the template
            child = R.layout.headers;
            Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(child, layoutParent, false);
            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setText(header.getName());
            layoutParent.addView(headerView);

            Log.i(".Layout", "Reader questions for header " + header.toString());
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

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(question);

                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Spinner spinner = (Spinner)parentView;
                                Option triggeredOption = (Option)spinner.getItemAtPosition(position);
                                Question triggeredQuestion = (Question)spinner.getTag();


                                if (triggeredOption.getName() != null && triggeredOption.getName() != Constants.DEFAULT_SELECT_OPTION) {
                                    Float numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();

                                    ((TextView)((View)spinner.getParent().getParent().getParent().getParent().getParent()).findViewById(R.id.total_num)).setText("10");



                                    //String text= ((TextView)((View)spinner.getParent().getParent().getParent().getParent().getParent()).findViewById(R.id.total_num)).getText().toString();

                                    Float denominator=new Float(0);

                                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w())==0)
                                        denominator=triggeredQuestion.getDenominator_w();
                                    else
                                        if (triggeredQuestion.getNumerator_w().compareTo(new Float(0))==0 && triggeredQuestion.getDenominator_w().compareTo(new Float(0))!=0)
                                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();

                                    ((TextView) ((View) spinner.getParent().getParent()).findViewById(R.id.num)).setText(Float.toString(numerator));
                                    ((TextView) ((View) spinner.getParent().getParent()).findViewById(R.id.den)).setText(Float.toString(denominator));
                                }
                                else{
                                    ((TextView) ((View) spinner.getParent().getParent()).findViewById(R.id.num)).setText(Float.toString(0.0F));
                                    ((TextView) ((View) spinner.getParent().getParent()).findViewById(R.id.den)).setText(Float.toString(0.0F));
                                }



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

        child = R.layout.totalscore_tab;
        View totalscoreView = inflater.inflate(child,layoutScoreTab,false);
        //TextView numValue = (TextView) totalscoreView.findViewById(R.id.total_num);
        //TextView denValue = (TextView) totalscoreView.findViewById(R.id.total_den);
        layoutScoreTab.addView(totalscoreView);


        return child;
    }


}



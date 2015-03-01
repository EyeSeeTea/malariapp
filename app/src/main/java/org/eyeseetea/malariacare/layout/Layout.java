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
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.adapters.ReportingResultsArrayAdapter;
import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.models.ReportingResults;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.LoadCustomQuestions;
import org.eyeseetea.malariacare.utils.NumDenRecord;
import org.eyeseetea.malariacare.utils.TabConfiguration;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {

    //static final Score scores=new Score();
    static final Map<Integer, NumDenRecord> numDenRecordMap = new HashMap<Integer, NumDenRecord>();
    static final int [] backgrounds = {R.drawable.background_even, R.drawable.background_odd};

    // This method fill in a tab layout
    public static void insertTab(MainActivity mainActivity, Tab tab, final TabConfiguration tabConfiguration) {
        Log.i(".Layout", "Generating Tab " + tab.getName());

        //Iterator for background (odd and even)
        int iterBacks = 0;

        // This layout inflater is for joining other layouts
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This layout is for the tab content (questions)
        LinearLayout layoutGrandParent = (LinearLayout) mainActivity.findViewById(tabConfiguration.getTabId());
        layoutGrandParent.setTag(tab);
        ScrollView layoutParentScroll = (ScrollView) layoutGrandParent.getChildAt(0);
        GridLayout layoutParent = (GridLayout) layoutParentScroll.getChildAt(0);

        //Initialize numerator and denominator record map
        numDenRecordMap.put(tabConfiguration.getTabId(), new NumDenRecord());

        // We do this to have a default value in the ddl
        Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);

        Log.i(".Layout", "Get View For Tab");
        TabHost tabHost = (TabHost)mainActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        Log.i(".Layout", "Generate Tab");
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(Long.toString(tab.getId())); // Here we set the tag, we'll use later to move between tabs
        tabSpec.setIndicator(tab.getName());
        tabSpec.setContent(tabConfiguration.getTabId());
        tabHost.addTab(tabSpec);

        if (tabConfiguration.getLayoutId() != null){
            generateManualTab(mainActivity, tabConfiguration, inflater, layoutParent);
            return;
        }

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
                // The statement is present in every kind of question
                switch(question.getAnswer().getOutput()){
                    case Constants.DROPDOWN_LIST:
                        questionView = inflater.inflate(R.layout.ddl, layoutParent, false);
                        questionView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);

                        TextView statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(R.id.QuestionTag, question);
                        dropdown.setTag(R.id.HeaderViewTag, headerView);
                        dropdown.setTag(R.id.NumeratorViewTag, questionView.findViewById(R.id.num));
                        dropdown.setTag(R.id.DenominatorViewTag, questionView.findViewById(R.id.den));
                        dropdown.setTag(R.id.Tab, tabConfiguration.getTabId());

                        // If the question has children, we load the denominator, else we hide the question
                        if (!question.hasParent()) {
                            if (question.hasChildren()) questionView.setBackgroundResource(R.drawable.background_parent);

                            denominator.setText(Utils.round(question.getDenominator_w()));
                            headerView.setVisibility(View.VISIBLE);

                            numDenRecordMap.get(tabConfiguration.getTabId()).addRecord(question, 0F, question.getDenominator_w());
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        createDropDownListener(tabConfiguration, dropdown);

                        List<Option> optionList = question.getAnswer().getOptions();
                        optionList.add(0, defaultOption);
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                        dropdown.setAdapter(adapter);
                        break;
                    case Constants.INT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.integer);
                        break;
                    case Constants.LONG_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.longtext);
                        break;
                    case Constants.SHORT_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.shorttext);
                        break;
                    case Constants.SHORT_DATE: case Constants. LONG_DATE:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.date);
                        break;
                }
                layoutParent.addView(questionView);
                iterBacks++;
            }
        }

        if (tabConfiguration.isAutomaticTab()) {
            // This layout is for showing the accumulated score
            GridLayout layoutParentScore = (GridLayout) layoutGrandParent.getChildAt(1);
            Log.i(".Layout", "Grandpa layout children: " + layoutGrandParent.getChildCount());
            View subtotalView = inflater.inflate(R.layout.subtotal_num_dem, layoutParentScore, false);
            TextView totalNumText = (TextView) subtotalView.findViewById(R.id.totalNum);
            TextView totalDenText = (TextView) subtotalView.findViewById(R.id.totalDen);
            totalNumText.setText("0.0");
            List<Float> numDenSubTotal = numDenRecordMap.get(tabConfiguration.getTabId()).calculateNumDenTotal();
            totalDenText.setText(Utils.round(numDenSubTotal.get(1)));

            layoutParentScore.addView(subtotalView);
            TextView subscoreView = (TextView) subtotalView.findViewById(R.id.score);

            // Now, for being able to write Score in the score tab and score averages in its place (in score tab), we use setTag() to include a pointer to
            // the score View id, and in that id, we include a pointer to the average view id. This way, we can do the calculus here and represent there
            Integer generalScoreId = tabConfiguration.getScoreFieldId();
            if (tabConfiguration.getScoreFieldId() != null) {
                subscoreView.setTag(generalScoreId);
            }

            Log.i(".Layout", "after generated tab: " + numDenSubTotal.get(0) + " " + numDenSubTotal.get(1));
        }

    }

    private static void createDropDownListener(final TabConfiguration tabConfiguration, Spinner dropdown) {
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) parentView;
                Option triggeredOption = (Option) spinner.getItemAtPosition(position);

                Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);
                TextView numeratorView = (TextView) spinner.getTag(R.id.NumeratorViewTag);
                TextView denominatorView = (TextView) spinner.getTag(R.id.DenominatorViewTag);
                LinearLayout tabLayout = ((LinearLayout) LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab)));

                // Tab scores View
                TextView numSubtotal = (TextView) tabLayout.findViewById(R.id.totalNum);
                TextView denSubtotal = (TextView) tabLayout.findViewById(R.id.totalDen);
                TextView partialScoreView = (TextView) tabLayout.findViewById(R.id.score);
                // General scores View
                Integer generalScoreId = null, generalScoreAvgId = null;
                TextView generalScoreView = null, generalScoreAvgView = null;
                if (tabConfiguration.getScoreFieldId() != null) {
                    generalScoreId = ((Integer) partialScoreView.getTag());
                    View gridView = LayoutUtils.findParentRecursively(spinner, R.id.Grid);
                    generalScoreView = (TextView) gridView.findViewById(generalScoreId);
                    if (tabConfiguration.getScoreAvgFieldId() != null) {
                        generalScoreAvgId = ((Integer) tabConfiguration.getScoreAvgFieldId());
                        generalScoreAvgView = (TextView) gridView.findViewById(generalScoreAvgId);
                    }
                }

                Float numerator, denominator;
                if (triggeredOption.getName() != null && triggeredOption.getName() != Constants.DEFAULT_SELECT_OPTION) { // This is for capture the user selection
                    // First we do the calculus
                    numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();
                    Log.i(".Layout", "numerator: " + numerator);
                    denominator = new Float(0.0F);

                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w()) == 0) {
                        denominator = triggeredQuestion.getDenominator_w();
                        Log.i(".Layout", "denominator: " + denominator);
                    } else {
                        if (triggeredQuestion.getNumerator_w().compareTo(new Float(0.0F)) == 0 && triggeredQuestion.getDenominator_w().compareTo(new Float(0.0F)) != 0) {
                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();
                            Log.i(".Layout", "denominator: " + denominator);
                        }
                    }

                    numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(triggeredQuestion, numerator, denominator);

                    // If the option is changed to positive numerator and has children, we need to show the children and take their denominators into account
                    if (triggeredQuestion.hasChildren()) {
                        View parent = LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab));
                        for (Question childQuestion : triggeredQuestion.getQuestionChildren()) {
                            View childView = LayoutUtils.findChildRecursively(parent, childQuestion);
                            if (position == 1) { //FIXME: There must be a smarter way for saying "if the user selected yes"
                                LayoutUtils.toggleVisible(childView, View.VISIBLE);
                                ((View) ((View) childView).getTag(R.id.HeaderViewTag)).setVisibility(View.VISIBLE);
                                numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(childQuestion, 0F, childQuestion.getDenominator_w());
                            } else {
                                LayoutUtils.toggleVisible(childView, View.GONE);
                                if (LayoutUtils.isHeaderEmpty(triggeredQuestion.getQuestionChildren(), childQuestion.getHeader().getQuestions())) {
                                    ((View) ((View) childView).getTag(R.id.HeaderViewTag)).setVisibility(View.GONE);
                                }
                                numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).deleteRecord(childQuestion);
                            }
                        }
                    }

                    numeratorView.setText(Utils.round(numerator));
                    denominatorView.setText(Utils.round(denominator));

                } else {
                    // This is for capturing the event when the user leaves the dropdown list without selecting any option
                    numerator = new Float(0.0F);
                    denominator = triggeredQuestion.getDenominator_w();
                    if (selectedItemView != null) {
                        numeratorView.setText(Utils.round(numerator));
                        denominatorView.setText(Utils.round(denominator));
                    }
                    numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(triggeredQuestion, numerator, denominator);
                }


                List<Float> numDenSubTotal = numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).calculateNumDenTotal();

                if (numSubtotal != null && denSubtotal != null && partialScoreView != null) {
                    numSubtotal.setText(Utils.round(numDenSubTotal.get(0)));
                    denSubtotal.setText(Utils.round(numDenSubTotal.get(1)));
                    float score = (numDenSubTotal.get(0) / numDenSubTotal.get(1)) * 100;
                    float average = 0.0F;
                    TextView elementView = null;
                    partialScoreView.setText(Utils.round(score)); // We set the score in the tab score
                    if (tabConfiguration.getScoreFieldId() != null) {
                        generalScoreView.setText(Utils.round(score)); // We set the score in the score tab
                        if(tabConfiguration.getScoreAvgFieldId() != null){
                            List<Integer> averageElements = (ArrayList<Integer>) generalScoreAvgView.getTag();
                            if (averageElements == null) {
                                averageElements = new ArrayList<Integer>();
                                averageElements.add(generalScoreId);
                                generalScoreAvgView.setText(Utils.round(score));
                                generalScoreAvgView.setTag(averageElements);
                            } else {
                                boolean found = false;
                                for (Integer element : averageElements) {
                                    if (element.intValue() == generalScoreId) found = true;
                                    average += Float.parseFloat((String) ((TextView) LayoutUtils.findParentRecursively(generalScoreView, R.id.scoreTable).findViewById(element)).getText());
                                }
                                if ( !found ) averageElements.add(generalScoreId);
                                average = average / averageElements.size();
                                generalScoreAvgView.setText(Utils.round(average));
                                generalScoreAvgView.setTag(averageElements);
                            }
                        }
                    }
                }

                // Then we set the score in the Score tab

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private static View getView(int iterBacks, LayoutInflater inflater, GridLayout layoutParent, View headerView, Question question, Integer componentType) {
        View questionView = inflater.inflate(componentType, layoutParent, false);
        questionView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
        TextView statement = (TextView) questionView.findViewById(R.id.statement);
        statement.setText(question.getForm_name());
        EditText answerI = (EditText) questionView.findViewById(R.id.answer);
        answerI.setTag(R.id.QuestionTag, question);
        answerI.setTag(R.id.HeaderViewTag, headerView);

        // If the question has children, we load the denominator, else we hide the question
        if (!question.hasParent()) {
            //set header to visible
            headerView.setVisibility(View.VISIBLE);
        } else {
            questionView.setVisibility(View.GONE);
        }
        return questionView;
    }

    private static void generateManualTab(MainActivity mainActivity, TabConfiguration tabConfiguration, LayoutInflater inflater, GridLayout layoutParent) {
        View customView = inflater.inflate(tabConfiguration.getLayoutId(), layoutParent, false);

        switch (tabConfiguration.getLayoutId()){
            case R.layout.scoretab:
                layoutParent.addView(customView);
                break;
            case R.layout.reportingtab:
                ListView list=(ListView) customView.findViewById(R.id.listView);
                ArrayAdapter<ReportingResults> adapter = new ReportingResultsArrayAdapter(mainActivity, LoadCustomQuestions.addReportingQuestions());
                list.setAdapter(adapter);
                layoutParent.addView(customView);
                break;
            case R.layout.adherencetab:
                ListView list_supervision = (ListView) customView.findViewById(R.id.listTestSupervisor);
                //ArrayAdapter<DataHolder> adapterSupervision = new IQATestArrayAdapter(mainActivity, )
                break;

            case R.layout.iqatab:
                //Mi mierda
                break;

        }

        return;
    }
}



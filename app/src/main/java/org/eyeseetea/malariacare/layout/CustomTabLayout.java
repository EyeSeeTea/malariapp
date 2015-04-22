package org.eyeseetea.malariacare.layout;

import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositiveScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.listeners.CustomTabListeners;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CustomTabLayout {

    public static void generateCustomTab(MainActivity mainActivity, Tab tab, LayoutInflater inflater, GridLayout layoutParent) {
        View customView = inflater.inflate(LayoutConfiguration.getTabsConfiguration().get(tab).getLayoutId(), layoutParent, false);
        boolean getFromDatabase = false;
        boolean hasScoreLayout = false;
        // Array to get the needed layouts during question insertion
        List<Integer> layoutsToUse = new ArrayList<Integer>();
        // Array to capture and process events when user selection is done
        List<AdapterView.OnItemSelectedListener> listeners = new ArrayList<AdapterView.OnItemSelectedListener>(),
                listeners2 = new ArrayList<AdapterView.OnItemSelectedListener>();
        List<List> listenerTypes = new ArrayList<List>();
        List<TextWatcher> editListeners = new ArrayList<TextWatcher>();
        int iterListenerType = 0;
        int iterListeners = 0;
        int iterEditListeners = 0;
        int iterBacks = 0;

        switch (LayoutConfiguration.getTabsConfiguration().get(tab).getLayoutId()){
            case R.layout.scoretab:
                layoutParent.addView(customView);
                break;
            case R.layout.reportingtab:
                getFromDatabase = true;
                layoutsToUse.add(R.layout.reporting_record);
                layoutsToUse.add(R.layout.reporting_record2);
                TextWatcher editListener = CustomTabListeners.createReportingListener(mainActivity);
                TextWatcher editListener2 = CustomTabListeners.createReportingListener(mainActivity);
                editListeners.add(editListener);
                editListeners.add(editListener2);
                layoutParent.addView(customView);
                break;
            case R.layout.adherencetab:
                Switch visibility = (Switch) customView.findViewById(R.id.visibilitySwitch);
                CustomTabListeners.createAdherenceSwitchListener(visibility);
                getFromDatabase = true;
                layoutsToUse.add(R.layout.pharmacy_register);
                layoutsToUse.add(R.layout.pharmacy_register2);
                // Add onItemSelectedListener to manage score
                AdapterView.OnItemSelectedListener listener = CustomTabListeners.createAdherenceListener(1);
                listeners.add(listener);
                listenerTypes.add(listeners);
                AdapterView.OnItemSelectedListener listener2 = CustomTabListeners.createAdherenceListener(2);
                listeners2.add(listener2);
                listenerTypes.add(listeners2);
                layoutParent.addView(customView);
                break;

            case R.layout.iqatab:
                getFromDatabase = true;
                layoutsToUse.add(R.layout.iqatab_record);
                layoutsToUse.add(R.layout.iqatab_record);
                // Add onItemSelectedListener to manage score
                AdapterView.OnItemSelectedListener tabListener = CustomTabListeners.createIQAListener(customView, R.id.labStaffTable, R.id.matchTable);
                listeners.add(tabListener);
                listenerTypes.add(listeners);
                AdapterView.OnItemSelectedListener tabListener2 = CustomTabListeners.createIQAListener(customView, R.id.supervisorTable, R.id.matchTable);
                listeners2.add(tabListener2);
                listenerTypes.add(listeners2);
                layoutParent.addView(customView);
                break;
            case R.layout.compositivescoretab:
                // FIXME: Be careful here. For the rest of the tabs, this is not the place where the questions are being placed in their rows, but in the case of this tab it is. We may rethink all the process and do it in the same way
                for (CompositiveScore compositiveScore : CompositiveScore.listAll(CompositiveScore.class)){
                    ScoreRegister.registerScore(compositiveScore);
                    List<View> tables = LayoutUtils.getChildrenByTag((ViewGroup) customView, null, "CompositivesScore");
                    if(tables.size() == 1) {
                        TableLayout table = (TableLayout) tables.get(0);
                        View rowView = inflater.inflate(R.layout.compositive_scores_record, table, false);
                        rowView.setTag("CompositiveScore_" + compositiveScore.getId());
                        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));
                        ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(0)).getChildAt(0)).setText(compositiveScore.getCode());
                        ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(1)).getChildAt(0)).setText(compositiveScore.getLabel());
                        table.addView(rowView);
                        iterBacks++;
                    }
                    else{
                        Log.e(".Layout", "Error: Header name is supposed to be used to distinguish where to place associated questions in custom tabs, but when looking for header named CompositivesScore we've found " + tables.size() + " results");
                    }
                }
                layoutParent.addView(customView);
                break;
        }

        // Some manual tabs, like adherence and IQA EQA get their questions from the database, here we manage how they are represented in the layout
        // as long as they don't use the same convention.
        // Standards:
        //  * They use phantom questions to group some related questions and so have them referenced (a parent question only indicates their children are related)
        //  * Questions are represented in tables. Header name will be set as a tag in the TableLayout (directly in the xml) component where its questions have to be represented
        //  * Once found, we iterate on the parent questions (with children) and represent each of their questions in a different column
        //  * Any score or thing that affects all the row questions, will be added to the parent question
        //  * Another important thing to improve is that at this moment I'm creating a List called layoutsToUse that contains, ordered, the different layouts that must be used for each questions group
        if (getFromDatabase){
            iterListenerType = 0;
            List<Header> headers = tab.getHeaders();
            for (int i=0; i<headers.size(); i++){
                iterBacks = 0;
                String headerName = headers.get(i).getName(); // this is also the ID
                // This tables list must be a list of only one element if we have not failed in layout creation
                List<View> tables = LayoutUtils.getChildrenByTag((ViewGroup)customView, null, headerName);
                if(tables.size() == 1){
                    TableLayout table = (TableLayout)tables.get(0);
                    // Now we have the table element, we have to search for the parent questions
                    List <Question> questions = headers.get(i).getQuestions(); // FIXME: improve this search to get only the parent questions
                    for (Question question: questions) {
                        // If the question is a parent, do don't show it but use it to put the row layout
                        if (question.getQuestion() == null) { // FIXME: when the search above is improve this check will be unnecessary

                            View rowView = inflater.inflate(layoutsToUse.get(i), table, false);

                            if (question.hasChildren()){
                                List<Question> children = question.getQuestionChildren();

                                iterListeners = 0;
                                iterEditListeners = 0;
                                EditText answer = null;
                                int offset = 0;

                                // Set the row number
                                TextView number = (TextView) rowView.findViewById(R.id.number);
                                if (number != null) {
                                    number.setText(question.getForm_name());
                                    offset = offset + 1;
                                }

                                // Set the row background
                                rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(iterBacks));
                                table.addView(rowView);
                                Log.d(".Layout", "Row Question");

                                for (int j = 0; j < children.size(); j++) {
                                    if (children.get(j).getAnswer() != null) {
                                        // Check previous existing value
                                        Value value = children.get(j).getValueBySession();
                                        switch (children.get(j).getAnswer().getOutput()) {
                                            case Constants.DROPDOWN_LIST:
                                                Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);
                                                List<Option> optionList = children.get(j).getAnswer().getOptions();
                                                optionList.add(0, defaultOption);
                                                ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                                                adapter.setDropDownViewResource(R.layout.simple_spinner_item);
                                                Spinner dropdown = (Spinner) ((ViewGroup)((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0)).getChildAt(0); // We take the spinner
                                                dropdown.setTag(R.id.QuestionTypeTag, Constants.DROPDOWN_LIST);
                                                dropdown.setTag(R.id.QuestionTag, children.get(j));
                                                dropdown.setAdapter(adapter);
                                                if ("listener".equals(dropdown.getTag())) { // listeners that contribute to punctuation
                                                    dropdown.setOnItemSelectedListener(((List<AdapterView.OnItemSelectedListener>)listenerTypes.get(iterListenerType)).get(iterListeners));
                                                    iterListeners++;
                                                } else { // listeners that do not contribute. Only for local storage
                                                    CustomTabListeners.createDropDownListener(dropdown, mainActivity);
                                                }
                                                if (value != null) dropdown.setSelection(optionList.indexOf(value.getOption()));

                                                break;
                                            case Constants.INT:
                                                Log.d(".Layout", "Question int");
                                                answer = (EditText) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.INT);
                                                answer.setTag(R.id.QuestionTag, children.get(j));
                                                if (value != null) answer.setText(value.getValue());
                                                if ("listener".equals(answer.getTag())) {
                                                    answer.addTextChangedListener(editListeners.get(iterEditListeners));
                                                    iterEditListeners++;
                                                } else {
                                                    CustomTabListeners.createTextListener(answer, mainActivity);
                                                }
                                                break;
                                            case Constants.LONG_TEXT:
                                                Log.i(".Layout", "Question longtext");
                                                answer = (EditText) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.LONG_TEXT);
                                                answer.setTag(R.id.QuestionTag, children.get(j));
                                                if (value != null) answer.setText(value.getValue());
                                                if ("listener".equals(answer.getTag())) {
                                                    answer.addTextChangedListener(editListeners.get(iterEditListeners));
                                                    iterEditListeners++;
                                                } else {
                                                    CustomTabListeners.createTextListener(answer, mainActivity);
                                                }
                                                break;
                                            case Constants.SHORT_TEXT:
                                                Log.i(".Layout", "Question shorttext");
                                                answer = (EditText) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.SHORT_TEXT);
                                                answer.setTag(R.id.QuestionTag, children.get(j));
                                                if (value != null) answer.setText(value.getValue());
                                                if ("listener".equals(answer.getTag())) {
                                                    answer.addTextChangedListener(editListeners.get(iterEditListeners));
                                                    iterEditListeners++;
                                                } else {
                                                    CustomTabListeners.createTextListener(answer, mainActivity);
                                                }
                                                break;
                                            case Constants.DATE:
                                                Log.i(".Layout", "Question date");
                                                answer = (EditText) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.DATE);
                                                answer.setTag(R.id.QuestionTag, children.get(j));
                                                if (value != null) answer.setText(value.getValue());
                                                CustomTabListeners.createTextListener(answer, mainActivity);
                                                break;
                                        }
                                    } else {
                                        ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0)).setText(children.get(j).getForm_name());
                                    }
                                }
                                iterBacks++;
                            } else{
                                ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(0)).getChildAt(0)).setText(question.getForm_name());
                                table.addView(rowView);
                            }
                        }
                    }
                }else{
                    Log.e(".Layout", "Error: Header name is supposed to be used to distinguish where to place associated questions in custom tabs, but when looking for header named " + headerName + " we've found " + tables.size() + " results");
                }
                iterListenerType++;
            }
        }
    }
}

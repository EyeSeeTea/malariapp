package org.eyeseetea.malariacare.layout.listeners;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

public class CustomTabListeners {

    public static void createDropDownListener(Spinner dropdown, final MainActivity mainActivity){
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) parentView;
                Option triggeredOption = (Option) spinner.getItemAtPosition(position);
                Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);

                Value value = triggeredQuestion.getValue(MainActivity.session.getSurvey());
                // If the value is not found we create one
                if (value == null) {
                    value = new Value(triggeredOption, triggeredQuestion, MainActivity.session.getSurvey());
                    value.save();
                } else {
                    value.setOption(triggeredOption);
                    value.setValue(triggeredOption.getName());
                    value.save();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    public static AdapterView.OnItemSelectedListener createAdherenceListener(int type){
        switch(type){
            case 1:
                return new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // This will occur when an item is selected in first table Adherence spinners
                        // For this table, when Test result is "Malaria Positive" score=1, score=0 otherwise
                        int score = 0;
                        float totalScore = 0.0F;
                        if (position == 1) score = 1;
                        else score = 0;

                        Spinner spinner = (Spinner) parent;
                        Option triggeredOption = (Option) spinner.getItemAtPosition(position);
                        Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);

                        TextView scoreText = (TextView)((ViewGroup)((ViewGroup)((ViewGroup)parent.getParent().getParent().getParent())).getChildAt(5)).getChildAt(0);
                        scoreText.setText((String)Integer.toString(score));
                        // Set the total score in the score tab
                        LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(parent, LayoutConfiguration.getTabsConfigurationIds());

                        // Persistence in local database
                        Value value = triggeredQuestion.getValue(MainActivity.session.getSurvey());
                        // If the value is not found we create one
                        if (value == null) {
                            value = new Value(triggeredOption, triggeredQuestion, MainActivity.session.getSurvey());
                            value.save();
                        } else {
                            value.setOption(triggeredOption);
                            value.setValue(triggeredOption.getName());
                            value.save();
                        }

                        TableLayout table1 = (TableLayout)tabLayout.findViewById(R.id.register1Table);
                        for (int i=1; i<((ViewGroup) table1).getChildCount(); i++){
                            TableRow row = (TableRow) table1.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(5)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        TableLayout table2 = (TableLayout)tabLayout.findViewById(R.id.register2Table);
                        for (int i=1; i<((ViewGroup) table2).getChildCount(); i++){
                            TableRow row = (TableRow) table2.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(4)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(parent, R.id.Grid);
                        TextView totalScoreView = (TextView) root.findViewById(R.id.adherenceScore);
                        totalScore = totalScore*100.0F/40.0F;
                        LayoutUtils.setScore(totalScore, totalScoreView);

                        TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                        TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                        TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                        LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                };
            case 2:
                return new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // This will occur when an item is selected in second table Adherence spinners
                        // For this table, when Test results is RDT* then ACT Prescribed=Yes means score=1, otherwise score=0
                        //               , when Test results is Microscopy* then ACT Prescribed=No means Score=1, otherwise score=0
                        int score = 0;
                        float totalScore = 0.0F;
                        TextView actPrescribed = (TextView)((ViewGroup)((ViewGroup)((ViewGroup)parent.getParent().getParent().getParent())).getChildAt(2)).getChildAt(0);

                        Spinner spinner = (Spinner) parent;
                        Option triggeredOption = (Option) spinner.getItemAtPosition(position);
                        Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);

                        if("RDT Positive".equals(actPrescribed.getText()) || "RDT Negative".equals(actPrescribed.getText())){
                            if (position == 1) score=1;
                            else score=0;
                        }else if("Microscopy Positive".equals(actPrescribed.getText()) || "Microscopy Negative".equals(actPrescribed.getText())){
                            if (position == 2) score=1;
                            else score=0;
                        }
                        TextView scoreText = (TextView)((ViewGroup)((ViewGroup)((ViewGroup)parent.getParent().getParent().getParent())).getChildAt(4)).getChildAt(0);
                        scoreText.setText((String)Integer.toString(score));
                        // Set the total score in the score tab
                        LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(parent, LayoutConfiguration.getTabsConfigurationIds());

                        // Persistence in local database
                        Value value = triggeredQuestion.getValue(MainActivity.session.getSurvey());
                        // If the value is not found we create one
                        if (value == null) {
                            value = new Value(triggeredOption, triggeredQuestion, MainActivity.session.getSurvey());
                            value.save();
                        } else {
                            value.setOption(triggeredOption);
                            value.setValue(triggeredOption.getName());
                            value.save();
                        }

                        TableLayout table1 = (TableLayout)tabLayout.findViewById(R.id.register1Table);
                        for (int i=1; i<((ViewGroup) table1).getChildCount(); i++){
                            TableRow row = (TableRow) table1.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(5)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        TableLayout table2 = (TableLayout)tabLayout.findViewById(R.id.register2Table);
                        for (int i=1; i<((ViewGroup) table2).getChildCount(); i++){
                            TableRow row = (TableRow) table2.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(4)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(parent, R.id.Grid);
                        TextView totalScoreView = (TextView) root.findViewById(R.id.adherenceScore);
                        totalScore = totalScore*100.0F/40.0F;
                        LayoutUtils.setScore(totalScore, totalScoreView);

                        TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                        TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                        TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                        LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                };
        }
        return null;
    }

    public static void createAdherenceSwitchListener(Switch switchView){
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LinearLayout grandpa = (LinearLayout) ((ViewGroup) buttonView.getParent()).getParent();
                for (int i=1; i<grandpa.getChildCount(); i++){
                    if (isChecked) (grandpa.getChildAt(i)).setVisibility(View.VISIBLE);
                    else (grandpa.getChildAt(i)).setVisibility(View.GONE);
                }
                // We set invisible also the subscore layout
                if (isChecked) ((GridLayout)((ViewGroup)((ViewGroup)((ViewGroup)grandpa.getParent()).getParent()).getParent()).getChildAt(1)).setVisibility(View.VISIBLE);
                else ((GridLayout)((ViewGroup)((ViewGroup)((ViewGroup)grandpa.getParent()).getParent()).getParent()).getChildAt(1)).setVisibility(View.GONE);
                return;
            }
        });
    }

    public static AdapterView.OnItemSelectedListener createIQAListener(View view, int opositeTableLayout, int matchTableLayout){
        final TableLayout opositeTable = (TableLayout) view.findViewById(opositeTableLayout);
        final TableLayout matchTable = (TableLayout) view.findViewById(matchTableLayout);
        return new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // This will occur when an item is selected in IQAEQA spinners
                // For IQAEQA, when file each file spinner option match with its equivalent in the other table, that means score = 1
                int score = 0;
                float totalScore = 0.0F;
                TextView totalScoreView = null;
                TableRow thisRow = (TableRow)((ViewGroup)((ViewGroup)parent.getParent()).getParent()).getParent();
                int numberOfRow = Integer.parseInt((String)((TextView) thisRow.getChildAt(0)).getText());
                int thisPosition = parent.getSelectedItemPosition();
                int oppositePosition = ((Spinner)((ViewGroup)((ViewGroup)((ViewGroup)opositeTable.getChildAt(numberOfRow)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getSelectedItemPosition();
                if (thisPosition == oppositePosition && thisPosition != 0 && oppositePosition != 0) score = 1;
                TextView scoreView = (TextView)((ViewGroup)matchTable.getChildAt(numberOfRow)).getChildAt(1);
                scoreView.setText(Integer.toString(score));

                // Update in score tab
                LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(view, R.id.Grid);
                for (int i=1; i<matchTable.getChildCount(); i++){
                    totalScoreView = (TextView)((ViewGroup) matchTable.getChildAt(i)).getChildAt(1);
                    if (!("".equals((String)totalScoreView.getText()))) totalScore += Float.parseFloat((String)totalScoreView.getText());
                }
                totalScore = totalScore*10.0F;
                TextView iqaEqaScoreView = (TextView) root.findViewById(R.id.iqaeqaScore);
                LayoutUtils.setScore(totalScore, iqaEqaScoreView);
                LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(parent, LayoutConfiguration.getTabsConfigurationIds());
                TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public static TextWatcher createReportingListener(final Activity myActivity){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float totalScore = 0.0F;
                TextView totalScoreView = null;
                View myView = myActivity.getCurrentFocus();
                Log.d(".Layout", "instance of: ");
                if (s.length() == 0) return;
                EditText myEdit = (EditText)myActivity.getCurrentFocus();
                TableRow myRow = (TableRow) ((ViewGroup) myEdit.getParent()).getParent();
                TableLayout myTable = (TableLayout)((ViewGroup)myRow).getParent();
                EditText registerView = (EditText)((ViewGroup)myRow.getChildAt(1)).getChildAt(0);
                EditText monthlyView = (EditText)((ViewGroup)myRow.getChildAt(2)).getChildAt(0);
                TextView scoreView = (TextView)((ViewGroup)myRow.getChildAt(3)).getChildAt(0);
                if (!("".equals(registerView.getText())) && !("".equals(monthlyView.getText()))){
                    if (registerView.getText().toString().equals(monthlyView.getText().toString())){
                        scoreView.setText("1");
                    } else {
                        scoreView.setText("0");
                    }
                } else {
                    scoreView.setText("0");
                }

                // Update in score tab
                LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(myEdit, R.id.Grid);
                for (int i=1; i<myTable.getChildCount(); i++){
                    totalScoreView = (TextView)((ViewGroup)((ViewGroup) myTable.getChildAt(i)).getChildAt(3)).getChildAt(0);
                    if (!("".equals((String)totalScoreView.getText()))) totalScore += Float.parseFloat((String)totalScoreView.getText());
                }
                totalScore = totalScore*10.0F;
                TextView reportingScoreView = (TextView) root.findViewById(R.id.reportingScore);
                LayoutUtils.setScore(totalScore, reportingScoreView);
                LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(myEdit, LayoutConfiguration.getTabsConfigurationIds());
                TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
            }
        };
    }

    public static void createTextListener(EditText editable, final Activity myActivity){
        editable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                View myView = myActivity.getCurrentFocus();
                if (s.length() == 0) return;
                EditText myEdit = (EditText)myActivity.getCurrentFocus();
                Question triggeredQuestion = (Question) myEdit.getTag(R.id.QuestionTag);
                Value value = triggeredQuestion.getValue(MainActivity.session.getSurvey());
                // If the value is not found we create one
                if (value == null) {
                    value = new Value("", triggeredQuestion, MainActivity.session.getSurvey());
                    value.save();
                } else {
                    value.setOption(null);
                    value.setValue(s.toString());
                    value.save();
                }
            }
        });
    }
}

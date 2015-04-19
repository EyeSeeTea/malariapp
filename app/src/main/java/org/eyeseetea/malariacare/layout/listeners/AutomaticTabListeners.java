package org.eyeseetea.malariacare.layout.listeners;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang3.ObjectUtils;
import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.configuration.LayoutConfiguration;
import org.eyeseetea.malariacare.layout.dialog.DialogDispatcher;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AutomaticTabListeners {

    public static void createDropDownListener(final Tab tab, Spinner dropdown, final MainActivity mainActivity) {
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) parentView;
                Option triggeredOption = (Option) spinner.getItemAtPosition(position);

                Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);
                TextView numeratorView = (TextView) spinner.getTag(R.id.NumeratorViewTag);
                TextView denominatorView = (TextView) spinner.getTag(R.id.DenominatorViewTag);
                LinearLayout tabLayout = ((LinearLayout) LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab)));
                Value value = triggeredQuestion.getValue(MainActivity.session.getSurvey());
                // If the value is not found we create one
                if (value == null) {
                    value = new Value(triggeredOption, triggeredQuestion, MainActivity.session.getSurvey());
                    value.save();
                } else {
                    value.setOption(triggeredOption);
                    value.save();
                }

                // Tab scores View
                TextView numSubtotal = (TextView) tabLayout.findViewById(R.id.totalNum);
                TextView denSubtotal = (TextView) tabLayout.findViewById(R.id.totalDen);
                TextView partialScoreView = (TextView) tabLayout.findViewById(R.id.score);
                TextView partialScorePercentageView = (TextView) tabLayout.findViewById(R.id.percentageSymbol);
                TextView partialCualitativeScoreView = (TextView) tabLayout.findViewById(R.id.cualitativeScore);
                View gridView = null;
                // General scores View
                Integer generalScoreId = null, generalScoreAvgId = null;
                TextView generalScoreView = null, generalScoreAvgView = null;
                if (LayoutConfiguration.getTabsConfiguration().get(tab).getScoreFieldId() != null) {
                    generalScoreId = (Integer) LayoutConfiguration.getTabsConfiguration().get(tab).getScoreFieldId();
                    gridView = LayoutUtils.findParentRecursively(spinner, R.id.Grid);
                    generalScoreView = (TextView) gridView.findViewById(generalScoreId);
                    if (LayoutConfiguration.getTabsConfiguration().get(tab).getScoreAvgFieldId() != null) {
                        generalScoreAvgId = (Integer) LayoutConfiguration.getTabsConfiguration().get(tab).getScoreAvgFieldId();
                        generalScoreAvgView = (TextView) gridView.findViewById(generalScoreAvgId);
                    }
                }

                Float numerator, denominator;
                if (triggeredOption.getName() != null && !(triggeredOption.getName().equals(Constants.DEFAULT_SELECT_OPTION))) { // This is for capture the user selection
                    // First we do the calculus
                    numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();
                    Log.i(".Layout", "numerator: " + numerator);
                    denominator = 0.0F;

                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w()) == 0) {
                        denominator = triggeredQuestion.getDenominator_w();
                        Log.i(".Layout", "denominator: " + denominator);
                    } else {
                        if (triggeredQuestion.getNumerator_w().compareTo(0.0F) == 0 && triggeredQuestion.getDenominator_w().compareTo(0.0F) != 0) {
                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();
                            Log.i(".Layout", "denominator: " + denominator);
                        }
                    }

                    ScoreRegister.addRecord(triggeredQuestion, numerator, denominator);

                    // If the option is changed to positive numerator and has children, we need to show the children and take their denominators into account
                    if (triggeredQuestion.hasChildren()) {
                        LayoutUtils.toggleVisibleChildren(position, spinner, triggeredQuestion);
                    }

                    numeratorView.setText(Utils.round(numerator));
                    denominatorView.setText(Utils.round(denominator));

                } else {
                    // This is for capturing the event when the user leaves the dropdown list without selecting any option
                    numerator = 0.0F;
                    if (triggeredQuestion.hasChildren()){
                        denominator = 0.0F;
                        LayoutUtils.toggleVisibleChildren(position, spinner, triggeredQuestion);
                    }
                    else{
                        denominator = triggeredQuestion.getDenominator_w();
                    }

                    if (selectedItemView != null) {
                        numeratorView.setText(Utils.round(numerator));
                        denominatorView.setText(Utils.round(denominator));
                    }

                    ScoreRegister.addRecord(triggeredQuestion, numerator, denominator);
                }


                // FIXME: THIS PART NEEDS A REFACTOR. FROM HERE...
                List<Float> numDenSubTotal = ScoreRegister.calculateGeneralScore(tab);
                ScoreRegister.updateCompositivesScore(triggeredQuestion.getCompositiveScore(), gridView);

                if (numSubtotal != null && denSubtotal != null && partialScoreView != null) {
                    numSubtotal.setText(Utils.round(numDenSubTotal.get(0)));
                    denSubtotal.setText(Utils.round(numDenSubTotal.get(1)));
                    float score;
                    float average = 0.0F, totalAverage = 0.0F;
                    if (numDenSubTotal.get(0) == 0 && numDenSubTotal.get(1) == 0){
                        score = 100;
                    }
                    else if (numDenSubTotal.get(0) > 0 && numDenSubTotal.get(1) == 0){
                        score = 0;
                        DialogDispatcher mf = DialogDispatcher.newInstance(null);
                        mf.showDialog(mainActivity.getFragmentManager(), DialogDispatcher.ERROR_DIALOG);
                    }
                    else {
                        score = (numDenSubTotal.get(0) / numDenSubTotal.get(1)) * 100;
                    }
                    TextView elementView = null;

                    LayoutUtils.setScore(score, partialScoreView, partialScorePercentageView, partialCualitativeScoreView); // We set the score in the tab score

                    if (LayoutConfiguration.getTabsConfiguration().get(tab).getScoreFieldId() != null) {
                        LayoutUtils.setScore(score, generalScoreView);
                        if(LayoutConfiguration.getTabsConfiguration().get(tab).getScoreAvgFieldId() != null){
                            List<Integer> averageElements = (ArrayList<Integer>) generalScoreAvgView.getTag();
                            if (averageElements == null) {
                                averageElements = new ArrayList<Integer>();
                                averageElements.add(generalScoreId);
                                LayoutUtils.setScore(score, generalScoreAvgView);
                                generalScoreAvgView.setTag(averageElements);
                            } else {
                                boolean found = false;
                                for (Integer element : averageElements) {
                                    if (element.intValue() == generalScoreId) found = true;
                                    average += Float.parseFloat((String) ((TextView) LayoutUtils.findParentRecursively(generalScoreView, R.id.scoreTable).findViewById(element)).getText());
                                }
                                if (!found) averageElements.add(generalScoreId);
                                average = average / averageElements.size();
                                LayoutUtils.setScore(average, generalScoreAvgView);
                                generalScoreAvgView.setTag(averageElements);
                            }
                        }
                        List<Integer> scoreElements = (ArrayList<Integer>) gridView.findViewById(R.id.totalScore).getTag();
                        TextView totalScoreView = (TextView) gridView.findViewById(R.id.totalScore);
                        if (scoreElements == null) {
                            scoreElements = new ArrayList<Integer>();
                            if (LayoutConfiguration.getTabsConfiguration().get(tab).getScoreAvgFieldId() != null) scoreElements.add(generalScoreAvgId);
                            else scoreElements.add(generalScoreId);
                            totalScoreView.setTag(scoreElements);
                        } else {
                            boolean foundElement = false;
                            for (Integer element : scoreElements){
                                if (LayoutConfiguration.getTabsConfiguration().get(tab).getScoreAvgFieldId() != null) {
                                    if (element.intValue() == generalScoreAvgId.intValue()) foundElement = true;
                                } else {
                                    if (element.intValue() == generalScoreId.intValue()) foundElement = true;
                                }
                                totalAverage += Float.parseFloat((String) ((TextView) LayoutUtils.findParentRecursively(generalScoreView, R.id.scoreTable).findViewById(element)).getText());
                            }
                            if (!foundElement){
                                if (LayoutConfiguration.getTabsConfiguration().get(tab).getScoreAvgFieldId() != null) scoreElements.add(generalScoreAvgId);
                                else scoreElements.add(generalScoreId);
                            }
                            totalAverage = totalAverage / scoreElements.size();
                            LayoutUtils.setScore(totalAverage, totalScoreView);
                            totalScoreView.setTag(scoreElements);
                        }
                    }
                }
                // FIXME: ...TO HERE
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

}

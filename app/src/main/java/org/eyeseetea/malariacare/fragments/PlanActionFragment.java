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

package org.eyeseetea.malariacare.fragments;

import static org.eyeseetea.malariacare.services.SurveyService.PREPARE_FEEDBACK_ACTION_ITEMS;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlan;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.ExportData;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.FileIOUtils;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PlanActionFragment extends Fragment implements IModuleFragment {

    public static final String TAG = ".PlanActionFragment";

    private ObsActionPlan mObsActionPlan;
    private String moduleName;
    boolean isFABOpen;
    FloatingActionButton fabHtmlOption;
    CustomTextView mTextViewHtml;
    FloatingActionButton fabPlainTextOption;
    CustomTextView mTextViewPlainText;
    CustomEditText mCustomGapsEditText;
    CustomEditText mCustomActionPlanEditText;
    CustomEditText mCustomActionOtherEditText;
    CustomSpinner actionSpinner;
    CustomSpinner secondaryActionSpinner;
    FloatingActionButton fabComplete;
    /**
     * Parent layout
     */
    RelativeLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        llLayout = (RelativeLayout) inflater.inflate(R.layout.plan_action_fragment, container,
                false);
        mObsActionPlan = ObsActionPlan.findObsActionPlanBySurvey(
                Session.getSurveyByModule(moduleName).getId_survey());
        if (mObsActionPlan == null) {
            mObsActionPlan = new ObsActionPlan(
                    Session.getSurveyByModule(moduleName).getId_survey());
            mObsActionPlan.save();
        }
        initLayoutHeaders(llLayout);
        initEditTexts(llLayout);
        initSpinner(llLayout);
        initFAB(llLayout);
        initBackButton(llLayout);
        if(!mObsActionPlan.getStatus().equals(Constants.SURVEY_IN_PROGRESS)){
            setReadOnlyMode();
        }
        return llLayout; // We must return the loaded Layout
    }

    private void setReadOnlyMode() {
        mCustomGapsEditText.setEnabled(false);
        mCustomActionPlanEditText.setEnabled(false);
        mCustomActionOtherEditText.setEnabled(false);
        actionSpinner.setEnabled(false);
        secondaryActionSpinner.setEnabled(false);
        fabComplete.setEnabled(false);
    }

    private void initEditTexts(RelativeLayout llLayout) {
        mCustomGapsEditText = (CustomEditText) llLayout.findViewById(
                R.id.plan_action_gasp_edit_text);
        mCustomGapsEditText.setText(mObsActionPlan.getGaps());
        mCustomGapsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mObsActionPlan.setGaps(editable.toString());
                mObsActionPlan.save();
            }
        });
        mCustomActionPlanEditText = (CustomEditText) llLayout.findViewById(
                R.id.plan_action_action_plan_edit_text);
        mCustomActionPlanEditText.setText(mObsActionPlan.getAction_plan());
        mCustomActionPlanEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mObsActionPlan.setAction_plan(editable.toString());
                mObsActionPlan.save();
            }
        });
        mCustomActionOtherEditText = (CustomEditText) llLayout.findViewById(
                R.id.plan_action_others_edit_text);
        String options[] = getResources().getStringArray(
                R.array.plan_action_dropdown_options);
        if (mObsActionPlan.getAction1()!=null && mObsActionPlan.getAction1().equals(options[5])) {
            if (mObsActionPlan.getAction2() != null) {
                mCustomActionOtherEditText.setText(mObsActionPlan.getAction2());
            }
        }
        mCustomActionOtherEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mObsActionPlan.setAction2(editable.toString());
                mObsActionPlan.save();
            }
        });
    }

    private void initBackButton(RelativeLayout llLayout) {
        CustomRadioButton goback = (CustomRadioButton) llLayout.findViewById(
                R.id.backToSentSurveys);
        goback.setText(Session.getSurveyByModule(moduleName).getOrgUnit().getName());
        goback.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          getActivity().onBackPressed();
                                      }
                                  }
        );
    }

    private void initFAB(RelativeLayout llLayout) {
        initFabComplete(llLayout);

        FloatingActionButton fab = (FloatingActionButton) llLayout.findViewById(R.id.fab);
        fabHtmlOption = (FloatingActionButton) llLayout.findViewById(R.id.fab2);
        mTextViewHtml = (CustomTextView) llLayout.findViewById(R.id.text2);
        fabPlainTextOption = (FloatingActionButton) llLayout.findViewById(R.id.fab1);
        mTextViewPlainText = (CustomTextView) llLayout.findViewById(R.id.text1);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFABOpen) {
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });
        fabHtmlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareHtmlText();
            }
        });
        fabPlainTextOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePlainText();
            }
        });
    }

    private void initFabComplete(RelativeLayout llLayout) {
        fabComplete = (FloatingActionButton) llLayout.findViewById(R.id.fab_save);
        if(!mObsActionPlan.getStatus().equals(Constants.SURVEY_IN_PROGRESS)){
            fabComplete.setImageResource(R.drawable.ic_action_check);
        }

        fabComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(null)
                        .setMessage(getActivity().getString(R.string.dialog_info_ask_for_completion_plan))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                mObsActionPlan.setStatus(Constants.SURVEY_COMPLETED);
                                mObsActionPlan.save();
                                fabComplete.setImageResource(R.drawable.ic_action_check);
                                setReadOnlyMode();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).create().show();
            }
        });
    }

    private void shareHtmlText() {
        Survey survey = Session.getSurveyByModule(moduleName);

        String title = getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName()
                + "/" + survey.getProgram().getName() + "\n";
        String data = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"utf-8\"/>"
                + "</head>"
                + "<body>"
                + "<div class=\"header\" style=\"height: 210px;\">";
        data +=
                "<img  class=\"headerImage\" src=\"https://lh3.googleusercontent"
                        + ".com/dLn5w5rNHKkMm1axNlD1iZuwBxqgUqRRD5d9N_F"
                        + "-H3CIN7wDHiSEm2vK6fnSRXRBj7te=w300-rw\" style=\"float: left; height: "
                        + "210px;\"/>"
                        + "<p class=\"headerText\" style=\"font-size: 210%; color: #6E6E6E;><b "
                        + "style=\"color: black;\">"
                        + PreferencesState.getInstance().getContext().getString(
                        R.string.app_name) + "</b><br/>";
        data += getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName()
                + "/" + survey.getProgram().getName() + "<br/>";
        data += getString(R.string.on) + " " + String.format(
                getString(R.string.plan_action_next_date), EventExtended.format
                        (survey.getCompletionDate(), getString(R.string.date_month_text_format)))
                + "<br/>";
        data += getString(R.string.quality_of_care) + " <em style=\"color: #FFBF00;\">"
                + Math.round(survey.getMainScore())
                + "%</em><br/>";
        data += "</p></div><p class=\"nextDate\" style=\"margin-left: 50%; color: #6E6E6E;\">"
                + String.format(
                getString(R.string.plan_action_next_date), EventExtended.format
                        (survey.getScheduledDate(), EventExtended.EUROPEAN_DATE_FORMAT)) + "</p>";
        data += "<p><b>" + getString(R.string.plan_action_gasp_title) + "</b> " +
                mCustomGapsEditText.getText().toString() + "</p>";
        data += "<p><b>" + getString(R.string.plan_action_action_plan_title) + "</b> " +
                mCustomActionPlanEditText.getText().toString() + "</p>";
        if(!actionSpinner.getSelectedItem().equals(actionSpinner.getItemAtPosition(0))) {
            data += "<p><b>" + getString(R.string.plan_action_action_title) + "</b> " +
                    actionSpinner.getSelectedItem().toString();
        }
        if(actionSpinner.getSelectedItem().equals(actionSpinner.getItemAtPosition(1))){
            data +=secondaryActionSpinner.getSelectedItem().toString()  + "</p>";
        }else if(actionSpinner.getSelectedItem().equals(actionSpinner.getItemAtPosition(5))){
            data +=mCustomActionOtherEditText.getText().toString()  +"</p>";
        }
        else{
            data +="</p>";
        }
        data +="<p><b>"+getString(R.string.critical_steps) + "</p>";

        List<Question> criticalQuestions = Question.getCriticalFailedQuestions(Session
                .getSurveyByModule(moduleName).getId_survey());

        List<CompositeScore> compositeScoresTree = getValidTreeOfCompositeScores();


        //For each score add proper items
        for (Iterator<CompositeScore> iterator = compositeScoresTree.iterator();
                iterator.hasNext(); ) {
            CompositeScore compositeScore = iterator.next();
            data += "<p><b>" + compositeScore.getHierarchical_code() + " " + compositeScore.getLabel
                    () + "</b></p>";
            for(Question question : criticalQuestions){
                if(question.getCompositeScoreFk()==(compositeScore.getId_composite_score())) {
                    data += "<p style=\"font-style: italic;\">" + "-" + question.getForm_name()
                            + "</p>";
                }
            }
        }
        data += getString(R.string.see_full_assessment)+ "</p>";
        if(survey.isSent()) {
            data += "<a href=https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid()
                    +
                    ">https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid()
                    + "</a></p>";
        }else{
            data += getString(R.string.url_not_available) + "</p>";
        }
        data += "</body>"
                + "</html>";
        File attached = null;
        try {
            attached = FileIOUtils.saveStringToFile("shared_html.html", data, getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }


        createHtmlIntent(getActivity(), "", title, attached);
    }

    private void sharePlainText() {
        Survey survey = Session.getSurveyByModule(moduleName);
        String data =
                PreferencesState.getInstance().getContext().getString(
                        R.string.app_name) + "\n\n";
        data += getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName() + "/"
                + survey.getProgram().getName() + "\n\n";
        data += getString(R.string.quality_of_care) + " " + survey.getMainScore() + "\n\n";
        data += String.format(getString(R.string.plan_action_next_date),
                EventExtended.format(survey.getScheduledDate(),
                        EventExtended.EUROPEAN_DATE_FORMAT)) + "\n\n";
        data += getString(R.string.plan_action_gasp_title) + " "
                + mCustomGapsEditText.getText().toString() + "\n\n";
        data += getString(R.string.plan_action_action_plan_title) + " "
                + mCustomActionPlanEditText.getText().toString() + "\n\n";
        if (!actionSpinner.getSelectedItem().equals(actionSpinner.getItemAtPosition(0))) {
            data += getString(R.string.plan_action_action_title) + " "
                    + actionSpinner.getSelectedItem().toString() + "\n\n";
        }
        if (actionSpinner.getSelectedItem().equals(actionSpinner.getItemAtPosition(1))) {
            data += secondaryActionSpinner.getSelectedItem().toString() + "\n";
        } else if (actionSpinner.getSelectedItem().equals(actionSpinner.getItemAtPosition(5))) {
            data += mCustomActionOtherEditText.getText().toString() + "\n";
        }

        List<Question> criticalQuestions = Question.getCriticalFailedQuestions(
                Session.getSurveyByModule(moduleName).getId_survey());
        if(criticalQuestions!=null && criticalQuestions.size()>0) {
            data += getString(R.string.critical_steps) + "\n\n";

            List<CompositeScore> compositeScoresTree = getValidTreeOfCompositeScores();
            //For each score add proper items
            for (Iterator<CompositeScore> iterator = compositeScoresTree.iterator();
                    iterator.hasNext(); ) {
                CompositeScore compositeScore = iterator.next();
                data += compositeScore.getHierarchical_code() + " " + compositeScore.getLabel()
                        + "\n";
                for (Question question : criticalQuestions) {
                    if (question.getCompositeScoreFk()
                            == (compositeScore.getId_composite_score())) {
                        data += "-" + question.getForm_name() + "\n";
                    }
                }
            }
        }
        data += "\n" + getString(R.string.see_full_assessment) + "\n";
        if (survey.isSent()) {
            data += "https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid() + "\n";
        } else {
            data += getString(R.string.url_not_available) + "\n";
        }
        System.out.println("data:"+data);
        createTextIntent(getActivity(), data);
    }


    /**
     * This method create the email intent
     */
    private static void createHtmlIntent(Activity activity, String data, String title,
            File attached) {
        ExportData.shareFileIntent(activity, data, title, attached);
    }

    @NonNull
    private List<CompositeScore> getValidTreeOfCompositeScores() {
        List<CompositeScore> compositeScoreList = Question.getCSOfriticalFailedQuestions(
                Session.getSurveyByModule(moduleName).getId_survey());
        List<CompositeScore> compositeScoresTree = new ArrayList<>();
        for (CompositeScore compositeScore : compositeScoreList) {
            buildCompositeScoreTree(compositeScore, compositeScoresTree);
        }

        //Order composite scores
        Collections.sort(compositeScoresTree, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {

                CompositeScore cs1 = (CompositeScore) o1;
                CompositeScore cs2 = (CompositeScore) o2;

                return new Integer(cs1.getOrder_pos().compareTo(cs2.getOrder_pos()));
            }
        });
        return compositeScoresTree;
    }

    //Recursive compositescore parent builder
    private void buildCompositeScoreTree(CompositeScore compositeScore,
            List<CompositeScore> compositeScoresTree) {
        if(compositeScore.getHierarchical_code().equals("0")){
            //ignore composite score root
            return;
        }
        if(!compositeScoresTree.contains(compositeScore)){
            compositeScoresTree.add(compositeScore);
        }
        if(compositeScore.hasParent()){
            buildCompositeScoreTree(compositeScore.getComposite_score(), compositeScoresTree);
        }
    }

    /**
     * This method create the email intent
     */
    private static void createTextIntent(Activity activity, String data) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    private List<CompositeScore> prepareCompositeScores(Survey survey, List<Question> criticalQuestions) {
        //Calculate main score
        List<CompositeScore> compositeScoreList =ScoreRegister.loadCompositeScores(survey, moduleName);
        survey.setMainScore(ScoreRegister.calculateMainScore(compositeScoreList,survey.getId_survey(), moduleName));

        //Remove parents from list (to avoid showing the parent composite that is there just to
        // push the overall score)
        for (Iterator<CompositeScore> iterator = compositeScoreList.iterator(); iterator.hasNext(); ) {
            CompositeScore compositeScore = iterator.next();
            //Show only if a parent have questions.
            if(compositeScore.getQuestions().size()<1) {
                if (!compositeScore.hasParent()) iterator.remove();
            }
            else{
                boolean isValid=false;
                for(Question question : compositeScore.getQuestions()){
                    for(Question criticalQuestion : criticalQuestions){
                        if(question.getUid().equals(criticalQuestion.getUid())){
                            isValid=true;
                        }
                    }
                }
                if(!isValid){
                    if (!compositeScore.hasParent()) iterator.remove();
                }
            }
        }
        return compositeScoreList;
    }

    private void showFABMenu() {
        isFABOpen = true;
        fabHtmlOption.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mTextViewHtml.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mTextViewHtml.setVisibility(View.VISIBLE);
        fabPlainTextOption.animate().translationY(
                -getResources().getDimension(R.dimen.standard_105));
        mTextViewPlainText.animate().translationY(
                -getResources().getDimension(R.dimen.standard_105));
        mTextViewPlainText.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu() {
        isFABOpen = false;
        mTextViewPlainText.animate().translationY(0);
        mTextViewHtml.animate().translationY(0);
        fabHtmlOption.animate().translationY(0);
        fabPlainTextOption.animate().translationY(0);
        mTextViewPlainText.setVisibility(View.GONE);
        mTextViewHtml.setVisibility(View.GONE);
    }

    public boolean onBackPressed() {
        if (!isFABOpen) {
            return false;
        } else {
            closeFABMenu();
            return true;
        }
    }

    private void initSpinner(RelativeLayout llLayout) {
        actionSpinner = (CustomSpinner) llLayout.findViewById(R.id.plan_action_spinner);
        final View secondaryView = llLayout.findViewById(R.id.secondaryView);
        final View otherView = llLayout.findViewById(R.id.otherView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(llLayout.getContext(),
                R.array.plan_action_dropdown_options, android.R.layout.simple_spinner_item);
        actionSpinner.setAdapter(adapter);
        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                    long l) {
                String[] options = getResources().getStringArray(
                        R.array.plan_action_dropdown_options);
                String selectedItem = adapterView.getItemAtPosition(position).toString();
                String lastItem = mObsActionPlan.getAction1();
                if (selectedItem.equals(options[0])) {
                    mObsActionPlan.setAction1(null);
                } else {
                    mObsActionPlan.setAction1(selectedItem);
                }
                mObsActionPlan.save();
                if (selectedItem.equals(options[1])) {
                    secondaryActionSpinner.setVisibility(View.VISIBLE);
                    secondaryView.setVisibility(View.VISIBLE);
                    mCustomActionOtherEditText.setVisibility(View.GONE);
                    otherView.setVisibility(View.GONE);
                } else if (selectedItem.equals(options[5])) {
                    secondaryActionSpinner.setVisibility(View.GONE);
                    secondaryView.setVisibility(View.GONE);
                    mCustomActionOtherEditText.setVisibility(View.VISIBLE);
                    otherView.setVisibility(View.VISIBLE);
                } else {
                    secondaryActionSpinner.setVisibility(View.GONE);
                    secondaryView.setVisibility(View.GONE);
                    mCustomActionOtherEditText.setVisibility(View.GONE);
                    otherView.setVisibility(View.GONE);
                }
                if (lastItem == null || lastItem.equals(selectedItem)) {
                    return;
                } else if (!lastItem.equals(options[5])) {
                    secondaryActionSpinner.setSelection(0, false);
                } else if (!lastItem.equals(options[1])) {
                    mCustomActionOtherEditText.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        secondaryActionSpinner = (CustomSpinner) llLayout.findViewById(
                R.id.plan_action_secondary_spinner);


        final ArrayAdapter<CharSequence> secondaryAdapter = ArrayAdapter.createFromResource(
                llLayout.getContext(), R.array.plan_action_dropdown_suboptions,
                android.R.layout.simple_spinner_item);
        secondaryActionSpinner.setAdapter(secondaryAdapter);


        secondaryActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                    long l) {
                String[] options = getResources().getStringArray(
                        R.array.plan_action_dropdown_suboptions);
                String selectedItem = adapterView.getItemAtPosition(position).toString();
                if (selectedItem.equals(options[0])) {
                    mObsActionPlan.setAction2(null);
                } else {
                    mObsActionPlan.setAction2(selectedItem);
                }
                mObsActionPlan.save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        setSpinnerValues();

    }

    private void setSpinnerValues() {
        String options[] = getResources().getStringArray(
                R.array.plan_action_dropdown_options);
        if (mObsActionPlan.getAction1() != null) {
            for (int i = 0; i < options.length; i++) {
                if (mObsActionPlan.getAction1().equals(options[i])) {
                    actionSpinner.setSelection(i);
                }
            }
        }
        if (mObsActionPlan.getAction2() != null && mObsActionPlan.getAction1() != null) {
            if (mObsActionPlan.getAction1().equals(options[1])) {
                String subOptions[] = getResources().getStringArray(
                        R.array.plan_action_dropdown_suboptions);
                for (int i = 0; i < subOptions.length; i++) {
                    if (mObsActionPlan.getAction2().equals(subOptions[i])) {
                        secondaryActionSpinner.setSelection(i);
                    }
                }
            }
        }
    }

    private void initLayoutHeaders(RelativeLayout llLayout) {
        Survey survey = Session.getSurveyByModule(moduleName);
        if (survey.hasMainScore()) {
            float average = survey.getMainScore();
            CustomTextView item = (CustomTextView) llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("%.1f%%", average));
            int colorId = LayoutUtils.trafficColor(average);
            item.setBackgroundColor(getResources().getColor(colorId));
        } else {
            CustomTextView item = (CustomTextView) llLayout.findViewById(R.id.feedback_total_score);
            item.setText(String.format("NaN"));
            float average = 0;
            int colorId = LayoutUtils.trafficColor(average);
            item.setBackgroundColor(getResources().getColor(colorId));
        }
        CustomTextView nextDate = (CustomTextView) llLayout.findViewById(R.id.plan_completion_day);
        String formattedCompletionDate = "NaN";
        if (survey.getCompletionDate() != null) {
            formattedCompletionDate = EventExtended.format(survey.getCompletionDate(),
                    EventExtended.EUROPEAN_DATE_FORMAT);
        }
        nextDate.setText(
                String.format(getString(R.string.plan_action_today_date), formattedCompletionDate));

        CustomTextView completionDate = (CustomTextView) llLayout.findViewById(
                R.id.new_supervision_date);
        String formattedNextDate = "NaN";
        if (survey.getScheduledDate() != null) {
            formattedNextDate = EventExtended.format(survey.getScheduledDate(),
                    EventExtended.EUROPEAN_DATE_FORMAT);
        }
        completionDate.setText(
                String.format(getString(R.string.plan_action_next_date), formattedNextDate));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        List<Feedback> feedbackList = new ArrayList<>();
        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setModuleName(String simpleName) {
        this.moduleName = simpleName;
    }

    @Override
    public void reloadData() {
    }
}

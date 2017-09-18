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
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlanActionFragment extends Fragment implements IModuleFragment {

    public static final String TAG = ".PlanActionFragment";

    private String moduleName;
    boolean isFABOpen;
    FloatingActionButton fabHtmlOption;
    CustomTextView mTextViewHtml;
    FloatingActionButton fabPlainTextOption;
    CustomTextView mTextViewPlainText;
    CustomEditText mCustomGapsEditText;
    CustomEditText mCustomActionPlanEditText;
    CustomEditText mCustomActionOtherEditText;
    CustomSpinner actionDropdown;
    CustomSpinner secondaryActionDropdown;

    /**
     * Parent layout
     */
    RelativeLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        llLayout = (RelativeLayout) inflater.inflate(R.layout.plan_action_fragment, container, false);
        initLayoutHeaders(llLayout);
        initEditTexts(llLayout);
        initSpinner(llLayout);
        initFAB(llLayout);
        initBackButton(llLayout);
        return llLayout; // We must return the loaded Layout
    }

    private void initEditTexts(RelativeLayout llLayout) {
        mCustomGapsEditText = (CustomEditText) llLayout.findViewById(
                R.id.plan_action_gasp_edit_text);
        mCustomActionPlanEditText = (CustomEditText) llLayout.findViewById(
                R.id.plan_action_action_plan_edit_text);
        mCustomActionOtherEditText = (CustomEditText) llLayout.findViewById(
                R.id.plan_action_others_edit_text);
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
        FloatingActionButton fab = (FloatingActionButton) llLayout.findViewById(R.id.fab);
        fabHtmlOption = (FloatingActionButton) llLayout.findViewById(R.id.fab2);
        mTextViewHtml = (CustomTextView) llLayout.findViewById(R.id.text2);
        fabPlainTextOption = (FloatingActionButton) llLayout.findViewById(R.id.fab1);
        mTextViewPlainText = (CustomTextView) llLayout.findViewById(R.id.text1);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        fabHtmlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        fabPlainTextOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePlainText();
            }
        });
    }

    private void sharePlainText() {
        Survey survey = Session.getSurveyByModule(moduleName);
        String data=
                PreferencesState.getInstance().getContext().getString(
                        R.string.app_name) + "\n";
        data +="Results supervision on: " + survey.getOrgUnit().getName() +"/"+survey.getProgram().getName() + "\n";
        data +="Quality of Care (QoC) Score: " + survey.getMainScore() + "\n";
        data +="Next Supervision Date: " + EventExtended.format(survey.getCompletionDate(),EventExtended.EUROPEAN_DATE_FORMAT) + "\n";
        data +="Gaps: " + mCustomGapsEditText.getText().toString() + "\n";
        data +="Action plan: " + mCustomActionPlanEditText.getText().toString() + "\n";
        if(!actionDropdown.getSelectedItem().equals(actionDropdown.getItemAtPosition(0))) {
            data += "Action: " + actionDropdown.getSelectedItem().toString() + "\n";
        }
        if(actionDropdown.getSelectedItem().equals(actionDropdown.getItemAtPosition(1))){
            data += secondaryActionDropdown.getSelectedItem().toString() + "\n";
        }else if(actionDropdown.getSelectedItem().equals(actionDropdown.getItemAtPosition(5))){
            data += mCustomActionOtherEditText.getText().toString() + "\n";
        }
        data +="Critical Steps missed:" + "\n";

        List<Question> criticalQuestions = Question.getCriticalFailedQuestions(Session.getSurveyByModule(moduleName).getId_survey());

        List<CompositeScore> compositeScoreList= ScoreRegister.loadCompositeScores(survey, moduleName);


        //Calculate main score
        survey.setMainScore(ScoreRegister.calculateMainScore(compositeScoreList,survey.getId_survey(), moduleName));

            //Remove parents from list (to avoid showing the parent composite that is there just to push the overall score)
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

        //For each score add proper items
        for(CompositeScore compositeScore:compositeScoreList) {
            data +=compositeScore.getHierarchical_code() +" "+ compositeScore.getLabel() +"\n";
            for(Question question : criticalQuestions){
                if(question.getCompositeScoreFk()==(compositeScore.getId_composite_score())) {
                    data += "-" + question.getForm_name() + "\n";
                }
            }
        }
        System.out.println(data);
        createTextIntent(getActivity(), data);
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

    private void showFABMenu(){
        isFABOpen=true;
        fabHtmlOption.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mTextViewHtml.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mTextViewHtml.setVisibility(View.VISIBLE);
        fabPlainTextOption.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        mTextViewPlainText.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        mTextViewPlainText.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu(){
        isFABOpen = false;
        mTextViewPlainText.animate().translationY(0);
        mTextViewHtml.animate().translationY(0);
        fabHtmlOption.animate().translationY(0);
        fabPlainTextOption.animate().translationY(0);
        mTextViewPlainText.setVisibility(View.GONE);
        mTextViewHtml.setVisibility(View.GONE);
    }

    public boolean onBackPressed() {
        if(!isFABOpen){
            return false;
        }else{
            closeFABMenu();
            return true;
        }
    }

    private void initSpinner(RelativeLayout llLayout) {
        actionDropdown = (CustomSpinner) llLayout.findViewById(R.id.plan_action_spinner);

        secondaryActionDropdown = (CustomSpinner) llLayout.findViewById(R.id.plan_action_secondary_spinner);
        mCustomActionOtherEditText = (CustomEditText) llLayout.findViewById(R.id.plan_action_others_edit_text);

        ArrayAdapter<CharSequence> secondaryAdapter = ArrayAdapter.createFromResource(llLayout.getContext(),R.array.plan_action_dropdown_suboptions, android.R.layout.simple_spinner_item);
        secondaryActionDropdown.setAdapter(secondaryAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(llLayout.getContext(),R.array.plan_action_dropdown_options, android.R.layout.simple_spinner_item);
        actionDropdown.setAdapter(adapter);
        actionDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String[] options = getResources().getStringArray(R.array.plan_action_dropdown_options);
                if(adapterView.getItemAtPosition(position).equals(options[1])){
                    secondaryActionDropdown.setVisibility(View.VISIBLE);
                    mCustomActionOtherEditText.setVisibility(View.GONE);
                }else if(adapterView.getItemAtPosition(position).equals(options[5])) {
                    secondaryActionDropdown.setVisibility(View.GONE);
                    mCustomActionOtherEditText.setVisibility(View.VISIBLE);
                }
                else{
                    secondaryActionDropdown.setVisibility(View.GONE);
                    mCustomActionOtherEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                secondaryActionDropdown.setVisibility(View.GONE);
                mCustomActionOtherEditText.setVisibility(View.GONE);
            }
        });

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
        String formattedCompletionDate="NaN";
        if(survey.getCompletionDate()!=null){
            formattedCompletionDate =  EventExtended.format(survey.getCompletionDate(),EventExtended.EUROPEAN_DATE_FORMAT);
        }
        nextDate.setText(String.format(getString(R.string.plan_action_today_date),formattedCompletionDate));

        CustomTextView completionDate = (CustomTextView) llLayout.findViewById(R.id.new_supervision_date);
        String formattedNextDate="NaN";
        if(survey.getScheduledDate()!=null){
            formattedNextDate =  EventExtended.format(survey.getScheduledDate(),EventExtended.EUROPEAN_DATE_FORMAT);
        }
        completionDate.setText(String.format(getString(R.string.plan_action_next_date),formattedNextDate));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        List<Feedback> feedbackList= new ArrayList<>();
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

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
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.feedback.Feedback;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PlanActionFragment extends Fragment implements IModuleFragment {

    public static final String TAG = ".PlanActionFragment";

    private ObsActionPlanDB mObsActionPlan;
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
    FloatingActionButton fabSave;

    /**
     * Parent layout
     */
    RelativeLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        llLayout = (RelativeLayout) inflater.inflate(R.layout.plan_action_fragment, container,
                false);
        mObsActionPlan = ObsActionPlanDB.findObsActionPlanBySurvey(
                Session.getSurveyByModule(moduleName).getId_survey());
        if (mObsActionPlan == null) {
            mObsActionPlan = new ObsActionPlanDB(
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
        actionDropdown.setEnabled(false);
        secondaryActionDropdown.setEnabled(false);
        fabSave.setEnabled(false);
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
        fabSave = (FloatingActionButton) llLayout.findViewById(R.id.fab_save);
        if(!mObsActionPlan.getStatus().equals(Constants.SURVEY_IN_PROGRESS)){
            fabSave.setImageResource(R.drawable.ic_action_check);
        }
        FloatingActionButton fab = (FloatingActionButton) llLayout.findViewById(R.id.fab);
        fabHtmlOption = (FloatingActionButton) llLayout.findViewById(R.id.fab2);
        mTextViewHtml = (CustomTextView) llLayout.findViewById(R.id.text2);
        fabPlainTextOption = (FloatingActionButton) llLayout.findViewById(R.id.fab1);
        mTextViewPlainText = (CustomTextView) llLayout.findViewById(R.id.text1);

        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(null)
                        .setMessage(getActivity().getString(R.string.dialog_info_ask_for_completion_plan))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                mObsActionPlan.setStatus(Constants.SURVEY_COMPLETED);
                                mObsActionPlan.save();
                                fabSave.setImageResource(R.drawable.ic_action_check);
                                setReadOnlyMode();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).create().show();
            }
        });
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
                if (!isFABOpen) {
                    showFABMenu();
                } else {
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
        SurveyDB survey = Session.getSurveyByModule(moduleName);
        String data =
                PreferencesState.getInstance().getContext().getString(
                        R.string.app_name) + "\n";
        data += getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName() + "/"
                + survey.getProgram().getName() + "\n";
        data += getString(R.string.quality_of_care) + " " + survey.getMainScore() + "\n";
        data += String.format(getString(R.string.plan_action_next_date),
                EventExtended.format(survey.getCompletionDate(),
                        EventExtended.EUROPEAN_DATE_FORMAT)) + "\n";
        data += getString(R.string.plan_action_gasp_title) + " "
                + mCustomGapsEditText.getText().toString() + "\n";
        data += getString(R.string.plan_action_action_plan_title) + " "
                + mCustomActionPlanEditText.getText().toString() + "\n";
        if (!actionDropdown.getSelectedItem().equals(actionDropdown.getItemAtPosition(0))) {
            data += getString(R.string.plan_action_action_title) + " "
                    + actionDropdown.getSelectedItem().toString() + "\n";
        }
        if (actionDropdown.getSelectedItem().equals(actionDropdown.getItemAtPosition(1))) {
            data += secondaryActionDropdown.getSelectedItem().toString() + "\n";
        } else if (actionDropdown.getSelectedItem().equals(actionDropdown.getItemAtPosition(5))) {
            data += mCustomActionOtherEditText.getText().toString() + "\n";
        }
        data += getString(R.string.critical_steps) + "\n";

        List<QuestionDB> criticalQuestions = QuestionDB.getCriticalFailedQuestions(
                Session.getSurveyByModule(moduleName).getId_survey());

        List<CompositeScoreDB> compositeScoreList = ScoreRegister.loadCompositeScores(survey,
                moduleName);


        //Calculate main score
        survey.setMainScore(
                ScoreRegister.calculateMainScore(compositeScoreList, survey.getId_survey(),
                        moduleName));

        //Remove parents from list (to avoid showing the parent composite that is there just to
        // push the overall score)
        for (Iterator<CompositeScoreDB> iterator = compositeScoreList.iterator();
                iterator.hasNext(); ) {
            CompositeScoreDB compositeScore = iterator.next();
            //Show only if a parent have questions.
            if (compositeScore.getQuestions().size() < 1) {
                if (!compositeScore.hasParent()) iterator.remove();
            } else {
                boolean isValid = false;
                for (QuestionDB question : compositeScore.getQuestions()) {
                    for (QuestionDB criticalQuestion : criticalQuestions) {
                        if (question.getUid().equals(criticalQuestion.getUid())) {
                            isValid = true;
                        }
                    }
                }
                if (!isValid) {
                    if (!compositeScore.hasParent()) iterator.remove();
                }
            }
        }

        //For each score add proper items
        for (CompositeScoreDB compositeScore : compositeScoreList) {
            data += compositeScore.getHierarchical_code() + " " + compositeScore.getLabel() + "\n";
            for (QuestionDB question : criticalQuestions) {
                if (question.getCompositeScoreFk() == (compositeScore.getId_composite_score())) {
                    data += "-" + question.getForm_name() + "\n";
                }
            }
        }
        data += getString(R.string.see_full_assessment) + "\n";
        if (survey.isSent()) {
            data += "https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid() + "\n";
        } else {
            data += getString(R.string.url_not_available) + "\n";
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
        actionDropdown = (CustomSpinner) llLayout.findViewById(R.id.plan_action_spinner);

        final View secondaryView = llLayout.findViewById(R.id.secondaryView);
        final View otherView = llLayout.findViewById(R.id.otherView);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(llLayout.getContext(),
                R.array.plan_action_dropdown_options, android.R.layout.simple_spinner_item);
        actionDropdown.setAdapter(adapter);

        actionDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                if (lastItem == null || lastItem.equals(selectedItem)) {
                } else if (!lastItem.equals(options[5])) {
                    secondaryActionDropdown.setSelection(0, false);
                } else if (!lastItem.equals(options[1])) {
                    mCustomActionOtherEditText.setText("");
                }
                if (selectedItem.equals(options[1])) {
                    secondaryActionDropdown.setVisibility(View.VISIBLE);
                    secondaryView.setVisibility(View.VISIBLE);
                    mCustomActionOtherEditText.setVisibility(View.GONE);
                    otherView.setVisibility(View.GONE);
                } else if (selectedItem.equals(options[5])) {
                    secondaryActionDropdown.setVisibility(View.GONE);
                    secondaryView.setVisibility(View.GONE);
                    mCustomActionOtherEditText.setVisibility(View.VISIBLE);
                    otherView.setVisibility(View.VISIBLE);
                } else {
                    secondaryActionDropdown.setVisibility(View.GONE);
                    secondaryView.setVisibility(View.GONE);
                    mCustomActionOtherEditText.setVisibility(View.GONE);
                    otherView.setVisibility(View.GONE);
                }
                mObsActionPlan.save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        secondaryActionDropdown = (CustomSpinner) llLayout.findViewById(
                R.id.plan_action_secondary_spinner);

        final ArrayAdapter<CharSequence> secondaryAdapter = ArrayAdapter.createFromResource(
                llLayout.getContext(), R.array.plan_action_dropdown_suboptions,
                android.R.layout.simple_spinner_item);
        secondaryActionDropdown.setAdapter(secondaryAdapter);

        secondaryActionDropdown.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                            int position,
                            long l) {
                        String[] options = getResources().getStringArray(
                                R.array.plan_action_dropdown_suboptions);
                        String selectedItem = adapterView.getItemAtPosition(
                                position).toString();
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
                    actionDropdown.setSelection(i);
                }
            }
        }
        if (mObsActionPlan.getAction2() != null && mObsActionPlan.getAction1() != null) {
            if (mObsActionPlan.getAction1().equals(options[1])) {
                String subOptions[] = getResources().getStringArray(
                        R.array.plan_action_dropdown_suboptions);
                for (int i = 0; i < subOptions.length; i++) {
                    if (mObsActionPlan.getAction2().equals(subOptions[i])) {
                        secondaryActionDropdown.setSelection(i);
                    }
                }
            }
        }
    }

    private void initLayoutHeaders(RelativeLayout llLayout) {
        SurveyDB survey = Session.getSurveyByModule(moduleName);
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

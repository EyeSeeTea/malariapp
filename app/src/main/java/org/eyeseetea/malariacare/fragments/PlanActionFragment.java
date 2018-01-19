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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.ExportData;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.survey.PlannedStyleStrategy;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.presenters.ObsActionPlanPresenter;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.sdk.common.FileUtils;
import org.eyeseetea.sdk.presentation.views.DoubleRectChart;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class PlanActionFragment extends Fragment implements IModuleFragment,
        ObsActionPlanPresenter.View {

    public static final String TAG = ".PlanActionFragment";
    private static final String SURVEY_ID = "surveyId";
    private ArrayAdapter<CharSequence> mActionsAdapter;
    private ArrayAdapter<CharSequence> mSubActionsAdapter;

    private CustomTextView mTotalScoreTextView;
    private DoubleRectChart mDoubleRectChart;
    private CustomTextView mOrgUnitTextView;
    private CustomTextView mNextDateTextView;
    private CustomTextView mCompletionDateTextView;
    private View otherView;
    private View secondaryView;
    private ImageButton mGoBack;

    private FloatingActionButton fabHtmlOption;
    private CustomTextView mTextViewHtml;
    private FloatingActionButton fabPlainTextOption;
    private CustomTextView mTextViewPlainText;
    private CustomEditText mCustomGapsEditText;
    private CustomEditText mCustomActionPlanEditText;
    private CustomEditText mCustomActionOtherEditText;
    private CustomSpinner actionSpinner;
    private CustomSpinner secondaryActionSpinner;
    private FloatingActionButton mFabComplete;
    private RelativeLayout mRootView;

    private ObsActionPlanPresenter presenter;

    boolean isFABOpen;

    public static PlanActionFragment newInstance(long surveyId) {
        PlanActionFragment myFragment = new PlanActionFragment();

        Bundle args = new Bundle();
        args.putLong(SURVEY_ID, surveyId);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        /*List<Feedback> feedbackList = new ArrayList<>();
        Session.putServiceValue(PREPARE_FEEDBACK_ACTION_ITEMS, feedbackList);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.plan_action_fragment, container,
                false);

        long surveyId = getArguments().getLong(SURVEY_ID);

        initLayoutHeaders();
        initEditTexts();
        initActions();
        initSubActions();
        initFAB();
        initBackButton();
        initPresenter(surveyId);

        return mRootView;
    }

    @Override
    public void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private void initPresenter(long surveyId) {
        presenter = new ObsActionPlanPresenter(getActivity());
        presenter.attachView(this, surveyId);
    }

    private void initEditTexts() {
        mCustomGapsEditText = (CustomEditText) mRootView.findViewById(
                R.id.plan_action_gasp_edit_text);
        mCustomGapsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                presenter.gaspChanged(editable.toString());
            }
        });

        mCustomActionPlanEditText = (CustomEditText) mRootView.findViewById(
                R.id.plan_action_action_plan_edit_text);

        mCustomActionPlanEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                presenter.actionPlanChanged(editable.toString());
            }
        });
        mCustomActionOtherEditText = (CustomEditText) mRootView.findViewById(
                R.id.plan_action_others_edit_text);

        mCustomActionOtherEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                presenter.subActionOtherChanged(editable.toString());
            }
        });
    }

    private void initBackButton() {
        mGoBack = (ImageButton) mRootView.findViewById(
                R.id.backToSentSurveys);

        mGoBack.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           getActivity().onBackPressed();
                                       }
                                   }
        );
    }

    private void initFAB() {
        initFabComplete(mRootView);

        FloatingActionButton fab = (FloatingActionButton) mRootView.findViewById(R.id.fab);
        fabHtmlOption = (FloatingActionButton) mRootView.findViewById(R.id.fab2);
        mTextViewHtml = (CustomTextView) mRootView.findViewById(R.id.text2);
        fabPlainTextOption = (FloatingActionButton) mRootView.findViewById(R.id.fab1);
        mTextViewPlainText = (CustomTextView) mRootView.findViewById(R.id.text1);


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
                presenter.shareObsActionPlan(ObsActionPlanPresenter.ShareType.HTML);
            }
        });
        fabPlainTextOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.shareObsActionPlan(ObsActionPlanPresenter.ShareType.TEXT);
            }
        });
    }

    private void initFabComplete(RelativeLayout llLayout) {
        mFabComplete = (FloatingActionButton) llLayout.findViewById(R.id.fab_save);

        mFabComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(null)
                        .setMessage(getActivity().getString(
                                R.string.dialog_info_ask_for_completion_plan))
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        presenter.completePlan();
                                    }
                                })
                        .setNegativeButton(android.R.string.no, null).create().show();
            }
        });
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

    private void initActions() {
        actionSpinner = (CustomSpinner) mRootView.findViewById(R.id.plan_action_spinner);

        mActionsAdapter =
                new ArrayAdapter(mRootView.getContext(), android.R.layout.simple_spinner_item);

        actionSpinner.setAdapter(mActionsAdapter);
        actionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                    long l) {
                presenter.onActionSelected(adapterView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void initSubActions() {
        secondaryActionSpinner = (CustomSpinner) mRootView.findViewById(
                R.id.plan_action_secondary_spinner);
        secondaryView = mRootView.findViewById(R.id.secondaryView);
        otherView = mRootView.findViewById(R.id.otherView);

        mSubActionsAdapter = new ArrayAdapter(
                mRootView.getContext(), android.R.layout.simple_spinner_item);


        secondaryActionSpinner.setAdapter(mSubActionsAdapter);


        secondaryActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                    long l) {
                presenter.onSubActionSelected(adapterView.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    private void initLayoutHeaders() {
        mTotalScoreTextView = (CustomTextView) mRootView.findViewById(R.id.feedback_total_score);
        mDoubleRectChart = (DoubleRectChart) mRootView.findViewById(R.id.scoreChart);
        mOrgUnitTextView = (CustomTextView) mRootView.findViewById(
                R.id.org_unit);
        mCompletionDateTextView = (CustomTextView) mRootView.findViewById(
                R.id.completion_date);
        mNextDateTextView = (CustomTextView) mRootView.findViewById(R.id.next_date);
    }

    @Override
    public void reloadData() {
    }


    @Override
    public void loadActions(String[] actions) {
        mActionsAdapter.addAll(actions);
    }

    @Override
    public void loadSubActions(String[] subActions) {
        mSubActionsAdapter.addAll(subActions);
    }

    @Override
    public void changeToReadOnlyMode() {
        mCustomGapsEditText.setEnabled(false);
        mCustomActionPlanEditText.setEnabled(false);
        mCustomActionOtherEditText.setEnabled(false);
        actionSpinner.setEnabled(false);
        secondaryActionSpinner.setEnabled(false);
        mFabComplete.setEnabled(false);
    }

    @Override
    public void renderBasicPlanInfo(String gasp, String actionPlan) {
        mCustomGapsEditText.setText(gasp);
        mCustomActionPlanEditText.setText(actionPlan);
    }

    @Override
    public void showSubActionOptionsView() {
        secondaryActionSpinner.setVisibility(View.VISIBLE);
        secondaryView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSubActionOptionsView() {
        secondaryActionSpinner.setVisibility(View.GONE);
        secondaryActionSpinner.setSelection(0);
        secondaryView.setVisibility(View.GONE);
    }

    @Override
    public void showSubActionOtherView() {
        mCustomActionOtherEditText.setVisibility(View.VISIBLE);
        otherView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSubActionOtherView() {
        mCustomActionOtherEditText.setVisibility(View.GONE);
        mCustomActionOtherEditText.setText("");
        otherView.setVisibility(View.GONE);
    }

    @Override
    public void updateStatusView(Integer status) {
        PlanActionStyleStrategy.fabIcons(mFabComplete, status);

    }

    @Override
    public void renderHeaderInfo(String orgUnitName, Float mainScore, String completionDate,
            String nextDate) {

        mOrgUnitTextView.setText(orgUnitName);

        if(mTotalScoreTextView!=null) {
            if (mainScore > 0f) {
                mTotalScoreTextView.setText(String.format("%.1f%%", mainScore));
                int colorId = LayoutUtils.trafficColor(mainScore);
                mTotalScoreTextView.setBackgroundColor(getResources().getColor(colorId));
            } else {
                mTotalScoreTextView.setText(String.format("NaN"));
                int colorId = LayoutUtils.trafficColor(mainScore);
                mTotalScoreTextView.setBackgroundColor(getResources().getColor(colorId));
            }
        }else if (mDoubleRectChart!=null){
            LayoutUtils.drawScore(mainScore, mDoubleRectChart);

        }

        mCompletionDateTextView.setText(
                String.format(getString(R.string.plan_action_today_date), completionDate));

        mNextDateTextView.setText(
                String.format(getString(R.string.plan_action_next_date), nextDate));

    }

    @Override
    public void selectAction(int index) {
        actionSpinner.setSelection(index);
    }

    @Override
    public void selectSubAction(int index) {
        secondaryActionSpinner.setSelection(index);
    }

    @Override
    public void renderOtherSubAction(String subAction) {
        mCustomActionOtherEditText.setText(subAction);
    }

    @Override
    public void shareByText(ObsActionPlanDB obsActionPlan, SurveyDB survey,
            List<QuestionDB> criticalQuestions, List<CompositeScoreDB> compositeScoresTree) {
        String data = extractTextData(obsActionPlan, survey, criticalQuestions,
                compositeScoresTree);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        getActivity().startActivity(sendIntent);

        System.out.println("data:" + data);
    }

    @Override
    public void shareByHtml(ObsActionPlanDB obsActionPlan, SurveyDB survey,
            List<QuestionDB> criticalQuestions, List<CompositeScoreDB> compositeScoresTree) {
        String data = extractHtmlData(obsActionPlan, survey, criticalQuestions,
                compositeScoresTree);

        String title = getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName()
                + "/" + survey.getProgram().getName();

        File attached = null;
        try {
            attached = FileUtils.saveStringToFile("shared_html.html", data, getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExportData.shareFileIntent(getActivity(), "", title, attached);

        System.out.println("data:" + data);
    }

    private String extractTextData(ObsActionPlanDB obsActionPlan, SurveyDB survey,
            List<QuestionDB> criticalQuestions, List<CompositeScoreDB> compositeScoresTree) {
        String data =
                PreferencesState.getInstance().getContext().getString(
                        R.string.app_name) + "\n\n";
        data += getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName() + "/"
                + survey.getProgram().getName() + "\n\n";

        data += getString(R.string.quality_of_care) + " " + survey.getMainScore() + "\n\n";

        data += String.format(getString(R.string.plan_action_next_date),
                EventExtended.format(survey.getScheduledDate(),
                        EventExtended.EUROPEAN_DATE_FORMAT));

        data += "\n\n" + getString(R.string.plan_action_gasp_title) + " ";

        if (obsActionPlan.getGaps() != null && !obsActionPlan.getGaps().isEmpty()) {
            data += obsActionPlan.getGaps();
        }

        data += "\n\n" + getString(R.string.plan_action_action_plan_title) + " ";

        if (obsActionPlan.getAction_plan() != null && !obsActionPlan.getAction_plan().isEmpty()) {
            data += obsActionPlan.getAction_plan();
        }

        data += "\n\n" + getString(R.string.plan_action_action_title) + " ";

        if (obsActionPlan.getAction1() != null && !obsActionPlan.getAction1().isEmpty()) {
            data += obsActionPlan.getAction1();
        }


        if (obsActionPlan.getAction2() != null && !obsActionPlan.getAction2().isEmpty()) {
            data += "\n" + obsActionPlan.getAction2();
        }

        if (criticalQuestions != null && criticalQuestions.size() > 0) {
            data += "\n\n" + getString(R.string.critical_steps) + "\n\n";

            //For each score add proper items
            for (Iterator<CompositeScoreDB> iterator = compositeScoresTree.iterator();
                    iterator.hasNext(); ) {
                CompositeScoreDB compositeScore = iterator.next();
                data += compositeScore.getHierarchical_code() + " " + compositeScore.getLabel()
                        + "\n";
                for (QuestionDB question : criticalQuestions) {
                    if (question.getCompositeScoreFk()
                            == (compositeScore.getId_composite_score())) {
                        data += "-" + question.getForm_name() + "\n";
                    }
                }
            }
        }
        data += "\n\n" + getString(R.string.see_full_assessment) + "\n";
        if (survey.isSent()) {
            data += "https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid() + "\n";
        } else {
            data += getString(R.string.url_not_available) + "\n";
        }
        System.out.println("data:" + data);
        return data;
    }

    private String extractHtmlData(ObsActionPlanDB obsActionPlan, SurveyDB survey,
            List<QuestionDB> criticalQuestions, List<CompositeScoreDB> compositeScoresTree) {

        String gasp = "";
        String actionPlan = "";
        String action1 = "";
        String action2 = "";

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

        if (obsActionPlan.getGaps() != null) {
            gasp = obsActionPlan.getGaps();
        }

        data += "<p><b>" + getString(R.string.plan_action_gasp_title) + "</b> " + gasp + "</p>";


        if (obsActionPlan.getAction_plan() != null) {
            actionPlan = obsActionPlan.getAction_plan();
        }

        data += "<p><b>" + getString(R.string.plan_action_action_plan_title) + "</b> " +
                actionPlan + "</p>";


        if (obsActionPlan.getAction1() != null) {
            action1 = obsActionPlan.getAction1();
        }


        if (obsActionPlan.getAction2() != null) {
            action2 = obsActionPlan.getAction2();
        }

        if (!action1.isEmpty()) {
            data += "<p><b>" + getString(R.string.plan_action_action_title) + "</b> " + action1;
        }
        if (!action2.isEmpty()) {
            data += action2 + "</p>";
        } else {
            data += "</p>";
        }

        if (criticalQuestions != null && criticalQuestions.size() > 0) {
            data += "<p><b>" + getString(R.string.critical_steps) + "</p>";

            //For each score add proper items
            for (Iterator<CompositeScoreDB> iterator = compositeScoresTree.iterator();
                    iterator.hasNext(); ) {
                CompositeScoreDB compositeScore = iterator.next();
                data += "<p><b>" + compositeScore.getHierarchical_code() + " "
                        + compositeScore.getLabel
                        () + "</b></p>";
                for (QuestionDB question : criticalQuestions) {
                    if (question.getCompositeScoreFk()
                            == (compositeScore.getId_composite_score())) {
                        data += "<p style=\"font-style: italic;\">" + "-" + question.getForm_name()
                                + "</p>";
                    }
                }
            }
        }
        data += getString(R.string.see_full_assessment) + "</p>";
        if (survey.isSent()) {
            data += "<a href=https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid()
                    +
                    ">https://apps.psi-mis.org/hnqis/feedback?event=" + survey.getEventUid()
                    + "</a></p>";
        } else {
            data += getString(R.string.url_not_available) + "</p>";
        }
        data += "</body>"
                + "</html>";
        return data;
    }
}

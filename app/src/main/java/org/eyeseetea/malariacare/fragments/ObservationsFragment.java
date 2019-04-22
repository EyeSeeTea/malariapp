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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
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
import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.data.repositories.ObservationRepository;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.usecase.GetObservationBySurveyUidUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetServerMetadataUseCase;
import org.eyeseetea.malariacare.domain.usecase.SaveObservationUseCase;
import org.eyeseetea.malariacare.layout.adapters.MissedCriticalStepsAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.presentation.presenters.ObservationsPresenter;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.MissedCriticalStepViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.ObservationViewModel;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;
import org.eyeseetea.malariacare.views.CustomEditText;
import org.eyeseetea.malariacare.views.CustomSpinner;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.List;

public class ObservationsFragment extends Fragment implements IModuleFragment,
        ObservationsPresenter.View {

    public static final String TAG = ".ObservationsFragment";
    private static final String SURVEY_UID = "surveyUid";
    private ArrayAdapter<CharSequence> mActionsAdapter;
    private ArrayAdapter<CharSequence> mSubActionsAdapter;

    private CustomTextView mTotalScoreTextView;
    private CustomTextView mCompetencyTextView;
    private CustomTextView mOrgUnitTextView;
    private CustomTextView mNextDateTextView;
    private CustomTextView mCompletionDateTextView;
    private View otherView;
    private View secondaryView;
    private ImageButton mGoBack;
    private CustomEditText mCustomProviderText;
    private CustomEditText mCustomActionPlanEditText;
    private CustomEditText mCustomActionOtherEditText;
    private CustomSpinner actionSpinner;
    private CustomSpinner secondaryActionSpinner;
    private FloatingActionButton mFabComplete;
    private FloatingActionButton fabShare;
    private RelativeLayout mRootView;
    private ObservationsPresenter presenter;
    private RecyclerView missedCriticalStepsView;
    private MissedCriticalStepsAdapter missedCriticalStepsAdapter;

    public static ObservationsFragment newInstance(String surveyUid) {
        ObservationsFragment myFragment = new ObservationsFragment();

        Bundle args = new Bundle();
        args.putString(SURVEY_UID, surveyUid);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_observations, container,
                false);

        String surveyUid = getArguments().getString(SURVEY_UID);

        initActions();
        initSubActions();
        initLayoutHeaders();
        initEditTexts();
        initFAB();
        initBackButton();
        initRecyclerView();
        initPresenter(surveyUid);

        return mRootView;
    }

    private void initRecyclerView() {
        missedCriticalStepsView = mRootView.findViewById(R.id.missed_critical_steps_view);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(missedCriticalStepsView.getContext(),
                        DividerItemDecoration.VERTICAL);
        missedCriticalStepsView.addItemDecoration(dividerItemDecoration);

        missedCriticalStepsAdapter = new MissedCriticalStepsAdapter();

        missedCriticalStepsView.setAdapter(missedCriticalStepsAdapter);
    }

    @Override
    public void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private void initPresenter(String surveyUid) {
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();
        ObservationLocalDataSource observationLocalDataSource = new ObservationLocalDataSource();

        IObservationRepository observationRepository =
                new ObservationRepository(observationLocalDataSource);

        GetObservationBySurveyUidUseCase getObservationBySurveyUidUseCase =
                new GetObservationBySurveyUidUseCase(asyncExecutor, mainExecutor,
                        observationRepository);

        IServerMetadataRepository serverMetadataRepository =
                new ServerMetadataRepository(getActivity());

        GetServerMetadataUseCase getServerMetadataUseCase =
                new GetServerMetadataUseCase(asyncExecutor, mainExecutor, serverMetadataRepository);

        SaveObservationUseCase saveObservationUseCase =
                new SaveObservationUseCase(asyncExecutor, mainExecutor, observationRepository);

        presenter = new ObservationsPresenter(getActivity(),
                getObservationBySurveyUidUseCase, getServerMetadataUseCase, saveObservationUseCase);


        presenter.attachView(this, surveyUid);

    }

    private void initEditTexts() {
        mCustomProviderText = mRootView.findViewById(
                R.id.plan_action_provider_text);

        mCustomProviderText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                presenter.providerChanged(editable.toString());
            }
        });

        mCustomActionPlanEditText = mRootView.findViewById(
                R.id.plan_action_action_plan_edit_text);

        mCustomActionPlanEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                presenter.actionPlanChanged(editable.toString());
            }
        });
        mCustomActionOtherEditText = mRootView.findViewById(
                R.id.plan_action_others_edit_text);

        mCustomActionOtherEditText.addTextChangedListener(new CustomTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                presenter.subActionOtherChanged(editable.toString());
            }
        });
    }

    private void initBackButton() {
        mGoBack = (ImageButton) mRootView.findViewById(
                R.id.backToSentSurveys);

        mGoBack.setOnClickListener(v -> getActivity().onBackPressed()
        );
    }

    private void initFAB() {
        initFabComplete(mRootView);

        fabShare = mRootView.findViewById(R.id.fab_share);

        fabShare.setOnClickListener(view -> presenter.shareObsActionPlan());
    }

    private void initFabComplete(RelativeLayout llLayout) {
        mFabComplete = llLayout.findViewById(R.id.fab_save);

        mFabComplete.setOnClickListener(view -> new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setMessage(getActivity().getString(
                        R.string.dialog_info_ask_for_completion_plan))
                .setPositiveButton(android.R.string.yes,
                        (arg0, arg1) -> presenter.completeObservation())
                .setNegativeButton(android.R.string.no, null).create().show());
    }

    private void initActions() {
        actionSpinner = mRootView.findViewById(R.id.plan_action_spinner);

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
        secondaryActionSpinner = mRootView.findViewById(
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
        mCompetencyTextView = mRootView.findViewById(R.id.feedback_competency);
        mTotalScoreTextView = mRootView.findViewById(R.id.feedback_total_score);
        mOrgUnitTextView = mRootView.findViewById(
                R.id.org_unit);
        mCompletionDateTextView = mRootView.findViewById(
                R.id.completion_date);
        mNextDateTextView = mRootView.findViewById(R.id.next_date);
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
        mCustomProviderText.setEnabled(false);
        mCustomActionPlanEditText.setEnabled(false);
        mCustomActionOtherEditText.setEnabled(false);
        actionSpinner.setEnabled(false);
        secondaryActionSpinner.setEnabled(false);
        mFabComplete.setEnabled(false);
    }

    @Override
    public void renderBasicObservations(String provider, String actionPlan) {
        mCustomProviderText.setText(provider);
        mCustomActionPlanEditText.setText(actionPlan);
    }

    @Override
    public void renderMissedCriticalSteps(
            List<MissedCriticalStepViewModel> missedCriticalSteps) {
        missedCriticalStepsAdapter.setMissedCriticalSteps(missedCriticalSteps);
    }

    @Override
    public void showSubActionOptionsView() {
        secondaryActionSpinner.setVisibility(View.VISIBLE);
        secondaryView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSubActionOptionsView() {
        secondaryActionSpinner.setVisibility(View.GONE);
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
        otherView.setVisibility(View.GONE);
    }

    @Override
    public void updateStatusView(ObservationStatus status) {
        if (status.equals(ObservationStatus.IN_PROGRESS)) {
            mFabComplete.setImageResource(R.drawable.ic_action_uncheck);
        } else if (status.equals(ObservationStatus.SENT)) {
            mFabComplete.setImageResource(R.drawable.ic_double_check);
        } else {
            mFabComplete.setImageResource(R.drawable.ic_action_check);
        }
    }

    @Override
    public void renderHeaderInfo(String orgUnitName, Float mainScore, String completionDate,
            String nextDate,
            CompetencyScoreClassification classification) {

        mOrgUnitTextView.setText(orgUnitName);

        CompetencyUtils.setBackgroundByCompetency(mCompetencyTextView, classification);
        CompetencyUtils.setTextColorByCompetency(mCompetencyTextView, classification);
        CompetencyUtils.setTextByCompetency(mCompetencyTextView, classification);

        if (mainScore > 0f) {
            mTotalScoreTextView.setText(String.format("%.1f%%", mainScore));
            int colorId = LayoutUtils.trafficColor(mainScore);
            mTotalScoreTextView.setBackgroundColor(getResources().getColor(colorId));
        } else {
            mTotalScoreTextView.setText(String.format("NaN"));
            int colorId = LayoutUtils.trafficColor(mainScore);
            mTotalScoreTextView.setBackgroundColor(getResources().getColor(colorId));
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
    public void shareByText(ObservationViewModel observationViewModel, SurveyDB survey,
            List<MissedCriticalStepViewModel> missedCriticalStepViewModels) {
        String data = extractTextData(observationViewModel, survey, missedCriticalStepViewModels);

        shareData(data);
    }

    @Override
    public void shareNotSent(String surveyNoSentMessage) {
        shareData(surveyNoSentMessage);
    }

    @Override
    public void enableShareButton() {
        fabShare.setEnabled(true);
        fabShare.getBackground().clearColorFilter();
        int shareColor = ContextCompat.getColor(fabShare.getContext(),
                R.color.share_fab_background);

        fabShare.getBackground().setColorFilter(shareColor, PorterDuff.Mode.SRC_IN);

    }

    @Override
    public void disableShareButton() {
        fabShare.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        fabShare.setEnabled(false);
    }

    private String extractTextData(ObservationViewModel observationViewModel, SurveyDB survey,
            List<MissedCriticalStepViewModel> missedCriticalStepViewModels) {
        String data =
                PreferencesState.getInstance().getContext().getString(
                        R.string.app_name) + "- \n";
        DateParser dateParser = new DateParser();
        data += getString(R.string.supervision_on) + " " + survey.getOrgUnit().getName() + "/"
                + survey.getProgram().getName() + "\n";

        data += getString(R.string.on) + " " + dateParser.format
                (survey.getCompletionDate(), DateParser.EUROPEAN_DATE_FORMAT)
                + "\n";


        CompetencyScoreClassification classification =
                CompetencyScoreClassification.get(
                        survey.getCompetencyScoreClassification());

        String competencyText = CompetencyUtils.getTextByCompetency(classification, getActivity());
        data += getString(R.string.competency_title).toUpperCase() + ": "
                + competencyText + "\n";

        int roundedScore = Math.round(survey.getMainScore());
        data += getString(R.string.quality_of_care) + " " + roundedScore + "% \n";

        data += String.format(getString(R.string.plan_action_next_date),
                dateParser.format(SurveyPlanner.getInstance().findScheduledDateBySurvey(survey),
                        DateParser.EUROPEAN_DATE_FORMAT));

        if (observationViewModel.getProvider() != null && !observationViewModel.getProvider().isEmpty()) {
            data += "\n\n" + getString(R.string.plan_action_provider_title) + " "
                    + observationViewModel.getProvider();
        }


        data += "\n" + getString(R.string.plan_action_action_plan_title) + " ";

        if (observationViewModel.getActionPlan() != null && !observationViewModel.getActionPlan().isEmpty()) {
            data += observationViewModel.getActionPlan();
        }

        data += "\n" + getString(R.string.plan_action_action_title) + " ";

        if (observationViewModel.getAction1() != null &&
                !observationViewModel.getAction1().getActivityAction().isEmpty()) {
            data += observationViewModel.getAction1();
        }


        if (observationViewModel.getAction2() != null &&
                !observationViewModel.getAction2().getSubActivityAction().isEmpty()) {
            data += "\n" + observationViewModel.getAction2();
        }

        if (missedCriticalStepViewModels != null && missedCriticalStepViewModels.size() > 0) {
            data += "\n\n" + getString(R.string.critical_steps) + "\n";

            //For each score add proper items
            for (MissedCriticalStepViewModel missedCriticalStepViewModel :
                    missedCriticalStepViewModels) {

                if (missedCriticalStepViewModel.isCompositeScore()) {
                    data += missedCriticalStepViewModel.getLabel() + "\n";
                } else {
                    data += "-" + missedCriticalStepViewModel.getLabel()  + "\n";
                }
            }
        }
        data += "\n\n" + getString(R.string.see_full_assessment) + "\n";
        if (survey.isSent()) {
            data += String.format(getActivity().getString(R.string.feedback_url),
                    survey.getEventUid(), Session.getCredentials().getServerURL());
        } else {
            data += getString(R.string.url_not_available) + "\n";
        }
        System.out.println("data:" + data);
        return data;
    }

    private void shareData(String data) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, data);
        sendIntent.setType("text/plain");
        getActivity().startActivity(sendIntent);

        System.out.println("data:" + data);
    }

    class CustomTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}



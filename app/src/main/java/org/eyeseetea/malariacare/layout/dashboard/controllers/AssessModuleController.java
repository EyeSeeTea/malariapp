/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.dashboard.controllers;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.data.repositories.SurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.CompleteSurveyUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;
import org.eyeseetea.malariacare.domain.usecase.SaveSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.fragments.CreateSurveyFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.DoublePieChart;
import org.eyeseetea.malariacare.views.SurveyDialog;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

/**
 * Created by idelcano on 25/02/2016.
 */
public class AssessModuleController extends ModuleController {


    SurveyFragment surveyFragment;

    CreateSurveyFragment createSurveyFragment;

    OrgUnitProgramFilterView orgUnitProgramFilterView;

    SaveSurveyAnsweredRatioUseCase saveSurveyAnsweredRatioUseCase;
    GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase;

    CompleteSurveyUseCase completeSurveyUseCase;

    public AssessModuleController(ModuleSettings moduleSettings) {
        super(moduleSettings);
        this.tabLayout = R.id.tab_assess_layout;
        this.idVerticalTitle = R.id.titleInProgress;

        initializeDependencies();
    }

    private void initializeDependencies() {
        ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository =
                new SurveyAnsweredRatioRepository();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();
        IMainExecutor mainExecutor = new UIThreadExecutor();

        saveSurveyAnsweredRatioUseCase = new SaveSurveyAnsweredRatioUseCase(
                surveyAnsweredRatioRepository, mainExecutor, asyncExecutor);

        getSurveyAnsweredRatioUseCase = new GetSurveyAnsweredRatioUseCase(
                surveyAnsweredRatioRepository, mainExecutor, asyncExecutor);

        completeSurveyUseCase = new CompleteSurveyUseCase(mainExecutor,asyncExecutor);
    }

    public static String getSimpleName() {
        return AssessModuleController.class.getSimpleName();
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        fragment = new DashboardUnsentFragment();

        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) dashboardActivity.findViewById(
                        R.id.assess_org_unit_program_filter_view);

        orgUnitProgramFilterView.setVisibility(View.VISIBLE);

    }

    /**
     * Leaving this tab might imply a couple of checks:
     * - Before leaving a survey
     * -
     */
    public void onExitTab() {
        if (!isFragmentActive(SurveyFragment.class)) {
            return;
        }

        final SurveyDB survey = Session.getSurveyByModule(getSimpleName());
        if (survey.isCompleted() || survey.isSent()) {
            dashboardController.setNavigatingBackwards(false);
            closeSurveyFragment();
            return;
        }

        surveyFragment.showProgress();
        closeSurveyFragment(survey, org.eyeseetea.malariacare.domain
                .utils.Action.CHANGE_TAB);
    }

    private void closeSurveyFragment(final SurveyDB survey,
            final org.eyeseetea.malariacare.domain.utils.Action action) {
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                        surveyFragment.nextProgressMessage();
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        if (action.equals(
                                org.eyeseetea.malariacare.domain.utils.Action.PRESS_BACK_BUTTON)) {
                            surveyFragment.hideProgress();
                            boolean isDialogShown = onSurveyBackPressed(surveyAnsweredRatio, survey);
                            if (!isDialogShown) {
                                //Confirm closing
                                if (survey.isCompleted() || survey.isSent()) {
                                    dashboardController.setNavigatingBackwards(false);
                                    surveyFragment.hideProgress();
                                    closeSurveyFragment();
                                    return;
                                } else {
                                    askToCloseSurvey();
                                }
                            }
                        } else if (action.equals(
                                org.eyeseetea.malariacare.domain.utils.Action.CHANGE_TAB)) {
                            if (surveyAnsweredRatio.getCompulsoryAnswered()
                                    == surveyAnsweredRatio.getTotalCompulsory()
                                    && surveyAnsweredRatio.getTotalCompulsory() != 0) {
                                askToSendCompulsoryCompletedSurvey(survey);
                            }
                            surveyFragment.hideProgress();
                            closeSurveyFragment();

                        }
                    }
                });
    }

    public void onBackPressed() {

        //Creating survey -> nothing to do
        if (isFragmentActive(CreateSurveyFragment.class)) {

            //Vertical -> full reload
            if (DashboardOrientation.VERTICAL.equals(dashboardController.getOrientation())) {
                dashboardController.reloadVertical();
            } else {
                reloadFragment();
            }
            return;
        }
        //List Unsent surveys -> ask before leaving
        if (isFragmentActive(DashboardUnsentFragment.class)) {
            super.onBackPressed();
            return;
        }

        surveyFragment.showProgress();
        final SurveyDB survey = Session.getSurveyByModule(getSimpleName());

        closeSurveyFragment(survey, org.eyeseetea.malariacare.domain
                .utils.Action.PRESS_BACK_BUTTON);
        //if the survey is opened in review mode exit.
    }

    public void onSurveySelected(SurveyDB survey) {

        Session.setSurveyByModule(survey, getSimpleName());

        //Planned surveys needs to be started
        if (survey.getStatus() == Constants.SURVEY_PLANNED) {
            survey = SurveyPlanner.getInstance().startSurvey(survey);
        }

        //Set the survey into the session
        Session.setSurveyByModule(survey, getSimpleName());

        if (!survey.isReadOnly()) {
            //Start looking for geo if the survey is not sent/completed
            dashboardActivity.prepareLocationListener(survey);
        }
        //Prepare survey fragment
        surveyFragment = new SurveyFragment();



        surveyFragment.setModuleName(getSimpleName());
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
        orgUnitProgramFilterView.setVisibility(View.GONE);

        final SurveyDB finalSurvey = survey;
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {

                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        saveSurveyAnsweredRatioUseCase.execute(
                                new ISurveyAnsweredRatioCallback() {
                                    @Override
                                    public void nextProgressMessage() {
                                        Log.d(getClass().getName(), "nextProgressMessage");
                                        surveyFragment.nextProgressMessage();
                                    }

                                    @Override
                                    public void onComplete(
                                            SurveyAnsweredRatio surveyAnsweredRatio) {
                                        Log.d(getClass().getName(), "onComplete");
                                        if (surveyAnsweredRatio != null) {
                                            LayoutUtils.setActionBarTitleForSurveyAndChart(
                                                    dashboardActivity, finalSurvey, getTitle(),
                                                    surveyAnsweredRatio);

                                            initializeStatusChart();
                                        }
                                    }
                                }, surveyAnsweredRatio);
                    }
                });
    }

    private void initializeStatusChart() {
        DoublePieChart doublePieChart =
                (DoublePieChart) DashboardActivity.dashboardActivity.getSupportActionBar
                        ().getCustomView().findViewById(
                        R.id.action_bar_chart);

        doublePieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final SurveyDB survey = Session.getSurveyByModule(getSimpleName());

                getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                        new ISurveyAnsweredRatioCallback() {
                            @Override
                            public void nextProgressMessage() {
                            }

                            @Override
                            public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                                SurveyDialog.Builder builder = SurveyDialog.newBuilder(
                                        dashboardActivity, survey);

                                final View.OnClickListener completeButtonListener =
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                onMarkAsCompleted(survey);
                                            }
                                        };

                                builder.completeButton(completeButtonListener, surveyAnsweredRatio.isCompulsoryCompleted())
                                        .bodyTextID(R.string.dialog_pie_chart_label_explanation)
                                        .build();
                            }
                        });
            }
        });
    }

    public void onMarkAsCompleted(final SurveyDB survey) {
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {
                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                        //This cannot be mark as completed
                        if (!surveyAnsweredRatio.isCompulsoryCompleted()) {
                            alertCompulsoryQuestionIncompleted();
                            return;
                        } else {
                            alertAreYouSureYouWantToComplete(survey);
                        }
                    }
                });
    }

    public void onNewSurvey() {
        if (PreferencesState.getInstance().isVerticalDashboard()) {
            LayoutUtils.setActionBarBackButton(dashboardActivity);
            CustomTextView sentTitle = (CustomTextView) dashboardActivity.findViewById(
                    R.id.titleCompleted);
            sentTitle.setText("");
        }

        if (createSurveyFragment == null) {
            createSurveyFragment = new CreateSurveyFragment();
        }
        orgUnitProgramFilterView.setVisibility(View.GONE);
        replaceFragment(getLayout(), createSurveyFragment);
    }

    /**
     * It is called when the user press back in a surveyFragment
     */
    private boolean onSurveyBackPressed(SurveyAnsweredRatio surveyAnsweredRatio, SurveyDB surveyDB) {
        //Completed or Mandatory ok -> ask to send
        if (surveyAnsweredRatio.getCompulsoryAnswered() == surveyAnsweredRatio.getTotalCompulsory()
                && surveyAnsweredRatio.getTotalCompulsory() != 0) {
            askToSendCompulsoryCompletedSurvey(surveyDB);
            return true;
        }
        return false;
    }

    public void setActionBarDashboard() {
        super.setActionBarDashboard();
    }

    /**
     * This dialog is called when the user have a survey open, with compulsory questions completed,
     * and close this survey, or when the user change of tab
     */
    private void askToSendCompulsoryCompletedSurvey(final SurveyDB surveyDB) {
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(R.string.dialog_question_complete_survey)
                .setNegativeButton(R.string.dialog_complete_option,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                confirmSendCompleteSurvey(surveyDB);
                            }
                        })
                .setPositiveButton(R.string.dialog_continue_later_option,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                dashboardController.setNavigatingBackwards(true);
                                closeSurveyFragment();
                                if (DashboardOrientation.VERTICAL.equals(
                                        dashboardController.getOrientation())) {
                                    dashboardController.reloadVertical();
                                }
                                dashboardController.setNavigatingBackwards(false);
                            }
                        }).create().show();
    }

    /**
     * This dialog is called when the user have a survey open, and close this survey, or when the
     * user change of tab
     */
    private void askToCloseSurvey() {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(R.string.survey_title_exit)
                .setMessage(R.string.survey_info_exit).setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        dashboardController.setNavigatingBackwards(true);
                        closeSurveyFragment();
                        dashboardController.setNavigatingBackwards(false);
                    }
                }).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        //Closing survey cancel -> Nothing to do
                    }
                }).create().show();
    }

    /**
     * This dialog is called to confirm before set a survey as complete
     */
    private void confirmSendCompleteSurvey(final SurveyDB surveyDB) {
        //if you select complete_option, this dialog will showed.
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(R.string.dialog_are_you_sure_complete_survey)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        completeAndCloseSurvey(surveyDB);
                    }
                }).create().show();
    }

    private void closeSurveyFragment() {
        //Clear survey fragment
        if (isFragmentActive(SurveyFragment.class)) {
            surveyFragment.hideProgress();
            SurveyFragment surveyFragment = getSurveyFragment();
            surveyFragment.unregisterReceiver();
        }
        //Reload Assess fragment
        if (DashboardOrientation.VERTICAL.equals(dashboardController.getOrientation())) {
            dashboardController.reloadVertical();
        } else {
            reloadFragment();
        }

        //Reset score register
//        ScoreRegister.clear();

        //Update action bar title
        super.setActionBarDashboard();
    }

    public void alertCompulsoryQuestionIncompleted() {
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(
                        dashboardActivity.getString(R.string.dialog_incompleted_compulsory_survey))
                .setPositiveButton(dashboardActivity.getString(R.string.ok), null)
                .create().show();
    }

    private void alertAreYouSureYouWantToComplete(final SurveyDB survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(
                        R.string.dialog_info_ask_for_completion), survey.getProgram().getName()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        completeAndCloseSurvey(survey);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show();
    }

    private void completeAndCloseSurvey(SurveyDB survey) {
        completeSurveyUseCase.execute(survey, new CompleteSurveyUseCase.Callback() {
            @Override
            public void onCompleteSurveySuccess() {
                if (!survey.isInProgress()) {
                    alertOnCompleteGoToFeedback(survey);
                }

                dashboardController.setNavigatingBackwards(true);
                closeSurveyFragment();
                if (DashboardOrientation.VERTICAL.equals(
                        dashboardController.getOrientation())) {
                    dashboardController.reloadVertical();
                }
                dashboardController.setNavigatingBackwards(false);
            }

            @Override
            public void onCompleteSurveyError(Exception e) {
                Log.e(getSimpleName(), e.getMessage());
            }
        });
    }

    private void alertOnComplete(SurveyDB survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(
                        R.string.dialog_info_on_complete), survey.getProgram().getName()))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .create().show();
    }

    public void alertOnCompleteGoToFeedback(final SurveyDB survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(
                        R.string.dialog_info_on_complete), survey.getProgram().getName()))
                .setNeutralButton(android.R.string.ok, null)
                .setPositiveButton((R.string.go_to_feedback),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                //Move to feedbackfragment
                                dashboardActivity.openFeedback(survey, true);
                            }
                        })
                .setCancelable(true)
                .create().show();
    }

    private SurveyFragment getSurveyFragment() {
        return (SurveyFragment) dashboardActivity.getSupportFragmentManager().findFragmentById(
                R.id.dashboard_details_container);
    }

    private SurveyDialog surveyDialog;

    public void assessModelDialog(@NonNull final SurveyDB survey) {

        if (surveyDialog == null || !surveyDialog.isShowing()) {

            SurveyDialog.Builder builder = SurveyDialog.newBuilder(dashboardActivity, survey);

            final View.OnClickListener editButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSurveySelected(survey);
                }
            };

            final View.OnClickListener completeButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMarkAsCompleted(survey);
                }
            };

            final View.OnClickListener deleteButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //this method create a new survey getting the getScheduledDate date of the
                    // oldsurvey, and remove it.
                    SurveyPlanner.getInstance().deleteSurveyAndBuildNext(survey);
                    DashboardActivity.reloadDashboard();
                }
            };

            surveyDialog = builder.editButton(editButtonListener)
                    .completeButton(completeButtonListener, true)
                    .deleteButton(deleteButtonListener)
                    .build();

            getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                    new ISurveyAnsweredRatioCallback() {
                        @Override
                        public void nextProgressMessage() {

                        }

                        @Override
                        public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                            boolean isCompulsoryCompleted =
                                    surveyAnsweredRatio.isCompulsoryCompleted();
                            Button button = surveyDialog.getMarkCompleteButton();

                            if (!isCompulsoryCompleted) {
                                button.getBackground().setColorFilter(Color.GRAY,
                                        PorterDuff.Mode.SRC_IN);
                                button.setEnabled(false);
                            }
                        }
                    });
        }

    }
}
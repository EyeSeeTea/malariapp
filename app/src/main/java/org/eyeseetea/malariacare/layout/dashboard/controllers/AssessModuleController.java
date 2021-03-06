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
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.domain.usecase.GetSurveyAnsweredRatioUseCase;
import org.eyeseetea.malariacare.domain.utils.Action;
import org.eyeseetea.malariacare.fragments.CreateSurveyFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.DashboardOrientation;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomTextView;

/**
 * Created by idelcano on 25/02/2016.
 */
public class AssessModuleController extends ModuleController {


    SurveyFragment surveyFragment;

    CreateSurveyFragment createSurveyFragment;

    public AssessModuleController(ModuleSettings moduleSettings) {
        super(moduleSettings);
        this.tabLayout = R.id.tab_assess_layout;
        this.idVerticalTitle = R.id.titleInProgress;
    }

    public static String getSimpleName() {
        return AssessModuleController.class.getSimpleName();
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        fragment = new DashboardUnsentFragment();
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

        Survey survey = Session.getSurveyByModule(getSimpleName());
        if (survey.isCompleted() || survey.isSent()) {
            dashboardController.setNavigatingBackwards(false);
            closeSurveyFragment();
            return;
        }
        new AsyncOnCloseSurveyFragment(surveyFragment, survey, Action.CHANGE_TAB).execute();

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
        surveyFragment.nextProgressMessage();
        final Survey survey = Session.getSurveyByModule(getSimpleName());
        new AsyncOnCloseSurveyFragment(surveyFragment, survey, Action.PRESS_BACK_BUTTON).execute();
        //if the survey is opened in review mode exit.
    }

    public void onSurveySelected(Survey survey) {

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
        surveyFragment = SurveyFragment.newInstance(1);

        surveyFragment.setModuleName(getSimpleName());
        replaceFragment(R.id.dashboard_details_container, surveyFragment);
        LayoutUtils.setActionBarTitleForSurvey(dashboardActivity, survey);
    }

    public void onMarkAsCompleted(final Survey survey) {
        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase = new GetSurveyAnsweredRatioUseCase();
        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                GetSurveyAnsweredRatioUseCase.RecoveryFrom.DATABASE,
                new GetSurveyAnsweredRatioUseCase.Callback() {
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
            createSurveyFragment = CreateSurveyFragment.newInstance(1);
        }
        replaceFragment(getLayout(), createSurveyFragment);
    }

    /**
     * It is called when the user press back in a surveyFragment
     */
    private boolean onSurveyBackPressed(SurveyAnsweredRatio surveyAnsweredRatio) {
        //Completed or Mandatory ok -> ask to send
        if (surveyAnsweredRatio.getCompulsoryAnswered() == surveyAnsweredRatio.getTotalCompulsory()
                && surveyAnsweredRatio.getTotalCompulsory() != 0) {
            askToSendCompulsoryCompletedSurvey();
            return true;
        }
        return false;
    }

    public void setActionBarDashboard() {
        if (!isFragmentActive(SurveyFragment.class)) {
            super.setActionBarDashboard();
            return;
        }

        //In survey -> custom action bar
        Survey survey = Session.getSurveyByModule(getSimpleName());
        String appNameColorString = LayoutUtils.getAppNameColorString();
        String title = getActionBarTitleBySurvey(survey);
        String subtitle = getActionBarSubTitleBySurvey(survey);

        if (PreferencesState.getInstance().isVerticalDashboard()) {
            LayoutUtils.setActionbarVerticalSurvey(dashboardActivity, title, subtitle);
        } else {
            Spanned spannedTitle = Html.fromHtml(
                    String.format("<font color=\"#%s\"><b>%s</b></font>", appNameColorString,
                            title));
            LayoutUtils.setActionbarTitle(dashboardActivity, spannedTitle, subtitle);
        }
    }

    /**
     * This dialog is called when the user have a survey open, with compulsory questions completed,
     * and close this survey, or when the user change of tab
     */
    private void askToSendCompulsoryCompletedSurvey() {
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(R.string.dialog_question_complete_survey)
                .setNegativeButton(R.string.dialog_complete_option,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                confirmSendCompleteSurvey();
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
                        final Survey survey = Session.getSurveyByModule(getSimpleName());

                        GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase = new GetSurveyAnsweredRatioUseCase();
                        getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                                GetSurveyAnsweredRatioUseCase.RecoveryFrom.MEMORY_FIRST,
                                new GetSurveyAnsweredRatioUseCase.Callback() {
                                    @Override
                                    public void nextProgressMessage() {
                                        Log.d(getClass().getName(), "nextProgressMessage");
                                    }

                                    @Override
                                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                                        Survey dbSurvey = Survey.findById(survey.getId_survey());
                                        dbSurvey.updateSurveyStatus(surveyAnsweredRatio);
                                    }
                                });
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
    private void confirmSendCompleteSurvey() {
        //if you select complete_option, this dialog will showed.
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(R.string.dialog_are_you_sure_complete_survey)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        Survey survey = Session.getSurveyByModule(getSimpleName());
                        survey.setCompleteSurveyState(getSimpleName());
                        alertOnComplete(survey);
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
                .setPositiveButton(dashboardActivity.getString(R.string.accept), null)
                .create().show();
    }

    private void alertAreYouSureYouWantToComplete(final Survey survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(
                        R.string.dialog_info_ask_for_completion), survey.getProgram().getName()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Change state
                        survey.setCompleteSurveyState(getSimpleName());
                        if (!survey.isInProgress()) {
                            alertOnCompleteGoToFeedback(survey);
                        }
                        //Remove from list
                        ((DashboardUnsentFragment) fragment).removeSurveyFromAdapter(survey);
                        //Reload sent surveys
                        ((DashboardUnsentFragment) fragment).reloadToSend();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show();
    }

    private void alertOnComplete(Survey survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(
                        R.string.dialog_info_on_complete), survey.getProgram().getName()))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .create().show();
    }

    public void alertOnCompleteGoToFeedback(final Survey survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(
                        R.string.dialog_info_on_complete), survey.getProgram().getName()))
                .setNeutralButton(android.R.string.ok, null)
                .setPositiveButton((R.string.go_to_feedback),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                //Move to feedbackfragment
                                dashboardActivity.onFeedbackSelected(survey);
                            }
                        })
                .setCancelable(true)
                .create().show();
    }

    private SurveyFragment getSurveyFragment() {
        return (SurveyFragment) dashboardActivity.getFragmentManager().findFragmentById(
                R.id.dashboard_details_container);
    }

    public class AsyncOnCloseSurveyFragment extends AsyncTask<Void, Integer, SurveyAnsweredRatio> {
        SurveyAnsweredRatio mSurveyAnsweredRatio;
        SurveyFragment surveyFragment;
        Survey survey;
        Action action;

        public AsyncOnCloseSurveyFragment(SurveyFragment surveyFragment, Survey survey, Action action) {
            this.surveyFragment = surveyFragment;
            this.survey = survey;
            this.action = action;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //spinner
            surveyFragment.showProgress();
            surveyFragment.nextProgressMessage();
        }


        @Override
        protected SurveyAnsweredRatio doInBackground(Void... voids) {
            GetSurveyAnsweredRatioUseCase getSurveyAnsweredRatioUseCase = new GetSurveyAnsweredRatioUseCase();
            getSurveyAnsweredRatioUseCase.execute(survey.getId_survey(),
                    GetSurveyAnsweredRatioUseCase.RecoveryFrom.DATABASE,
                    new GetSurveyAnsweredRatioUseCase.Callback() {
                        @Override
                        public void nextProgressMessage() {
                            surveyFragment.nextProgressMessage();
                        }

                        @Override
                        public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {
                            mSurveyAnsweredRatio = surveyAnsweredRatio;
                        }
                    });
            return mSurveyAnsweredRatio;
        }

        @Override
        protected void onPostExecute(SurveyAnsweredRatio surveyAnsweredRatio) {
            if(action.equals(Action.PRESS_BACK_BUTTON)) {
                surveyFragment.hideProgress();
                boolean isDialogShown = onSurveyBackPressed(surveyAnsweredRatio);
                if(!isDialogShown){
                    //Confirm closing
                    if (survey.isCompleted() || survey.isSent()) {
                        dashboardController.setNavigatingBackwards(false);
                        surveyFragment.hideProgress();
                        closeSurveyFragment();
                        return;
                    }else{
                        askToCloseSurvey();
                    }
                }
            }
            else if (action.equals(Action.CHANGE_TAB)){
                super.onPostExecute(surveyAnsweredRatio);
                if (surveyAnsweredRatio.getCompulsoryAnswered()
                        == surveyAnsweredRatio.getTotalCompulsory()
                        && surveyAnsweredRatio.getTotalCompulsory() != 0) {
                    askToSendCompulsoryCompletedSurvey();
                }
                surveyFragment.hideProgress();
                closeSurveyFragment();

            }
        }
    }

}
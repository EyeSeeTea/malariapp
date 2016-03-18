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
import android.app.Fragment;
import android.content.DialogInterface;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.SurveyFragment;
import org.eyeseetea.malariacare.layout.dashboard.config.ModuleSettings;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;

/**
 * Created by idelcano on 25/02/2016.
 */
public class AssessModuleController extends ModuleController {

    public AssessModuleController(ModuleSettings moduleSettings){
        super(moduleSettings);
        this.tabLayout=R.id.tab_assess_layout;
    }

    @Override
    public void init(DashboardActivity activity) {
        super.init(activity);
        fragment = new DashboardUnsentFragment();
    }

    /**
     * Leaving this tab might imply a couple of checks:
     *  - Before leaving a survey
     *  -
     */
    public void onExitTab(){
        if(!isFragmentActive(R.id.dashboard_details_container, SurveyFragment.class)){
            return;
        }

        Survey survey = Session.getSurvey();
        SurveyAnsweredRatio surveyAnsweredRatio = survey.reloadSurveyAnsweredRatio();
        if (surveyAnsweredRatio.getCompulsoryAnswered() == surveyAnsweredRatio.getTotalCompulsory() && surveyAnsweredRatio.getTotalCompulsory() != 0) {
            askToSendCompulsoryCompletedSurvey();
        }
        closeSurveyFragment();
    }

    public void setActionBarDashboard(){
        if(!isFragmentActive(R.id.dashboard_details_container, SurveyFragment.class)){
            super.setActionBarDashboard();
            return;
        }
        setActionBarTitleForSurvey(Session.getSurvey());
    }

    /**
     * This dialog is called when the user have a survey open, with compulsory questions completed, and close this survey, or when the user change of tab
     */
    private void askToSendCompulsoryCompletedSurvey() {
        new AlertDialog.Builder(dashboardActivity)
                .setMessage(R.string.dialog_question_complete_survey)
                .setNegativeButton(R.string.dialog_complete_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        confirmSendCompleteSurvey();
                    }
                })
                .setPositiveButton(R.string.dialog_continue_later_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        closeSurveyFragment();
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
                        Survey survey=Session.getSurvey();
                        survey.setCompleteSurveyState();
                        alertOnComplete(survey);
                        closeSurveyFragment();
                    }
                }).create().show();
    }

    private void closeSurveyFragment(){
        ScoreRegister.clear();
        SurveyFragment surveyFragment = (SurveyFragment) dashboardActivity.getFragmentManager ().findFragmentById(R.id.dashboard_details_container);
        surveyFragment.unregisterReceiver();
        reloadFragment();
    }

    private void alertOnComplete(Survey survey) {
        new AlertDialog.Builder(dashboardActivity)
                .setTitle(null)
                .setMessage(String.format(dashboardActivity.getResources().getString(R.string.dialog_info_on_complete),survey.getProgram().getName()))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .create().show();
    }
}
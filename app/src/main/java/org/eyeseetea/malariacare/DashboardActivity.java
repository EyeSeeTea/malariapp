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

package org.eyeseetea.malariacare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.metadata.PhoneMetaData;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.PushUseCase;
import org.eyeseetea.malariacare.drive.DriveRestController;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.dashboard.controllers.DashboardController;
import org.eyeseetea.malariacare.layout.dashboard.controllers.PlanModuleController;
import org.eyeseetea.malariacare.network.PullClient;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;


public class DashboardActivity extends BaseActivity {

    private final static String TAG = ".DDetailsActivity";
    private boolean reloadOnResume = true;
    public DashboardController dashboardController;
    static Handler handler;
    public static DashboardActivity dashboardActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        dashboardActivity = this;
        if (getIntent().getBooleanExtra(getString(R.string.show_announcement_key), true) && !Session.getCredentials().isDemoCredentials()) {
            new AsyncAnnouncement().execute();
        }

        //XXX to remove?
        initDataIfRequired();

        loadPhoneMetadata();

        //get dashboardcontroller from settings.json
        dashboardController = AppSettingsBuilder.getInstance().getDashboardController();

        //layout according to config
        setContentView(dashboardController.getLayout());

        //delegate modules initialization
        dashboardController.onCreate(this, savedInstanceState);

        if (!Session.getCredentials().isDemoCredentials()) {
            //Media: init drive credentials
            DriveRestController.getInstance().init(this);
        }
    }


    PhoneMetaData getPhoneMetadata() {
        PhoneMetaData phoneMetaData = new PhoneMetaData();
        TelephonyManager phoneManagerMetaData = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        String imei = phoneManagerMetaData.getDeviceId();
        String phone = phoneManagerMetaData.getLine1Number();
        String serial = phoneManagerMetaData.getSimSerialNumber();
        phoneMetaData.setImei(imei);
        phoneMetaData.setPhone_number(phone);
        phoneMetaData.setPhone_serial(serial);

        return phoneMetaData;
    }


    public void loadPhoneMetadata() {
        PhoneMetaData phoneMetaData = getPhoneMetadata();
        Session.setPhoneMetaData(phoneMetaData);
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        Log.d(TAG, String.format("onActivityResult(%d, %d)", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, data);

        //Delegate activity result to media controller
        DriveRestController.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
        }
        //Any common option
        if (item.getItemId() != R.id.action_pull) {
            return super.onOptionsItemSelected(item);
        }

        //Pull
        final int unsentSurveysCount = SurveyDB.countAllUnsentUnplannedSurveys();

        //No unsent data -> pull (no confirmation)
        if (unsentSurveysCount == 0) {
            pullMetadata();
            return true;
        }

        //No unsent data -> pull (no confirmation)
        String message = getApplicationContext().getResources().getString(
                R.string.dialog_action_refresh);
        if (unsentSurveysCount > 0) {
            message += String.format(getApplicationContext().getResources().getString(
                    R.string.dialog_incomplete_surveys_before_refresh),
                    unsentSurveysCount);
        }
        //check if exist a compulsory question without awnser before push and pull.

        new AlertDialog.Builder(this)
                .setTitle(getApplicationContext().getResources().getString(
                        R.string.settings_menu_pull))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        pullMetadata();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setCancelable(true)
                .create().show();
        return true;
    }

    private void pullMetadata() {
        if (PreferencesState.getInstance().isPushInProgress()) {
            Toast.makeText(getBaseContext(), R.string.toast_push_in_progress,
                    Toast.LENGTH_LONG).show();
            return;
        }
        PreferencesState.getInstance().clearOrgUnitPreference();
        finishAndGo(ProgressActivity.class);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void initTransition() {
        this.overridePendingTransition(R.transition.anim_slide_in_right,
                R.transition.anim_slide_out_right);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        getSurveysFromService();
        DriveRestController.getInstance().syncMedia();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    public void setReloadOnResume(boolean doReload) {
        this.reloadOnResume = false;
    }

    public void getSurveysFromService() {
        Log.d(TAG, "getSurveysFromService (" + reloadOnResume + ")");
        if (!reloadOnResume) {
            //Flag is readjusted
            reloadOnResume = true;
            return;
        }
        reloadDashboard();
    }

    public static void reloadDashboard() {
        Intent surveysIntent = new Intent(dashboardActivity, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        dashboardActivity.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        dashboardController.onBackPressed();
    }

    /**
     * PUll data from DHIS server and turn into our model
     */
    private void initDataIfRequired() {
        initUserSessionIfRequired();
    }

    /**
     * In case Session doesn't have the user set, here we set it to the first entry of User table
     */
    private void initUserSessionIfRequired() {
        // already a user in session -> done
        if (Session.getUser() != null) {
            return;
        }

        // If we're in dashboard and User is not yet in session we have to put it
        // FIXME: for the moment there will be only one user in the User table, but in the future
        // we will have to think about tagging the logged user in the DB
        UserDB user = UserDB.getLoggedUser();
        Session.setUser(user);
    }

    /**
     * Handler that starts or edits a given survey
     */
    public void onSurveySelected(Survey survey) {
        dashboardController.onSurveySelected(survey);
    }

    /**
     * Handler that starts or edits a given survey
     */
    public void onOrgUnitSelected(OrgUnitDB orgUnit) {
        dashboardController.onOrgUnitSelected(orgUnit);
    }

    /**
     * Handler that starts or edits a given survey
     */
    public void onProgramSelected(ProgramDB program) {
        dashboardController.onProgramSelected(program);
    }

    /**
     * Handler that marks the given sucloseFeedbackFragmentrvey as completed.
     * This includes a pair or corner cases
     */
    public void onMarkAsCompleted(Survey survey) {
        dashboardController.onMarkAsCompleted(survey);
    }

    /**
     * Handler that enter into the feedback for the given survey
     */
    public void onFeedbackSelected(Survey survey) {
        dashboardController.onFeedbackSelected(survey);
    }

    /**
     * Moving into createSurvey fragment
     */
    public void onNewSurvey(View view) {
        dashboardController.onNewSurvey();
    }

    /**
     * Create new survey from CreateSurveyFragment
     */
    public void onCreateSurvey(final OrgUnitDB orgUnit, final ProgramDB program) {
        createNewSurvey(orgUnit, program);
    }

    /**
     * Create new survey from VariantSpecificUtils
     */
    public void createNewSurvey(OrgUnitDB orgUnit, ProgramDB program) {
        Survey survey = SurveyPlanner.getInstance().startSurvey(orgUnit.getId_org_unit(), program.getId_program());
        prepareLocationListener(survey.getId());
        Session.setSurveyByModule(survey, Constants.FRAGMENT_SURVEY_KEY);
        dashboardController.onSurveySelected(survey);
    }

    /**
     * Shows a quick toast message on screen
     */

    public static void toast(String message) {
        Toast.makeText(DashboardActivity.dashboardActivity, message, Toast.LENGTH_LONG).show();
    }

    public static void toastFromTask(final String message) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        toast(message);
                    }
                });
            }
        }, 1000);
    }

    //Show dialog exception from class without activity.
    public static void showException(final String title, final String errorMessage) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Run your task here
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String dialogTitle = "", dialogMessage = "";
                        if (title != null) {
                            dialogTitle = title;
                        }
                        if (errorMessage != null) {
                            dialogMessage = errorMessage;
                        }
                        new AlertDialog.Builder(dashboardActivity)
                                .setCancelable(false)
                                .setTitle(dialogTitle)
                                .setMessage(dialogMessage)
                                .setNeutralButton(android.R.string.ok, null)
                                .create().show();
                    }
                });
            }
        }, 1000);
    }

    public void preparePlanningFilters(List<ProgramDB> programList, List<OrgUnitDB> orgUnitList) {
        ((PlanModuleController) dashboardController.getModuleByName(
                PlanModuleController.getSimpleName())).prepareFilters(programList, orgUnitList);
    }

    @Override
    public void clickOrgUnitSpinner(View v) {
        PlanModuleController planModuleController =
                (PlanModuleController) dashboardController.getModuleByName(
                        PlanModuleController.getSimpleName());
        planModuleController.clickOrgUnitSpinner();
    }

    @Override
    public void clickProgramSpinner(View v) {
        PlanModuleController planModuleController =
                (PlanModuleController) dashboardController.getModuleByName(
                        PlanModuleController.getSimpleName());
        planModuleController.clickOrgProgramSpinner();
    }


    public class AsyncAnnouncement extends AsyncTask<Void, Void, Void> {
        UserDB loggedUser;

        @Override
        protected Void doInBackground(Void... params) {
            PullClient pullClient = new PullClient(PreferencesState.getInstance().getContext());
            loggedUser = UserDB.getLoggedUser();
            /* Ignoring the update date
            boolean isUpdated = pullClient.isUserUpdated(loggedUser);
            if (isUpdated) {
                pullClient.pullUserAttributes(loggedUser);
            }*/
            loggedUser = pullClient.pullUserAttributes(loggedUser);
            loggedUser.save();//save the lastUpdated info and attributes
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (loggedUser.getAnnouncement() != null && !loggedUser.getAnnouncement().equals("")
                    && !PreferencesState.getInstance().isUserAccept()) {
                Log.d(TAG, "show logged announcement");
                AUtils.showAnnouncement(R.string.admin_announcement, loggedUser.getAnnouncement(),
                        DashboardActivity.this);
                //show model dialog
            } else {
                AUtils.checkUserClosed(loggedUser, DashboardActivity.this);
            }
        }
    }
}

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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.LocalPullController;
import org.eyeseetea.malariacare.data.database.utils.ExportData;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.listeners.SurveyLocationListener;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;

import java.io.IOException;
import java.util.List;

public abstract class BaseActivity extends ActionBarActivity {
    /**
     * Extra param to annotate the activity to return after settings
     */
    public static final String SETTINGS_CALLER_ACTIVITY = "SETTINGS_CALLER_ACTIVITY";
    private static final int DUMP_REQUEST_CODE = 0;
    protected static String TAG = ".BaseActivity";
    private SurveyLocationListener locationListener;

    LogoutUseCase mLogoutUseCase;
    IUserAccountRepository mUserAccountRepository;
    private AlarmPushReceiver alarmPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesState.getInstance().initalizateActivityDependencies();
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);
        initView(savedInstanceState);

        mUserAccountRepository = new UserAccountRepository(this);
        mLogoutUseCase = new LogoutUseCase(mUserAccountRepository);
        checkQuarantineSurveys();
        alarmPush = new AlarmPushReceiver();
        alarmPush.setPushAlarm(this);
    }

    private void checkQuarantineSurveys() {
        PreferencesState.getInstance().setPushInProgress(false);
        List<SurveyDB> surveys = SurveyDB.getAllSendingSurveys();
        Log.d(TAG + "B&D", "Pending surveys sending: "
                + surveys.size());
        for (SurveyDB survey : surveys) {
            survey.setStatus(Constants.SURVEY_QUARANTINE);
            survey.save();
        }
    }

    /**
     * Common styling
     */
    private void initView(Bundle savedInstanceState) {
        setTheme(R.style.EyeSeeTheme);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);

        if (savedInstanceState == null) {
            initTransition();
        }
    }

    /**
     * Customize transitions for these activities
     */
    protected void initTransition() {
        this.overridePendingTransition(R.transition.anim_slide_in_left,
                R.transition.anim_slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                debugMessage("User asked for settings");
                goSettings();
                break;
            case R.id.action_license:
                debugMessage("User asked for license");
                AUtils.showAlertWithMessage(R.string.settings_menu_licence, R.raw.gpl,
                        BaseActivity.this);
                break;
            case R.id.action_about:
                debugMessage("User asked for about");
                AUtils.showAlertWithHtmlMessageAndLastCommit(R.string.settings_menu_about,
                        R.raw.about, BaseActivity.this);
                break;
            case R.id.action_copyright:
                debugMessage("User asked for copyright");
                AUtils.showAlertWithMessage(R.string.settings_menu_copyright, R.raw.copyright,
                        BaseActivity.this);
                break;
            case R.id.action_licenses:
                debugMessage("User asked for software licenses");
                AUtils.showAlertWithHtmlMessage(R.string.settings_menu_licenses, R.raw.licenses,
                        BaseActivity.this);
                break;
            case R.id.action_eula:
                debugMessage("User asked for EULA");
                AUtils.showAlertWithHtmlMessage(R.string.settings_menu_eula, R.raw.eula,
                        BaseActivity.this);
                break;
            case R.id.action_logout:
                debugMessage("User asked for logout");
                logout();
                break;
            case android.R.id.home:
                debugMessage("Go back");
                onBackPressed();
                break;
            case R.id.downloaded_media:
                debugMessage("Go downloaded media");
                startActivity(new Intent(this, DownloadedMediaActivity.class));
                break;
            case R.id.export_db:
                debugMessage("Export db");
                Intent emailIntent = ExportData.dumpAndSendToAIntent(this);
                if (emailIntent != null) {
                    startActivityForResult(emailIntent, DUMP_REQUEST_CODE);
                }
                break;
            case R.id.import_db:
                debugMessage("Import db");
                showFileChooser();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/octet-stream");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent,
                            PreferencesState.getInstance().getContext().getString(
                                    R.string.get_db_file_dialog)),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, PreferencesState.getInstance().getContext().getString(
                    R.string.install_file_manager),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    LocalPullController localPullController = new LocalPullController(
                            getApplicationContext());
                    try {
                        localPullController.importDB(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!PreferencesState.getInstance().isDevelopOptionActive()
                || !AppSettingsBuilder.isDeveloperOptionsActive()) {
            MenuItem item = menu.findItem(R.id.export_db);
            item.setVisible(false);
            item = menu.findItem(R.id.import_db);
            item.setVisible(false);
        }
        return true;
    }

    /**
     * Every BaseActivity(Details, Create, Survey) goes back to DashBoard
     */
    public void onBackPressed() {
        finishAndGo(DashboardActivity.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent;
        intent = (getCallingActivity() != null) ? new Intent(getCallingActivity().getClassName())
                : getIntent();

        if (intent.getStringExtra("activity") != null && getCallingActivity() != null
                && intent.getStringExtra("activity").equals("settings")) {
            Log.i(".onResume", "coming from settings");
            overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();

            overridePendingTransition(0, 0);
            startActivity(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void goSettings() {
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        intentSettings.putExtra(SETTINGS_CALLER_ACTIVITY, this.getClass());
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Closes current session and goes back to loginactivity
     */
    public void logout() {
        int unsentSurveyCount = SurveyDB.countAllUnsentUnplannedSurveys();
        String message = getApplicationContext().getString(
                R.string.dialog_action_logout);
        if (unsentSurveyCount == 0) {
            message += getApplicationContext().getString(
                    R.string.dialog_all_surveys_sent_before_refresh);
        } else {
            message += String.format(getApplicationContext().getString(
                    R.string.dialog_incomplete_surveys_before_refresh), unsentSurveyCount);
        }

        new AlertDialog.Builder(this)
                .setTitle(getApplicationContext().getString(R.string.settings_menu_logout))
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Start logout
                        debugMessage("Logging out from sdk...");
                        executeLogout();
                    }
                })
                .setNegativeButton(android.R.string.no, null).create().show();
    }

    public void executeLogout() {
        mLogoutUseCase.execute(new LogoutUseCase.Callback() {
            @Override
            public void onLogoutSuccess() {
                finishAndGo(LoginActivity.class);
            }

            @Override
            public void onLogoutError(String message) {
                Log.e("." + this.getClass().getSimpleName(), message);
            }
        });
    }

    public void clickOrgUnitSpinner(View view) {
    }

    public void clickProgramSpinner(View view) {
    }

    /**
     * Asks for location (required while starting to edit a survey)
     */
    public void prepareLocationListener(Long surveyId) {

        locationListener = new SurveyLocationListener(surveyId);
        LocationManager locationManager =
                (LocationManager) LocationMemory.getContext().getSystemService(
                        Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            debugMessage("requestLocationUpdates via GPS");
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        locationListener);
            }
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            debugMessage("requestLocationUpdates via NETWORK");
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_NETWORK_STATE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        locationListener);
            }
        } else {
            Location lastLocation = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            debugMessage("location not available via GPS|NETWORK, last know: " + lastLocation);
            locationListener.saveLocation(lastLocation);
        }
    }

    /**
     * Finish current activity and launches an activity with the given class
     *
     * @param targetActivityClass Given target activity class
     */
    public void finishAndGo(Class targetActivityClass) {
        Intent targetActivityIntent = new Intent(this, targetActivityClass);
        finish();
        startActivity(targetActivityIntent);
    }

    /**
     * Launches an activity with the given class
     *
     * @param targetActivityClass Given target activity class
     */
    public void go(Class targetActivityClass) {
        Intent targetActivityIntent = new Intent(this, targetActivityClass);
        startActivity(targetActivityIntent);
    }

    /**
     * Logs a debug message using current activity SimpleName as tag. Ex:
     * SurveyActivity => ".SurveyActivity"
     */
    private void debugMessage(String message) {
        Log.d("." + this.getClass().getSimpleName(), message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        alarmPush.cancelPushAlarm(this);
    }
}

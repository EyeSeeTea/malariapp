package org.eyeseetea.malariacare.drive;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.drive.DriveScopes;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by arrizabalaga on 28/05/16.
 */
public class DriveRestController {


    private static final String TAG = "DriveRestController";
    private static DriveRestController instance;

    private GoogleCredential serviceCredential;
    private static final String[] SCOPES = {DriveScopes.DRIVE};

    static final int REQUEST_AUTHORIZATION = 101;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 102;

    private DashboardActivity dashboardActivity;

    DriveRestController() {

    }

    public static DriveRestController getInstance() {
        if (instance == null) {
            instance = new DriveRestController();
        }
        return instance;
    }

    public void init(DashboardActivity dashboardActivity) {
        this.dashboardActivity = dashboardActivity;
        Log.d(TAG, "Init drive credential");
        initServiceAccountCredential();
    }

    private void initServiceAccountCredential() {

        try {
            InputStream privateJsonStream = dashboardActivity.getResources().openRawResource(R.raw.driveserviceprivatekey);
            serviceCredential = GoogleCredential.fromStream(privateJsonStream).createScoped(Arrays.asList(SCOPES));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void syncMedia() {

        if (!isDeviceOnline()) {
            Log.w(TAG, "No wifi connection available. Media will not be synced");
            return;
        }

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
            return;
        }

        new DownloadMediaTask(serviceCredential).execute();
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    DashboardActivity.toast(dashboardActivity.getString(R.string.google_play_required));
                } else {
                    syncMedia();
                }
                break;
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) DashboardActivity.dashboardActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(DashboardActivity.dashboardActivity);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(dashboardActivity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                DashboardActivity.dashboardActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}

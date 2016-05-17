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

package org.eyeseetea.malariacare.media;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;

import org.eyeseetea.malariacare.DashboardActivity;

/**
 * Ease drive API integration
 * Created by arrizabalaga on 16/05/16.
 */
public class DriveApiController  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG=".DriveApiController";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    public static final int REQUEST_CODE_RESOLUTION = 1;

    private static DriveApiController instance;

    private GoogleApiClient googleApiClient;
    private DashboardActivity dashboardActivity;

    public DriveApiController(){}

    public static DriveApiController getInstance(){
        if(instance==null){
            instance = new DriveApiController();
        }
        return instance;
    }

    /**
     * Connects from the Google Drive API
     * @param dashboardActivity
     */
    public void connect(DashboardActivity dashboardActivity){
        this.dashboardActivity=dashboardActivity;

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(dashboardActivity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect();
    }

    /**
     * Disconnects from the Google Drive API
     */
    public void disconnect(){
        if(googleApiClient==null){
            return;
        }

        googleApiClient.disconnect();
    }


    /**
     * Checks if the params from onActivityResult matches
     * a Google Drive connect error that has been resolved.
     * @param requestCode
     * @param resultCode
     * @return
     */
    public static boolean isConnectResolutionOK(int requestCode, int resultCode){
        return requestCode==REQUEST_CODE_RESOLUTION && resultCode== Activity.RESULT_OK;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");
        dashboardActivity.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(connectionResult==null){
            return;
        }

        Log.i(TAG, "onConnectionFailed ->"+connectionResult.toString());
        //No potential resolution -> show error
        if (!connectionResult.hasResolution()) {
            GoogleApiAvailability.getInstance().getErrorDialog(dashboardActivity, connectionResult.getErrorCode(), 0).show();
            return;
        }

        //Potential resolution -> init intent
        initConnectResolution(connectionResult);
    }

    /**
     * Returns if the client is connected or not
     * @return
     */
    public boolean isConnected(){
        return this.googleApiClient!=null && this.googleApiClient.isConnected();
    }

    public PendingResult<DriveApi.DriveIdResult> fetchDriveId(String fileID){
        if(fileID==null || fileID.isEmpty()){
            return null;
        }
        return Drive.DriveApi.fetchDriveId(this.googleApiClient, fileID);
    }

    public PendingResult<DriveResource.MetadataResult> getMetadata(DriveFile driveFile){
        if(driveFile==null){
            return null;
        }
        return driveFile.getMetadata(this.googleApiClient);
    }

    public PendingResult<DriveResource.MetadataResult> updateMetadata(DriveFile driveFile){
        if(driveFile==null){
            return null;
        }

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setPinned(true).build();
        return driveFile.updateMetadata(this.googleApiClient, changeSet);
    }


    private void initConnectResolution(ConnectionResult connectionResult) {
        try {
            connectionResult.startResolutionForResult(dashboardActivity, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "initConnectResolution ->"+e.getMessage());
            e.printStackTrace();
        }
    }

}

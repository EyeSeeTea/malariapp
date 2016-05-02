/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  QA App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QA App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QA App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.media;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

public class DriveUtils {

    /**
     * Tag to show on log messages
     */
    public static final String TAG = ".DriveUtils";

    /**
     * Singleton object
     */
    private static DriveUtils driveUtils = null;

    /**
     * Api client
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Context, needed essentially for the Toast messages
     */
    private Activity activity;

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Next available request code.
     */
    protected static final int NEXT_AVAILABLE_REQUEST_CODE = 2;

    /**
     * Drive id in string format (as you get it copying from the drive URL)
     */
    private String fileId;

    /**
     * DriveId object
     */
    private DriveId mFileId;

    /**
     * Metadata Drive object
     */
    private Metadata metadata;

    private static final boolean INTRUSIVE_MESSAGES = false;

    /**
     * Handles the metadata response. If file is pinnable and not
     * already pinned, makes a request to pin the file.
     */
    final ResultCallback<DriveResource.MetadataResult> metadataCallback = new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(DriveResource.MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Problem while trying to retrieve the file metadata");
                return;
            }
            if (result.getMetadata().isPinnable()) {
                showMessage("File is not pinnable");
                return;
            }
            if (result.getMetadata().isPinned()) {
                showMessage("File is already pinned");
                return;
            }
            metadata = result.getMetadata();
            showMessage("Metadata successfully fetched. Title: " + metadata.getTitle());
            DriveFile file = mFileId.asDriveFile();
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setPinned(true)
                    .build();
            file.updateMetadata(getGoogleApiClient(), changeSet)
                    .setResultCallback(pinningCallback);
        }
    };

    final private ResultCallback<DriveApi.DriveIdResult> idCallback = new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            mFileId = result.getDriveId();
            DriveFile file = mFileId.asDriveFile();
            file.getMetadata(getGoogleApiClient())
                    .setResultCallback(metadataCallback);
        }
    };

    /**
     * Handles the pinning request's response.
     */
    final ResultCallback<DriveResource.MetadataResult> pinningCallback = new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(DriveResource.MetadataResult result) {
            if (!result.getStatus().isSuccess()) {
                showMessage("Problem while trying to pin the file");
                return;
            }
            showMessage("File successfully pinned to the device");
        }
    };

    /**
     * Singleton pattern
     * @param activity
     * @param fileId
     * @return
     */
    public static DriveUtils getInstance(Activity activity, String fileId){
        driveUtils = getInstance(activity);
        driveUtils.setFileId(fileId);
        driveUtils.setDriveId();
        return driveUtils;
    }

    /**
     * Singleton pattern
     * @param activity
     * @return
     */
    public static DriveUtils getInstance(Activity activity){
        if (driveUtils == null){
            driveUtils = new DriveUtils(activity);
        }
        return driveUtils;
    }

    /**
     * Set the file ID
     * @param fileId
     */
    public void setFileId(String fileId){
        this.fileId = fileId;
    }

    /**
     * Retrieve the DriveId object
     */
    public void setDriveId(){
        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), fileId)
                .setResultCallback(idCallback);
    }

    /**
     * DriveUtils must be built with a Context to show the messages on their context
     * @param activity
     */
    public DriveUtils(Activity activity){
        this.activity = activity;
    }

    /**
     * Getter for the {@code GoogleApiClient}.
     */
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    /**
     * Setter for the {@code GoogleApiClient}.
     */
    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    /**
     * Overloads showMessage function to put messages
     * @param message
     * @param popup
     */
    public void showMessage(String message, boolean popup){
        if (popup) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, message);
        }
    }

    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        showMessage(message, INTRUSIVE_MESSAGES);
    }
}

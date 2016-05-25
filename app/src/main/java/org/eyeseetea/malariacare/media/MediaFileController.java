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

import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.database.model.Media;

/**
 * Created by arrizabalaga on 16/05/16.
 */
public class MediaFileController{

    private static final String TAG=".MediaFileController";

    /**
     * The media that will be synced
     */
    private Media media;

    /**
     * DriveId object
     */
    private DriveId driveId;

    /**
     * DriveFile object
     */
    private DriveFile driveFile;

    /**
     * File metadata
     */
    private Metadata metadata;

    /**
     * Find file callback (1)
     */
    private ResultCallback<DriveApi.DriveIdResult> foundFileCallback;

    /**
     * File metadata received (2)
     */
    private ResultCallback<DriveResource.MetadataResult> metadataCallback;

    /**
     * File synced callback (3)
     */
    private ResultCallback<DriveResource.MetadataResult> pinningCallback;

    MediaFileController(Media media){
        this.media=media;
        initCallbacks();
    }

    /**
     * Sync the drive file into local
     */

    public void sync(){
        if(media==null){
            return;
        }

        Log.d(TAG, String.format("Syncing %s ...",media.toString()));
        //No connected -> error
        if(!DriveApiController.getInstance().isConnected()){
            Log.e(TAG, "->Not connected to drive");
            return;
        }

        //Look for file in drive
        PendingResult<DriveApi.DriveIdResult> driveIdResultPendingResult = DriveApiController.getInstance().fetchDriveId(media.getResourceUrl());
        if(driveIdResultPendingResult==null){
            return;
        }
        //Sets callback once file has been found
        driveIdResultPendingResult.setResultCallback(foundFileCallback);
    }

    private void initCallbacks(){

        //File found callback -> Looks for metadata
        this.foundFileCallback =  new ResultCallback<DriveApi.DriveIdResult>() {
            @Override
            public void onResult(DriveApi.DriveIdResult result) {
                Log.i(TAG, String.format("fetchDriveId(%s) ->",media.getResourceUrl()));

                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, String.format("\t-> %s",media.getResourceUrl(), result.getStatus().getStatusMessage()));
                    DashboardActivity.toast(String.format("File %s not found", media.getResourceUrl()));
                    return;
                }
                driveId = result.getDriveId();
                driveFile = driveId.asDriveFile();

                PendingResult<DriveResource.MetadataResult> metadataResultPendingResult = DriveApiController.getInstance().getMetadata(driveFile);
                if (metadataResultPendingResult == null) {
                    return;
                }

                metadataResultPendingResult.setResultCallback(metadataCallback);
            }
        };

        //Metadata found callback -> Starting sync
        this.metadataCallback = new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult result) {
                Log.i(TAG, String.format("getMetadata(%s) ->",media.getResourceUrl()));

                if (!result.getStatus().isSuccess()) {
                    DashboardActivity.toast(String.format("Metadata not found for %s:\n%s", media.getResourceUrl(),result.getStatus().getStatusMessage()));
                    return;
                }
                if (result.getMetadata().isPinnable()) {
                    DashboardActivity.toast(String.format("According to metadata, %s cannot be synced due to its type", media.getResourceUrl()));
                    return;
                }
                if (result.getMetadata().isPinned()) {
                    DashboardActivity.toast(String.format("File %s is already synced", media.getResourceUrl()));
                    return;
                }

                metadata = result.getMetadata();
                Log.i(TAG, String.format("\t-> Metadata for %s found. Title: %s",media.getResourceUrl(),metadata.getTitle()));

                PendingResult<DriveResource.MetadataResult> metadataResultPendingResult = DriveApiController.getInstance().updateMetadata(driveFile);
                if (metadataResultPendingResult == null) {
                    return;
                }
                metadataResultPendingResult.setResultCallback(pinningCallback);
            }
        };

        //Pinned finish callback -> Do whatever
        this.pinningCallback = new ResultCallback<DriveResource.MetadataResult>() {
            @Override
            public void onResult(DriveResource.MetadataResult result) {
                //TODO
                Log.i(TAG, String.format("updateMetadata(%s) -> %b",media.getResourceUrl(), result.getStatus().isSuccess()));
                if (!result.getStatus().isSuccess()) {
                    return;
                }
                MediaController.getInstance().done(media);
            }
        };

    }


}

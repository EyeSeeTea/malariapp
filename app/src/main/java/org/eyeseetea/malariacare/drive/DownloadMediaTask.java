package org.eyeseetea.malariacare.drive;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.database.model.Media;

import java.io.FileOutputStream;
import java.util.List;

/**
 * AsyncTask that downloads media files
 * Created by arrizabalaga on 28/05/16.
 */
class DownloadMediaTask extends AsyncTask<Void, Void, Integer> {
    private final String TAG = "DownloadMediaTask";
    private com.google.api.services.drive.Drive mService = null;
    private Exception mLastError = null;


    public DownloadMediaTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        this.mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Drive API Android Quickstart")
                .build();
    }

    @Override
    protected Integer doInBackground(Void... params) {

        //Check elements to download
        List<Media> mediaList = Media.getAllNotInLocal();

        //Nothing to sync -> 0
        if(mediaList==null || mediaList.isEmpty()){
            return 0;
        }

        //Try to download every non local media
        int numSyncedFiles = 0;
        for (Media media : mediaList) {
            Exception ex=sync(media);
            //No exception -> inc & next
            if (ex==null) {
                numSyncedFiles++;
            }else if (ex instanceof UserRecoverableAuthIOException){
                //First time auth confirm from user lets stop and try again after confirming
                break;
            }

            //Any other exception -> Non existent resource, just move forward
        }
        return numSyncedFiles;
    }

    private Exception sync(Media media) {
        try {
            //download file
            String absolutePath=downloadFile(media.getResourceUrl());
            //save local path to use later
            media.setFilename(absolutePath);
            media.save();
            Log.d(TAG,"\tsaved in "+absolutePath);
            return null;
        }catch (Exception ex) {
            mLastError = ex;
            Log.e(TAG, "downloadFile error: " + ex.getMessage());
            cancel(true);
            return ex;
        }
    }

    @Override
    protected void onPostExecute(Integer numSyncedFiles) {
        DashboardActivity.toast(String.format("%d files synced", numSyncedFiles));
    }

    private String downloadFile(String resourceId) throws Exception {
        Log.d(TAG, String.format("Downloading resource: %s ...", resourceId));

        Drive.Files.Get getFile = mService.files().get(resourceId);
        File fileDrive = getFile.execute();
        Log.d(TAG, "\tfilename: " + fileDrive.getName());

        java.io.File localFile = new java.io.File(DashboardActivity.dashboardActivity.getFilesDir(), fileDrive.getName());
        FileOutputStream fileOutputStream = new FileOutputStream(localFile);
        getFile.executeMediaAndDownloadTo(fileOutputStream);
        fileOutputStream.close();
        return localFile.getAbsolutePath();
    }

    @Override
    protected void onCancelled() {

        //Need to complete credentials (ack from user first time)
        if (mLastError instanceof UserRecoverableAuthIOException) {
            DashboardActivity.dashboardActivity.startActivityForResult(
                    ((UserRecoverableAuthIOException) mLastError).getIntent(),
                    DriveRestController.REQUEST_AUTHORIZATION);
            return;
        }

        //Real connection google error
        if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
            DriveRestController.getInstance().showGooglePlayServicesAvailabilityErrorDialog(
                    ((GooglePlayServicesAvailabilityIOException) mLastError)
                            .getConnectionStatusCode());
            return;
        }

        //Other error
        Log.e(TAG, "onCancelled: " + mLastError == null ? "" : mLastError.getMessage());
    }
}

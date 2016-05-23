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

import android.os.AsyncTask;
import android.util.Log;

import org.eyeseetea.malariacare.database.model.Media;

import java.io.IOException;

/**
 * A plain async task that downloads a drive media resource into the device
 * Created by arrizabalaga on 17/05/16.
 */
public class DownloadMediaTask extends AsyncTask<Void, Void, String>{

    private final static String TAG="DownloadMediaTask";

    private Media media;

    public DownloadMediaTask(Media media){
        this.media=media;
    }
    @Override
    protected String doInBackground(Void... params) {
        if(media==null){
            return null;
        }

        String filename=null;
        try {
            Log.i(TAG,String.format("Downloading %s ...",media.getResourceUrl()));
            filename = HttpDownloadUtility.downloadFile(media.getResourceUrl());
        }catch (IOException exception){
            Log.e(TAG,String.format("Error downloading %s: %s",media.getResourceUrl(),exception.getMessage()));
        }
        return filename;
    }

    @Override
    protected void onPostExecute(String filename){
        if(filename==null){
            return;
        }

        //Save file locally
        Log.i(TAG,String.format("Downloading %s -> %s .DONE",media.getResourceUrl(), filename));
        media.setFilename(filename);
        media.save();
    }
}

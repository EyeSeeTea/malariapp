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

import org.eyeseetea.malariacare.database.model.Media;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arrizabalaga on 16/05/16.
 */
public class MediaController {

    private static final String TAG=".MediaController";

    private Map<String,MediaFileController> mediaSyncing;

    private static MediaController instance;

    MediaController(){
        mediaSyncing = new HashMap<>();
    }

    public static MediaController getInstance(){
        if(instance==null){
            instance = new MediaController();
        }
        return instance;
    }

    /**
     * Starts syncing all media resources
     */
    public void syncAll(){
        for(Media media: Media.getAllNotInLocal()) {
            sync(media);
        }
    }

    /**
     * Starts syncing 1 media file
     * @param media
     */
    public void sync(Media media){
        if(media==null || media.getResourceUrl()==null || media.getResourceUrl().isEmpty()){
            return;
        }
        Log.d(TAG, "Syncing file %s" + media.getResourceUrl());
//        new DownloadMediaTask(media).execute();
        MediaFileController mediaFileController = new MediaFileController(media);
        mediaSyncing.put(media.getResourceUrl(),mediaFileController);
        mediaFileController.sync();
    }

    /**
     * Notifies the media has been synced
     * @param media
     */
    public void done(Media media){
        if(media==null){
            return;
        }
        Log.d(TAG, "Syncing file %s DONE" + media.getResourceUrl());
        mediaSyncing.remove(media.getResourceUrl());
    }


}

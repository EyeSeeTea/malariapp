/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.data.database.local;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.raizlabs.android.dbflow.config.DHIS2GeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.EyeSeeTeaGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.data.IPullSourceCallback;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.utils.FileIOUtils;
import org.eyeseetea.malariacare.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class PullLocalSDKDataSource {
    final String TAG = "PullLocalSDKDataSource";

    final String DATABASE_FOLDER = "database/";
    final String DATABASE_FILE = "EyeSeeTeaDB.db";

    public void pull(IPullSourceCallback callback, Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        Log.d(TAG, "pull from local source");
        try {
            inputStream = assetManager.open(DATABASE_FOLDER + DATABASE_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Copy Database error");
        }
        try {
            if (inputStream != null) {
                Log.d(TAG, "Copy Database from assets started");
                FlowManager.destroy();
                copyDBFromAssets(inputStream);
                reinitializeDbFlowDatabases(context);
                callback.onComplete();
                Log.d(TAG, "Copy Database from assets finished");
            } else {
                Log.d(TAG, "Populate from csv start");
                FlowConfig flowConfig = new FlowConfig
                        .Builder(context)
                        .addDatabaseHolder(EyeSeeTeaGeneratedDatabaseHolder.class)
                        .build();
                FlowManager.init(flowConfig);
                populateFromDB(context);
                Log.d(TAG, "Populate from csv finished");
                callback.onComplete();
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    public void copyDBFromAssets(InputStream inputStream)
            throws IOException {
        FileIOUtils.copyInputStreamToFile(inputStream, FileIOUtils.getAppDatabaseFile());
    }

    public void populateFromDB(Context context) throws IOException {
        PopulateDB.populateDB(context.getAssets());
    }

    public void reinitializeDbFlowDatabases(Context context){
        FlowConfig flowConfig = new FlowConfig
                .Builder(context)
                .addDatabaseHolder(EyeSeeTeaGeneratedDatabaseHolder.class)
                .build();
        FlowManager.init(flowConfig);
        FlowConfig flowConfigDhis = new FlowConfig
                .Builder(context)
                .addDatabaseHolder(DHIS2GeneratedDatabaseHolder.class)
                .build();
        FlowManager.init(flowConfigDhis);
    }
}

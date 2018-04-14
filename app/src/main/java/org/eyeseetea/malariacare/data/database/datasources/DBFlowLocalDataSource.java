package org.eyeseetea.malariacare.data.database.datasources;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.raizlabs.android.dbflow.config.DHIS2GeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.EyeSeeTeaGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.sdk.common.DatabaseUtils;
import org.eyeseetea.sdk.common.FileUtils;

import java.io.IOException;
import java.io.InputStream;

public class DBFlowLocalDataSource {
    String TAG = "DBFlowLocalDataSource";
    Context context;
    public DBFlowLocalDataSource(Context context){
        this.context = context;
    }

    public void importDB(Uri uri) throws IOException {
        Log.d(TAG, "Import Database from user file started");
        FlowManager.destroy();
        copyDBFromFile(context.getContentResolver().openInputStream(uri));
        reinitializeDbFlowDatabases(context);
        Log.d(TAG, "Import Database from user file finished");
    }


    public void copyDBFromFile(InputStream inputStream)
            throws IOException {
        FileUtils.copyInputStreamToFile(inputStream, DatabaseUtils.getAppDatabaseFile(AppDatabase.NAME, context.getPackageName()));
    }
    /**
     * This method reinitialize the DBFlow configurations to make DBFlow work in the appDB and in
     * the SDK.
     */
    public void reinitializeDbFlowDatabases(Context context) {
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

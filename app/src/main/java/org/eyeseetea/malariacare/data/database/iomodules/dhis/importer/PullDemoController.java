package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.raizlabs.android.dbflow.config.DHIS2GeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.EyeSeeTeaGeneratedDatabaseHolder;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.datasources.ConversionLocalDataSource;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.domain.boundary.IPullDemoController;
import org.eyeseetea.sdk.common.DatabaseUtils;
import org.eyeseetea.sdk.common.FileUtils;

import java.io.IOException;
import java.io.InputStream;

public class PullDemoController implements IPullDemoController {
    final String TAG = "PullLocalDataSource";

    final String DATABASE_FOLDER = "database/";
    final String DATABASE_FILE = "EyeSeeTeaDB.db";
    Context context;

    public PullDemoController(Context context) {
        this.context = context;
    }

    @Override
    public void pull(IPullDemoControllerCallback callback) {
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
                pullFromDB(context, inputStream);
            } else {
                pullFromCsv(context);
            }
            callback.onComplete();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    private void pullFromCsv(Context context) throws IOException {
        ConversionLocalDataSource.wipeDataBase();
        deleteSQLiteMetadata();
        Log.d(TAG, "Populate from csv start");
        populateFromDB(context);
        Log.d(TAG, "Populate from csv finished");
    }

    private void pullFromDB(Context context,
            InputStream inputStream) throws IOException {
        Log.d(TAG, "Copy Database from assets started");
        FlowManager.destroy();
        copyDBFromFile(inputStream);
        reinitializeDbFlowDatabases(context);
        Log.d(TAG, "Copy Database from assets finished");
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

    public void populateFromDB(Context context) throws IOException {
        PopulateDB.populateDB(context.getAssets());
    }

    /**
     * This method removes the sqlite_sequence table that contains the last autoincrement value for
     * each table
     */
    private void deleteSQLiteMetadata() {
        String sqlCopy = "Delete from sqlite_sequence";
        DatabaseDefinition databaseDefinition =
                FlowManager.getDatabase(AppDatabase.class);
        databaseDefinition.getWritableDatabase().execSQL(sqlCopy);

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

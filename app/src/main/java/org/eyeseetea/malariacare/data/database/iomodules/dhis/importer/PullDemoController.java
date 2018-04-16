package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.datasources.DatabaseImporter;
import org.eyeseetea.malariacare.data.database.utils.PopulateDB;
import org.eyeseetea.malariacare.domain.boundary.IPullDemoController;

import java.io.IOException;
import java.io.InputStream;

public class PullDemoController implements IPullDemoController {
    final String TAG = "PullDemoController";

    final String DATABASE_FOLDER = "database/";
    final String DATABASE_FILE = "EyeSeeTeaDB.db";
    DatabaseImporter mDatabaseImporter;
    Context context;

    public PullDemoController(Context context) {
        this.context = context;
        mDatabaseImporter = new DatabaseImporter(context);
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
                mDatabaseImporter.importDB(context, inputStream);
            } else {
                pullFromCsv(context);
            }
            callback.onComplete();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    public void pullFromCsv(Context context) throws IOException {
        AppDatabase.wipeDataBase();
        deleteSQLiteMetadata();
        Log.d(TAG, "Populate from csv start");
        populateFromCSV(context);
        Log.d(TAG, "Populate from csv finished");
    }

    public void populateFromCSV(Context context) throws IOException {
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
}

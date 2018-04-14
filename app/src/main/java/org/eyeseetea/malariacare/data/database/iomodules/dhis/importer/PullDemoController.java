package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import org.eyeseetea.malariacare.data.database.datasources.DBFlowLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPullDemoController;

import java.io.IOException;
import java.io.InputStream;

public class PullDemoController implements IPullDemoController {
    final String TAG = "PullDemoController";

    final String DATABASE_FOLDER = "database/";
    final String DATABASE_FILE = "EyeSeeTeaDB.db";
    DBFlowLocalDataSource dbFlowLocalDataSource;
    Context context;

    public PullDemoController(Context context) {
        this.context = context;
        dbFlowLocalDataSource = new DBFlowLocalDataSource(context);
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
                dbFlowLocalDataSource.pullFromDB(context, inputStream);
            } else {
                dbFlowLocalDataSource.pullFromCsv(context);
            }
            callback.onComplete();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }
}

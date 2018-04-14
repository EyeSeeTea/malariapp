package org.eyeseetea.malariacare.data.database.iomodules.local.importer;

import android.content.Context;
import android.net.Uri;

import org.eyeseetea.malariacare.data.database.datasources.DBFlowLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.IImportController;

import java.io.IOException;

public class ImportController implements IImportController {
    DBFlowLocalDataSource dbFlowLocalDataSource;

    public ImportController(Context context) {
        dbFlowLocalDataSource = new DBFlowLocalDataSource(context);
    }

    @Override
    public void importDB(Uri uri, IImportControllerCallback callback) {
        try{
            dbFlowLocalDataSource.importDB(uri);
        }catch (IOException e){
            callback.onError(e);
            return;
        }
        callback.onComplete();
    }
}

package org.eyeseetea.malariacare.data.database.iomodules.local.importer;

import android.content.Context;
import android.net.Uri;

import org.eyeseetea.malariacare.data.database.datasources.DatabaseImporter;
import org.eyeseetea.malariacare.domain.boundary.IImportController;

import java.io.IOException;

public class ImportController implements IImportController {
    DatabaseImporter mDatabaseImporter;

    public ImportController(Context context) {
        mDatabaseImporter = new DatabaseImporter(context);
    }

    @Override
    public void importDB(Uri uri, IImportControllerCallback callback) {
        try{
            mDatabaseImporter.importDB(uri);
        }catch (IOException e){
            callback.onError(e);
            return;
        }
        callback.onComplete();
    }
}

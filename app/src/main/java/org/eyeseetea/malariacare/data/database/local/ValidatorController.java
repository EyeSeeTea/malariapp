package org.eyeseetea.malariacare.data.database.local;

import android.util.Log;

import org.eyeseetea.malariacare.data.database.datasources.DatabaseLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.IValidatorController;

public class ValidatorController implements IValidatorController {
    private static final String TAG = "ValidatorController";

    DatabaseLocalDataSource databaseLocalDataSource;

    public ValidatorController(){
        databaseLocalDataSource = new DatabaseLocalDataSource();
    }

    @Override
    public void validateTables(IValidatorControllerCallback callback) {
        boolean validate = databaseLocalDataSource.mandatoryMetadataTablesNotEmpty();
        callback.validate(validate);
    }

    @Override
    public void removeInvalidCS() {
        if(databaseLocalDataSource.validateCS()){
            Log.d(TAG, "Some composite scores are wrong");
        }
    }
}

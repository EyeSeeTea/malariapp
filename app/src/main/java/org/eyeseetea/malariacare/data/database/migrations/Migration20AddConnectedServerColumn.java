package org.eyeseetea.malariacare.data.database.migrations;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ServerDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.hisp.dhis.client.sdk.core.common.utils.CodeGenerator;

@Migration(version = 20, database = AppDatabase.class)
public class Migration20AddConnectedServerColumn extends BaseMigration {

    public Migration20AddConnectedServerColumn() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, ServerDB.class, "connected", "integer");

        // Set current and unique server in table (old version) to connected
        database.execSQL("update server set connected = 1");
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

}
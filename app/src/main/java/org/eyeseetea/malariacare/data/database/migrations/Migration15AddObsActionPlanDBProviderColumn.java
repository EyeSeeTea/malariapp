package org.eyeseetea.malariacare.data.database.migrations;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

@Migration(version = 15, database = AppDatabase.class)
public class Migration15AddObsActionPlanDBProviderColumn extends BaseMigration {

    public Migration15AddObsActionPlanDBProviderColumn() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, ObsActionPlanDB.class, "provider", "string");
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

}
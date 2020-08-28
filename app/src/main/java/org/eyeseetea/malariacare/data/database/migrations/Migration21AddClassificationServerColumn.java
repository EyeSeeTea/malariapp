package org.eyeseetea.malariacare.data.database.migrations;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ServerDB;

@Migration(version = 21, database = AppDatabase.class)
public class Migration21AddClassificationServerColumn extends BaseMigration {

    public Migration21AddClassificationServerColumn() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, ServerDB.class, "classification", "integer");

        // Set competencies by default
        database.execSQL("update server set classification = 1");
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

}
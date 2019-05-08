package org.eyeseetea.malariacare.data.database.migrations;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

@Migration(version = 17, database = AppDatabase.class)
public class Migration17AddNextScheduleDeltaMatrixProgramColumn extends BaseMigration {

    public Migration17AddNextScheduleDeltaMatrixProgramColumn() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, SurveyDB.class, "next_schedule_delta_matrix", "String");

        database.execSQL("update survey set next_schedule_delta_matrix = '" +
                ProgramDB.DEFAULT_PROGRAM_DELTA_MATRIX + "'");
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

}
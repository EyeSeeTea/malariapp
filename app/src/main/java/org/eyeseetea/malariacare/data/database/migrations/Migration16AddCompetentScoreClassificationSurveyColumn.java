package org.eyeseetea.malariacare.data.database.migrations;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

@Migration(version = 16, database = AppDatabase.class)
public class Migration16AddCompetentScoreClassificationSurveyColumn extends BaseMigration {

    public Migration16AddCompetentScoreClassificationSurveyColumn() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, SurveyDB.class, "competent_score_classification", "integer");

        database.execSQL("update survey set competent_score_classification = 0");
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

}
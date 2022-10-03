package org.eyeseetea.malariacare.data.database.migrations;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

import android.content.Context;
import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.hisp.dhis.client.sdk.core.common.utils.CodeGenerator;

import java.util.ArrayList;
import java.util.List;

@Migration(version = 19, database = AppDatabase.class)
public class Migration19AddCompetencyScoreClassificationSurveyColumn extends BaseMigration {

    public Migration19AddCompetencyScoreClassificationSurveyColumn() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumn(database, SurveyDB.class, "competency_score_classification", "integer");

        database.execSQL("update survey set competency_score_classification = 0");

        assignUIdToPlanSurveys(database);
    }

    private void assignUIdToPlanSurveys(DatabaseWrapper database) {
        Cursor planSurveysCursor = null;

        try {
            planSurveysCursor = database.rawQuery(
                    "Select id_survey from Survey where status = -1 and uid_event_fk is null", null);

            if (planSurveysCursor.moveToFirst()) {

                do {

                    long surveyId = planSurveysCursor.getLong(0);

                    String surveyUid = CodeGenerator.generateCode();

                    database.execSQL(
                            "UPDATE Survey set uid_event_fk = '" + surveyUid +
                                    "' where id_survey = " + surveyId);

                } while (planSurveysCursor.moveToNext());

            }
        } finally {
            if (planSurveysCursor != null) {
                planSurveysCursor.close();
            }
        }
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

}
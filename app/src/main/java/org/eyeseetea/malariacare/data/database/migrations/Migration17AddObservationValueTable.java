package org.eyeseetea.malariacare.data.database.migrations;

import android.content.Context;
import android.database.Cursor;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB_Table;

import java.util.ArrayList;
import java.util.List;

@Migration(version = 17, database = AppDatabase.class)
public class Migration17AddObservationValueTable extends BaseMigration {

    public Migration17AddObservationValueTable() {
        super();
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        Cursor obsActionPlanDBCursor = null;

        try {


            obsActionPlanDBCursor = database.rawQuery(
                    "Select id_obs_action_plan, id_survey_obs_action_fk, provider, gaps, "
                            + "action_plan, action1, action2, status from ObsActionPlan", null);

            if (obsActionPlanDBCursor.moveToFirst()) {

                Context context = FlowManager.getContext();

                String providerUid = findControlDataElementByCode(database,
                        context.getString(R.string.providerCode)).getUid();

                String gapsUid = findControlDataElementByCode(database,
                        context.getString(R.string.gaps_code)).getUid();

                String actionPlanUid = findControlDataElementByCode(database,
                        context.getString(R.string.action_plan_code)).getUid();

                String action1Uid = findControlDataElementByCode(database,
                        context.getString(R.string.action1_code)).getUid();

                String action2Uid = findControlDataElementByCode(database,
                        context.getString(R.string.action2_code)).getUid();

                List<String> sqlDataMigrations = new ArrayList();

                do {
                    Cursor lastIdCursor = null;
                    try {


                        long surveyId = obsActionPlanDBCursor.getLong(1);
                        String provider = obsActionPlanDBCursor.getString(2);
                        String gaps = obsActionPlanDBCursor.getString(3);
                        String actionPlan = obsActionPlanDBCursor.getString(4);
                        String action1 = obsActionPlanDBCursor.getString(5);
                        String action2 = obsActionPlanDBCursor.getString(6);
                        int status = obsActionPlanDBCursor.getInt(7);

                        database.execSQL(
                                String.format(
                                        "INSERT INTO Observation(id_survey_observation_fk,"
                                                + "status_observation)"
                                                + " values(%d, %d)", surveyId, status));

                        lastIdCursor = database.rawQuery("SELECT last_insert_rowid()", null);

                        lastIdCursor.moveToFirst();
                        long observationId = lastIdCursor.getLong(0);

                        if (hasValue(provider)) {
                            sqlDataMigrations.add(
                                    createObservationValuesQuery(observationId, provider, providerUid));
                        }

                        if (hasValue(gaps)) {
                            sqlDataMigrations.add(
                                    createObservationValuesQuery(observationId, gaps, gapsUid));
                        }

                        if (hasValue(actionPlan)) {
                            sqlDataMigrations.add(
                                    createObservationValuesQuery(observationId, actionPlan, actionPlanUid));
                        }

                        if (hasValue(action1)) {
                            sqlDataMigrations.add(
                                    createObservationValuesQuery(observationId, action1, action1Uid));
                        }

                        if (hasValue(action2)) {
                            sqlDataMigrations.add(
                                    createObservationValuesQuery(observationId, action2, action2Uid));
                        }

                    } finally {
                        if (lastIdCursor != null) {
                            lastIdCursor.close();
                        }
                    }

                } while (obsActionPlanDBCursor.moveToNext());

                for (String dataMigration : sqlDataMigrations) {
                    database.execSQL(dataMigration);
                }

            }

            database.execSQL("DROP TABLE IF EXISTS ObsActionPlan");

        } finally {
            if (obsActionPlanDBCursor != null) {
                obsActionPlanDBCursor.close();
            }
        }
    }

    private boolean hasValue(String observationValue) {
        return observationValue != null && !observationValue.isEmpty();
    }

    private String createObservationValuesQuery(long observationId, String value, String uid) {

        String query =
                String.format(
                        "INSERT INTO ObservationValue(id_observation_fk, value, "
                                + "uid_observation_value) "
                                +
                                "values(%d, '%s','%s')", observationId, value, uid);

        return query;
    }


    public static ServerMetadataDB findControlDataElementByCode(DatabaseWrapper database,
                                                                String code) {
        return new Select().from(ServerMetadataDB.class)
                .where(ServerMetadataDB_Table.code.eq(code)).querySingle(database);
    }
}
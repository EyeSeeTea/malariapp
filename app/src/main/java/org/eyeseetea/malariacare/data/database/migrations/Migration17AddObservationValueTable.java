package org.eyeseetea.malariacare.data.database.migrations;

import android.content.Context;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
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
        List<ObsActionPlanDB> obsActionPlanDBS =
                new Select().from(ObsActionPlanDB.class).queryList(database);

        if (obsActionPlanDBS.size() > 0) {

            Context context = FlowManager.getContext();

            String providerUid = findControlDataElementByCode(database,
                    context.getString(R.string.providerCode)).getUid();

            String gapsUid = findControlDataElementByCode(database,
                    context.getString(R.string.gaps_code)).getUid();

            String planActionUid = findControlDataElementByCode(database,
                    context.getString(R.string.action_plan_code)).getUid();

            String action1Uid = findControlDataElementByCode(database,
                    context.getString(R.string.action1_code)).getUid();

            String action2Uid = findControlDataElementByCode(database,
                    context.getString(R.string.action2_code)).getUid();

            List<String> sqlDataMigrations = new ArrayList();

            String insertQueryBase =
                    "INSERT INTO ObservationValue(id_survey_observation_value_fk, value, uid_observation, status)";


            for (ObsActionPlanDB obsActionPlanDB : obsActionPlanDBS) {
                //Provider
                sqlDataMigrations.add(createObservationValueMigrationQuery(insertQueryBase,
                        obsActionPlanDB.getId_survey_obs_action_fk(),
                        obsActionPlanDB.getProvider(),
                        providerUid,
                        obsActionPlanDB.getStatus()));

                //gaps
                sqlDataMigrations.add(createObservationValueMigrationQuery(insertQueryBase,
                        obsActionPlanDB.getId_survey_obs_action_fk(),
                        obsActionPlanDB.getGaps(),
                        gapsUid,
                        obsActionPlanDB.getStatus()));

                //planAction
                sqlDataMigrations.add(createObservationValueMigrationQuery(insertQueryBase,
                        obsActionPlanDB.getId_survey_obs_action_fk(),
                        obsActionPlanDB.getAction_plan(),
                        planActionUid,
                        obsActionPlanDB.getStatus()));

                //action1
                sqlDataMigrations.add(createObservationValueMigrationQuery(insertQueryBase,
                        obsActionPlanDB.getId_survey_obs_action_fk(),
                        obsActionPlanDB.getAction1(),
                        action1Uid,
                        obsActionPlanDB.getStatus()));

                //action2
                sqlDataMigrations.add(createObservationValueMigrationQuery(insertQueryBase,
                        obsActionPlanDB.getId_survey_obs_action_fk(),
                        obsActionPlanDB.getAction2(),
                        action2Uid,
                        obsActionPlanDB.getStatus()));
            }

            try {
                database.beginTransaction();

                for (String dataMigration:sqlDataMigrations) {
                    database.execSQL(dataMigration);
                }

                //removeOldTable(database);
                database.setTransactionSuccessful();

            }catch (Exception e){
                Log.e("Migration17","An error occur executing migration: " + e.getMessage());
            } finally {
                database.endTransaction();
            }
        }
    }

    private String createObservationValueMigrationQuery(
            String insertQueryBase, long surveyId, String value, String uid, int status) {
        String valuesQuery =
                String.format(" values(%d, '%s', '%s', %d);",
                        surveyId,
                        value,
                        uid,
                        status);

        return insertQueryBase + valuesQuery;
    }

    private void removeOldTable(DatabaseWrapper database) {
        database.execSQL("DROP TABLE IF EXISTS ObsActionPlan");
    }

    public static ServerMetadataDB findControlDataElementByCode(DatabaseWrapper database, String code){
        return new Select().from(ServerMetadataDB.class)
                .where(ServerMetadataDB_Table.code.eq(code)).querySingle(database);
    }
}

/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.migrations;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB_Table;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB_Table;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 23/03/2016.
 */
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

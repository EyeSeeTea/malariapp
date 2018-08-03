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

        Cursor obsActionPlanDBCursor = database.rawQuery(
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

                long surveyId = obsActionPlanDBCursor.getLong(1);
                String provider = obsActionPlanDBCursor.getString(2);
                String gaps = obsActionPlanDBCursor.getString(3);
                String actionPlan = obsActionPlanDBCursor.getString(4);
                String action1 = obsActionPlanDBCursor.getString(5);
                String action2 = obsActionPlanDBCursor.getString(6);
                int status = obsActionPlanDBCursor.getInt(7);

                database.execSQL(
                        String.format("INSERT INTO Observation(id_survey_observation_fk,status_observation)"
                                        + " values(%d, %d)", surveyId, status));

                Cursor lastIdCursor = database.rawQuery("SELECT last_insert_rowid()", null);

                lastIdCursor.moveToFirst();
                long observationId = lastIdCursor.getLong(0);

                sqlDataMigrations.add(
                        createObservationValuesQuery(observationId, provider, providerUid));

                sqlDataMigrations.add(
                        createObservationValuesQuery(observationId, gaps,gapsUid));

                sqlDataMigrations.add(
                        createObservationValuesQuery(observationId, actionPlan, actionPlanUid));

                sqlDataMigrations.add(
                        createObservationValuesQuery(observationId, action1, action1Uid));

                sqlDataMigrations.add(
                        createObservationValuesQuery(observationId, action2, action2Uid));

            } while(obsActionPlanDBCursor.moveToNext());

            for (String dataMigration : sqlDataMigrations) {
                database.execSQL(dataMigration);
            }

            //database.execSQL("DROP TABLE IF EXISTS ObsActionPlan");
        }
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
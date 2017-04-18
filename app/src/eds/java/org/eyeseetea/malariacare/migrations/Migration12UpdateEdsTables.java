/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.migrations;


import static org.eyeseetea.malariacare.database.migrations.MigrationUtils.addColumn;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.migrations.MigrationUtils;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.ServerMetadata;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;

@Migration(version = 12, databaseName = AppDatabase.NAME)
public class Migration12UpdateEdsTables extends BaseMigration {

    public Migration12UpdateEdsTables() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        //The column name can't be renamed in sqlite. It is needed create a temporal table with
        // the new column name.
        migrateSurveyTable(database);
        migrateProgramTable(database);
        MigrationUtils.addColumnSafe(database, "Option", "uid", "string");
        MigrationUtils.addColumnSafe(database, "User", "username", "string");
        MigrationUtils.addColumnSafe(database, "Option", "id_option_attribute", "integer");
    }

    private void migrateProgramTable(SQLiteDatabase database) {

        ModelAdapter myAdapter = FlowManager.getModelAdapter(Program.class);
        String programTable = "Program";
        String programTempTable = "Program_temp";
        //Create temporal table
        String sql = myAdapter.getCreationQuery();
        Log.d("DBMIGRATION", "old table " + sql);
        sql = sql.replace(programTable, programTempTable);
        Log.d("DBMIGRATION", "create temp table " + sql);
        database.execSQL(sql);
        //Insert the data in temporal table
        String sqlCopy = "INSERT INTO " + programTempTable
                + "(id_program, uid, name, stage_uid) SELECT id_program, uid, name, programStage "
                + "FROM "
                + programTable;
        database.execSQL(sqlCopy);

        //Replace old table by new table with the new column name.
        database.execSQL("DROP TABLE IF EXISTS " + programTable);
        database.execSQL("ALTER TABLE " + programTempTable + " RENAME TO " + programTable);
    }

    private void migrateSurveyTable(SQLiteDatabase database) {
        ModelAdapter myAdapter = FlowManager.getModelAdapter(Survey.class);

        //Create temporal table
        String sql = myAdapter.getCreationQuery();
        Log.d("DBMIGRATION", "old table " + sql);
        sql = sql.replace("Survey", "Survey_temp");
        Log.d("DBMIGRATION", "create temp table " + sql);
        database.execSQL(sql);

        //Insert the data in temporal table
        String sqlCopy =
                "INSERT INTO Survey_temp(id_survey, id_program, id_org_unit, id_user, "
                        + "creation_date, completion_date, upload_date, scheduled_date, status, "
                        + "eventuid) SELECT id_survey, id_program, id_org_unit, id_user, "
                        + "creation_date, completion_date, upload_date, schedule_date, status, "
                        + "eventuid FROM Survey";
        database.execSQL(sqlCopy);

        //Replace old table by new table with the new column name.
        database.execSQL("DROP TABLE IF EXISTS Survey");
        database.execSQL("ALTER TABLE Survey_temp RENAME TO Survey");
    }

    private void migrateServerMetadataTable(SQLiteDatabase database) {
        ModelAdapter myAdapter = FlowManager.getModelAdapter(ServerMetadata.class);

        //Insert the data in new table
        String sqlCopy =
                "INSERT INTO ServerMetadata(id_control_dataelement, name, code, uid, value_type) "
                        + "SELECT id_control_dataelement, name, code, uid, valueType FROM "
                        + "ControlDataelement";
        database.execSQL(sqlCopy);

        //Replace old table by new table with the new column name.
        database.execSQL("DROP TABLE IF EXISTS ControlDataelement");
    }

    @Override
    public void onPostMigrate() {
    }

}
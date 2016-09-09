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

package org.eyeseetea.malariacare.database.migrations;


import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Program$Table;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey$Table;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Tab$Table;

import static org.eyeseetea.malariacare.database.migrations.MigrationUtils.addColumn;

/**
 * Created by idelcano on 23/03/2016.
 */
@Migration(version =10, databaseName = AppDatabase.NAME)
public class MigrationRemoveTabGroups extends BaseMigration {

    public MigrationRemoveTabGroups() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        addProgramStageToProgram(database);
        addProgramToTab(database);
        addProgramToSurvey(database);
        removeTabGroups(database);
    }

    private void addProgramStageToProgram(SQLiteDatabase database) {
        addColumn(database, Program.class, Program$Table.STAGE_UID, "text");
        //move tabgroup uid (programStage) into program
        database.execSQL("update program set programStage = (select uid from tabgroup where id_program=program.id_program)");
    }

    private void addProgramToTab(SQLiteDatabase database) {
        addColumn(database, Tab.class, Tab$Table.ID_PROGRAM, "integer");
        //move id_program into tab
        database.execSQL("update tab set id_program = (select id_program from tabgroup where id_tab_group=tab.id_tab_group)");
    }

    private void addProgramToSurvey(SQLiteDatabase database) {
        addColumn(database, Survey.class, Survey$Table.ID_PROGRAM, "integer");
        //move id_program into survey
        database.execSQL("update survey set id_program = (select id_program from tabgroup where id_tab_group=survey.id_tab_group)");
    }

    private void removeTabGroups(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS tabgroup");
    }

    @Override
    public void onPostMigrate() {
    }

}
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


import static org.eyeseetea.malariacare.database.migrations.MigrationUtils.addColumn;

import android.database.sqlite.SQLiteException;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Program_Table;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Survey_Table;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Tab_Table;

/**
 * Created by idelcano on 23/03/2016.
 */
@Migration(version =11, database = AppDatabase.class)
public class Migration11RemoveTabGroups extends BaseMigration {

    public Migration11RemoveTabGroups() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addProgramStageToProgram(database);
        addProgramToTab(database);
        addProgramToSurvey(database);
        removeTabGroups(database);
    }

    private void addProgramStageToProgram(DatabaseWrapper database) {
        addColumn(database, Program.class, Program_Table.stage_uid.getDefinition(), "string");
        //move programStage uid into program
        database.execSQL("update program set stage_uid = (select uid from tabgroup where id_program=program.id_program)");
    }

    private void addProgramToTab(DatabaseWrapper database) {
        addColumn(database, Tab.class, Tab_Table.id_program.getDefinition(), "integer");
        //move id_program into tab
        database.execSQL("update tab set id_program = (select id_program from tabgroup where id_tab_group=tab.id_tab_group)");
    }

    private void addProgramToSurvey(DatabaseWrapper database) {
        try {
            //Is possible in some devices between versions the column id_program not exist and it will make a sqliteexception
            addColumn(database, Survey.class, Survey_Table.id_program.getDefinition(), "integer");
            database.execSQL("update survey set id_program = (select id_program from tabgroup where id_tab_group=survey.id_tab_group)");
        } catch (SQLiteException e){
            e.printStackTrace();
            //In the last migration the survey.id_tab_group was renamed to survey.id_program, but here is the fixed value.
            database.execSQL("update survey set id_program = (select id_program from tabgroup where id_tab_group=survey.id_program)");
        }
        //move id_program into survey

    }

    private void removeTabGroups(DatabaseWrapper database) {
        database.execSQL("DROP TABLE IF EXISTS tabgroup");
    }

    @Override
    public void onPostMigrate() {
    }

}
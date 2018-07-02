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

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

import static org.eyeseetea.malariacare.data.database.migrations.MigrationUtils.addColumn;

/**
 * Created by ignac on 30/11/2015.
 */
@Migration(version = 16, database = AppDatabase.class)
public class Migration16AddSurveyColumns extends BaseMigration {

    public Migration16AddSurveyColumns() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        addColumnAndCopyOldData(database,"total_questions");
        addColumnAndCopyOldData(database,"answered_questions");
        addColumnAndCopyOldData(database,"total_compulsory_questions");
        addColumnAndCopyOldData(database,"answered_compulsory_questions");

        removeTabGroups(database);

    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

    private void addColumnAndCopyOldData(DatabaseWrapper database, String column) {
        addColumn(database, SurveyDB.class, column, "int");
        //move programStage uid into program
        database.execSQL("update Survey set "+column+" = (select "+column+" from SurveyAnsweredRatio where id_survey=Survey.id_survey)");
    }
    private void removeTabGroups(DatabaseWrapper database) {
        database.execSQL("DROP TABLE IF EXISTS SurveyAnsweredRatio");
    }
}
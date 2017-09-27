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

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

/**
 * Created by idelcano on 23/03/2016.
 */
@Migration(version = 7, database = AppDatabase.class)
public class Migration7RenameEventDate extends BaseMigration {

    public Migration7RenameEventDate() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        //The column name can't be renamed in sqlite. It is needed create a temporal table with the new column name.
        ModelAdapter myAdapter = FlowManager.getModelAdapter(SurveyDB.class);

        //Create temporal table
        String sql=myAdapter.getCreationQuery();
        Log.d("DBMIGRATION", "old table " + sql);
        sql=sql.replace("Survey", "Survey_temp");
        Log.d("DBMIGRATION", "create temp table " + sql);
        database.execSQL(sql);

        //Insert the data in temporal table
        String sqlCopy="INSERT INTO Survey_temp(id_survey, id_tab_group, id_org_unit, id_user, creationDate, completionDate, uploadedDate, scheduledDate, status, eventuid) SELECT id_survey, id_tab_group, id_org_unit, id_user, creationDate, completionDate, eventDate, scheduledDate, status, eventuid FROM Survey";
        database.execSQL(sqlCopy);

        //Replace old table by new table with the new column name.
        database.execSQL("DROP TABLE IF EXISTS Survey");
        database.execSQL("ALTER TABLE Survey_temp RENAME TO Survey");
    }

    @Override
    public void onPostMigrate() {
    }

}

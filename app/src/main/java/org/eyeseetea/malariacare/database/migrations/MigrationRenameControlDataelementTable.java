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
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.hisp.dhis.android.sdk.persistence.Dhis2Database;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue;

/**
 * Created by idelcano on 13/04/2016.
 */

@Migration(version = 9, databaseName = Dhis2Database.NAME)
public class MigrationRenameControlDataelementTable extends BaseMigration {

    public static final String DROP_TABLE_IF_EXIST = "DROP TABLE IF EXISTS %s";
    public static final String RENAME_TABLE = "ALTER TABLE %s RENAME TO %s";
    public MigrationRenameControlDataelementTable() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        renameTable(database,"ServerMetadata",DataElementAttributeValue.class);
    }

    @Override
    public void onPostMigrate() {
        //release migration resources
    }

    private void renameTable(SQLiteDatabase database, String oldTable, Class newTable) {
        ModelAdapter newTableAdapter= FlowManager.getModelAdapter(newTable);
        database.execSQL(String.format(DROP_TABLE_IF_EXIST, newTableAdapter.getTableName()));
        database.execSQL(String.format(RENAME_TABLE, oldTable, newTableAdapter.getTableName()));
    }
}
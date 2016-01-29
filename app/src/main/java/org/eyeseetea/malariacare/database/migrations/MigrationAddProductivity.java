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
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.Question;

/**
 * Created by idelcano on 27/01/2016.
 */
@Migration(version = 5, databaseName = AppDatabase.NAME)
public class MigrationAddProductivity extends BaseMigration {

    private final static String TAG=".Migration";

    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s %s";
    public static final String UPDATE_COLUMN = "UPDATE %s SET %s =\"%s\"";

    public MigrationAddProductivity() {
        super();
    }

    public void onPreMigrate() {
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        addColumn(database, OrgUnit.class, "productivity", "Integer");
        updateColumn(database, OrgUnit.class, "productivity", "0");
    }

    @Override
    public void onPostMigrate() {
    }

    private void addColumn(SQLiteDatabase database, Class model, String columnName,String type){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);
        database.execSQL(String.format(ALTER_TABLE_ADD_COLUMN, myAdapter.getTableName(), columnName, type));

    }

    private void updateColumn(SQLiteDatabase database, Class model, String columnName,String value){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);
        Log.d(TAG,String.format(UPDATE_COLUMN, myAdapter.getTableName(),columnName,value) );
        database.execSQL(String.format(UPDATE_COLUMN, myAdapter.getTableName(),columnName,value) );
    }
}

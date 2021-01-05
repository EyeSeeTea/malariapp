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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Helper methods to add, update columns from db model.
 * Created by arrizabalaga on 10/02/16.
 */
public class MigrationUtils {

    private static final String TAG="MigrationUtils";


    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s %s";
    public static final String UPDATE_COLUMN = "UPDATE %s SET %s =\"%s\"";

    public static void addColumn(DatabaseWrapper database, Class model, String columnName, String type){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);

        if (!isColumnExists(database, myAdapter.getTableName(), columnName)) {
            database.execSQL(String.format(ALTER_TABLE_ADD_COLUMN, myAdapter.getTableName(), columnName, type));
        }
    }

    public static void updateColumn(DatabaseWrapper database, Class model, String columnName,String value){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);
        Log.d(TAG, String.format(UPDATE_COLUMN, myAdapter.getTableName(), columnName, value));
        database.execSQL(String.format(UPDATE_COLUMN, myAdapter.getTableName(),columnName,value) );
    }

    public static void recreateTables(DatabaseWrapper database,Class[] tables){
        for(int i=0;i<tables.length;i++){
            ModelAdapter myAdapter = FlowManager.getModelAdapter(tables[i]);
            database.execSQL(DROP_TABLE_IF_EXISTS + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());
        }
    }

    public static boolean isColumnExists(DatabaseWrapper database,
            String tableName,
            String columnToFind) {
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "PRAGMA table_info(" + tableName + ")",
                    null
            );

            int nameColumnIndex = cursor.getColumnIndexOrThrow("name");

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameColumnIndex);

                if (name.equals(columnToFind)) {
                    return true;
                }
            }

            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

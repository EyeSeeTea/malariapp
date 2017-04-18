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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

/**
 * Helper methods to add, update columns from db model.
 * Created by arrizabalaga on 10/02/16.
 */
public class MigrationUtils {

    private static final String TAG="MigrationUtils";


    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    public static final String ALTER_TABLE_ADD_COLUMN = "ALTER TABLE %s ADD COLUMN %s %s";
    public static final String UPDATE_COLUMN = "UPDATE %s SET %s =\"%s\"";

    public static void addColumn(SQLiteDatabase database, Class model, String columnName,String type){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);
        database.execSQL(String.format(ALTER_TABLE_ADD_COLUMN, myAdapter.getTableName(), columnName, type));

    }

    public static void updateColumn(SQLiteDatabase database, Class model, String columnName,String value){
        ModelAdapter myAdapter = FlowManager.getModelAdapter(model);
        Log.d(TAG, String.format(UPDATE_COLUMN, myAdapter.getTableName(), columnName, value));
        database.execSQL(String.format(UPDATE_COLUMN, myAdapter.getTableName(),columnName,value) );
    }

    public static void recreateTables(SQLiteDatabase database,Class[] tables){
        for(int i=0;i<tables.length;i++){
            ModelAdapter myAdapter = FlowManager.getModelAdapter(tables[i]);
            database.execSQL(DROP_TABLE_IF_EXISTS + myAdapter.getTableName());
            database.execSQL(myAdapter.getCreationQuery());
        }
    }
    public static void addColumnSafe(SQLiteDatabase database, String tableName, String newColumnName, String columnType) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
        if (cursor == null) {
            return;
        }
        if (!hasColumnName(cursor, newColumnName)) {
            try {
                database.execSQL("ALTER TABLE " + tableName + " ADD COLUMN `" + newColumnName + "` " + columnType);
            } catch (Exception e) {
                Log.d(TAG, "Error adding column " + newColumnName + ". Message: " + e.getMessage());
            }
        }
    }

    private static boolean hasColumnName(Cursor cursor, String columnName) {
        return cursor.getColumnIndex(columnName) != -1;
    }
}

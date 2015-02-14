package org.psi.malariacare.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by adrian on 10/02/15.
 */
public class MalariaCareDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MalariaCare.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_DATAELEMENTS =
            "CREATE TABLE " + MalariaCareTables.DataElements.TABLE_NAME + " (" +
                    MalariaCareTables.DataElements._ID + " INTEGER PRIMARY KEY," +
                    MalariaCareTables.DataElements.NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    MalariaCareTables.DataElements.NAME_TAB + TEXT_TYPE + COMMA_SEP +
                    MalariaCareTables.DataElements.NAME_OPTION_SET + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_DATAELEMENTS =
            "DROP TABLE IF EXISTS " + MalariaCareTables.DataElements.TABLE_NAME;

    public MalariaCareDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DATAELEMENTS);
        db.execSQL("insert into " + MalariaCareTables.DataElements.TABLE_NAME + "(" + MalariaCareTables.DataElements.NAME_TITLE + "," + MalariaCareTables.DataElements.NAME_TAB + "," + MalariaCareTables.DataElements.NAME_OPTION_SET + ") values ('Patient sex','GNR','GENDER')");

    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_DATAELEMENTS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

package org.psi.malariacare;

import android.provider.BaseColumns;

/**
 * Created by adrian on 10/02/15.
 */
public class MalariaCareDb {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public MalariaCareDb() {}


    /* Inner class that defines the table contents */
    public static abstract class DataElements implements BaseColumns {
        public static final String TABLE_NAME = "DataElements";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TAB = "tab";
        public static final String COLUMN_NAME_OPTION_SET = "optionset";
    }

    /* Inner class that defines the table contents
    public static abstract class DataValue implements BaseColumns {
        public static final String TABLE_NAME = "DataValue";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TAB = "tab";
        public static final String COLUMN_NAME_OPTION_SET = "optionset";
    }*/


}

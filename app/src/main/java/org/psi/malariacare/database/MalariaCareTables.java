package org.psi.malariacare.database;

import android.provider.BaseColumns;

/**
 * Created by adrian on 10/02/15.
 */
public class MalariaCareTables {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public MalariaCareTables() {}


    /* Inner class that defines the table contents */
    public static abstract class DataElements implements BaseColumns {
        public static final String TABLE_NAME = "DataElements";
        public static final String NAME_TITLE = "title";
        public static final String NAME_TAB = "tab";
        public static final String NAME_OPTION_SET = "optionset";
    }

    /* Inner class that defines the table contents */
    public static abstract class DataValue implements BaseColumns {
        public static final String TABLE_NAME = "DataValue";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TAB = "tab";
        public static final String COLUMN_NAME_OPTION_SET = "optionset";
    }


}

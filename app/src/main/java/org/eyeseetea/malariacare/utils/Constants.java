package org.eyeseetea.malariacare.utils;

public class Constants {

    // ############# QUESTION TYPE ###############
    public static final int DROPDOWN_LIST = 1,
            INT = 2,
            LONG_TEXT = 3,
            SHORT_TEXT = 4,
            DATE = 5,
            POSITIVE_INT = 6,
            NO_ANSWER = 7;

    public static final String DEFAULT_SELECT_OPTION = "";

    public static final int MAX_INT_CHARS = 5;

    // ############# TAB TYPE ###############
    public static final int TAB_AUTOMATIC_SCORED = 0,
            TAB_AUTOMATIC_NON_SCORED = 1,
            TAB_CUSTOM_SCORED = 2,
            TAB_CUSTOM_NON_SCORED = 3,
            TAB_SCORE_SUMMARY = 4,
            TAB_OTHER = 5;

    // ############# SURVEY STATUS ###############
    public static final int SURVEY_IN_PROGRESS = 0,
            SURVEY_COMPLETED = 1,
            SURVEY_SENT = 2;

    //############# OPERATION TYPE ##############
    public static final int OPERATION_TYPE_MATCH = 0,
            OPERATION_TYPE_PARENT = 1,
            OPERATION_TYPE_OTHER = 2;

    public static final String FONTS_SMALL = "small",
            FONTS_MEDIUM = "medium",
            FONTS_LARGE = "large",
            FONTS_EXTRA_LARGE = "extra",
            FONTS_SYSTEM = "system";

}

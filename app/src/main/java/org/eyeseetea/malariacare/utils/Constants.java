package org.eyeseetea.malariacare.utils;

public class Constants {

    // ############# QUESTION TYPE ###############
    public static final int DROPDOWN_LIST = 1,
            INT = 2,
            LONG_TEXT = 3,
            SHORT_TEXT = 4,
            DATE = 5,
            POSITIVE_INT = 6,
            NO_ANSWER = 7,
            RADIO_GROUP_HORIZONTAL = 8,
            RADIO_GROUP_VERTICAL = 9,
            DROPDOWN_LIST_DISABLED = 10,
            IMAGES_2 = 15,//it was 10
            IMAGES_3 = 14,
            IMAGES_4 = 11,
            IMAGES_6 = 12,
            PHONE = 13,
            NUMERIC_PICKER=16;
    //Fixme dropdown_list==images_2


    public static final String DEFAULT_SELECT_OPTION = "";

    public static final int MAX_INT_CHARS = 5;

    public static final int MAX_INT_AGE = 99;

    // ############# TAB TYPE ###############
    public static final int TAB_AUTOMATIC = 0,
            TAB_AUTOMATIC_NON_SCORED = 1,//we need to delete this1
            TAB_COMPOSITE_SCORE = 2,
            TAB_SCORE_SUMMARY = 4, //we need to delete this
            TAB_ADHERENCE = 6,
            TAB_IQATAB=7,
            TAB_REPORTING=8;

    // ############# TAB PICTUREAPP TYPE ###############
    public static final int TAB_AUTOMATIC_SCORED = 0,
            TAB_CUSTOM_SCORED = 2,
            TAB_CUSTOM_NON_SCORED = 3,
            TAB_OTHER = 5,
            TAB_DYNAMIC_AUTOMATIC_TAB=8;

    // ############# ANSWER TYPE ###############
    public static final String TO_BE_REMOVED="OuiNon (to be removed)",
    LABEL="Label";

    //FIXME So far the special sub type of composite scores is treated by name
    public static final String COMPOSITE_SCORE_TAB_NAME="Composite Scores";

    // ############# SURVEY STATUS ###############
    public static final int SURVEY_IN_PROGRESS = 0,
            SURVEY_COMPLETED = 1,
            SURVEY_SENT = 2,
            SURVEY_HIDE = 3;

    //############# OPERATION TYPE ##############
    public static final int OPERATION_TYPE_MATCH = 0,
            OPERATION_TYPE_PARENT = 1,
            OPERATION_TYPE_OTHER = 2;

    public static final String FONTS_XSMALL = "xsmall",
            FONTS_SMALL = "small",
            FONTS_MEDIUM = "medium",
            FONTS_LARGE = "large",
            FONTS_XLARGE = "xlarge",
            FONTS_SYSTEM = "system";

    public static final int MAX_ITEMS_IN_DASHBOARD=5;

    //############# LOGIN AUTHORIZATION ACTIONS ##############
    public static final int AUTHORIZE_PUSH = 0,
            AUTHORIZE_PULL = 1;

    public static final String CHECKBOX_YES_OPTION="Yes";

}

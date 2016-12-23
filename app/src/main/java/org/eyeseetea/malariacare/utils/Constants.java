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
            SWITCH_BUTTON=12,

            //TODO now review this constants
            IMAGES_2 = 10,
            IMAGES_4 = 11,
            IMAGES_6 = 12,
            PHONE = 13,
            IMAGES_3 = 14;



    public static final String DEFAULT_SELECT_OPTION = "";

    public static final int MAX_INT_CHARS = 5;

    // ############# TAB TYPE ###############
    public static final int TAB_AUTOMATIC = 0,
            TAB_AUTOMATIC_NON_SCORED = 1,//we need to delete this
            TAB_COMPOSITE_SCORE = 2,
            TAB_SCORE_SUMMARY = 4, //we need to delete this
            TAB_ADHERENCE = 6,
            TAB_IQATAB=7,
            TAB_REPORTING=8,
            TAB_DYNAMIC_AUTOMATIC_TAB=9;

    // ############# ANSWER TYPE ###############
    public static final String TO_BE_REMOVED="OuiNon (to be removed)",
    LABEL="Label";

    //FIXME So far the special sub type of composite scores is treated by name
    public static final String COMPOSITE_SCORE_TAB_NAME="Composite Scores";

    // ############# SURVEY STATUS ###############
    public static final int SURVEY_PLANNED = -1,
            SURVEY_IN_PROGRESS = 0,
            SURVEY_COMPLETED = 1,
            SURVEY_SENT = 2,
            SURVEY_HIDE = 3,
            SURVEY_CONFLICT = 4;

    //############# OPERATION TYPE ##############
    public static final int OPERATION_TYPE_MATCH = 0,
            OPERATION_TYPE_PARENT = 1,
            OPERATION_TYPE_OTHER = 2;

    //############# MEDIA TYPE ##############
    // Values to identify image/video on media_type column in the DB
    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final String MEDIA_SEPARATOR="#";
    public static final int NO_MEDIA_ID=-1;

    public static final int MAX_ITEMS_IN_DASHBOARD=5;

    //############# LOGIN AUTHORIZATION ACTIONS ##############
    public static final int AUTHORIZE_PUSH = 0,
            AUTHORIZE_PULL = 1;

    public static final String CHECKBOX_YES_OPTION="Yes";

    public static final float MAX_AMBER = 80f;
    public static final float MAX_RED = 50f;

    //Keys used to know if a survey is saved in surveyfragment, in feedbackfragment, or in push.
    public static final String PUSH_MODULE_KEY="PUSH_MODULE_KEY";
    public static final String PROGRESSACTIVITY_MODULE_KEY="PROGRESSACTIVITY_MODULE_KEY";
    public static final String TEST_MODULE_KEY="TEST_MODULE_KEY";
    public static final String MODULE_KEY="MODULE_KEY";
    public static final String FRAGMENT_SURVEY_KEY ="FRAGMENT_SURVEY_KEY";
    public static final String FRAGMENT_FEEDBACK_KEY ="FRAGMENT_FEEDBACK_KEY";

    public static final int MINIMAL_WIDTH_PIXEL_RESOLUTION_TO_SHOW_LARGE_TEXT = 500;
    public static final int MINIMAL_HEIGHT_PIXEL_RESOLUTION_TO_SHOW_LARGE_TEXT = 1000;
}

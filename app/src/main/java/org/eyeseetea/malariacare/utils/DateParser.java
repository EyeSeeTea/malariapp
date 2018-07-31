package org.eyeseetea.malariacare.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {

    public final static String DHIS2_GMT_NEW_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public final static String DHIS2_GMT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final static String AMERICAN_DATE_FORMAT = "yyyy-MM-dd";

    public static Date parseNewLongDate(String dateAsString) {
        try {
            return parseDate(dateAsString, DHIS2_GMT_NEW_DATE_FORMAT);
        } catch (ParseException ex) {
            return parseLongDate(dateAsString);
        }
    }
    public static Date parseLongDate(String dateAsString) {
        try {
            return parseDate(dateAsString, DHIS2_GMT_DATE_FORMAT);
        } catch (ParseException ex) {
            return parseShortDate(dateAsString);
        }
    }
    public static Date parseDate(String dateAsString, String format) throws ParseException {
        return (dateAsString != null) ? new SimpleDateFormat(format).parse(dateAsString) : null;
    }

    public static Date parseShortDate(String dateAsString) {
        try {
            return parseDate(dateAsString, AMERICAN_DATE_FORMAT);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    /**
     * Turns a given date into a parseable String according to sdk date format
     */
    public static String formatAmerican(Date date) {
        String format = AMERICAN_DATE_FORMAT;
        return (date != null) ? new SimpleDateFormat(format).format(date) : null;
    }
}

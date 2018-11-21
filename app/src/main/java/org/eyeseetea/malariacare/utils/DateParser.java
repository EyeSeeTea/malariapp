package org.eyeseetea.malariacare.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser {
    public final static String AMERICAN_DATE_FORMAT = "yyyy-MM-dd";
    public final static String EUROPEAN_DATE_FORMAT = "dd-MM-yyyy";
    public final static String EUROPEAN_FORMATTED_DATE_WITH_SHORT_YEAR = "dd/MM/yy";
    public final static String LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public final static String LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public String getEuropeanFormattedDateWithShortYear(Date date) {
        return formatDate(date, EUROPEAN_FORMATTED_DATE_WITH_SHORT_YEAR);
    }

    public String getEuropeanFormattedDate(Date date) {
        return formatDate(date, EUROPEAN_DATE_FORMAT);
    }

    public String userFormatDate(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return formatDate(date, locale);
    }

    public String formatDateToServer(Date date) {
        if (date == null) {
            return "";
        }

        return format(date,
                AMERICAN_DATE_FORMAT);
    }

    private String formatDate(Date date, Locale locale) {
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        return dateFormatter.format(date);
    }

    private String formatDate(Date date, String pattern) {
        if(date==null){
            return "";
        }
        try {
            return format(date, pattern);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
    /**
     * Turns a given date into a parseable String according to sdk date format
     */
    public static String formatAmerican(Date date) {
        String format = AMERICAN_DATE_FORMAT;
        return (date != null) ? new SimpleDateFormat(format).format(date) : null;
    }

    /**
     * Turns a given date into a parseable String according to sdk date format
     */
    public String format(Date date, String format) {
        return (date != null) ? new SimpleDateFormat(format).format(date) : null;
    }

    public Date parseDate(String dateAsString, String format) {
        try {
            return (dateAsString != null) ? new SimpleDateFormat(format).parse(dateAsString) : null;
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }
}

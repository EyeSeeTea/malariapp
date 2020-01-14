package org.eyeseetea.malariacare.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateParser {
    public final static String AMERICAN_DATE_FORMAT = "yyyy-MM-dd";
    public final static String EUROPEAN_DATE_FORMAT = "dd-MM-yyyy";
    public final static String LONG_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public final static String LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static String getEuropeanFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(EUROPEAN_DATE_FORMAT);
        if(date==null){
            return "";
        }
        return sdf.format(date);
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
        try {
            return dateFormatter.format(date);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }


    /**
     * Turns a given date into a parseable String according to sdk date format
     */
    public String format(Date date, String format) {
        try {
            return (date != null) ? new SimpleDateFormat(format).format(date) : null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Date parseDate(String dateAsString, String format) {
        try {
            return (dateAsString != null) ? new SimpleDateFormat(format).parse(dateAsString) : null;
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public Date parseDate(String dateAsString, String format, TimeZone timeZone) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            simpleDateFormat.setTimeZone(timeZone);
            return (dateAsString != null) ? simpleDateFormat.parse(dateAsString) : null;
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public Date toLocalDate(Date utcDate) {
        try {
            SimpleDateFormat dateFormatUTC = new SimpleDateFormat(LONG_DATE_FORMAT);
            dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

            //Local time zone
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat(LONG_DATE_FORMAT);

            return (utcDate != null) ? dateFormatLocal.parse(dateFormatUTC.format(utcDate)) : null;
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public Date toUTCDate(Date localDate) {
        try {
            SimpleDateFormat dateFormatUTC = new SimpleDateFormat(LONG_DATE_FORMAT);
            dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

            //Local time zone
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat(LONG_DATE_FORMAT);

            return (localDate != null) ? dateFormatUTC.parse(dateFormatLocal.format(localDate)) : null;
        } catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }
}

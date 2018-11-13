package org.eyeseetea.malariacare.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateParser {
    public final static String AMERICAN_DATE_FORMAT = "yyyy-MM-dd";
    public final static String DHIS2_GMT_NEW_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public final static String DHIS2_GMT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public final static String EUROPEAN_DATE_FORMAT = "dd-MM-yyyy";

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

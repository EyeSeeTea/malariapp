package org.eyeseetea.malariacare.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.core.Is.is;

public class DateParserShould {
    private final String dateHumanFormat = "8 nov. 2018";
    private final  String dateServerAmericanFormat = "2018-11-08";
    private final  String dateServerLongFormat = "2018-11-08T15:43:59.944+000";
    private final  String dateServerLongFormatWithTimeZone = "2018-11-08T15:43:59.944+0000";
    private final int datePickerYear=2018;
    private final int datePickerMonth=10;
    private final int datePickerDayOfMonth=8;
    private final Locale locale = Locale.FRENCH;
    private final DateParser dateParser = new DateParser();

    @Test
    public void return_string_date_with_human_format_when_convert_a_server_american_format_string_date() {
        Date date = dateParser.parseDate(dateServerAmericanFormat, DateParser.AMERICAN_DATE_FORMAT);
        Assert.assertThat(dateParser.userFormatDate(date, locale), is(dateHumanFormat));
    }

    @Test
    public void return_string_date_with_human_format_when_convert_a_server_long_format_string_date() {
        Date date = dateParser.parseDate(dateServerLongFormat, DateParser.LONG_DATE_FORMAT);
        Assert.assertThat(dateParser.userFormatDate(date, locale), is(dateHumanFormat));
    }

    @Test
    public void return_string_date_with_human_format_when_convert_a_server_long_with_time_zone_format_string_date() {
        Date date = dateParser.parseDate(dateServerLongFormatWithTimeZone, DateParser.LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE);
        Assert.assertThat(dateParser.userFormatDate(date, locale), is(dateHumanFormat));
    }

    @Test
    public void return_string_date_with_human_format_when_convert_a_date_picker_date() {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(datePickerYear, datePickerMonth, datePickerDayOfMonth);
        Assert.assertThat(dateParser.userFormatDate(newCalendar.getTime(), locale), is(dateHumanFormat));
    }

    @Test
    public void return_string_date_with_server_format_when_convert_a_date_picker_date() {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(datePickerYear, datePickerMonth, datePickerDayOfMonth);
        Assert.assertThat(dateParser.formatDateToServer(newCalendar.getTime()), is(dateServerAmericanFormat));
    }

    @Test
    public void return_correct_value_string_when_parse_null_user_date() {
        String dateFormatted = dateParser.userFormatDate(null, locale);
        Assert.assertThat(dateFormatted, is(""));
    }
}

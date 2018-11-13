package org.eyeseetea.malariacare.test;

import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.utils.DateParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class DateParserShould {
    private final String dateHumanFormat = "8 nov.2018";
    private final  String dateServerFormat = "2018-11-08";
    private final int datePickerYear=2018;
    private final int datePickerMonth=10;
    private final int datePickerDayOfMonth=8;
    private final Locale locale = new Locale("es", "ES");
    private final DateParser dateParser = new DateParser();

    @Test
    public void return_string_date_with_human_format_when_convert_a_server_format_string_date() {
        Date date = dateParser.parseDate(dateServerFormat, DateParser.LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE);
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
        Assert.assertThat(dateParser.formatDateToServer(newCalendar.getTime()), is(dateServerFormat));
    }

    @Test
    public void return_correct_value_string_when_parse_null_user_date() {
        String dateFormatted = dateParser.userFormatDate(null, locale);
        Assert.assertThat(dateFormatted, is("-"));
    }
}

package org.eyeseetea.malariacare.test;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.utils.AUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class DateParserShould {
    final String dateHumanFormat = "8 nov.2018";
    final String dateServerFormat = "2018-11-08";
    final int datePickerYear=2018;
    final int datePickerMonth=10;
    final int datePickerDayOfMonth=8;

    @Test
    public void return_string_date_with_human_format_when_convert_a_server_format_string_date() {
        Date date = EventExtended.parseLongDate(dateServerFormat);
        Assert.assertThat(AUtils.userFormatDate(date), is(dateHumanFormat));
    }

    @Test
    public void return_string_date_with_human_format_when_convert_a_date_picker_date() {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(datePickerYear, datePickerMonth, datePickerDayOfMonth);
        Assert.assertThat(AUtils.userFormatDate(newCalendar.getTime()), is(dateHumanFormat));
    }

    @Test
    public void return_string_date_with_server_format_when_convert_a_date_picker_date() {
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(datePickerYear, datePickerMonth, datePickerDayOfMonth);
        Assert.assertThat(AUtils.formatDateToServer(newCalendar.getTime()), is(dateServerFormat));
    }

    @Test
    public void return_empty_string_when_parse_null_date() {
        Assert.assertThat(AUtils.userFormatDate(null), is(""));
    }

    @Test
    public void return_correct_value_string_when_parse_null_schedule_date() {
        Assert.assertThat(AUtils.scheduleFormatDate(null), is("-"));
    }
}

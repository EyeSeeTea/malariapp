package org.eyeseetea.malariacare.domain.entity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ServerShould {

    public static final String DEFAULT_SCHEDULE_MONTHS_VALUE ="https://data.psi-mis.org";
    public static final HashMap<String, int[]> nextScheduleMonths = new HashMap<>();

    static {
        nextScheduleMonths.put(DEFAULT_SCHEDULE_MONTHS_VALUE, new int[]{2, 4, 6});
        nextScheduleMonths.put("https://zw.hnqis.org/", new int[]{1, 1, 6});
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void return_values_when_is_created_with_correct_values() {
        Server server = new Server(DEFAULT_SCHEDULE_MONTHS_VALUE);

        assertThat(server.getUrl(), is(DEFAULT_SCHEDULE_MONTHS_VALUE));
    }


    @Test
    public void throw_exception_when_url_is_null() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("url is required");
        new Server(null);
    }
}

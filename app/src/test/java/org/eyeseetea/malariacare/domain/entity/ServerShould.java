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
        Server server = new Server(DEFAULT_SCHEDULE_MONTHS_VALUE, new NextScheduleMonths(nextScheduleMonths.get(DEFAULT_SCHEDULE_MONTHS_VALUE)));

        assertThat(server.getUrl(), is(DEFAULT_SCHEDULE_MONTHS_VALUE));
        assertThat(server.getNextScheduleMatrix().getScoreAMonths(), is(6));
        assertThat(server.getNextScheduleMatrix().getHighProductivityMonths(), is(2));
        assertThat(server.getNextScheduleMatrix().getLowProductivityMonths(), is(4));
    }

    @Test
    public void return_correct_values_when_is_created_with_zimbabwe_values() {
        Server server = new Server(DEFAULT_SCHEDULE_MONTHS_VALUE, new NextScheduleMonths(nextScheduleMonths.get("https://zw.hnqis.org/")));

        assertThat(server.getUrl(), is(DEFAULT_SCHEDULE_MONTHS_VALUE));
        assertThat(server.getNextScheduleMatrix().getScoreAMonths(), is(6));
        assertThat(server.getNextScheduleMatrix().getLowProductivityMonths(), is(1));
        assertThat(server.getNextScheduleMatrix().getHighProductivityMonths(), is(1));
    }

    @Test
    public void throw_exception_when_url_is_null() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("url is required");
        new Server(null, new NextScheduleMonths(nextScheduleMonths.get("https://zw.hnqis.org/")));
    }

    @Test
    public void throw_exception_when_next_schedule_months_is_null() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("nextScheduleMatrix is required");
        new Server(DEFAULT_SCHEDULE_MONTHS_VALUE, null);
    }

    @Test
    public void throw_exception_when_next_schedule_months_has_invalid_matrix() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("array with three dimensions required");
        new Server(DEFAULT_SCHEDULE_MONTHS_VALUE, new NextScheduleMonths(new int[]{1, 1}));
    }
}

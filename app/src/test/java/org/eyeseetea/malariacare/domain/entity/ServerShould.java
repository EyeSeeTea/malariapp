package org.eyeseetea.malariacare.domain.entity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ServerShould {

    public static final String DEFAULT_URL ="https://data.psi-mis.org";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void return_values_when_is_created_with_correct_values() {
        Server server = new Server(DEFAULT_URL);

        assertThat(server.getUrl(), is(DEFAULT_URL));
    }
}

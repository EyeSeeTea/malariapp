package org.eyeseetea.malariacare.domain.entity;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CredentialsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void have_isDemoCredentials_equal_to_true_when_is_a_demo_credentials() {
        Credentials demoCredentials = Credentials.createDemoCredentials();

        assertThat(demoCredentials.isDemoCredentials(), is(true));
    }

    @Test
    public void have_isDemoCredentials_equal_to_false_when_is_not_a_demo_credentials() {
        Credentials credentials = new Credentials("server", "user", "pwd");

        assertThat(credentials.isDemoCredentials(), is(false));
    }

    @Test
    public void throw_exception_if_server_url_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Server URL is required");

        Credentials credentials = new Credentials(null, "user", "pwd");
    }

    @Test
    public void throw_exception_if_username_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Username is required");

        Credentials credentials = new Credentials("server", null, "pwd");
    }

    @Test
    public void throw_exception_if_password_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Password is required");

        Credentials credentials = new Credentials("server", "user", null);
    }
}

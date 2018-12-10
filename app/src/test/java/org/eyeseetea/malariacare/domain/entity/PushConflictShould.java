package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.eyeseetea.malariacare.domain.entity.pushsummary.PushConflict;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PushConflictShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throw_exception_when_create_pushConflict_with_null_uid() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Uid is required");
        new PushConflict(null, "value");
    }

    @Test
    public void throw_exception_when_create_pushConflict_with_empty_uid() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Uid is required");
        new PushConflict("", "value");
    }

    @Test
    public void throw_exception_when_create_pushConflict_with_null_value() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Value is required");
        new PushConflict("fc44xds", null);
    }

    @Test
    public void throw_exception_when_create_pushConflict_with_empty_value() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Value is required");
        new PushConflict("fc44xds", "");
    }

    @Test
    public void get_uid_return_correct_uid() {
        String uid = "fc44xds";
        PushConflict pushConflict = new PushConflict(uid, "value");
        assertThat(pushConflict.getUid(), is(uid));
    }

    @Test
    public void get_value_return_correct_value() {
        String value = "value";
        PushConflict pushConflict = new PushConflict("fc44xds", value);
        assertThat(pushConflict.getValue(), is(value));
    }

}


package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ProgramShould {

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void create_program_with_mandatory_fields() {
        Program program = new Program("FGHY22F67", "program name", "FGJUTDCGH");

        assertThat(program, is(notNullValue()));
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_null() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new Program(null, "program name", "FGJUTDCGH");
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_null() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new Program("FGHY22F67", null, "FGJUTDCGH");
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_empty() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new Program("", "program name", "FGJUTDCGH");
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_empty() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new Program("FGHY22F67", "", "FGJUTDCGH");
    }
}
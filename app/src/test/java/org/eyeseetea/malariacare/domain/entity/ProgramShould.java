package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

public class ProgramShould {

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void create_program_with_mandatory_fields(){
        Program program = new Program("FGHY22F67", "program name");

        assertThat(program, is(notNullValue()));
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_null(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new Program(null, "program name");
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_null(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new Program("FGHY22F67", null);
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_empty(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new Program("", "program name");
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_empty(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new Program("FGHY22F67", "");
    }

    @Test
    public void add_assigned_org_unit_uids_to_existed_program(){
        List<String> expectedAssignedOrgUnitUids = Arrays.asList("VBG678345", "VBGFD235V");

        Program program = new Program("FGHY22F67", "program name");

        program.addAssignedOrgUnits(expectedAssignedOrgUnitUids);

        assertThat(program.getAssignedOrgUnits(), is(expectedAssignedOrgUnitUids));
    }
}

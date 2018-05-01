package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

public class OrgUnitLevelShould {

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void create_org_unit_level_with_mandatory_fields(){
        OrgUnitLevel orgUnitLevel = new OrgUnitLevel("HLEL4kj45", "Org unit level name");

        assertThat(orgUnitLevel, is(notNullValue()));
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_null(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new OrgUnitLevel(null, "Org unit level name");
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_null(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new OrgUnitLevel("HLEL4kj45", null);
    }


    @Test
    public void throw_illegalArgumentException_if_uid_is_empty(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new OrgUnitLevel("", "Org unit level name");
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_empty(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new OrgUnitLevel("HLEL4kj45", "");
    }
}

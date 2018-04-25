package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

public class OrgUnitShould {

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void create_org_unit_with_mandatory_fields(){
        OrgUnit orgUnit = new OrgUnit("FKFLLFR", "Org unit name", "LELELL");

        assertThat(orgUnit, is(notNullValue()));
    }

    @Test
    public void throw_illegalException_if_uid_idnull(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new OrgUnit(null, "Org unit name", "LELELL");
    }

    @Test
    public void throw_illegalException_if_name_idnull(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new OrgUnit("FKFLLFR", null, "LELELL");
    }

    @Test
    public void throw_illegalException_if_org_unit_level_uid_idnull(){
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("orgUnitLevelUid is required");

        new OrgUnit("FKFLLFR", "Org unit name", null);;
    }

    @Test
    public void add_related_program_uids_to_existed_org_unit(){
        List<String> expectedRelatedProgramUids = Arrays.asList("EDERGEE", "edeiodi");

        OrgUnit orgUnit = new OrgUnit("FKFLLFR", "Org unit name", "LELELL");

        orgUnit.addRelatedPrograms(expectedRelatedProgramUids);

        assertThat(orgUnit.getRelatedPrograms(), is(expectedRelatedProgramUids));
    }
}

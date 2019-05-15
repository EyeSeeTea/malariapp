package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgUnitShould {

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();
    private final Map<String, Integer> productivityByProgram;

    public OrgUnitShould(){
        productivityByProgram = new HashMap<>();
        productivityByProgram.put("orgUnitUid", 0);
    }

    @Test
    public void create_org_unit_with_mandatory_fields() {
        OrgUnit orgUnit = new OrgUnit("FKFLLFR", "Org unit name", "LELELL", productivityByProgram);

        assertThat(orgUnit, is(notNullValue()));
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_null() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new OrgUnit(null, "Org unit name", "LELELL", productivityByProgram);
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_null() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new OrgUnit("FKFLLFR", null, "LELELL", productivityByProgram);
    }

    @Test
    public void throw_illegalArgumentException_if_org_unit_level_uid_is_null() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("orgUnitLevelUid is required");

        new OrgUnit("FKFLLFR", "Org unit name", null, productivityByProgram);
    }


    @Test
    public void throw_illegalArgumentException_if_productivity_by_org_unit_is_null() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("productivityByProgram is required");

        new OrgUnit("FKFLLFR", "Org unit name", "LELELL",null);
    }

    @Test
    public void throw_illegalArgumentException_if_uid_is_empty() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("uid is required");

        new OrgUnit("", "Org unit name", "LELELL", productivityByProgram);
    }

    @Test
    public void throw_illegalArgumentException_if_name_is_empty() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("name is required");

        new OrgUnit("FKFLLFR", "", "LELELL", productivityByProgram);
    }

    @Test
    public void throw_illegalArgumentExceptionn_if_org_unit_level_uid_is_empty() {
        mExpectedException.expect(IllegalArgumentException.class);
        mExpectedException.expectMessage("orgUnitLevelUid is required");

        new OrgUnit("FKFLLFR", "Org unit name", "", productivityByProgram);
        ;
    }

    @Test
    public void add_related_program_uids_to_existed_org_unit() {
        List<String> expectedRelatedProgramUids = Arrays.asList("EDERGEE", "edeiodi");

        OrgUnit orgUnit = new OrgUnit("FKFLLFR", "Org unit name", "LELELL", productivityByProgram);

        orgUnit.addRelatedPrograms(expectedRelatedProgramUids);

        assertThat(orgUnit.getRelatedPrograms(), is(expectedRelatedProgramUids));
    }
}
package org.eyeseetea.malariacare.domain.entity;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SurveyShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_survey_with_mandatory_fields(){
        Survey survey = new Survey("UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID");
        Assert.assertNotNull(survey);
    }

    @Test
    public void create_empty_survey(){
        Survey survey = Survey.createEmptySurvey("UID", "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID");
        Assert.assertNotNull(survey);
        Assert.assertTrue(survey.getStatus().equals(Survey.Status.IN_PROGRESS));
        Assert.assertNotNull(survey.getCreationDate());
    }

    @Test
    public void throw_exception_when_create_survey_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey uid is required");
        new Survey(null, "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID");
    }

    @Test
    public void throw_exception_when_create_survey_with_null_program_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey programUId is required");
        new Survey("UID", null, "ORG_UNIT_UID", "USER_UID");
    }

    @Test
    public void throw_exception_when_create_survey_with_null_org_unit_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey orgUnitUId is required");
        new Survey("UID", "PROGRAM_UID", null, "USER_UID");
    }

    @Test
    public void throw_exception_when_create_survey_with_null_user_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey userUId is required");
        new Survey("UID", "PROGRAM_UID", "ORG_UNIT_UID", null);
    }
}

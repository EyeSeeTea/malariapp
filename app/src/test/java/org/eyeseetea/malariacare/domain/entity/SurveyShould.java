package org.eyeseetea.malariacare.domain.entity;


import org.eyeseetea.malariacare.utils.Constants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SurveyShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throw_exception_if_program_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey program UId is required");

        Survey survey = Survey.builder(Constants.SURVEY_IN_PROGRESS,null,
                "orgUnitUId", "userUId").build();
    }
    @Test
    public void throw_exception_if_orgunit_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey orgUnit UId is required");

        Survey survey = Survey.builder(Constants.SURVEY_IN_PROGRESS,"programUId",
                null, "userUId").build();
    }
    @Test
    public void throw_exception_if_user_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey User UId is required");

        Survey survey = Survey.builder(Constants.SURVEY_IN_PROGRESS,"programUId",
                "orgUnitUId", null).build();
    }
}

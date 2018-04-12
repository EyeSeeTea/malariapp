package org.eyeseetea.malariacare.domain.entity;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SurveyShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throw_exception_if_new_survey_status_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey status is required");

        Survey.buildNewSurvey(null,"programUId",
                "orgUnitUId", "userUId").build();
    }

    @Test
    public void throw_exception_if_new_survey_program_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey program UId is required");

        Survey.buildNewSurvey(Survey.Status.IN_PROGRESS,null,
                "orgUnitUId", "userUId").build();
    }

    @Test
    public void throw_exception_if_new_survey_orgunit_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey orgUnit UId is required");

        Survey.buildNewSurvey(Survey.Status.IN_PROGRESS,"programUId",
                null, "userUId").build();
    }

    @Test
    public void throw_exception_if_new_survey_user_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey User UId is required");

        Survey.buildNewSurvey(Survey.Status.IN_PROGRESS,"programUId",
                "orgUnitUId", null).build();
    }

    @Test
    public void throw_exception_if_new_survey_creationDate_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey creation date is required");

        Survey.buildNewSurvey(Survey.Status.IN_PROGRESS,"programUId",
                "orgUnitUId", "userUId").build();
    }
}

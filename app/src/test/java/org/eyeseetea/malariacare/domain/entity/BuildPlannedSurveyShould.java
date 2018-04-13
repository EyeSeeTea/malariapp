package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.entity.Survey.ORG_UNIT_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.PROGRAM_REQUIRED;
import static org.eyeseetea.malariacare.domain.entity.Survey.USER_REQUIRED;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BuildPlannedSurveyShould {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void return_mandatory_values_for_each_getter_after_planned_survey_is_created() throws Exception {
        Survey survey = Survey.buildPlannedSurvey("programUId",
                "orgUnitUId", "userUId").build();
        Assert.assertTrue(survey.programUId().equals("programUId"));
        Assert.assertTrue(survey.orgUnitUId().equals("orgUnitUId"));
        Assert.assertTrue(survey.userUId().equals("userUId"));
        Assert.assertTrue(survey.status().equals(Survey.Status.PLANNED));
    }
    @Test
    public void throw_exception_if_planned_survey_program_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(PROGRAM_REQUIRED);

        Survey.buildNewSurvey(null,
                "orgUnitUId", "userUId").build();
    }

    @Test
    public void throw_exception_if_planned_survey_orgunit_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(ORG_UNIT_REQUIRED);

        Survey.buildNewSurvey("programUId",
                null, "userUId").build();
    }

    @Test
    public void throw_exception_if_planned_survey_user_uid_is_not_provided() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(USER_REQUIRED);

        Survey.buildNewSurvey("programUId",
                "orgUnitUId", null).build();
    }
}

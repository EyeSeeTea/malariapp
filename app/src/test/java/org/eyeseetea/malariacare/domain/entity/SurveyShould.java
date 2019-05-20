package org.eyeseetea.malariacare.domain.entity;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurveyShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_survey_with_mandatory_fields(){
        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);

        Assert.assertNotNull(survey);
        Assert.assertTrue(survey.getUId().equals("UID"));
        Assert.assertTrue(survey.getProgramUId().equals("PROGRAM_UID"));
        Assert.assertTrue(survey.getOrgUnitUId().equals("ORG_UNIT_UID"));
        Assert.assertTrue(survey.getUserUId().equals("USER_UID"));
    }

    @Test
    public void create_empty_survey(){
        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);

        Assert.assertNotNull(survey);
        Assert.assertNotNull(survey.getCreationDate());
        Assert.assertTrue(survey.getStatus().equals(SurveyStatus.IN_PROGRESS));
    }

    @Test
    public void throw_exception_when_create_survey_with_null_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey uid is required");
        Survey.createEmptySurvey(null, "PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);
    }

    @Test
    public void create_pulled_survey(){
        Date creationDate = new Date();
        Date uploadDate = new Date();
        Date scheduledDate = new Date();
        Date completionDate = new Date();
        List<QuestionValue> values = new ArrayList<>();
        Score score = new Score("ScoreUId", 100.0f);
        values.add(QuestionValue.createSimpleValue("UId", "value"));
        values.add(QuestionValue.createOptionValue("UId2", "optionUId", "value2"));

        Survey survey = Survey.createStoredSurvey( SurveyStatus.SENT,"UID", "PROGRAM_UID", "ORG_UNIT_UID",
                "USER_UID", creationDate, uploadDate, scheduledDate, completionDate, values, score,0, CompetencyScoreClassification.COMPETENT);

        Assert.assertNotNull(survey);
        Assert.assertTrue(survey.getStatus().equals(SurveyStatus.SENT));
        Assert.assertTrue(survey.getCreationDate().equals(creationDate));
        Assert.assertTrue(survey.getCompletionDate().equals(completionDate));
        Assert.assertTrue(survey.getScheduledDate().equals(scheduledDate));
        Assert.assertTrue(survey.getUploadDate().equals(uploadDate));
        Assert.assertTrue(survey.getValues().equals(values));
        Assert.assertTrue(survey.getScore().equals(score));
    }

    @Test
    public void throw_exception_when_create_survey_with_null_program_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey programUId is required");

        Survey.createEmptySurvey("UID", null, "ORG_UNIT_UID", "USER_UID", 0);
    }

    @Test
    public void throw_exception_when_create_survey_with_null_org_unit_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey orgUnitUId is required");

        Survey.createEmptySurvey("UID", "PROGRAM_UID", null, "USER_UID", 0);
    }

    @Test
    public void throw_exception_when_create_survey_with_null_user_uid(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Survey userUId is required");

        Survey.createEmptySurvey("UID", "PROGRAM_UID", "ORG_UNIT_UID", null, 0);
    }
}
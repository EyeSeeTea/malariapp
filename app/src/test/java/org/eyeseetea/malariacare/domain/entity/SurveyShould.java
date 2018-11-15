package org.eyeseetea.malariacare.domain.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eyeseetea.malariacare.domain.exception.CalculateNextScheduledDateException;
import org.joda.time.DateTime;
import org.joda.time.Months;
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
                "USER_UID", creationDate, uploadDate, scheduledDate, completionDate, values, score,0);

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

    @Test
    public void throw_exception_when_calculate_without_completion_date()
            throws CalculateNextScheduledDateException {
        thrown.expect(CalculateNextScheduledDateException.class);

        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);
        survey.calculateNextScheduledDate();
    }

    @Test
    public void throw_exception_when_calculate_without_score()
            throws CalculateNextScheduledDateException {
        thrown.expect(CalculateNextScheduledDateException.class);

        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);
        survey.assignCompletionDate(new Date());
        survey.calculateNextScheduledDate();
    }

    @Test
    public void return_next_schedule_date_with_more_6_months_than_completion_if_score_type_is_a()
            throws CalculateNextScheduledDateException {
        Date date = new Date();

        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);
        survey.assignScore(new Score("sesese",99f));
        survey.assignCompletionDate(date);

        DateTime completionDateTime = new DateTime(survey.getCompletionDate().getTime());
        DateTime nextScheduledDateTime = new DateTime( survey.calculateNextScheduledDate().getTime());

        Months differenceInMonths = Months.monthsBetween(completionDateTime,nextScheduledDateTime);

        assertThat(differenceInMonths.getMonths(), is(6));
    }

    @Test
    public void return_next_schedule_date_with_more_4_months_than_completion_if_score_type_is_bc_and_is_low_productivity()
            throws CalculateNextScheduledDateException {
        Date date = new Date();

        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 0);
        survey.assignScore(new Score("sesese",30f));
        survey.assignCompletionDate(date);

        DateTime completionDateTime = new DateTime(survey.getCompletionDate().getTime());
        DateTime nextScheduledDateTime = new DateTime( survey.calculateNextScheduledDate().getTime());

        Months differenceInMonths = Months.monthsBetween(completionDateTime,nextScheduledDateTime);

        assertThat(differenceInMonths.getMonths(), is(4));
    }

    @Test
    public void return_next_schedule_date_with_more_2_months_than_completion_if_score_type_is_bc_and_is_not_low_productivity()
            throws CalculateNextScheduledDateException {
        Date date = new Date();

        Survey survey = Survey.createEmptySurvey("UID","PROGRAM_UID", "ORG_UNIT_UID", "USER_UID", 9);
        survey.assignScore(new Score("sesese",30f));
        survey.assignCompletionDate(date);

        DateTime completionDateTime = new DateTime(survey.getCompletionDate().getTime());
        DateTime nextScheduledDateTime = new DateTime( survey.calculateNextScheduledDate().getTime());

        Months differenceInMonths = Months.monthsBetween(completionDateTime,nextScheduledDateTime);

        assertThat(differenceInMonths.getMonths(), is(2));
    }

    @Test
    public void create_quarantine_survey(){
        Date creationDate = new Date();
        Date completionDate = new Date();

        Survey survey = Survey.createQuarantineSurvey("UID", "PROGRAM_UID", "ORG_UNIT_UID",
                "USER_UID", creationDate, completionDate, 0);

        Assert.assertNotNull(survey);
        Assert.assertTrue(survey.getStatus().equals(SurveyStatus.QUARANTINE));
        Assert.assertTrue(survey.getCreationDate().equals(creationDate));
        Assert.assertTrue(survey.getCompletionDate().equals(completionDate));
    }
}

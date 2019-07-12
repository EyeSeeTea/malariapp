package org.eyeseetea.malariacare.domain.service;

import static org.junit.Assert.assertEquals;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Calendar;
import java.util.Date;

public class SurveyNextScheduleDomainServiceShould {
    private static String DELTA_MATRIX = "6,5;4,3;2,1";

    NextScheduleDateConfiguration nextScheduleDateConfiguration =
            new NextScheduleDateConfiguration(DELTA_MATRIX);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void throw_exception_if_next_schedule_configuration_is_null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("nextScheduleDateConfiguration is required");

        new SurveyNextScheduleDomainService().calculate(
                null,
                new Date(),
                CompetencyScoreClassification.COMPETENT,
                true);
    }


    @Test
    public void throw_exception_if_previous_survey_date_is_null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("previousSurveyDate is required");

        new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                null,
                CompetencyScoreClassification.COMPETENT,
                true);
    }


    @Test
    public void throw_exception_if_previous_survey_competency_is_null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("previousSurveyCompetency is required");

        new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                new Date(),
                null,
                true);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_competent_low_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.COMPETENT;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 6);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                true);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_competent_high_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.COMPETENT;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 5);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                false);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_competent_needs_improvement_low_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 4);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                true);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_competent_needs_improvement_high_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 3);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                false);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_not_competent_low_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.NOT_COMPETENT;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 2);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                true);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_not_competent_high_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.NOT_COMPETENT;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 1);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                false);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_not_available_low_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.NOT_AVAILABLE;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 2);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                true);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    @Test
    public void should_return_expected_next_schedule_date_by_not_available_high_productivity() {
        CompetencyScoreClassification previousSurveyCompetency =
                CompetencyScoreClassification.NOT_AVAILABLE;
        Date previousSurveyDate = new Date();
        Date expectedNextScheduleDate = getInXMonths(previousSurveyDate, 1);

        Date nextScheduleDate = new SurveyNextScheduleDomainService().calculate(
                nextScheduleDateConfiguration,
                previousSurveyDate,
                previousSurveyCompetency,
                false);

        assertEquals(expectedNextScheduleDate, nextScheduleDate);
    }

    private Date getInXMonths(Date date, int numMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, numMonths);
        return calendar.getTime();
    }
}

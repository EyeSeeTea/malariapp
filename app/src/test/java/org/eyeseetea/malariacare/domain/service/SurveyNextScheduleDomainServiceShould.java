package org.eyeseetea.malariacare.domain.service;

import static org.junit.Assert.assertEquals;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class SurveyNextScheduleDomainServiceShould {
    private static String DELTA_MATRIX = "6,5;4,3;2,1";

    NextScheduleDateConfiguration nextScheduleDateConfiguration =
            new NextScheduleDateConfiguration(DELTA_MATRIX);

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

package org.eyeseetea.malariacare.domain.service;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;

import java.util.Calendar;
import java.util.Date;

public class SurveyNextScheduleDomainService {
    public Date calculate(
            NextScheduleDateConfiguration nextScheduleDateConfiguration, Date previousSurveyCompleteDate,
            CompetencyScoreClassification previousSurveyCompetency,
            boolean previousSurveyIsLowProductivity){

        Date nextScheduleDate;

        if (previousSurveyCompetency == CompetencyScoreClassification.COMPETENT) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getCompetentLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getCompetentHighProductivityMonths());
            }
        } else if (previousSurveyCompetency
                == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getCompetentNeedsImprovementLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getCompetentNeedsImprovementHighProductivityMonths());
            }
        } else if (previousSurveyCompetency
                == CompetencyScoreClassification.NOT_COMPETENT) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getNotCompetentLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getNotCompetentHighProductivityMonths());
            }
        } else {
            // NA
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getNotCompetentLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyCompleteDate,
                        nextScheduleDateConfiguration.getNotCompetentHighProductivityMonths());
            }
        }

        return nextScheduleDate;
    }

    private Date getInXMonths(Date date, int numMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, numMonths);
        return calendar.getTime();
    }
}

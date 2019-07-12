package org.eyeseetea.malariacare.domain.service;

import org.eyeseetea.malariacare.domain.common.RequiredChecker;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;

import java.util.Calendar;
import java.util.Date;

public class SurveyNextScheduleDomainService {
    private RequiredChecker RequiredChecker;

    public Date calculate(
            NextScheduleDateConfiguration nextScheduleDateConfiguration,
            Date previousSurveyDate,
            CompetencyScoreClassification previousSurveyCompetency,
            boolean previousSurveyIsLowProductivity) {

        RequiredChecker.required(nextScheduleDateConfiguration,
                "nextScheduleDateConfiguration is required");
        RequiredChecker.required(previousSurveyDate,
                "previousSurveyDate is required");
        RequiredChecker.required(previousSurveyCompetency,
                "previousSurveyCompetency is required");

        Date nextScheduleDate;

        if (previousSurveyCompetency == CompetencyScoreClassification.COMPETENT) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentHighProductivityMonths());
            }
        } else if (previousSurveyCompetency
                == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentNeedsImprovementLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentNeedsImprovementHighProductivityMonths());
            }
        } else if (previousSurveyCompetency
                == CompetencyScoreClassification.NOT_COMPETENT) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getNotCompetentLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getNotCompetentHighProductivityMonths());
            }
        } else {
            // NA
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getNotCompetentLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
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

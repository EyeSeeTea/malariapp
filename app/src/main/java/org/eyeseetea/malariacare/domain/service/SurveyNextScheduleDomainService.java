package org.eyeseetea.malariacare.domain.service;

import org.eyeseetea.malariacare.domain.common.RequiredChecker;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;

import java.util.Calendar;
import java.util.Date;

public class SurveyNextScheduleDomainService {
    public Date calculate(
            NextScheduleDateConfiguration nextScheduleDateConfiguration,
            Date previousSurveyDate,
            CompetencyScoreClassification previousSurveyCompetency,
            boolean previousSurveyIsLowProductivity,
            float previousSurveyScore,
            ServerClassification serverClassification) {

        RequiredChecker.required(nextScheduleDateConfiguration,
                "nextScheduleDateConfiguration is required");
        RequiredChecker.required(previousSurveyDate,
                "previousSurveyDate is required");
        RequiredChecker.required(previousSurveyCompetency,
                "previousSurveyCompetency is required");
        RequiredChecker.required(serverClassification,
                "serverClassification is required");

        Date nextScheduleDate;

        ScoreType scoreType = new ScoreType(previousSurveyScore);

        if (isCompetentOrScoreAType(previousSurveyCompetency, scoreType, serverClassification)) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentOrALowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentOrAHighProductivityMonths());
            }
        } else if (isCompetentNeedImprovementOrScoreBType(previousSurveyCompetency, scoreType, serverClassification)) {
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentNeedsImprovementOrBLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getCompetentNeedsImprovementOrBHighProductivityMonths());
            }
        } else {
            // Competencies NA or NOT Competent | Scoring Type C
            if (previousSurveyIsLowProductivity) {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getNotCompetentOrCLowProductivityMonths());
            } else {
                nextScheduleDate = getInXMonths(previousSurveyDate,
                        nextScheduleDateConfiguration.getNotCompetentOrCHighProductivityMonths());
            }
        }

        return nextScheduleDate;
    }

    private boolean isCompetentOrScoreAType(
            CompetencyScoreClassification previousSurveyCompetency,
            ScoreType previousSurveyScoreType,
            ServerClassification serverClassification) {
        return (serverClassification == ServerClassification.COMPETENCIES &&
                previousSurveyCompetency == CompetencyScoreClassification.COMPETENT) ||
                (serverClassification == ServerClassification.SCORING
                        && previousSurveyScoreType.isTypeA());
    }

    private boolean isCompetentNeedImprovementOrScoreBType(
            CompetencyScoreClassification previousSurveyCompetency,
            ScoreType previousSurveyScoreType,
            ServerClassification serverClassification) {
        return (serverClassification == ServerClassification.COMPETENCIES &&
                previousSurveyCompetency
                        == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT) ||
                (serverClassification == ServerClassification.SCORING
                        && previousSurveyScoreType.isTypeB());
    }

    private Date getInXMonths(Date date, int numMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, numMonths);
        return calendar.getTime();
    }
}

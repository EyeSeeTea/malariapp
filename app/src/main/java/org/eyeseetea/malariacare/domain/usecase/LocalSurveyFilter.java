package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.usecase.pull.PullSurveyFilter;

import java.util.Date;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class LocalSurveyFilter {

    private Date startDate;
    private Date endDate;
    private String programUId;
    private String orgUnitUId;
    private SurveyStatus surveyStatus;

    public static LocalSurveyFilter createGetQuarantineSurveys(String programUId, String orgUnitUId){
        return LocalSurveyFilter.Builder.create()
                .withOrgUnitUId(required(orgUnitUId, "orgUnitUId is required"))
                .withProgramUId(required(programUId, "programUId is required"))
                .withSurveyStatus(SurveyStatus.QUARANTINE).build();
    }

    public static LocalSurveyFilter createCheckQuarantineOnServerFilter(Date startDate, Date endDate, String programUId, String orgUnitUId){
        return LocalSurveyFilter.Builder.create()
                .withStartDate(required(startDate, "startDate is required"))
                .withEndDate(required(endDate, "endDate is required"))
                .withOrgUnitUId(required(orgUnitUId, "orgUnitUId is required"))
                .withProgramUId(required(programUId, "programUId is required"))
                .withSurveyStatus(SurveyStatus.QUARANTINE).build();
    }


    public LocalSurveyFilter(Date startDate, Date endDate, String programUId, String orgUnitUId, SurveyStatus surveyStatus) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.programUId = programUId;
        this.orgUnitUId = orgUnitUId;
        this.surveyStatus = surveyStatus;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getProgramUId() {
        return programUId;
    }

    public String getOrgUnitUId() {
        return orgUnitUId;
    }

    public boolean isQuarantineSurvey() {
        return surveyStatus.equals( SurveyStatus.QUARANTINE);
    }

    public SurveyStatus getSurveyStatus() {
        return surveyStatus;
    }

    public static class Builder {
        private Date startDate = null;
        private Date endDate = null;
        private Integer maxSize = 0;
        private String orgUnitUId;
        private String programUId;
        private SurveyStatus surveyStatus;

        private Builder() {
        }

        public static LocalSurveyFilter.Builder create() {
            return new LocalSurveyFilter.Builder();
        }

        public LocalSurveyFilter.Builder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public LocalSurveyFilter.Builder withEndDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }

        public LocalSurveyFilter.Builder withMaxSize(Integer maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public LocalSurveyFilter.Builder withProgramUId(String programUId) {
            this.programUId = programUId;
            return this;
        }

        public LocalSurveyFilter.Builder withOrgUnitUId(String orgUnitUId) {
            this.orgUnitUId = orgUnitUId;
            return this;
        }

        public LocalSurveyFilter.Builder withSurveyStatus(SurveyStatus surveyStatus) {
            this.surveyStatus = surveyStatus;
            return this;
        }

        public LocalSurveyFilter build() {
            return new LocalSurveyFilter(startDate, endDate, orgUnitUId, programUId, surveyStatus);
        }
    }
}

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.List;

public class SurveyFilter {

    private List<String> uids;
    private SurveyStatus surveyStatus;
    private ReadPolicy readPolicy;

    public static SurveyFilter getQuarantineSurveys(){
        return SurveyFilter.Builder.create()
                .withSurveyStatus(SurveyStatus.QUARANTINE)
                .withReadPolicy(ReadPolicy.CACHE).build();
    }

    public static SurveyFilter getSurveysUidsOnServer(List<String> uids){
        return SurveyFilter.Builder.create()
                .withSurveyStatus(SurveyStatus.QUARANTINE)
                .withSurveyUIds(uids)
                .withReadPolicy(ReadPolicy.NETWORK_NO_CACHE).build();
    }


    public SurveyFilter(List<String> uids, SurveyStatus surveyStatus, ReadPolicy readPolicy) {
        this.uids = uids;
        this.surveyStatus = surveyStatus;
        this.readPolicy = readPolicy;
    }

    public List<String> getUids() {
        return uids;
    }

    public ReadPolicy getReadPolicy() {
        return readPolicy;
    }

    public boolean isQuarantineSurvey() {
        return surveyStatus.equals( SurveyStatus.QUARANTINE);
    }

    public SurveyStatus getSurveyStatus() {
        return surveyStatus;
    }

    public static class Builder {
        private List<String> uids;
        private SurveyStatus surveyStatus;
        private ReadPolicy readPolicy;

        private Builder() {
        }

        public static SurveyFilter.Builder create() {
            return new SurveyFilter.Builder();
        }

        public SurveyFilter.Builder withSurveyStatus(SurveyStatus surveyStatus) {
            this.surveyStatus = surveyStatus;
            return this;
        }

        public SurveyFilter.Builder withSurveyUIds(List<String> uids) {
            this.uids = uids;
            return this;
        }

        public SurveyFilter.Builder withReadPolicy(ReadPolicy readPolicy) {
            this.readPolicy = readPolicy;
            return this;
        }

        public SurveyFilter build() {
            return new SurveyFilter(uids, surveyStatus, readPolicy);
        }
    }
}

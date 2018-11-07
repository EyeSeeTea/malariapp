package org.eyeseetea.malariacare.domain.entity;

public class SurveyFilter {

    private SurveyStatus surveyStatus;
    private String orgUnitUId;
    private String programUId;
    private boolean isLastSurvey;

    public SurveyFilter(SurveyStatus surveyStatus) {
        this.surveyStatus = surveyStatus;
    }

    public SurveyFilter(SurveyStatus surveyStatus, String orgUnitUId, String programUId, boolean isLastSurvey) {
        this.surveyStatus = surveyStatus;
        this.orgUnitUId = orgUnitUId;
        this.programUId = programUId;
        this.isLastSurvey = isLastSurvey;
    }

    public SurveyStatus getSurveyStatus() {
        return surveyStatus;
    }

    public String getOrgUnitUId() {
        return orgUnitUId;
    }

    public String getProgramUId() {
        return programUId;
    }

    public boolean isLastSurvey() {
        return isLastSurvey;
    }

    public static SurveyFilter createLastSurveyFilter(){
        return new SurveyFilter(null, null, null, true);
    }

    public static SurveyFilter createLastSurveyByOrgUnitAndProgramFilter(String orgUnitUId, String programUId){
        return new SurveyFilter(null, orgUnitUId, programUId, true);
    }
}

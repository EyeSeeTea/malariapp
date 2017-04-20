package org.eyeseetea.malariacare.domain.entity;

import java.util.List;

public class PushReport {

    private String eventUid;

    private Status status;

    private String description;

    private PushedValuesCount mPushedValuesCount;

    private String reference;

    private String href;

    private List<SurveyConflict> mSurveyConflicts;

    public PushReport() {
        // explicit empty constructor
    }


    public Status getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public PushedValuesCount getPushedValues() {
        return mPushedValuesCount;
    }

    public String getReference() {
        return reference;
    }

    public String getHref() {
        return href;
    }

    public List<SurveyConflict> getSurveyConflicts() {
        return mSurveyConflicts;
    }

    public enum Status {
        SUCCESS, OK, ERROR
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPushedValuesCount(PushedValuesCount pushedValuesCount) {
        this.mPushedValuesCount = pushedValuesCount;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setSurveyConflicts(List<SurveyConflict> surveyConflicts) {
        this.mSurveyConflicts = surveyConflicts;
    }

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }
}

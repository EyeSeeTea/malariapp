package org.eyeseetea.malariacare.domain.entity.pushsummary;

import java.util.List;

public class PushReport {

    private String eventUid;

    private Status status;

    private String description;

    private PushedValuesCount mPushedValuesCount;

    private String reference;

    private String href;

    private List<PushConflict> mPushConflicts;

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

    public List<PushConflict> getPushConflicts() {
        return mPushConflicts;
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

    public void setPushConflicts(List<PushConflict> pushConflicts) {
        this.mPushConflicts = pushConflicts;
    }

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }
}

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

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PushedValuesCount getPushedValues() {
        return mPushedValuesCount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<PushConflict> getPushConflicts() {
        return mPushConflicts;
    }

    public void setPushConflicts(List<PushConflict> pushConflicts) {
        this.mPushConflicts = pushConflicts;
    }

    public void setPushedValuesCount(PushedValuesCount pushedValuesCount) {
        this.mPushedValuesCount = pushedValuesCount;
    }

    public String getEventUid() {
        return eventUid;
    }

    public void setEventUid(String eventUid) {
        this.eventUid = eventUid;
    }

    public enum Status {
        SUCCESS, OK, ERROR
    }

    /**
     * Checks whether the PushReport contains errors or has been successful.
     * An import with 0 importedItems is an error too.
     */
    public boolean hasPushErrors() {

        if (this.getPushedValues() == null) {
            return true;
        }
        if (this.getStatus() == null) {
            return true;
        }
        if (!this.getStatus().equals(PushReport.Status.SUCCESS)) {
            return true;
        }
        return this.getPushedValues().getImported() == 0;
    }
}

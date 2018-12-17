package org.eyeseetea.malariacare.domain.entity.pushsummary;


import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

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

    public PushReport(String eventUid,
            Status status, String description,
            PushedValuesCount pushedValuesCount, String reference, String href,
            List<PushConflict> pushConflicts) {
        this.eventUid = required(eventUid,"EventUid is required");
        this.status = required(status,"Status is required");
        this.description = description;
        mPushedValuesCount = pushedValuesCount;
        this.reference = reference;
        this.href = href;
        mPushConflicts = pushConflicts;
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

    public String getEventUid() {
        return eventUid;
    }

    public enum Status {
        SUCCESS, OK, ERROR
    }

    /**
     * Checks whether the PushReport contains errors or has been successful.
     * An import with 0 importedItems is an error too.
     */
    public boolean hasPushErrors() {
        return hasPushErrors(false);
    }
    /**
     * Checks whether the PushReport contains errors or has been successful.
     * An import with 0 importedItems is an error only if is required.
     */
    public boolean hasPushErrors(Boolean emptyImportAllowed) {

        if (this.getPushedValues() == null) {
            return true;
        }
        if (this.getStatus() == null) {
            return true;
        }
        if (!this.getStatus().equals(PushReport.Status.SUCCESS)) {
            return true;
        }
        if(emptyImportAllowed){
            return false;
        }
        return this.getPushedValues().getImported() == 0;
    }
}
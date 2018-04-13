package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.Date;
import java.util.List;

@AutoValue
public abstract class Survey {
    protected static final String STATUS_REQUIRED = "Survey status is required";
    protected static final String PROGRAM_REQUIRED = "Survey program uid is required";
    protected static final String ORG_UNIT_REQUIRED = "Survey organisation unit uid is required";
    protected static final String USER_REQUIRED = "Survey User UId is required";
    protected static final String CREATION_DATE_REQUIRED = "Survey created date is required";
    protected static final String COMPLETION_DATE_REQUIRED = "Survey completion date is required";
    protected static final String UPLOAD_DATE_REQUIRED = "Survey upload date is required";
    protected static final String SCHEDULED_DATE_REQUIRED = "Survey scheduled date is required";
    protected static final String EVENT_UID_REQUIRED = "Survey event uid required is required";
    protected static final String VALUE_UIDS_REQUIRED = "Survey value uid is required";

    public enum Status {
        PLANNED, IN_PROGRESS, COMPLETED, SENT, CONFLICT, QUARANTINE, SENDING
    }

    @Nullable
    public abstract Long id();
    //Survey default value
    public abstract Status status();

    public abstract String programUId();

    public abstract String orgUnitUId();

    @Nullable
    public abstract String userUId();

    @Nullable
    public abstract Date creationDate();

    @Nullable
    public abstract Date completionDate();

    @Nullable
    public abstract Date uploadDate();

    @Nullable
    public abstract Date scheduledDate();

    @Nullable
    public abstract String referencedEventUId();

    /**
     * List of value ids for this survey
     */
    @Nullable
    public abstract List<Integer> valueIds();

    /**
     * List of historic previous schedules
     */
    @Nullable
    public abstract List<String> scheduledSurveyUids();

    @Nullable
    public abstract SurveyAnsweredRatio surveyAnsweredRatio();

    @Nullable
    public abstract Float mainScore();

    @Nullable
    public abstract Boolean hasMainScore();

    /**
     * Expected productivity for this survey according to its orgunit + program.
     * Just a cached value from orgunitprogramproductivity
     */
    @Nullable
    public abstract Integer productivity();

    /**
     * Returns if this survey has low productivity or not.
     * [0..4]: Low
     * [5..): Not Low
     */
    public boolean isLowProductivity() {
        return productivity() < 5;
    }

    static Builder buildNewSurvey(String programUId, String orgUnitUId,
            String userUId) {
        required(programUId, PROGRAM_REQUIRED);
        required(orgUnitUId, ORG_UNIT_REQUIRED);
        required(userUId, USER_REQUIRED);
        Date creationDate = new Date();

        return new AutoValue_Survey.Builder().setStatus(Status.IN_PROGRESS).setProgramUId(programUId)
                .setOrgUnitUId(orgUnitUId).setCreationDate(creationDate)
                .setUserUId(userUId);
    }

    static Builder buildPulledSurvey(String programUId,
            String orgUnitUId, Date creationDate,
            Date completionDate, Date uploadDate,
            Date scheduledDate, String eventUId, List<Integer> valueUIds) {
        required(programUId, PROGRAM_REQUIRED);
        required(orgUnitUId, ORG_UNIT_REQUIRED);
        required(creationDate, CREATION_DATE_REQUIRED);
        required(completionDate, COMPLETION_DATE_REQUIRED);
        required(uploadDate, UPLOAD_DATE_REQUIRED);
        required(scheduledDate, SCHEDULED_DATE_REQUIRED);
        required(eventUId, EVENT_UID_REQUIRED);
        required(valueUIds, VALUE_UIDS_REQUIRED);

        return new AutoValue_Survey.Builder().setStatus(Status.SENT).setProgramUId(programUId)
                .setOrgUnitUId(orgUnitUId).setCreationDate(creationDate).setCompletionDate(completionDate)
                .setUploadDate(uploadDate).setScheduledDate(scheduledDate).setReferencedEventUId(eventUId)
                .setValueIds(valueUIds);
    }

    static Builder buildPlannedSurvey(String programUId,
                                     String orgUnitUId, String userUId) {
        required(programUId, PROGRAM_REQUIRED);
        required(orgUnitUId, ORG_UNIT_REQUIRED);
        required(userUId, USER_REQUIRED);

        return new AutoValue_Survey.Builder().setStatus(Status.PLANNED).setProgramUId(programUId)
                .setOrgUnitUId(orgUnitUId).setUserUId(userUId);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setId(Long id);

        abstract Builder setStatus(Status status);

        abstract Builder setProgramUId(String programUId);

        abstract Builder setOrgUnitUId(String orgUnitUId);

        abstract Builder setUserUId(String userUId);

        abstract Builder setCreationDate(Date creationDate);

        abstract Builder setCompletionDate(Date completionDate);

        abstract Builder setUploadDate(Date uploadDate);

        abstract Builder setScheduledDate(Date scheduledDate);

        abstract Builder setReferencedEventUId(String referencedEventUId);

        abstract Builder setValueIds(List<Integer> valueIds);

        abstract Builder setScheduledSurveyUids(List<String> scheduledSurveyUids);

        abstract Builder setSurveyAnsweredRatio(
                SurveyAnsweredRatio surveyAnsweredRatio);

        abstract Builder setMainScore(Float mainScore);

        abstract Builder setHasMainScore(Boolean hasMainScore);

        public abstract Builder setProductivity(Integer productivity);

        abstract Survey build();
    }
}

package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import com.google.auto.value.AutoValue;

import java.util.Date;
import java.util.List;

@AutoValue
public abstract class Survey {
    public enum Status {
        PLANNED, IN_PROGRESS, COMPLETED, SENT, CONFLICT, QUARANTINE, SENDING
    }

    public abstract long id();
    //Survey default value
    public abstract Status status();

    public abstract String programUId();

    public abstract String orgUnitUId();

    public abstract String userUId();

    public abstract Date creationDate();

    public abstract Date completionDate();

    public abstract Date uploadDate();

    public abstract Date scheduledDate();

    public abstract String referencedEventUId();

    /**
     * List of value ids for this survey
     */
    public abstract List<Integer> valueIds();

    /**
     * List of historic previous schedules
     */
    public abstract List<String> scheduledSurveyUids();

    public abstract SurveyAnsweredRatio surveyAnsweredRatio();

    public abstract Float mainScore();

    public abstract Boolean hasMainScore();

    /**
     * Expected productivity for this survey according to its orgunit + program.
     * Just a cached value from orgunitprogramproductivity
     */
    public abstract Integer productivity();

    static Builder buildNewSurvey(Status status, String programUId, String orgUnitUId,
            String userUId) {
        required(status, "Survey status is required");
        required(programUId, "Survey program UId is required");
        required(orgUnitUId, "Survey orgUnit UId is required");
        required(userUId, "Survey User UId is required");
        Date creationDate = new Date();

        return new AutoValue_Survey.Builder().setStatus(status).setProgramUId(programUId)
                .setOrgUnitUId(orgUnitUId).setCreationDate(creationDate)
                .setUserUId(userUId);
    }
    @AutoValue.Builder
    public abstract static class Builder {
        abstract Builder setId(long id);

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

package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Survey {

    public enum Status {
        PLANNED, IN_PROGRESS, COMPLETED, SENT, CONFLICT, QUARANTINE, SENDING
    }

    private final String uId;
    private final String programUId;
    private final String orgUnitUId;
    private Score score;
    private String userUId;
    private Date creationDate;
    private Date completionDate;
    private Date updateDate;
    private Date scheduledDate;
    private Status status;
    private List<SurveyValue> values;


    public Survey(String uId, String programUId, String orgUnitUId, String userUId) {
        this.uId=required(uId, "Survey uid is required");
        this.programUId=required(programUId, "Survey programUId is required");
        this.orgUnitUId=required(orgUnitUId, "Survey orgUnitUId is required");
        this.userUId=required(userUId, "Survey userUId is required");
        this.values = new ArrayList<>();
    }

    public static Survey createEmptySurvey(String uId, String programUId, String orgUnitUId,
            String userUId) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        survey.setStatus(Status.IN_PROGRESS);
        survey.setCreationDate(new Date());
        return survey;
    }

    public static Survey createPulledSurvey(String uId, String programUId, String orgUnitUId,
            String userUId, Date creationDate, Date updateDate, Date scheduledDate,
            Date completionDate, List<SurveyValue> values, Score score) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        survey.setStatus(Status.SENT);
        survey.setCreationDate(creationDate);
        survey.setUpdateDate(updateDate);
        survey.setScheduledDate(scheduledDate);
        survey.setCompletionDate(completionDate);
        survey.setValues(values);
        survey.setScore(score);
        return survey;
    }

    public List<SurveyValue> getValues() {
        return values;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void setUserUId(String uid) {
        userUId = uId;
    }

    private void setValues(List<SurveyValue> values) {
        this.values = values;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getUId() {
        return uId;
    }

    public String getProgramUId() {
        return programUId;
    }

    public String getOrgUnitUId() {
        return orgUnitUId;
    }

    public String getUserUId() {
        return userUId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Status getStatus() {
        return status;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }
}

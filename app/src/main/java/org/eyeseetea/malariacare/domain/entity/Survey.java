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
    private List<QuestionValue> values;

    private Survey(String uId, String programUId, String orgUnitUId, String userUId) {
        this.uId=required(uId, "Survey uid is required");
        this.programUId=required(programUId, "Survey programUId is required");
        this.orgUnitUId=required(orgUnitUId, "Survey orgUnitUId is required");
        this.userUId=required(userUId, "Survey userUId is required");
        this.values = new ArrayList<>();

        creationDate = new Date();
        status = Status.IN_PROGRESS;
    }

    public static Survey createEmptySurvey(String uId, String programUId, String orgUnitUId,
            String userUId) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        return survey;
    }

    public static Survey createExistedSurvey(String uId, String programUId, String orgUnitUId,
            String userUId, Date creationDate, Date updateDate, Date scheduledDate,
            Date completionDate, List<QuestionValue> values, Score score) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        survey.changeStatus(Status.SENT);
        survey.assignCreationDate(creationDate);
        survey.changeUpdateDate(updateDate);
        survey.changeScheduledDate(scheduledDate);
        survey.assignCompletionDate(completionDate);
        survey.addQuestionValues(values);
        survey.assignScore(score);
        return survey;
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

    public List<QuestionValue> getValues() {
        return new ArrayList<>(values);
    }

    public Score getScore() {
        return score;
    }

    public void assignScore(Score score) {
        this.score = score;
    }

    private void addQuestionValues(List<QuestionValue> values) {
        this.values.addAll(values);
    }

    public void assignCreationDate(Date date) {
        this.creationDate = date;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    public void assignCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void changeUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public void changeScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
}

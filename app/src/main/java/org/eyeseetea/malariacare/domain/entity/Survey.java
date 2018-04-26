package org.eyeseetea.malariacare.domain.entity;


import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.Date;

public class Survey {

    public enum Status {
        PLANNED, IN_PROGRESS, COMPLETED, SENT, CONFLICT, QUARANTINE, SENDING
    }

    private final String uId;
    private final String programUId;
    private final String orgUnitUId;
    private final String userUId;
    private Date creationDate;
    private Status status;

    public Survey(String uId, String programUId, String orgUnitUId, String userUId) {
        this.uId=required(uId, "Survey uid is required");
        this.programUId=required(programUId, "Survey programUId is required");
        this.orgUnitUId=required(orgUnitUId, "Survey orgUnitUId is required");
        this.userUId=required(userUId, "Survey userUId is required");
    }

    public static Survey createEmptySurvey(String uId, String programUId, String orgUnitUId,
            String userUId) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        survey.setStatus(Status.IN_PROGRESS);
        survey.setCreationDate(new Date());
        return survey;
    }

    private void setCreationDate(Date date) {
        this.creationDate = date;
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    public String getuId() {
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
}

package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

import org.eyeseetea.malariacare.data.database.model.ValueDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Survey {
    public static final int DEFAULT_PRODUCTIVITY = 0;
    private final String uId;
    private final String programUId;
    private final String orgUnitUId;
    private Score score;
    private String userUId;
    private Date creationDate;
    private Date completionDate;
    private Date uploadDate;
    private Date scheduledDate;
    private SurveyStatus status;
    private List<QuestionValue> values;
    private int productivity;
    private CompetencyScoreClassification competency;

    private Survey(String uId, String programUId, String orgUnitUId, String userUId,int productivity) {
        this.uId = required(uId, "Survey uid is required");
        this.programUId = required(programUId, "Survey programUId is required");
        this.orgUnitUId = required(orgUnitUId, "Survey orgUnitUId is required");
        this.userUId = required(userUId, "Survey userUId is required");
        this.values = new ArrayList<>();

        this.creationDate = new Date();
        this.status = SurveyStatus.IN_PROGRESS;
        this.productivity = productivity;
    }

    public static Survey createEmptySurvey(String uId, String programUId, String orgUnitUId,
            String userUId, int productivity) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId, productivity);
        return survey;
    }

    public static Survey createStoredSurvey(SurveyStatus status, String uId, String programUId,
            String orgUnitUId, String userUId, Date creationDate, Date uploadDate,
            Date scheduledDate, Date completionDate, List<QuestionValue> values, Score score,
            int productivity, CompetencyScoreClassification competency) {

        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId, productivity);
        survey.changeStatus(status);
        survey.assignCreationDate(creationDate);
        survey.assignUploadDate(uploadDate);
        survey.assignScheduledDate(scheduledDate);
        survey.assignCompletionDate(completionDate);
        survey.addQuestionValues(values);
        survey.assignScore(score);
        survey.assignCompetency(competency);
        return survey;
    }

    public boolean hasConflict(){
        for (QuestionValue value : values) {
            if (value.isConflict()) {
                return true;
            }
        }
        return false;
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

    public SurveyStatus getStatus() {
        return status;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public Date getUploadDate() {
        return uploadDate;
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

    public void changeStatus(SurveyStatus status) {
        this.status = status;
    }

    public void assignCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void assignUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void assignScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public CompetencyScoreClassification getCompetency() {
        return competency;
    }

    private void assignCompetency(CompetencyScoreClassification competency) {
        this.competency = competency;
    }

    public String getSurveyUid() {
        return getUId();
    }

    public int getProductivity() {
        return productivity;
    }

    private boolean isLowProductivity() {
        return getProductivity() < 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Survey survey = (Survey) o;

        if (!uId.equals(survey.uId)) return false;
        if (!programUId.equals(survey.programUId)) return false;
        if (!orgUnitUId.equals(survey.orgUnitUId)) return false;
        if (productivity != productivity) return false;
        if (score != null ? !score.equals(survey.score) : survey.score != null) return false;
        if (!userUId.equals(survey.userUId)) return false;
        if (!creationDate.equals(survey.creationDate)) return false;
        if (completionDate != null ? !completionDate.equals(survey.completionDate)
                : survey.completionDate != null) {
            return false;
        }
        if (uploadDate != null ? !uploadDate.equals(survey.uploadDate)
                : survey.uploadDate != null) {
            return false;
        }
        if (scheduledDate != null ? !scheduledDate.equals(survey.scheduledDate)
                : survey.scheduledDate != null) {
            return false;
        }
        if (status != survey.status) return false;
        return values.equals(survey.values);
    }

    @Override
    public int hashCode() {
        int result = uId.hashCode();
        result = 31 * result + programUId.hashCode();
        result = 31 * result + orgUnitUId.hashCode();
        result = 31 * result + productivity;
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + userUId.hashCode();
        result = 31 * result + creationDate.hashCode();
        result = 31 * result + (completionDate != null ? completionDate.hashCode() : 0);
        result = 31 * result + (uploadDate != null ? uploadDate.hashCode() : 0);
        result = 31 * result + (scheduledDate != null ? scheduledDate.hashCode() : 0);
        result = 31 * result + status.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "uId='" + uId + '\'' +
                ", programUId='" + programUId + '\'' +
                ", orgUnitUId='" + orgUnitUId + '\'' +
                ", productivity='" + productivity + '\'' +
                ", score=" + score +
                ", userUId='" + userUId + '\'' +
                ", creationDate=" + creationDate +
                ", completionDate=" + completionDate +
                ", uploadDate=" + uploadDate +
                ", scheduledDate=" + scheduledDate +
                ", status=" + status +
                ", values=" + values +
                '}';
    }
}
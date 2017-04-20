package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Survey {
    private long id;
    private int status;
    private Program mProgram;
    private OrgUnit mOrgUnit;
    private SurveyAnsweredRatio mSurveyAnsweredRatio;
    private Date completionDate;
    private Date creationDate;
    private Date scheduledDate;
    private Boolean hasConflict;
    private Productivity mProductivity;
    /**
     * hasMainScore is used to know if the survey have a compositeScore with only 1 query time.
     */
    private Boolean hasMainScore = null;

    /**
     * Calculated main Score for this survey, is not persisted, just calculated on runtime
     */
    Float mainScore;

    public Survey() {
    }

    public Survey(long id) {
        this.id = id;
    }

    public Survey(long id, int status,
            SurveyAnsweredRatio surveyAnsweredRatio) {
        this.id = id;
        this.status = status;
        mSurveyAnsweredRatio = surveyAnsweredRatio;
    }

    public Program getProgram() {
        return mProgram;
    }

    public void setProgram(Program program) {
        this.mProgram = program;
    }

    public OrgUnit getOrgUnit() {
        return mOrgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.mOrgUnit = orgUnit;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public SurveyAnsweredRatio getSurveyAnsweredRatio() {
        if(mSurveyAnsweredRatio==null){
            mSurveyAnsweredRatio = SurveyAnsweredRatio.getModelToEntity(id);
        }
        return mSurveyAnsweredRatio;
    }

    public void setSurveyAnsweredRatio(
            SurveyAnsweredRatio surveyAnsweredRatio) {
        mSurveyAnsweredRatio = surveyAnsweredRatio;
    }

    public Date getCompletionDate() {
        if(completionDate==null)
            completionDate = SurveyDB.findById(id).getCompletionDate();
        return completionDate;
    }

    public boolean hasConflict() {
        if(hasConflict==null)
            hasConflict = SurveyDB.findById(id).hasConflict();
        return hasConflict;
    }

    public boolean hasMainScore() {
        if(hasMainScore==null)
            hasMainScore = SurveyDB.findById(id).hasMainScore();
        return hasMainScore;
    }

    public Float getMainScore() {
        if(mainScore==null)
            mainScore = SurveyDB.findById(id).getMainScore();
        return mainScore;
    }

    public void setMainScore(Float mainScore) {
        this.mainScore=mainScore;
    }


    public boolean isCompleted() {
        return status == Constants.SURVEY_COMPLETED;
    }

    public boolean isSent() {
        return status == Constants.SURVEY_SENT;
    }

    public static List<Survey> convertModelListToEntity(List<SurveyDB> surveys){
        List<Survey> surveyEntities = new ArrayList<>();
        for(SurveyDB survey:surveys){
            surveyEntities.add(Survey.getFromModel(survey));
        }
        return surveyEntities;
    }

    public static Survey getFromModel(SurveyDB survey) {
        Survey surveyEntity = new Survey();
        surveyEntity.setId(survey.getId_survey());
        if(survey.getOrgUnit()!=null) {
            OrgUnit orgUnit =
                    new OrgUnit(survey.getOrgUnit().getName(), survey.getOrgUnit().getUid(), survey.getOrgUnit().getId_org_unit());
            surveyEntity.setOrgUnit(orgUnit);
        }
        if(survey.getProgram()!=null) {
            Program program = new Program(survey.getProgram().getName(), survey.getProgram().getUid(), survey.getProgram().getId_program());
            surveyEntity.setProgram(program);
        }

        surveyEntity.setStatus(survey.getStatus());
        surveyEntity.setCompletionDate(survey.getCompletionDate());
        surveyEntity.setCreationDate(survey.getCreationDate());
        surveyEntity.setHasConflict(survey.hasConflict());
        surveyEntity.setScheduledDate(survey.getScheduledDate());
        return surveyEntity;
    }

    public void setCompleteSurveyState(String simpleName) {
        SurveyDB survey = SurveyDB.findById(id);
        setStatus(Constants.SURVEY_COMPLETED);
        setMainScore(survey.getMainScore());
        survey.setCompleteSurveyState(simpleName);
    }

    public boolean isInProgress() {
        return status == Constants.SURVEY_IN_PROGRESS;
    }

    public boolean isReadOnly() {
        return (isCompleted() || isSent());
    }

    public Productivity getProductivity() {
        if(mProductivity == null){
            if(id > 0 && mOrgUnit !=null && mProgram != null) {
                mProductivity = new Productivity(id, mOrgUnit.getId(),
                        mProgram.getId());
            }
            else
            {
                mProductivity = new Productivity(Productivity.getDefaultProductivity());
            }
        }
        return mProductivity;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public void setHasConflict(Boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public void setProductivity(
            Productivity productivity) {
        this.mProductivity = productivity;
    }

    public Date getScheduledDate() {
        if(scheduledDate == null && id > 0) {
            scheduledDate = SurveyDB.findById(id).getScheduledDate();
        }
        return scheduledDate;
    }

    public void reschedule(Date newScheduledDate, String comment) {
        SurveyDB survey = SurveyDB.findById(id);
        scheduledDate = survey.reschedule(newScheduledDate, comment);
    }

    public Date getCreationDate() {
        if(creationDate == null && id > 0) {
            creationDate = SurveyDB.findById(id).getCreationDate();
        }
        return creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Survey surveyEntity = (Survey) o;

        if (id != surveyEntity.id) return false;
        return status == surveyEntity.status;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + status;
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "id=" + id +
                ", status=" + status +
                '}';
    }
}

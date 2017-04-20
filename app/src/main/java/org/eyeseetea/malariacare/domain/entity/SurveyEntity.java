package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurveyEntity {
    private long id;
    private int status;
    private ProgramEntity programEntity;
    private OrgUnitEntity orgUnitEntity;
    private SurveyAnsweredRatioEntity mSurveyAnsweredRatio;
    private Date completionDate;
    private Date creationDate;
    private Date scheduledDate;
    private Boolean hasConflict;
    private ProductivityEntity productivityEntity;
    /**
     * hasMainScore is used to know if the survey have a compositeScore with only 1 query time.
     */
    private Boolean hasMainScore = null;

    /**
     * Calculated main Score for this survey, is not persisted, just calculated on runtime
     */
    Float mainScore;

    public SurveyEntity() {
    }

    public SurveyEntity(long id) {
        this.id = id;
    }

    public SurveyEntity(long id, int status,
            SurveyAnsweredRatioEntity surveyAnsweredRatio) {
        this.id = id;
        this.status = status;
        mSurveyAnsweredRatio = surveyAnsweredRatio;
    }

    public ProgramEntity getProgramEntity() {
        return programEntity;
    }

    public void setProgramEntity(ProgramEntity programEntity) {
        this.programEntity = programEntity;
    }

    public OrgUnitEntity getOrgUnitEntity() {
        return orgUnitEntity;
    }

    public void setOrgUnitEntity(OrgUnitEntity orgUnitEntity) {
        this.orgUnitEntity = orgUnitEntity;
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

    public SurveyAnsweredRatioEntity getSurveyAnsweredRatio() {
        if(mSurveyAnsweredRatio==null){
            mSurveyAnsweredRatio = SurveyAnsweredRatioEntity.getModelToEntity(id);
        }
        return mSurveyAnsweredRatio;
    }

    public void setSurveyAnsweredRatio(
            SurveyAnsweredRatioEntity surveyAnsweredRatio) {
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

    public static List<SurveyEntity> convertModelListToEntity(List<SurveyDB> surveys){
        List<SurveyEntity> surveyEntities = new ArrayList<>();
        for(SurveyDB survey:surveys){
            surveyEntities.add(SurveyEntity.getFromModel(survey));
        }
        return surveyEntities;
    }

    public static SurveyEntity getFromModel(SurveyDB survey) {
        SurveyEntity surveyEntity = new SurveyEntity();
        surveyEntity.setId(survey.getId_survey());
        if(survey.getOrgUnit()!=null) {
            OrgUnitEntity orgUnitEntity =
                    new OrgUnitEntity(survey.getOrgUnit().getName(), survey.getOrgUnit().getUid(), survey.getOrgUnit().getId_org_unit());
            surveyEntity.setOrgUnitEntity(orgUnitEntity);
        }
        if(survey.getProgram()!=null) {
            ProgramEntity programEntity = new ProgramEntity(survey.getProgram().getName(), survey.getProgram().getUid(), survey.getProgram().getId_program());
            surveyEntity.setProgramEntity(programEntity);
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

    public ProductivityEntity getProductivityEntity() {
        if(productivityEntity == null){
            if(id > 0 && orgUnitEntity!=null && programEntity != null) {
                productivityEntity = new ProductivityEntity(id, orgUnitEntity.getId(),
                        programEntity.getId());
            }
            else
            {
                productivityEntity = new ProductivityEntity(ProductivityEntity.getDefaultProductivity());
            }
        }
        return productivityEntity;
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

    public void setProductivityEntity(
            ProductivityEntity productivityEntity) {
        this.productivityEntity = productivityEntity;
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

        SurveyEntity surveyEntity = (SurveyEntity) o;

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

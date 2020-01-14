package org.eyeseetea.malariacare.presentation.viewmodels;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;

import java.util.Date;

public class SurveyViewModel {
    private final String program;
    private final String orgUnit;
    private final Date date;
    private final CompetencyScoreClassification competency;
    private final String surveyUid;
    private boolean visible = true;

    public SurveyViewModel(String surveyUid, String program, String orgUnit, Date date,
            CompetencyScoreClassification competency) {
        this.surveyUid = surveyUid;
        this.program = program;
        this.orgUnit = orgUnit;
        this.date = date;
        this.competency = competency;
    }

    public String getProgram() {
        return program;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public Date getDate() {
        return date;
    }

    public Boolean isVisible(){
        return visible;
    }

    public void setVisible (boolean value){
        visible = value;
    }

    public CompetencyScoreClassification getCompetency() {
        return competency;
    }

    public String getSurveyUid() {
        return surveyUid;
    }
}

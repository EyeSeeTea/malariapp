package org.eyeseetea.malariacare.presentation.viewmodels;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.Score;

import java.util.Date;

public class SurveyViewModel {
    private final String program;
    private final String orgUnit;
    private final Date date;
    private final CompetencyScoreClassification competency;
    private final String surveyUid;
    private final Score score;
    private final boolean completed;
    private final boolean hasConflict;

    public SurveyViewModel(String surveyUid, String program, String orgUnit, Date date,
                           CompetencyScoreClassification competency, Score score,
                           boolean completed, boolean hasConflict) {
        this.surveyUid = surveyUid;
        this.program = program;
        this.orgUnit = orgUnit;
        this.date = date;
        this.competency = competency;
        this.score = score;
        this.completed = completed;
        this.hasConflict = hasConflict;
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

    public CompetencyScoreClassification getCompetency() {
        return competency;
    }

    public Score getScore() {
        return score;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean hasConflict() {
        return hasConflict;
    }

    public String getSurveyUid() {
        return surveyUid;
    }
}

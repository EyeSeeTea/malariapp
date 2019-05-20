package org.eyeseetea.malariacare.presentation.viewmodels;

import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;

import java.util.Date;

public class SurveyViewModel {
    public static String COMPLETED = "Completed";
    public static String INCOMPLETED = "Incompleted";

    private final String program;
    private final String orgUnit;
    private final Date date;
    private final CompetencyScoreClassification competency;

    public SurveyViewModel(String program, String orgUnit, Date date,
            CompetencyScoreClassification competency) {
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

    public CompetencyScoreClassification getCompetency() {
        return competency;
    }
}

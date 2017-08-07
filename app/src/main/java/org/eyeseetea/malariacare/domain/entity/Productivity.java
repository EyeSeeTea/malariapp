package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;

public class Productivity {
    /**
     * Expected productivity for this survey according to its orgunit + program.
     * Just a cached value from orgunitprogramproductivity
     */
    Integer productivity;

    Long surveyId;
    Long orgUnitId;
    Long programId;

    public Productivity(Integer productivity) {
        this.productivity = productivity;
    }

    public Productivity(Long surveyId, Long orgUnitId, Long programId) {
        this.surveyId = surveyId;
        this.orgUnitId = orgUnitId;
        this.programId = programId;
    }

    /**
     * Returns if this survey has low productivity or not.
     * [0..4]: Low
     * [5..): Not Low
     */
    public boolean isLowProductivity() {
        return getProductivity() < 5;
    }
    /**
     * Returns the productivity for this survey according to its orgunit + program
     */
    public Integer getProductivity() {
        if (productivity == null) {
            productivity = OrgUnitProgramRelationDB.getProductivity(surveyId, orgUnitId, programId);
        }
        return productivity;
    }
    /**
     * Returns the productivity for this survey according to its orgunit + program
     */
    public static Integer getDefaultProductivity() {
        return OrgUnitProgramRelationDB.getDefaultProductivity();
    }
}

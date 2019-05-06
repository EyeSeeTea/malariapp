package org.eyeseetea.malariacare.domain.entity;

import static java.lang.Integer.*;

public class NextScheduleConfiguration {
    private final String nextScheduleDeltaMatrix;
    private int competentHighProductivityMonths;
    private int competentLowProductivityMonths;

    private int competentNeedsImprovementHighProductivityMonths;
    private int competentNeedsImprovementLowProductivityMonths;

    private int notCompetentHighProductivityMonths;
    private int notCompetentLowProductivityMonths;

    /**
     *
     * @param nextScheduleDeltaMatrix provide next schedule by competency and by productivity
     * Example: 4,4;3,3;3,1
     *
     *  |      Program A          | High Productivity | Low Productivity |
     *  |-------------------------|-------------------|------------------|
     *  |Competency A (Competent) |         4         |        4         |
     *  |-------------------------|-------------------|------------------|
     *  |Competency B (Competent  |                   |                  |
     *  |needs improvement)       |         3         |        3         |
     *  |-------------------------|-------------------|------------------|
     *  |Competency A (Competent) |         3         |        1         |
     *  |-------------------------|-------------------|------------------|
     */

    public NextScheduleConfiguration(String nextScheduleDeltaMatrix) {
        if (nextScheduleDeltaMatrix == null){
            throw new IllegalArgumentException("nextScheduleDeltaMatrix is required");
        }

        this.nextScheduleDeltaMatrix = nextScheduleDeltaMatrix;

        String[] nextSchedulesByCompetencies = nextScheduleDeltaMatrix.split(";");

        if (nextSchedulesByCompetencies.length != 3){
            throw new IllegalArgumentException("Should exists three items by competency");
        }

        for (int index = 0; index < nextSchedulesByCompetencies.length; index++) {
            String nextSchedulesByProductivity = nextSchedulesByCompetencies[index];

            String[] nextSchedulesProductivity = nextSchedulesByProductivity.split(",");

            if (nextSchedulesProductivity.length != 2){
                throw new IllegalArgumentException("Should exists two items by productivity");
            }

            if (index == 0){
                extractCompetentNextSchedule(nextSchedulesProductivity);
            } else if(index == 1){
                extractCompetentNeedsImprovementNextSchedule(nextSchedulesProductivity);
            } else if(index == 2){
                extractNotCompetentNextSchedule(nextSchedulesProductivity);
            }
        }
    }

    private void extractCompetentNextSchedule(String[] nextSchedulesProductivity) {
        competentHighProductivityMonths = parseInt(nextSchedulesProductivity[0]);
        competentLowProductivityMonths = parseInt(nextSchedulesProductivity[1]);
    }

    private void extractCompetentNeedsImprovementNextSchedule(String[] nextSchedulesProductivity) {
        competentNeedsImprovementHighProductivityMonths = parseInt(nextSchedulesProductivity[0]);
        competentNeedsImprovementLowProductivityMonths = parseInt(nextSchedulesProductivity[1]);
    }

    private void extractNotCompetentNextSchedule(String[] nextSchedulesProductivity) {
        notCompetentHighProductivityMonths = parseInt(nextSchedulesProductivity[0]);
        notCompetentLowProductivityMonths = parseInt(nextSchedulesProductivity[1]);
    }

    public String getNextScheduleDeltaMatrix() {
        return nextScheduleDeltaMatrix;
    }

    public int getCompetentHighProductivityMonths() {
        return competentHighProductivityMonths;
    }

    public int getCompetentLowProductivityMonths() {
        return competentLowProductivityMonths;
    }

    public int getCompetentNeedsImprovementHighProductivityMonths() {
        return competentNeedsImprovementHighProductivityMonths;
    }

    public int getCompetentNeedsImprovementLowProductivityMonths() {
        return competentNeedsImprovementLowProductivityMonths;
    }

    public int getNotCompetentHighProductivityMonths() {
        return notCompetentHighProductivityMonths;
    }

    public int getNotCompetentLowProductivityMonths() {
        return notCompetentLowProductivityMonths;
    }
}

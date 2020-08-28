package org.eyeseetea.malariacare.domain.entity;

import static java.lang.Integer.*;

public class NextScheduleDateConfiguration {
    private final String nextScheduleDeltaMatrix;
    private int competentOrAHighProductivityMonths;
    private int competentOrALowProductivityMonths;

    private int competentNeedsImprovementOrBHighProductivityMonths;
    private int competentNeedsImprovementOrBLowProductivityMonths;

    private int notCompetentOrCHighProductivityMonths;
    private int notCompetentOrCLowProductivityMonths;

        /**
         *
         * @param nextScheduleDeltaMatrix provide next schedule Date by competency or score and productivity
         * Example: 4,4;3,3;3,1
         *
         *  |      Program A          | Low Productivity  | High Productivity |
         *  |-------------------------|-------------------|-------------------|
         *  |     A (Competent        |                   |                   |
         *  |    or high score)       |         4         |        4          |
         *  |-------------------------|-------------------|-------------------|
         *  |     B (Competent needs  |                   |                   |
         *  |     improvement or      |         3         |        3          |
         *  |     medium score)       |                   |                   |
         *  |-------------------------|-------------------|-------------------|
         *  |     C (Not Competent    |                   |                   |
         *  |      or low score)      |         3         |        1          |
         *  |-------------------------|-------------------|-------------------|
         */

    public NextScheduleDateConfiguration(String nextScheduleDeltaMatrix) {
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
                extractCompetentOrANextSchedule(nextSchedulesProductivity);
            } else if(index == 1){
                extractCompetentNeedsImprovementOrBNextSchedule(nextSchedulesProductivity);
            } else if(index == 2){
                extractNotCompetentOrCNextSchedule(nextSchedulesProductivity);
            }
        }
    }

    private void extractCompetentOrANextSchedule(String[] nextSchedulesProductivity) {
        competentOrALowProductivityMonths = parseInt(nextSchedulesProductivity[0]);
        competentOrAHighProductivityMonths = parseInt(nextSchedulesProductivity[1]);
    }

    private void extractCompetentNeedsImprovementOrBNextSchedule(String[] nextSchedulesProductivity) {
        competentNeedsImprovementOrBLowProductivityMonths = parseInt(nextSchedulesProductivity[0]);
        competentNeedsImprovementOrBHighProductivityMonths = parseInt(nextSchedulesProductivity[1]);
    }

    private void extractNotCompetentOrCNextSchedule(String[] nextSchedulesProductivity) {
        notCompetentOrCLowProductivityMonths = parseInt(nextSchedulesProductivity[0]);
        notCompetentOrCHighProductivityMonths = parseInt(nextSchedulesProductivity[1]);
    }

    public String getNextScheduleDeltaMatrix() {
        return nextScheduleDeltaMatrix;
    }

    public int getCompetentOrAHighProductivityMonths() {
        return competentOrAHighProductivityMonths;
    }

    public int getCompetentOrALowProductivityMonths() {
        return competentOrALowProductivityMonths;
    }

    public int getCompetentNeedsImprovementOrBHighProductivityMonths() {
        return competentNeedsImprovementOrBHighProductivityMonths;
    }

    public int getCompetentNeedsImprovementOrBLowProductivityMonths() {
        return competentNeedsImprovementOrBLowProductivityMonths;
    }

    public int getNotCompetentOrCHighProductivityMonths() {
        return notCompetentOrCHighProductivityMonths;
    }

    public int getNotCompetentOrCLowProductivityMonths() {
        return notCompetentOrCLowProductivityMonths;
    }
}

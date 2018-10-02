package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class NextScheduleMonths {

    private int [] month;

    public NextScheduleMonths(int [] month){
        validate(month);
        this.month = required(month, "month is required");
    }

    private void validate(int[] month) {
        if(month == null || month.length!=3){
            throw new IllegalArgumentException("array with three dimensions required");
        }
    }

    public int getScoreAMonths(){
        return month[2];
    }

    public int getLowProductivityMonths(){
        return month[1];
    }

    public int getHighProductivityMonths(){
        return month[0];
    }

}

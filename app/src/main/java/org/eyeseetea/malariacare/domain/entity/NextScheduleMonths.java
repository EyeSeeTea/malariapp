package org.eyeseetea.malariacare.domain.entity;

import java.util.HashMap;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class NextScheduleMonths {

    public static final String DEFAULT_SCHEDULE_MONTHS_VALUE ="https://data.psi-mis.org";
    public static final String ALTERNATIVE_SCHEDULE_MONTHS_VALUE ="https://zw.hnqis.org/";
    public static final HashMap<String, int[]> nextScheduleMonths = new HashMap<>();

    static {
        nextScheduleMonths.put(DEFAULT_SCHEDULE_MONTHS_VALUE, new int[]{2, 4, 6});
        nextScheduleMonths.put(ALTERNATIVE_SCHEDULE_MONTHS_VALUE, new int[]{1, 1, 6});
    }

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


    public static int[] getMonthArray(String serverUrl) {
        if(NextScheduleMonths.nextScheduleMonths.containsKey(serverUrl))
        {
            return NextScheduleMonths.nextScheduleMonths.get(serverUrl);
        } else {
            return NextScheduleMonths.nextScheduleMonths.get(NextScheduleMonths.DEFAULT_SCHEDULE_MONTHS_VALUE);
        }
    }
}

package org.eyeseetea.malariacare.domain.entity;

import java.util.Calendar;
import java.util.Date;

public class DateFilter {
    boolean all = true;
    private boolean noData;
    private boolean last6Days;
    private boolean last6Weeks;
    private boolean last6Month;

    public boolean isNoData() {
        return noData;
    }

    public void setNoData(boolean noData) {
        all = false;
        this.noData = noData;
    }

    //This filter include the current day
    public boolean isLast6Days() {
        return last6Days;
    }

    public void setLast6Days(boolean last6Days) {
        all = false;
        this.last6Days = last6Days;
    }

    //This filter include the current week
    public boolean isLast6Weeks() {
        return last6Weeks;
    }

    public void setLast6Weeks(boolean last6Weeks) {
        all = false;
        this.last6Weeks = last6Weeks;
    }

    //This filter ignore the current month
    public boolean isLast6Month() {
        return last6Month;
    }

    public void setLast6Month(boolean last6Month) {
        all = false;
        this.last6Month = last6Month;
    }

    public Date getStartFilterDate(Calendar calendar) {
        if (isLast6Days()) {
            calendar.add(Calendar.DAY_OF_YEAR, -5);
        } else if (isLast6Weeks()) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.WEEK_OF_YEAR, -5);
        } else if (isLast6Month()) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, -5);
        } else if (isNoData()) {
            return Calendar.getInstance().getTime();
        }
        clearTime(calendar);
        return calendar.getTime();
    }

    /**
     * Wipes off time info from date
     *
     * @return A new date without time data (00:00:00:000)
     */
    private Calendar clearTime(Calendar date) {
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }
}

/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.utils.monitor.AllAssessment;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arrizabalaga on 7/10/15.
 */
public class EntrySentSurveysChartBase implements Comparable<EntrySentSurveysChartBase>{
    /**
     * Number of surveys sent this month
     */
    int sent;
    /**
     * Number of surveys expected to be sent for this month
     */
    int expected;
    /**
     * The date whose month represents this point (day info is discarded)
     */
    private Date date;


    private static final String MONTH_FORMAT="MMM";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT=new SimpleDateFormat(MONTH_FORMAT);

    public EntrySentSurveysChartBase(int expected, Date date) {
        this.expected = expected;
        this.date = date;
    }

    /**
     * Increments the number of surveys sent for this month
     */
    public void incSent(){
        this.sent++;
    }

    /**
     * Formats the date as the name of the Month
     * @return Ex: 07/13/2015 ->'Jul'
     */
    public String getDateAsString(){
        return getDateAsString(date);
    }

    public Date getDate(){return date;}
    /**
     * Formats the date as the name of the Month
     * @param date
     * @return Ex: 07/13/2015 ->'Jul'
     */
    public static String getDateAsString(Date date){
        if(date==null){
            return "";
        }
        return SIMPLE_DATE_FORMAT.format(date);
    }


    @Override
    public int compareTo(EntrySentSurveysChartBase another) {
        return date.compareTo(another.date);
    }
}

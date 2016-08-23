/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.utils.monitor;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arrizabalaga on 7/10/15.
 */
public class EntrySentSurveysChart implements Comparable<EntrySentSurveysChart>{
    public static final String JAVASCRIPT_ADD_PROGRAM_DATA = "javascript:setProgramData([%d, %d, '%s', '%s','%s'])";
    public static final String JAVASCRIPT_ADD_ORGUNIT_DATA = "javascript:setOrgUnitData([%d, %d, '%s', '%s','%s'])";

    /**
     * Number of surveys sent this month
     */
    private int sent;
    /**
     * Number of surveys expected to be sent for this month
     */
    private int expected;
    /**
     * The date whose month represents this point (day info is discarded)
     */
    private Date date;

    /**
     * The date whose month represents this point (day info is discarded)
     */
    private Program program;
    /**
     * The date whose month represents this point (day info is discarded)
     */
    private OrgUnit orgUnit;


    private static final String MONTH_FORMAT="MMM";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT=new SimpleDateFormat(MONTH_FORMAT);

    public EntrySentSurveysChart(int expected, Date date, Program program, OrgUnit orgUnit) {
        this.expected = expected;
        this.date = date;
        this.program=program;
        this.orgUnit=orgUnit;
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
    /**
     * Returns a javascript that be inyected into the webview
     * @return
     */
    public String getEntryAsJSByOrgUnit(){
        return String.format(JAVASCRIPT_ADD_ORGUNIT_DATA,sent,expected,orgUnit.getName(),orgUnit.getUid(),getDateAsString());
    }
    /**
     * Returns a javascript that be inyected into the webview
     * @return
     */
    public String getEntryAsJSByProgram(){
        return String.format(JAVASCRIPT_ADD_PROGRAM_DATA,sent,expected,program.getName(),program.getUid(),getDateAsString());
    }

    @Override
    public int compareTo(EntrySentSurveysChart another) {
        return date.compareTo(another.date);
    }
}

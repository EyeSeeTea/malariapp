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

package org.eyeseetea.malariacare.data.database.utils.monitor.facilities;

import org.eyeseetea.malariacare.domain.entity.ServerClassification;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

public class FacilityTableDataBase {
    private static final String MONTH_FORMAT="MMM";

    String title;

    String uid;

    String id;

    protected ServerClassification serverClassification;

    public FacilityTableDataBase(
            ServerClassification serverClassification) {
        this.serverClassification = serverClassification;
    }

    /**
     * Returns an ordered list of the previous 12 months: ['jan','feb',...]
     * @return
     */
    String getMonthsAsJSONArray() {
        String monthsJSON="";
        Calendar cal=Calendar.getInstance();
        SimpleDateFormat HEADER_FORMATTER=new SimpleDateFormat(MONTH_FORMAT);
        for(int i = 0; i< FacilityRowDataBase.NUM_MONTHS; i++){
            String monthLabel=String.format("'%s'",HEADER_FORMATTER.format(cal.getTime()));
            //Separator BUT last and first month (in chronological order)
            if(i!= FacilityRowDataBase.NUM_MONTHS && i!=0){
                monthsJSON=","+monthsJSON;
            }
            monthsJSON=monthLabel+monthsJSON;
            //Go previous month
            cal.add(Calendar.MONTH, -1);
        }
        return String.format("[%s]",monthsJSON);
    }


    /**
     * Returns a JSONArray with the rows for each facility
     */
    String getFacilitiesAsJSONArray(Map<String, FacilityRowDataBase> rowData) {
        StringBuffer facilitiesJSON=new StringBuffer("[");
        int i=0;
        Collection<FacilityRowDataBase> rows=rowData.values();
        for(FacilityRowDataBase row:rows){
            facilitiesJSON.append(row.getAsJSON());
            i++;
            if(i!=rows.size()){
                facilitiesJSON.append(",");
            }
        }
        facilitiesJSON.append("]");
        return facilitiesJSON.toString();
    }
}

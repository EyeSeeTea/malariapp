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
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityTableData {
    private static final String MONTH_FORMAT="MMM";
    private static final SimpleDateFormat HEADER_FORMATTER=new SimpleDateFormat(MONTH_FORMAT);

    private String title;

    private String uid;

    private String id;
    Map<OrgUnit,FacilityRowData> rowData;

    public FacilityTableData(TabGroup tabGroup){
        this.title=tabGroup.getName();
        this.uid=tabGroup.getUid();
        this.id=String.valueOf(tabGroup.getId_tab_group());
        rowData=new HashMap<>();
    }

    public void addSurvey(Survey survey){
        OrgUnit orgUnit=survey.getOrgUnit();
        //Get facility row
        FacilityRowData facilityRow = rowData.get(orgUnit);
        //First time facility
        if(facilityRow==null){
            facilityRow=new FacilityRowData(orgUnit);
            rowData.put(orgUnit,facilityRow);
        }
        //Add survey
        facilityRow.addSurvey(survey);
    }

    public String getAsJSON(){
        return String.format("{title:'%s',months:%s,facilities:%s,tableuid:'%s',id:'%s'}",title,getMonthsAsJSONArray(),getFacilitiesAsJSONArray(),uid,id);
    }

    /**
     * Returns an ordered list of the previous 12 months: ['jan','feb',...]
     * @return
     */
    private String getMonthsAsJSONArray() {
        String monthsJSON="";
        Calendar cal=Calendar.getInstance();
        for(int i=0;i<FacilityRowData.NUM_MONTHS;i++){
            String monthLabel=String.format("'%s'",HEADER_FORMATTER.format(cal.getTime()));
            //Separator BUT last and first month (in chronological order)
            if(i!=FacilityRowData.NUM_MONTHS && i!=0){
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
    private String getFacilitiesAsJSONArray() {
        StringBuffer facilitiesJSON=new StringBuffer("[");
        int i=0;
        Collection<FacilityRowData> rows=rowData.values();
        for(FacilityRowData row:rows){
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

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

package org.eyeseetea.malariacare.database.utils.monitor.facility;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by idelcano on 23/08/2016.
 */
public class FacilityTableDataByOrgUnit extends FacilityTableDataBase{

    private static final String TAG=".FacilityTableDataOU";
    Map<String,FacilityRowDataBase> rowData;
    public FacilityTableDataByOrgUnit(Program program, OrgUnit orgUnit){
        this.title=program.getName();
        this.uid=program.getUid();
        this.id=String.valueOf(program.getId_program());
        rowData=new HashMap<>();
    }

    public void addSurvey(Survey survey){
        OrgUnit orgUnit=survey.getOrgUnit();
        //Get facility row
        FacilityRowDataBase facilityRow = rowData.get(orgUnit.toString());
        //First time facility
        if(facilityRow==null){
            facilityRow=new FacilityRowDataBase(orgUnit.getName());
            rowData.put(orgUnit.toString(),facilityRow);
        }
        //Add survey
        facilityRow.addSurvey(survey);
    }
    public String getAsJSON(){
        return String.format("{title:'%s',months:%s,facilities:%s,tableuid:'%s',id:'%s'}",title,getMonthsAsJSONArray(),getFacilitiesAsJSONArray(rowData),uid,id);
    }
}

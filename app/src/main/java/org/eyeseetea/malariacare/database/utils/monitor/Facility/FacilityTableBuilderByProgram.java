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

package org.eyeseetea.malariacare.database.utils.monitor.Facility;

import android.content.Context;
import android.webkit.WebView;

import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 23/08/2016.
 */
public class FacilityTableBuilderByProgram extends  FacilityTableBuilderBase {
    private static final String TAG=".FacilityTableBuilderP";
    Map<TabGroup,FacilityTableDataByProgram> facilityTableDataMap;
    /**
     * Default constructor
     *
     * @param surveys
     * @param context
     */
    public FacilityTableBuilderByProgram(List<Survey> surveys, Context context) {
        super(surveys, context);
        this.facilityTableDataMap = new HashMap<>();
    }

    /**
     * Build table data from surveys
     * @param surveys
     * @return
     */
    private void build(List<Survey> surveys){
        for(Survey survey:surveys){
            //Current TabGroup
            TabGroup tabGroup=survey.getTabGroup();

            //Get right table
            FacilityTableDataByProgram facilityTableData=facilityTableDataMap.get(tabGroup);

            //Init entry first time of a tabgroup
            if(facilityTableData==null){
                facilityTableData=new FacilityTableDataByProgram(tabGroup);
                facilityTableDataMap.put(survey.getTabGroup(),facilityTableData);
            }

            //Add survey to that table
            facilityTableData.addSurvey(survey);
        }
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build tables
        build(surveys);
        //Inyect tables in view
        for(Map.Entry<TabGroup,FacilityTableDataByProgram> tableEntry:facilityTableDataMap.entrySet()){
            TabGroup tabGroup=tableEntry.getKey();
            FacilityTableDataByProgram facilityTableData=tableEntry.getValue();
            inyectDataInChart(webView, tabGroup.getId_tab_group(), facilityTableData.getAsJSON());
        }

    }
}

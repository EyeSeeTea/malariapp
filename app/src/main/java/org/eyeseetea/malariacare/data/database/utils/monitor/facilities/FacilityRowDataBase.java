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
import org.eyeseetea.malariacare.domain.entity.Survey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by arrizabalaga on 13/10/15.
 */
public class FacilityRowDataBase {
    public static int NUM_MONTHS=6;
    List<FacilityColumnCounterData> counterData;
    List<FacilityColumnData> columnData;
    private Map<String,Integer> monthsIndex;
    private SimpleDateFormat KEY_MONTH_FORMATTER=new SimpleDateFormat("yyyyMM");
    String name;

    public FacilityRowDataBase(String name, ServerClassification serverClassification) {
        this.name= name;
        init(serverClassification);
    }
    public void init(ServerClassification serverClassification){
        //Init empty months
        columnData=new ArrayList<>();
        for(int i=0;i<NUM_MONTHS;i++){
            columnData.add(new FacilityColumnData(serverClassification));
        }
        counterData=new ArrayList<>();
        for(int i=0;i<NUM_MONTHS;i++){
            counterData.add(new FacilityColumnCounterData());
        }
        //Build monthsIndex
        initMonthsIndex();
    }
    /**
     * Builds a helper map with:
     *  201510->11, 201509->10, ...201411->0
     */
    private void initMonthsIndex() {
        monthsIndex=new HashMap<>();
        Calendar cal=Calendar.getInstance();
        for(int i=0;i<NUM_MONTHS;i++){
            //Annotate position 201510->11
            monthsIndex.put(KEY_MONTH_FORMATTER.format(cal.getTime()),NUM_MONTHS-i-1);
            //Move previous month
            cal.add(Calendar.MONTH, -1);
        }
    }

    public void addSurvey(Survey survey){
        //Calculate the index
        int i=calculateIndex(survey);

        //Completion date before 12 months
        if(i==-1){
            return;
        }

        //Put survey in its cell
        columnData.get(i).addSurvey(survey);
        counterData.get(i).addSurvey(survey);
    }

    public String getAsJSON(){
        if(getTotalNumberOfSurveys()>0){
            return String.format("{name:'%s',values:%s, counter:%s}",name,getColumnDataAsJSON(), getNumberOfSurveysAsJSON());
        }else{
            return "";
        }
    }

    /**
     * Returns the column where the given survey belongs (0 = 12 months ago, 11 = now)
     *
     * @param survey
     * @return
     */
    private int calculateIndex(Survey survey){
        String keyMonth=KEY_MONTH_FORMATTER.format(survey.getCompletionDate());
        Integer index=monthsIndex.get(keyMonth);
        if(index==null){
            return -1;
        }
        return index;
    }

    private String getColumnDataAsJSON(){
        StringBuffer columnValues=new StringBuffer("[");
        int i=0;
        for(FacilityColumnData column:columnData){
            columnValues.append(column.getAsJSON());
            i++;
            if(i!=columnData.size()){
                columnValues.append(",");
            }
        }
        columnValues.append("]");
        return columnValues.toString();
    }

    private int getTotalNumberOfSurveys(){
        int count = 0;
        for(FacilityColumnCounterData column:counterData){
            count += Integer.parseInt(column.getAsJSON());
        }
        return count;
    }

    private String getNumberOfSurveysAsJSON(){
        StringBuffer columnValues=new StringBuffer("[");
        int i=0;
        for(FacilityColumnCounterData column:counterData){
            columnValues.append(column.getAsJSON());
            i++;
            if(i!=counterData.size()){
                columnValues.append(",");
            }
        }
        columnValues.append("]");
        return columnValues.toString();
    }
}

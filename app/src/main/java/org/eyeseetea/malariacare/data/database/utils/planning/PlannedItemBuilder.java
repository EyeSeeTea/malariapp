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

package org.eyeseetea.malariacare.data.database.utils.planning;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by arrizabalaga on 16/12/15.
 */
public class PlannedItemBuilder {

    /**
     * Builds an ordered list of planned items (header + surveys)
     * @return
     */
    public List<PlannedItem> buildPlannedItems(){
        List<PlannedItem> never;
        List<PlannedItem> overdue;
        List<PlannedItem> next30;
        List<PlannedItem> future;

        never = new ArrayList<>();
        never.add(PlannedHeader.buildNeverHeader());

        overdue = new ArrayList<>();
        overdue.add(PlannedHeader.buildOverdueHeader());

        next30 = new ArrayList<>();
        next30.add(PlannedHeader.buildNext30Header());

        future = new ArrayList<>();
        future.add(PlannedHeader.buildFutureHeader());

        //Find its place according to scheduleddate
        for(SurveyDB survey: SurveyDB.findPlannedOrInProgress()){
            findRightState(survey, never, overdue, next30, future);
        }

        //Fill potential gaps (a brand new program or orgunit)

        List<PlannedItem> plannedItems = new ArrayList<>();
        //Annotate number of items per accordion
        ((PlannedHeader)never.get(0)).setCounter(never.size()-1);
        ((PlannedHeader)overdue.get(0)).setCounter(overdue.size()-1);
        ((PlannedHeader)next30.get(0)).setCounter(next30.size()-1);
        ((PlannedHeader)future.get(0)).setCounter(future.size() - 1);

        //Put altogether in one list
        plannedItems.addAll(never);
        plannedItems.addAll(overdue);
        plannedItems.addAll(next30);
        plannedItems.addAll(future);
        return plannedItems;
    }


    /**
     * Puts the survey in its right list
     * @param survey
     * @param never
     * @param overdue
     * @param next30
     * @param future
     */
    private void findRightState(SurveyDB survey, List<PlannedItem> never, List<PlannedItem> overdue,
                                List<PlannedItem> next30, List<PlannedItem> future){

        //Check if belongs to NEVER section
        if(processAsNever(survey, never)){
            return;
        }

        //Check if belongs to NEVER section
        if(processAsOverdue(survey, overdue)){
            return;
        }

        //Check if belongs to NEVER section
        if(processAsNext30(survey, next30)){
            return;
        }

        //Otherwise a future
        addToSection(future, survey);
    }

    /**
     * Checks if the given survey belongs to the NEVER section
     * @param survey
     * @return
     */
    private boolean processAsNever(SurveyDB survey, List<PlannedItem> section){
        Date scheduledDate = survey.getScheduledDate();

        //No Scheduled
        if (scheduledDate==null) {
            addToSection(section, survey);
            return true;
        }

        //This survey does not belong to NEVER section
        return false;
    }

    /**
     * Checks if the given survey belongs to the OVERDUE section
     * @param survey
     * @return
     */
    private boolean processAsOverdue(SurveyDB survey, List<PlannedItem> section){
        Date scheduledDate = survey.getScheduledDate();
        Date today = new Date();

        //scheduledDate<today
        if(scheduledDate.before(today)){
            addToSection(section, survey);
            return true;
        }

        //This survey does not belong to OVERDUE section
        return false;
    }

    /**
     * Checks if the given survey belongs to the NEXT30 section
     * @param survey
     * @return
     */
    private boolean processAsNext30(SurveyDB survey, List<PlannedItem> section){
        Date scheduledDate = survey.getScheduledDate();
        Date today = new Date();
        Date today30 = getIn30Days(today);

        //planned in less 30 days
        if(scheduledDate.before(today30)) {
            addToSection(section,survey);
            return true;
        }

        //This survey does not belong to NEXT30 section
        return false;
    }

    /**
     * Builds a synthetic key for this survey
     * @param orgUnit
     * @param program
     * @return
     */
    private String getSurveyKey(OrgUnitDB orgUnit,ProgramDB program) {
        return orgUnit.getId_org_unit().toString()+"@"+program.getId_program().toString();
    }

    /**
     * Returns +30 days from the given date
     * @return
     */
    private Date getIn30Days(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,30);
        return calendar.getTime();
    }

    /**
     * Adds a survey to the given list (section), linking the new item to its header
     * @param section
     * @param survey
     */
    private void addToSection(List<PlannedItem> section, SurveyDB survey){
        if(section.size()>0) {
            PlannedHeader header = (PlannedHeader) section.get(0);
            section.add(new PlannedSurvey(survey, header));
        }
    }

}

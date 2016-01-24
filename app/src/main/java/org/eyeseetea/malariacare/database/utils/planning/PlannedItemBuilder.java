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

package org.eyeseetea.malariacare.database.utils.planning;

import android.content.Context;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;

import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by arrizabalaga on 16/12/15.
 */
public class PlannedItemBuilder {

    private final String TAG=".PlannedItemBuilder";

    /**
     * Memo to find non existant combinations
     */
    Map<String,Survey> surveyMap;

    /**
     * List of surveys not sent
     */
    private List<PlannedItem> never;

    /**
     * List of surveys overdued
     */
    private List<PlannedItem> overdue;

    /**
     * List of surveys for the next 30 days
     */
    private List<PlannedItem> next30;

    /**
     * List of surveys beyond the next 30 days
     */
    private List<PlannedItem> future;

    private static PlannedItemBuilder instance;

    public static PlannedItemBuilder getInstance(){
        if(instance==null){
            instance=new PlannedItemBuilder();
        }
        return instance;
    }

    /**
     * Inits aux data structures
     */
    private void initBuilder(){
        surveyMap = new HashMap<>();
        Context ctx=PreferencesState.getInstance().getContext();

        never = new ArrayList<>();
        never.add(PlannedHeader.buildNeverHeader(ctx));

        overdue = new ArrayList<>();
        overdue.add(PlannedHeader.buildOverdueHeader(ctx));

        next30 = new ArrayList<>();
        next30.add(PlannedHeader.buildNext30Header(ctx));

        future = new ArrayList<>();
        future.add(PlannedHeader.buildFutureHeader(ctx));
    }

    /**
     * Releases memory references
     */
    private void releaseState(){
        surveyMap=null;
        never=null;
        overdue=null;
        next30=null;
        future=null;
    }

    /**
     * Builds an ordered list of planned items (header + surveys)
     * @return
     */
    public List<PlannedItem> buildPlannedItems(){

        initBuilder();

        //Find its place according to scheduleddate
       for(Survey survey: Survey.findPlannedOrInProgressLastByOrgUnitTabGroup()){
            findRightState(survey);
        }

        //Fill potential gaps (a brand new program or orgunit)
        buildNonExistantCombinations();

        //Join lists together (never + overdue + next30 + future)
        return mergeLists();
    }

    private List<PlannedItem> mergeLists() {
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

        //Release state references
        releaseState();
        return plannedItems;
    }

    /**
     * Puts the survey in its right list
     * @param survey
     */
    private void findRightState(Survey survey){
        //Annotate this survey to fill its spot
        annotateSurvey(survey);

        //Check if belongs to NEVER section
        if(processAsNever(survey)){
            return;
        }

        //Check if belongs to NEVER section
        if(processAsOverdue(survey)){
            return;
        }

        //Check if belongs to NEVER section
        if(processAsNext30(survey)){
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
    private boolean processAsNever(Survey survey){
        Date scheduledDate = survey.getScheduledDate();
        Date today = new Date();

        //No Scheduled
        if (scheduledDate==null) {
            addToSection(never, survey);
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
    private boolean processAsOverdue(Survey survey){
        Date scheduledDate = survey.getScheduledDate();
        Date today = new Date();

        //scheduledDate<today
        if(scheduledDate.before(today)){
            addToSection(overdue, survey);
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
    private boolean processAsNext30(Survey survey){
        Date scheduledDate = survey.getScheduledDate();
        Date today = new Date();
        Date today30 = getIn30Days(today);

        //planned in less 30 days
        if(scheduledDate.before(today30)) {
            addToSection(next30,survey);
            return true;
        }

        //This survey does not belong to NEXT30 section
        return false;
    }

    /**
     * Annotates the survey in the map
     * @param survey
     */
    private void annotateSurvey(Survey survey){
        String key= getSurveyKey(survey.getOrgUnit(), survey.getTabGroup().getProgram());
        if(!surveyMap.containsKey(key))
        surveyMap.put(key,survey);
    }

    /**
     * Builds a synthetic key for this survey
     * @param orgUnit
     * @param program
     * @return
     */
    private String getSurveyKey(OrgUnit orgUnit,Program program) {
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
     * Builds brand new combinations for those orgunit + program without a planned item
     */
    private void buildNonExistantCombinations() {

        //Every orgunit
        for(OrgUnit orgUnit:OrgUnit.list()){
            //Each authorized program
            for(Program program:orgUnit.getPrograms()){
                String key=getSurveyKey(orgUnit,program);
                Survey survey=surveyMap.get(key);
                //Already built
                if(survey!=null){
                    continue;
                }

                //NOT exists
                survey=SurveyPlanner.getInstance().buildNext(orgUnit,program);

                //Process like any other survey
                findRightState(survey);
            }
        }
    }

    /**
     * Adds a survey to the given list (section), linking the new item to its header
     * @param section
     * @param survey
     */
    private void addToSection(List<PlannedItem> section,Survey survey){
        PlannedHeader header=(PlannedHeader)section.get(0);
        section.add(new PlannedSurvey(survey,header));
    }

}

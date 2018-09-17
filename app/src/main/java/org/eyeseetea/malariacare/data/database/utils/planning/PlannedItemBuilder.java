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

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;

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
    private enum Type { NEVER, NEXT, OVERDUE, FUTURE }
    /**
     * Builds an ordered list of planned items (header + surveys)
     * @return
     */
    public List<PlannedItem> buildPlannedItems(Context context){
        Context ctx = context;
        boolean hasInnerHeader = AppSettingsBuilder.isPlanInnerHeader();
        List<PlannedItem> never = buildNeverList(hasInnerHeader, context, Type.NEVER);
        List<PlannedItem> overdue = buildNeverList(hasInnerHeader, context, Type.OVERDUE);
        List<PlannedItem> next30 = buildNeverList(hasInnerHeader, context, Type.NEXT);
        List<PlannedItem> future = buildNeverList(hasInnerHeader, context, Type.FUTURE);

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

    private List<PlannedItem> buildNeverList(boolean hasInnerHeader, Context ctx, Type type) {
        List<PlannedItem> plannedItems = new ArrayList<>();
        PlannedHeader plannedHeader = null;
        switch (type) {
            case NEVER:
                plannedHeader = PlannedHeader.buildNeverHeader(ctx);
                break;
            case OVERDUE:
                plannedHeader = PlannedHeader.buildOverdueHeader(ctx);
                break;
            case NEXT:
                plannedHeader = PlannedHeader.buildNext30Header(ctx);
                break;
            case FUTURE:
                plannedHeader = PlannedHeader.buildFutureHeader(ctx);
                break;
        }
        plannedItems.add(plannedHeader);
        if(hasInnerHeader) {
            plannedItems.add(new PlannedSurveyHeader(plannedHeader));
        }
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

        Date scheduledDate = survey.getScheduledDate();
        Date today = new Date();
        //check if is never and add into never section
        if (scheduledDate == null) {
            addToSection(never, survey);
            return;
        }
        //check if is overdue and add into overdue section
        if(scheduledDate.before(today)){
            addToSection(overdue, survey);
            return;
        }
        //check if is next30 and add into next30 section
        Date today30 = getIn30Days(today);
        if (scheduledDate.before(today30)) {
            addToSection(next30, survey);
            return;
        }
        //Otherwise a future
        addToSection(future, survey);
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

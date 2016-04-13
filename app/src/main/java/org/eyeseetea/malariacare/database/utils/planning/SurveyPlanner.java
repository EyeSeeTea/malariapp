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

import android.util.Log;

import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper that creates a 'next' planned survey from a given survey or from a orgUnit + program
 * Created by arrizabalaga on 16/12/15.
 */
public class SurveyPlanner {

    private final static int TYPE_A_NEXT_DATE=6;
    private final static int TYPE_BC_LOW_NEXT_DATE=4;
    private final static int TYPE_BC_HIGH_NEXT_DATE=2;
    private static final String TAG = ".SurveyPlanner";

    private static SurveyPlanner instance;

    public static SurveyPlanner getInstance(){
        if(instance==null){
            instance=new SurveyPlanner();
        }
        return instance;
    }

    /**
     * Builds a 'NEVER' planned survey for the given combination
     * @param orgUnit
     * @param program
     * @return
     */
    public Survey buildNext(OrgUnit orgUnit,Program program){
        Survey survey = new Survey();
        survey.setStatus(Constants.SURVEY_PLANNED);
        survey.setOrgUnit(orgUnit);
        survey.setUser(Session.getUser());

        List<TabGroup> tabGroups = program.getTabGroups();
        survey.setTabGroup(tabGroups.get(0));
        survey.save();

        return survey;
    }


    /**
     * Builds a 'NEW' planned survey and delete the send survey
     * @param oldSurvey
     * @return newSurvey
     */
    public Survey deleteSurveyAndBuildNext(Survey oldSurvey){
        Survey newSurvey = new Survey();
        newSurvey.setStatus(Constants.SURVEY_PLANNED);
        newSurvey.setOrgUnit(oldSurvey.getOrgUnit());
        newSurvey.setUser(oldSurvey.getUser());

        newSurvey.setTabGroup(oldSurvey.getTabGroup());
        newSurvey.setSchedule_date(oldSurvey.getSchedule_date());
        newSurvey.setMainScore(oldSurvey.getMainScore());
        oldSurvey.setSurveyScheduleToSurvey(newSurvey);
        newSurvey.save();

        //remove oldSurvey
        oldSurvey.delete();
        return newSurvey;
    }


    /**
     * Plans a new survey according to the given sent survey and its values
     * @param survey
     * @return
     */
    public Survey buildNext(Survey survey){
        Survey plannedSurvey = new Survey();
        //Create and save a planned survey
        plannedSurvey.setStatus(Constants.SURVEY_PLANNED);
        plannedSurvey.setOrgUnit(survey.getOrgUnit());
        plannedSurvey.setUser(Session.getUser());
        plannedSurvey.setTabGroup(survey.getTabGroup());
        plannedSurvey.setMainScore(survey.getMainScore());
        plannedSurvey.setSchedule_date(findScheduledDateBySurvey(survey));
        plannedSurvey.save();

        //Save last main score
        plannedSurvey.saveMainScore();

        return plannedSurvey;
    }

    /**
     * Starts a planned survey with the given orgUnit and tabGroup
     * @param orgUnit
     * @param tabGroup
     * @return
     */
    public Survey startSurvey(OrgUnit orgUnit,TabGroup tabGroup){
        //Find planned survey
        Survey survey = Survey.findByOrgUnitAndTabGroup(orgUnit,tabGroup);
        return startSurvey(survey);
    }

    /**
     * Starts a planned survey
     * @param survey
     * @return
     */
    public Survey startSurvey(Survey survey){
        survey.setCreation_date(new Date());
        survey.setStatus(Constants.SURVEY_IN_PROGRESS);
        survey.setUser(Session.getUser());
        survey.save();

        //Reset mainscore for this 'real' survey
        survey.setMainScore(0f);
        survey.saveMainScore();
        return survey;
    }

    /**
     * Plans a new survey according to the last surveys that has been sent for each combo orgunit + program
     */
    public void buildNext(){
        //Plan a copy according to that survey
        for(Survey survey:Survey.listLastByOrgUnitTabGroup()){
            buildNext(survey);
        }

    }

    private Date findScheduledDateBySurvey(Survey survey) {
        if(survey==null){
            return null;
        }

        Date eventDate=survey.getCompletion_date();
        if(eventDate==null){
            return null;
        }

        //Load main score
        Log.d(TAG, String.format("finding scheduledDate for a survey with: eventDate: %s, score: %f , lowProductivity: %b", eventDate.toString(), survey.getMainScore(), survey.isLowProductivity()));

        //A -> 6 months
        if(survey.isTypeA()){
            return getInXMonths(eventDate,TYPE_A_NEXT_DATE);
        }

        //BC + Low OrgUnit -> 4
        if(survey.isLowProductivity()){
            return getInXMonths(eventDate,TYPE_BC_LOW_NEXT_DATE);
        }

        //BC + High OrgUnit -> 2
        return getInXMonths(eventDate,TYPE_BC_HIGH_NEXT_DATE);
    }

    /**
     * Returns +30 days from the given date
     * @return
     */
    private Date getInXMonths(Date date,int numMonths){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH,numMonths);
        return calendar.getTime();
    }

}

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

import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.ProductivityEntity;
import org.eyeseetea.malariacare.domain.entity.SurveyEntity;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Calendar;
import java.util.Date;

/**
 * Helper that creates a 'next' planned survey from a given survey or from a orgUnit + program
 * Created by arrizabalaga on 16/12/15.
 */
public class SurveyPlanner {

    private final static int TYPE_A_NEXT_DATE = 6;
    private final static int TYPE_BC_LOW_NEXT_DATE = 4;
    private final static int TYPE_BC_HIGH_NEXT_DATE = 2;
    private static final String TAG = ".SurveyPlanner";

    private static SurveyPlanner instance;

    public static SurveyPlanner getInstance() {
        if (instance == null) {
            instance = new SurveyPlanner();
        }
        return instance;
    }

    /**
     * Builds a 'NEVER' planned survey for the given combination
     */
    public SurveyEntity buildNext(Long orgUnitId, Long programId) {
        Survey survey = new Survey();
        survey.setStatus(Constants.SURVEY_PLANNED);
        survey.setOrgUnit(orgUnitId);
        survey.setUser(Session.getUser());

        survey.setProgram(programId);
        survey.save();

        SurveyEntity surveyEntity = new SurveyEntity();
        return surveyEntity.getFromModel(survey);
    }


    /**
     * Builds a 'NEW' planned survey and delete the send survey
     *
     * @return newSurvey
     */
    public Survey deleteSurveyAndBuildNext(SurveyEntity oldSurveyEntity) {
        Survey newSurvey = new Survey();
        Survey oldSurvey = Survey.findById(oldSurveyEntity.getId());
        newSurvey.save();//generate the new id
        newSurvey.setStatus(Constants.SURVEY_PLANNED);
        newSurvey.setOrgUnit(oldSurvey.getOrgUnit());
        newSurvey.setUser(oldSurvey.getUser());
        newSurvey.setProgram(oldSurvey.getProgram());
        newSurvey.setScheduledDate(oldSurvey.getScheduledDate());
        oldSurvey.setSurveyScheduleToSurvey(newSurvey);
        oldSurvey.delete();
        //Recovery the last valid main score if exists
        Survey lastSurveyScore = Survey.getLastSurvey(newSurvey.getOrgUnit().getId_org_unit(),
                newSurvey.getProgram().getId_program());
        if (lastSurveyScore != null) {
            if (lastSurveyScore.hasMainScore()) {
                newSurvey.setMainScore(lastSurveyScore.getMainScore());
                newSurvey.saveMainScore();
            } else {
                newSurvey.setMainScore(0f);
            }
        }
        newSurvey.save();
        return newSurvey;
    }


    /**
     * Plans a new survey according to the given sent survey and its values
     */
    public Survey buildNext(Survey survey) {
        Survey plannedSurvey = new Survey();
        //Create and save a planned survey
        plannedSurvey.setStatus(Constants.SURVEY_PLANNED);
        plannedSurvey.setOrgUnit(survey.getOrgUnit());
        plannedSurvey.setUser(Session.getUser());
        plannedSurvey.setProgram(survey.getProgram());
        plannedSurvey.setMainScore(survey.getMainScore());
        plannedSurvey.setScheduledDate(findScheduledDateBySurvey(survey));
        plannedSurvey.save();

        //Save last main score
        plannedSurvey.saveMainScore();

        return plannedSurvey;
    }



    /**
     * Starts a planned survey with the given orgUnit and tabGroup
     */
    public SurveyEntity startSurvey(Long orgUnitId, Long programId) {
        //Find planned survey
        Survey survey = Survey.findPlannedByOrgUnitAndProgram(orgUnitId, programId);
        if (survey == null) {
            survey = new Survey();
            survey.setProgram(programId);
            survey.setOrgUnit(orgUnitId);
        }
        return startSurvey(survey);
    }

    /**
     * Starts a planned survey
     */
    public SurveyEntity startSurvey(Survey survey){
        Date now = new Date();
        survey.setCreationDate(now);
        survey.setUploadDate(now);
        survey.setStatus(Constants.SURVEY_IN_PROGRESS);
        survey.setUser(Session.getUser());
        survey.save();

        //Reset mainscore for this 'real' survey
        survey.setMainScore(0f);
        survey.saveMainScore();
        SurveyEntity surveyEntity = new SurveyEntity();
        return surveyEntity.getFromModel(survey);
    }
    /**
     * Plans a new survey according to the last surveys that has been sent for each combo orgunit +
     * program
     */
    public void buildNext() {
        //Plan a copy according to that survey
        for (Survey survey : Survey.listLastByOrgUnitProgram()) {
            buildNext(survey);
        }

    }

    public Date findScheduledDateBySurvey(Survey survey) {
        if (survey == null) {
            return null;
        }

        Date eventDate = survey.getCompletionDate();
        if (eventDate == null) {
            return null;
        }
        ProductivityEntity productivityEntity = new ProductivityEntity(survey.getId_survey(), survey.getOrgUnit().getId_org_unit(), survey.getProgram().getId_program());

        //Load main score
        Log.d(TAG, String.format(
                "finding scheduledDate for a survey with: eventDate: %s, score: %f , "
                        + "lowProductivity: %b",
                eventDate.toString(), survey.getMainScore(), productivityEntity.isLowProductivity()));

        //A -> 6 months
        if (Survey.isTypeA(survey.getMainScore())) {
            return getInXMonths(eventDate, TYPE_A_NEXT_DATE);
        }

        //BC + Low OrgUnit -> 4
        if (productivityEntity.isLowProductivity()) {
            return getInXMonths(eventDate, TYPE_BC_LOW_NEXT_DATE);
        }

        //BC + High OrgUnit -> 2
        return getInXMonths(eventDate, TYPE_BC_HIGH_NEXT_DATE);
    }

    /**
     * Returns +30 days from the given date
     */
    private Date getInXMonths(Date date, int numMonths) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, numMonths);
        return calendar.getTime();
    }

}

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
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ServerDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.service.SurveyNextScheduleDomainService;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper that creates a 'next' planned survey from a given survey or from a orgUnit + program
 * Created by arrizabalaga on 16/12/15.
 */
public class SurveyPlanner {

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
    public SurveyDB buildNext(OrgUnitDB orgUnit, ProgramDB program) {
        SurveyDB survey = new SurveyDB();
        survey.setStatus(Constants.SURVEY_PLANNED);
        survey.setOrgUnit(orgUnit);
        survey.setUser(Session.getUser());

        survey.setProgram(program);
        survey.save();

        return survey;
    }


    /**
     * Builds a 'NEW' planned survey and delete the send survey
     *
     * @return newSurvey
     */
    public SurveyDB deleteSurveyAndBuildNext(SurveyDB oldSurvey) {
        SurveyDB newSurvey = new SurveyDB();
        newSurvey.save();//generate the new id
        newSurvey.setStatus(Constants.SURVEY_PLANNED);
        newSurvey.setOrgUnit(oldSurvey.getOrgUnit());
        newSurvey.setUser(oldSurvey.getUser());
        newSurvey.setProgram(oldSurvey.getProgram());
        newSurvey.setScheduledDate(oldSurvey.getScheduledDate());
        oldSurvey.setSurveyScheduleToSurvey(newSurvey);
        oldSurvey.delete();
        //Recovery the last valid main score if exists
        SurveyDB lastSurveyScore = SurveyDB.getLastSurvey(newSurvey.getOrgUnit().getId_org_unit(),
                newSurvey.getProgram().getId_program());
        if (lastSurveyScore != null) {
            if (lastSurveyScore.hasMainScore()) {
                newSurvey.setMainScore(lastSurveyScore.getId_survey(), lastSurveyScore.getScoreDB().getUid(), lastSurveyScore.getMainScoreValue());
                newSurvey.saveMainScore();
            } else {
                newSurvey.resetMainScore();
            }
        }
        //Set as creationDate the completion date of the last survey
        newSurvey.setCreationDate(lastSurveyScore.getCompletionDate());

        newSurvey.save();
        return newSurvey;
    }


    /**
     * Plans a new survey according to the given sent survey and its values
     */
    public SurveyDB buildNext(SurveyDB survey) {
        SurveyDB plannedSurvey = new SurveyDB();
        //Create and save a planned survey
        plannedSurvey.setStatus(Constants.SURVEY_PLANNED);
        plannedSurvey.setOrgUnit(survey.getOrgUnit());
        plannedSurvey.setUser(Session.getUser());
        plannedSurvey.setProgram(survey.getProgram());

        //Set as creationDate the completion date of the last survey
        plannedSurvey.setCreationDate(survey.getCompletionDate());

        plannedSurvey.setCompetencyScoreClassification(survey.getCompetencyScoreClassification());
        plannedSurvey.setScheduledDate(findScheduledDateBySurvey(survey));
        plannedSurvey.save();

        if (survey.hasMainScore()){
            plannedSurvey.setMainScore(plannedSurvey.getId_survey(), survey.getMainScore().getUid(), survey.getMainScore().getScore());
            //Save last main score
            plannedSurvey.saveMainScore();
        }

        return plannedSurvey;
    }

    /**
     * Starts a planned survey with the given orgUnit and tabGroup
     */
    public SurveyDB startSurvey(OrgUnitDB orgUnit, ProgramDB program) {
        //Find planned survey
        SurveyDB survey = SurveyDB.findPlannedByOrgUnitAndProgram(orgUnit, program);
        if (survey == null) {
            survey = new SurveyDB();
            survey.setProgram(program);
            survey.setOrgUnit(orgUnit.getId_org_unit());
        }
        return startSurvey(survey);
    }

    /**
     * Starts a planned survey
     */
    public SurveyDB startSurvey(SurveyDB survey) {
        Date now = new Date();
        survey.setCreationDate(now);
        survey.setUploadDate(now);
        survey.setStatus(Constants.SURVEY_IN_PROGRESS);
        survey.setUser(Session.getUser());
        survey.save();

        //Reset mainscore for this 'real' survey
        survey.resetMainScore();
        return survey;
    }

    /**
     * Plans a new survey according to the last surveys that has been sent for each combo orgunit +
     * program
     * This method is only used in the pull.
     */
    public void buildNext() {
        //Plan a copy according to that survey
        for (SurveyDB survey : SurveyDB.listLastByOrgUnitProgram()) {
            buildNext(survey);
        }
        //Plan non existant combinations
        buildNonExistentCombinations();

    }

    private Date findScheduledDateBySurvey(SurveyDB survey) {
        if (survey == null) {
            return null;
        }

        Date eventDate = survey.getCompletionDate();

        CompetencyScoreClassification competencyScoreClassification =
                CompetencyScoreClassification.get(survey.getCompetencyScoreClassification());

        NextScheduleDateConfiguration nextScheduleDateConfiguration =
                new NextScheduleDateConfiguration(survey.getProgram().getNextScheduleDeltaMatrix());

        SurveyNextScheduleDomainService surveyNextScheduleDomainService = new
                SurveyNextScheduleDomainService();

        ServerDB serverDB = ServerDB.getConnectedServerFromDB();

        Date nextScheduleDate = surveyNextScheduleDomainService.calculate(
                nextScheduleDateConfiguration,
                eventDate,
                competencyScoreClassification,
                survey.isLowProductivity(),
                survey.getMainScoreValue(),
                ServerClassification.Companion.get(serverDB.getClassification()));

        return nextScheduleDate;
    }

    private void buildNonExistentCombinations() {
        List<OrgUnitProgramRelationDB> orgUnitProgramRelations = OrgUnitProgramRelationDB.getAll();
        for (OrgUnitProgramRelationDB orgUnitProgramRelation : orgUnitProgramRelations) {
            SurveyDB survey = SurveyDB.findPlannedByOrgUnitAndProgram(
                    orgUnitProgramRelation.getOrgUnit(), orgUnitProgramRelation.getProgram());
            //Already built
            if (survey != null) {
                continue;
            }
            //NOT exists. Create a new survey and add to never
            buildNext(orgUnitProgramRelation.getOrgUnit(), orgUnitProgramRelation.getProgram());
        }
    }

}

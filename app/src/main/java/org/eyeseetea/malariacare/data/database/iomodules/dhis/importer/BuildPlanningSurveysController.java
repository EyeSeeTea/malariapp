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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import android.util.Log;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.datasources.SurveyLocalDataSource;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.boundary.IBuildPlannedController;
import org.eyeseetea.malariacare.domain.boundary.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.push.NullEventDateException;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.hisp.dhis.client.sdk.core.common.utils.CodeGenerator;

import java.util.ArrayList;
import java.util.List;

public class BuildPlanningSurveysController implements IBuildPlannedController {
    private final String TAG = ".BuildPlanning";

    Callback callback;

    private ISurveyDataSource localSurveyDataSource;
    private IOrgUnitRepository localOrgUnitDataSource;

    public BuildPlanningSurveysController(
            ISurveyDataSource localSurveyDataSource,
            IOrgUnitRepository orgUnitRepository) {
        this.localSurveyDataSource = localSurveyDataSource;
        this.localOrgUnitDataSource = orgUnitRepository;
    }

    @Override
    public void buildPlanningSurveys(final Callback callback) {
        Log.d(TAG, "building planning surveys");
        this.callback = callback;
        SurveyFilter surveyFilter = SurveyFilter.createFilterToBuildPlanningSurveys();

       try {
           getAndSaveFromExistingCombinations(surveyFilter);

           getAndSaveNotExistantCombinations(callback);

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private void getAndSaveNotExistantCombinations(Callback callback) throws Exception {
        List<Survey> plannedSurveys = new ArrayList<>();
        List <Survey> surveys = buildNonExistentCombinations();

        //Plan non existent combinations
        plannedSurveys.addAll(surveys);

        localSurveyDataSource.Save(plannedSurveys);

        callback.onComplete();
    }

    private void getAndSaveFromExistingCombinations(SurveyFilter surveyFilter) throws Exception {
        List<Survey> plannedSurveys = new ArrayList<>();
        List <Survey> surveys = localSurveyDataSource.getSurveys(surveyFilter);

        //Plan a copy according to that survey
        if(surveys!=null) {
            for (Survey survey : surveys) {
                Log.d(TAG, "building planning survey uid:" + survey.getUId());
                plannedSurveys.add(buildPlanningSurvey(survey));
            }
        }
        localSurveyDataSource.Save(plannedSurveys);
    }

    private Survey buildPlanningSurvey(Survey survey) {
        Survey plannedSurvey = Survey.createPlannedSurvey(survey.getUId(), survey.getProgramUId(), survey.getOrgUnitUId(), survey.getUserUId(),
                survey.getScheduledDate(), survey.getScore());
        return plannedSurvey;
    }

    private List<Survey> buildNonExistentCombinations() {
        Log.d(TAG, "building Non Existent Combinations:");
        List<OrgUnit> orgUnitProgramRelations = localOrgUnitDataSource.getAll();
        List<Survey> nonExistentCombinationList = new ArrayList<>();
        for (OrgUnit orgUnit : orgUnitProgramRelations) {
            for (String programUId : orgUnit.getRelatedPrograms()) {
                if (checkIfACombinationExist(orgUnit, programUId)) {
                    continue;
                } else {
                    //NOT exists. Create a new survey and add to never
                    nonExistentCombinationList.add(buildPlanningSurvey(programUId, orgUnit.getUid()));
                }
            }
        }
        return nonExistentCombinationList;
    }

    private boolean checkIfACombinationExist(OrgUnit orgUnit, String programUId) {
        SurveyFilter surveyFilter = SurveyFilter.createFilterToBuildPlanningSurveysByOrgUnitAndProgram(orgUnit.getUid(), programUId);
        List<Survey> survey = null;
        try {
            survey = localSurveyDataSource.getSurveys(surveyFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Already built
        if (survey != null && survey.size()>0) {
            Log.d(TAG, "existing combination orguid "+ orgUnit.getUid()+" proguid:"+programUId + " survey uid: "+survey.get(0).getUId());
            return true;
        }
        return false;
    }

    private Survey buildPlanningSurvey(String programUId, String orgUnitUId) {
        Survey survey = Survey.createPlannedSurvey(CodeGenerator.generateCode(), programUId, orgUnitUId, Session.getUser().getUid(), null, null);

        Log.d(TAG, "building not existing combination orguid:"+ orgUnitUId+" proguid:"+programUId + " survey uid: "+survey.getUId());
        return survey;
    }
}

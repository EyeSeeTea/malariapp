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

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.ISurveyQuarantineRepository;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;

import java.util.ArrayList;
import java.util.List;

public class SurveyQuarantineRepository implements ISurveyQuarantineRepository {

    private ISurveyDataSource remoteSurveyDataSource;
    private ISurveyDataSource localSurveyDataSource;

    public SurveyQuarantineRepository(
            ISurveyDataSource localSurveyDataSource,
            ISurveyDataSource remoteSurveyDataSource) {

        this.localSurveyDataSource = localSurveyDataSource;
        this.remoteSurveyDataSource = remoteSurveyDataSource;
    }

    @Override
    public void updateQuarantineSurveysOnServer(List<SurveyFilter> filters, Callback callback) {
        try{
            for(SurveyFilter filter: filters) {
                List<Survey> surveys = localSurveyDataSource.getSurveys(filter);
                List<Survey> quarantineSurveysInServer = remoteSurveyDataSource.getSurveys(filter);

                for(Survey localSurvey : surveys) {
                    localSurvey.changeStatus(SurveyStatus.COMPLETED);
                    for (Survey quarantineSurvey : quarantineSurveysInServer) {
                        if(localSurvey.getCreationDate().equals(quarantineSurvey.getCreationDate())){
                            localSurvey.changeStatus(SurveyStatus.SENT);
                        }
                    }
                }
                localSurveyDataSource.Save(surveys);
            }
            callback.onComplete();
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public List<Survey> getAll(SurveyFilter surveyFilter) {
        if(surveyFilter.isQuarantineSurvey()) {
            try {
                return localSurveyDataSource.getSurveys(surveyFilter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new ArrayList<>();
    }
}

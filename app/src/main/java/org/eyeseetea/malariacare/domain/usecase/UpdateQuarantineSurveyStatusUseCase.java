/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.ISurveyQuarantineRepository;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FixQuarantineSurveyStatusUseCase {

    public interface Callback {
        void onComplete();

        void onError();
    }

    ISurveyQuarantineRepository quarantineExistOnServerController;
    IOrgUnitRepository orgUnitRepository;

    public FixQuarantineSurveyStatusUseCase(ISurveyQuarantineRepository quarantineExistOnServerController,
                                            IOrgUnitRepository orgUnitRepository) {
        this.quarantineExistOnServerController = quarantineExistOnServerController;
        this.orgUnitRepository = orgUnitRepository;
    }

    public void execute(final Callback callback) {
        List<SurveyFilter> surveyFilters = new ArrayList<>();

        List<OrgUnit> orgUnitList = orgUnitRepository.getAll();
        for (OrgUnit orgUnit:orgUnitList) {
            for (String programUId : orgUnit.getRelatedPrograms()) {

                SurveyFilter getLocalQuarantineSurveysFilter = SurveyFilter.createGetQuarantineSurveys(programUId, orgUnit.getUid());
                List<Survey> quarantineSurveys = quarantineExistOnServerController.getAll(getLocalQuarantineSurveysFilter);

                if (quarantineSurveys.size() == 0) {
                    continue;
                }

                Date highestCompletionDate = findHighestCompletionDate(quarantineSurveys);
                Date lowestCompletionDate = findLowestCompletionDate(quarantineSurveys);

                surveyFilters.add(SurveyFilter.createCheckOnServerFilter(lowestCompletionDate, highestCompletionDate, programUId, orgUnit.getUid()));
            }
        }
        if(surveyFilters.size()==0){
            callback.onComplete();
            return;
        }

        quarantineExistOnServerController.updateQuarantineSurveysOnServer(surveyFilters, new ISurveyQuarantineRepository.Callback() {
            @Override
            public void onComplete() {
                callback.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError();
            }
        });

    }

    private Date findHighestCompletionDate(List<Survey> quarantineSurveys) {
        Date date = new Date();
        date.setTime(0);
        for(Survey survey: quarantineSurveys){
            if(survey.getCompletionDate()!=null && survey.getCompletionDate().after(date)) {
                date = survey.getCompletionDate();
            }
        }
        return date;
    }

    private Date findLowestCompletionDate(List<Survey> quarantineSurveys) {
        Date date = new Date();
        for(Survey survey: quarantineSurveys){
            if(survey.getCompletionDate()!=null && survey.getCompletionDate().before(date)) {
                date = survey.getCompletionDate();
            }
        }
        return date;
    }
}

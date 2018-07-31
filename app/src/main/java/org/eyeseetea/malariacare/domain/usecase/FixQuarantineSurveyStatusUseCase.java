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

import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.ISurveyQuarantineRepository;
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
    String createdOnuid;

    public FixQuarantineSurveyStatusUseCase(ISurveyQuarantineRepository quarantineExistOnServerController, String createdOnuid) {
        this.quarantineExistOnServerController = quarantineExistOnServerController;
        this.createdOnuid = createdOnuid;
    }

    public void execute(final Callback callback) {
        //// TODO: 31/07/2018 the programs and orgunits should be get from datasources
        List<ProgramDB> programs = ProgramDB.getAllPrograms();
        List<SurveyFilter> surveyFilters = new ArrayList<>();
        for (ProgramDB program : programs) {
            for (OrgUnitDB orgUnit : program.getOrgUnits()) {
                List<SurveyDB> quarantineSurveys = SurveyDB.getAllQuarantineSurveysByProgramAndOrgUnit(
                        program, orgUnit);
                if (quarantineSurveys.size() == 0) {
                    continue;
                }
                Date minDate = SurveyDB.getMinQuarantineCompletionDateByProgramAndOrgUnit(program,
                        orgUnit);
                Date maxDate = SurveyDB.getMaxQuarantineUpdatedDateByProgramAndOrgUnit(program,
                        orgUnit);
                 surveyFilters.add(SurveyFilter.createCheckOnServerFilter(minDate, maxDate, program.getUid(), orgUnit.getUid(), createdOnuid));
            }
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
}

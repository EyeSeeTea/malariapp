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

import org.eyeseetea.malariacare.data.boundaries.ISyncDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.ISyncDataRemoteDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPullDataController;
import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;

import java.util.List;

public class PullDataController implements IPullDataController {

    IPullDataController.Callback callback;

    private ISyncDataLocalDataSource mSurveyLocalDataSource;
    private ISyncDataRemoteDataSource mSurveyRemoteDataSource;

    public PullDataController(
            ISyncDataLocalDataSource surveyLocalDataSource,
            ISyncDataRemoteDataSource surveyRemoteDataSource) {

        this.mSurveyLocalDataSource = surveyLocalDataSource;
        this.mSurveyRemoteDataSource = surveyRemoteDataSource;
    }

    @Override
    public void pullData(final SurveyFilter filters, final IPullDataController.Callback callback) {
        this.callback = callback;

        try {
            callback.onStep(PullStep.PREPARING_SURVEYS);

            List<? extends ISyncData> surveys = mSurveyRemoteDataSource.get(filters);

            mSurveyLocalDataSource.save(surveys);

            callback.onComplete();

        } catch (Exception e) {
            callback.onError(e);
        }
    }
}

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
import org.eyeseetea.malariacare.domain.boundary.IPullDataController;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;

import java.util.List;

public class PullDataController implements IPullDataController {
    private final String TAG = ".PullDataController";

    IPullDataController.Callback callback;

    private ISurveyDataSource remoteSurveyDataSource;
    private ISurveyDataSource localSurveyDataSource;

    public PullDataController(
            ISurveyDataSource localSurveyDataSource,
            ISurveyDataSource remoteSurveyDataSource) {

        this.localSurveyDataSource = localSurveyDataSource;
        this.remoteSurveyDataSource = remoteSurveyDataSource;
    }

    @Override
    public void pullData(final SurveyFilter filters, final IPullDataController.Callback callback) {
        this.callback = callback;
        Log.d(TAG, "Pull data start");
       try {
            callback.onStep(PullStep.PREPARING_SURVEYS);

            List<Survey> surveys = remoteSurveyDataSource.getSurveys(filters);

            Log.d(TAG, "Pull data downloaded");
            localSurveyDataSource.Save(surveys);

            Log.d(TAG, "Pull data saved");

            callback.onComplete();

        } catch (Exception e) {
            callback.onError(e);
        }
    }
}

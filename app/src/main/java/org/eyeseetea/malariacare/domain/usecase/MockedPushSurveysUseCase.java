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


import static org.eyeseetea.malariacare.utils.Constants.SURVEY_SENT;

import org.eyeseetea.malariacare.data.database.model.Survey;

import java.util.List;

public class MockedPushSurveysUseCase {
    public void execute(Callback callback) {
        List<Survey> surveys = Survey.getAllCompletedUnsentSurveys();

        //Check surveys not in progress
        for (Survey survey : surveys) {
            survey.setStatus(SURVEY_SENT);
            survey.save();
        }

        callback.onPushFinished();
    }

    public interface Callback {
        void onPushFinished();
    }
}


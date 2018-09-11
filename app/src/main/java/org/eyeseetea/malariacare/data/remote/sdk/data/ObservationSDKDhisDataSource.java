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

package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.content.Context;

import org.eyeseetea.malariacare.data.boundaries.IObservationDataSource;
import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObservationSDKDhisDataSource implements IObservationDataSource {

    private final Context mContext;
    private final ISurveyDataSource mSurveyLocalDataSource;

    public ObservationSDKDhisDataSource(Context context, ISurveyDataSource surveyLocalDataSource) {
        mContext = context;
        mSurveyLocalDataSource = surveyLocalDataSource;
    }

    @Override
    public Observation getObservation(String surveyUId) throws Exception {
        // Not used for the moment
        // The app not realize pull of observations from Dhis2
        return null;
    }

    @Override
    public List<Observation> getObservations(
            ObservationsToRetrieve observationsToRetrieve) {
        // Not used for the moment
        // The app not realize pull of observations from Dhis2
        return null;
    }

    @Override
    public void save(Observation observation) {

    }

    @Override
    public void save(List<Observation> observations) throws Exception {
        SurveyFilter surveyFilter = SurveyFilter.Builder.create()
                .WithSurveysToRetrieve(SurveyFilter.SurveysToRetrieve.ALL)
                .build();

        FromObservationEventMapper eventMapper =
                new FromObservationEventMapper(mContext, getSafeUsername(),
                        mSurveyLocalDataSource.getSurveys(surveyFilter));

        List<Event> events = eventMapper.map(observations);
        Set<String> eventUIds = new HashSet<>();

        for (Event event:events) {
            D2.events().save(event).toBlocking().single();
            eventUIds.add(event.getUId());
        }

        Map<String,ImportSummary> importSummaryMap =
                D2.events().push(eventUIds).toBlocking().single();
    }

    private String getSafeUsername() {
        UserDB user = Session.getUser();
        if (user != null) {
            return user.getUsername();
        }
        return "";
    }
}

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

import org.eyeseetea.malariacare.data.boundaries.ISyncDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.ISyncDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.sync.mappers.PushReportMapper;
import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObservationSDKDhisDataSource implements ISyncDataRemoteDataSource {

    private final Context mContext;
    private final ISyncDataLocalDataSource mSurveyLocalDataSource;

    public ObservationSDKDhisDataSource(Context context, ISyncDataLocalDataSource surveyLocalDataSource) {
        mContext = context;
        mSurveyLocalDataSource = surveyLocalDataSource;
    }

    @Override
    public List<? extends ISyncData> get(SurveyFilter filters) throws Exception {
        // Not used for the moment
        // The app not realize pull of observations from Dhis2
        return null;
    }

    @Override
    public Map<String, PushReport> save(List<? extends ISyncData> syncData) throws Exception {
        List<Observation> observations = (List<Observation>) syncData;

        FromObservationEventMapper eventMapper =
                new FromObservationEventMapper(mContext, getSafeUsername(),
                        (List<Survey>) mSurveyLocalDataSource.getAll());

        List<Event> events = eventMapper.map(observations);
        Set<String> eventUIds = new HashSet<>();

        for (Event event:events) {
            D2.events().save(event).toBlocking().single();
            eventUIds.add(event.getUId());
        }

        Map<String,ImportSummary> importSummaryMap =
                D2.events().push(eventUIds).toBlocking().single();

        Map<String, PushReport> pushReportMap =
                PushReportMapper.mapFromImportSummariesToPushReports(importSummaryMap);

        return pushReportMap;
    }

    private String getSafeUsername() {
        UserDB user = Session.getUser();
        if (user != null) {
            return user.getUsername();
        }
        return "";
    }
}

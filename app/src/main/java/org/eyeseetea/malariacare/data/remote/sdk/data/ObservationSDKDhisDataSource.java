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

import org.eyeseetea.malariacare.data.boundaries.IDataLocalDataSource;
import org.eyeseetea.malariacare.data.boundaries.IDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.sync.mappers.PushReportMapper;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.IData;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.usecase.pull.PullSurveyFilter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.common.state.Action;
import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObservationSDKDhisDataSource implements IDataRemoteDataSource {

    private final Context mContext;
    private final IDataLocalDataSource mSurveyLocalDataSource;
    private final IOptionRepository mOptionRepository;
    private final IServerMetadataRepository mServerMetadataRepository;

    public ObservationSDKDhisDataSource(Context context, IDataLocalDataSource surveyLocalDataSource,
            IServerMetadataRepository serverMetadataRepository,
            IOptionRepository optionRepository) {
        mContext = context;
        mSurveyLocalDataSource = surveyLocalDataSource;
        mOptionRepository = optionRepository;
        mServerMetadataRepository = serverMetadataRepository;
    }

    @Override
    public List<? extends IData> get(PullSurveyFilter filters) throws Exception {
        // Not used for the moment
        // The app not realize pull of observations from Dhis2
        return null;
    }

    @Override
    public Map<String, PushReport> save(List<? extends IData> dataList) throws Exception {
        List<Observation> observations = (List<Observation>) dataList;

        ServerMetadata serverMetadata = mServerMetadataRepository.getServerMetadata();
        List<Option> options = mOptionRepository.getAll();

        FromObservationEventMapper eventMapper =
                new FromObservationEventMapper(mContext, getSafeUsername(),
                        (List<Survey>) mSurveyLocalDataSource.getAllData(), options,
                        serverMetadata);

        List<Event> events = eventMapper.map(observations);
        Set<String> eventUIds = new HashSet<>();

        for (Event event:events) {
            D2.events().save(event).toBlocking().single();
            eventUIds.add(event.getUId());
        }

        Map<String,ImportSummary> importSummaryMap =
                D2.events().push(eventUIds, Action.TO_UPDATE).toBlocking().single();

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

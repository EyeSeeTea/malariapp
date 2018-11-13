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

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.boundaries.IDataRemoteDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.repositories.ICompositeScoreRepository;
import org.eyeseetea.malariacare.data.sync.mappers.PushReportMapper;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOrgUnitRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.CompositeScore;
import org.eyeseetea.malariacare.domain.entity.IData;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.usecase.pull.PullSurveyFilter;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.event.EventFilters;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurveySDKDhisDataSource implements IDataRemoteDataSource {

    private final IServerMetadataRepository mServerMetadataRepository;
    private final IOptionRepository mOptionRepository;
    private final IQuestionRepository mQuestionRepository;
    private final IOrgUnitRepository mOrgUnitRepository;
    private final ICompositeScoreRepository mCompositeScoreRepository;
    private final Context mContext;

    public SurveySDKDhisDataSource(Context context, IServerMetadataRepository serverMetadataRepository,
            IQuestionRepository questionRepository, IOptionRepository optionRepository,
            ICompositeScoreRepository mCompositeScoreRepository,
            IOrgUnitRepository orgUnitRepository, IConnectivityManager connectivityManager) {
        this.mContext = context;
        this.mServerMetadataRepository = serverMetadataRepository;
        this.mQuestionRepository = questionRepository;
        this.mOptionRepository = optionRepository;
        this.mCompositeScoreRepository = mCompositeScoreRepository;
        this.mOrgUnitRepository = orgUnitRepository;
    }

    @Override
    public List<? extends IData> get(PullSurveyFilter filters) throws Exception {

            pullEvents(filters);

            List<Survey> surveys = convertToSurveys();

            return surveys;
    }


    @Override
    public Map<String, PushReport> save(List<? extends IData> dataList) throws Exception {
        List<Survey> surveys = (List<Survey>) dataList;

        ServerMetadata serverMetadata = mServerMetadataRepository.getServerMetadata();
        List<Option> options = mOptionRepository.getAll();

        FromSurveyEventMapper eventMapper =
                new FromSurveyEventMapper(mContext, getSafeUsername(), options, serverMetadata);

        List<Event> events = eventMapper.map(surveys);
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

    private void pullEvents(PullSurveyFilter filters) {
        for (OrganisationUnit organisationUnit : D2.me().organisationUnits().list().toBlocking()
                .single()) {
            for (Program program : getValidPrograms(organisationUnit)) {

                EventFilters eventFilters = new EventFilters();
                eventFilters.setProgramUId(program.getUId());
                eventFilters.setOrganisationUnitUId(organisationUnit.getUId());
                eventFilters.setStartDate(filters.getStartDate());
                eventFilters.setEndDate(filters.getEndDate());
                eventFilters.setMaxEvents(filters.getMaxSize());

                D2.events().pull(eventFilters).toBlocking().single();
            }
        }
    }

    private List<Survey> convertToSurveys() {
        ServerMetadata serverMetadata = mServerMetadataRepository.getServerMetadata();
        List<Option> options = mOptionRepository.getAll();
        List<Question> questions = mQuestionRepository.getAll();
        List<CompositeScore> compositeScores = mCompositeScoreRepository.getAll();
        List<OrgUnit> orgUnits = mOrgUnitRepository.getAll();

        SurveyMapper surveyMapper = new SurveyMapper(serverMetadata, orgUnits,compositeScores, questions,
                options);

        List<Event> events = getDownloadedEvents();

        List<Survey> surveys = surveyMapper.mapSurveys(events);

        return surveys;
    }

    private List<Event> getDownloadedEvents() {
        List<Event> events = D2.events().list().toBlocking().first();

        List<TrackedEntityDataValue> allValues = D2.trackedEntityDataValues().list().toBlocking().first();

        Collections.sort(allValues, new Comparator<TrackedEntityDataValue>() {
            @Override
            public int compare(TrackedEntityDataValue object1, TrackedEntityDataValue object2)
            {
                return  object1.getDataElement().compareTo(object2.getDataElement());
            }
        });

        Map<String, List<TrackedEntityDataValue>> valuesMap = new HashMap<>();
        for (TrackedEntityDataValue value : allValues) {
            if (!valuesMap.containsKey(value.getEvent().getUId()))
                valuesMap.put(value.getEvent().getUId(), new ArrayList<TrackedEntityDataValue>());

            valuesMap.get(value.getEvent().getUId()).add(value);
        }

        for (Event event : events) {
            if (valuesMap.containsKey(event.getUId())){
                event.setDataValues(valuesMap.get(event.getUId()));
            }
        }

        return events;
    }

    private List<Program> getValidPrograms(OrganisationUnit organisationUnit) {
        Set<ProgramType> programTypes = new HashSet<>();
        programTypes.add(ProgramType.WITHOUT_REGISTRATION);

        String attributeCodeToFilter =
                PreferencesState.getInstance().getContext().getString(R.string.program_type_code);
        String attributeValueToFilter =
                PreferencesState.getInstance().getContext().getString(R.string.pull_program_code);

        List<Program> allPrograms = D2.me().programs()
                .list(organisationUnit, programTypes, attributeCodeToFilter, attributeValueToFilter)
                .toBlocking().first();

        return allPrograms;
    }

    private String getSafeUsername() {
        UserDB user = Session.getUser();
        if (user != null) {
            return user.getUsername();
        }
        return "";
    }

}

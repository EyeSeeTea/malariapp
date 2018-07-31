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

package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.sdk.data.SurveyMapper;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.IConnectivityManager;
import org.eyeseetea.malariacare.domain.boundary.repositories.IOptionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IQuestionRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.eyeseetea.malariacare.utils.DateParser;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.core.event.EventFilters;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurveyAPIDataSource extends OkHttpClientDataSource implements ISurveyDataSource {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
                    + "&fields=event,orgUnit,program,dataValues";

    private static final String TAG = ".PullDhisApiDataSource";

    public static final String EVENT = "event";
    private static String uid;
    IServerMetadataRepository serverMetadataRepository;

    public SurveyAPIDataSource(Credentials credentials, IServerMetadataRepository serverMetadataRepository) {
        super(credentials);

        uid = serverMetadataRepository.getServerMetadata().getCreationDate().getUId();
    }

    @Override
    public List<Survey> getSurveys(SurveyFilter filters) throws Exception {
        //Here pull surveys code
        return null;
    }

    @Override
    public List<String> existOnServerList(SurveyFilter filter) throws Exception {
        return getEventsCompletionDatesOnServer(filter.getProgramUId(), filter.getOrgunitUId(), filter.getStartDate(), filter.getEndDate());
    }


    @Override
    public void Save(List<Survey> surveys) throws Exception {
        //Here push surveys code
    }



    private List<String> pullQuarantineEvents(String url) throws IOException, JSONException {
        String response = executeCall(url);
        JSONObject events = new JSONObject(response);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(mapper.readTree(events.toString()),
                JsonNode.class);
        return existOnServerFromJson(jsonNode);
    }


    /**
     * Get events filtered by program orgUnit and between dates.
     */
    public List<String> getEventsCompletionDatesOnServer(String program, String orgUnit, Date minDate,
                                         Date maxDate) throws IOException, JSONException {
        String startDate = DateParser.formatAmerican(minDate);
        String endDate = DateParser.formatAmerican(
                new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)));
        String url = String.format(DHIS_CHECK_EVENT_API, program, orgUnit, startDate,
                endDate);
        Log.d(TAG, url);
        return pullQuarantineEvents(url);
    }


    public static List<String> existOnServerFromJson(JsonNode jsonNode) {
        TypeReference<List<Event>> typeRef =
                new TypeReference<List<Event>>() {
                };
        List<Event> events;
        try {
            if (jsonNode.has("events")) {
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JodaModule());
                events = objectMapper.
                        readValue(jsonNode.get("events").traverse(), typeRef);
            } else {
                events = new ArrayList<>();
            }
        } catch (IOException e) {
            events = new ArrayList<>();
            e.printStackTrace();
        }
        List<String> completionDateValuesList = new ArrayList<>();
        for (Event event : events) {
            if (event.getDataValues() != null && event.getDataValues().size() > 0) {
                for(TrackedEntityDataValue trackedEntityDataValue: event.getDataValues()){
                  if(trackedEntityDataValue.getDataElement().equals(uid)){
                      completionDateValuesList.add(trackedEntityDataValue.getValue());
                  }
                }
            }
        }
        return completionDateValuesList;
    }
}

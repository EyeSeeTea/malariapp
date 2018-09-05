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
import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.eyeseetea.malariacare.utils.DateParser;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurveyAPIDataSource extends OkHttpClientDataSource implements ISurveyDataSource {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
                    + "&fields=event,orgUnit,program,dataValues";

    private static final String TAG = ".PullDhisApiDataSource";

    public static final String EVENT = "event";
    private static String uid;

    public SurveyAPIDataSource(Credentials credentials, IServerMetadataRepository serverMetadataRepository) {
        super(credentials);

        uid = serverMetadataRepository.getServerMetadata().getCreationDate().getUId();
    }

    @Override
    public List<Survey> getSurveys(SurveyFilter filter) throws Exception {
        //Here pull surveys code
        List<Survey> surveys = new ArrayList<>();
        if(filter.isQuarantineSurvey()) {
            surveys = getEventsCompletionDatesOnServer(filter.getProgramUId(), filter.getOrgUnitUId(), filter.getStartDate(), filter.getEndDate());
        }
        return surveys;
    }


    @Override
    public void Save(List<Survey> surveys) throws Exception {
        //Here push surveys code
    }



    private List<Survey> pullQuarantineEvents(String url, String program, String orgUnit) throws IOException, JSONException {
        String response = executeCall(url);
        JSONObject events = new JSONObject(response);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(mapper.readTree(events.toString()),
                JsonNode.class);
        return existOnServerFromJson(jsonNode, program, orgUnit);
    }


    /**
     * Returns a list of surveys with the correct creation date ( setted from trackedentitydatavalue value)
     */
    private List<Survey> getEventsCompletionDatesOnServer(String program, String orgUnit, Date minDate,
                                         Date maxDate) throws IOException, JSONException {
        String startDate = DateParser.formatAmerican(minDate);
        String endDate = DateParser.formatAmerican(
                new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)));
        String url = String.format(DHIS_CHECK_EVENT_API, program, orgUnit, startDate,
                endDate);
        Log.d(TAG, url);
        return pullQuarantineEvents(url, program, orgUnit);
    }


    private static List<Survey> existOnServerFromJson(JsonNode jsonNode, String program, String orgUnit) {
        TypeReference<List<Event>> typeRef =
                new TypeReference<List<Event>>() {
                };
        List<Event> events;

        events = getEventsFromJson(jsonNode, typeRef);

        List<Survey> completionList = addSurveysFromEventsWithCreationDate(program, orgUnit, events);

        return completionList;
    }

    private static List<Survey> addSurveysFromEventsWithCreationDate(String program, String orgUnit, List<Event> events) {
        List<Survey> completionList = new ArrayList<>();
        for (Event event : events) {
            if (event.getDataValues() != null && event.getDataValues().size() > 0) {
                for(TrackedEntityDataValue trackedEntityDataValue: event.getDataValues()){
                  if(trackedEntityDataValue.getDataElement().equals(uid)){
                      Survey survey = Survey.createQuarantineSentSurvey(event.getUId(),
                              program,
                              orgUnit,
                              trackedEntityDataValue.getStoredBy(),
                              DateParser.parseLongDate(trackedEntityDataValue.getValue()));
                      completionList.add(survey);
                  }
                }
            }
        }
        return completionList;
    }

    private static List<Event> getEventsFromJson(JsonNode jsonNode, TypeReference<List<Event>> typeRef) {
        List<Event> events = new ArrayList<>();
        try {
            if (jsonNode.has("events")) {
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JodaModule());
                events = objectMapper.
                        readValue(jsonNode.get("events").traverse(), typeRef);
            } else {
                events = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }
}

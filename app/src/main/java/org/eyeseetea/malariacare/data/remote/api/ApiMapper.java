package org.eyeseetea.malariacare.data.remote.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiMapper {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?event=%s";

    public static final String EVENT = "events";
    public static final String SEPARATOR_TOKEN = ";";

    public static String getFilteredEventPath(List<String> uids) {
        String endpoint = getUidFilter(uids);
        String url = String.format(DHIS_CHECK_EVENT_API, endpoint);
        return url;
    }

    private static String getUidFilter(List<String> uids) {
        String endpoint = "";
        for(String uid:uids){
            endpoint+=uid + SEPARATOR_TOKEN;
        }
        //remove last token
        if(!endpoint.isEmpty()) {
            endpoint = endpoint.substring(0, endpoint.lastIndexOf(SEPARATOR_TOKEN));
        }
        return endpoint;
    }

    public static List<Survey> mapSurveysFromJson(String response) throws IOException, JSONException {
        List<Event> events = getEventsFromResponse(response);

        List<Survey> completionList = addSurveysFromEventsWithCreationDate(events);
        return completionList;
    }

    private static List<Event> getEventsFromResponse(String response) throws IOException {
        List<Event> events;
        TypeReference<List<Event>> typeRef =
                new TypeReference<List<Event>>() {
                };

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(mapper.readTree(response),
                JsonNode.class);
        if (jsonNode.has(EVENT)) {
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new JodaModule());
            events = objectMapper.
                    readValue(jsonNode.get(EVENT).traverse(), typeRef);
        } else {
            events = new ArrayList<>();
        }
        return events;
    }

    private static List<Survey> addSurveysFromEventsWithCreationDate(List<Event> events) {
        List<Survey> completionList = new ArrayList<>();
        for (Event event : events) {
            if (event.getDataValues() != null && event.getDataValues().size() > 0) {
                Survey survey = Survey.createQuarantineSentSurvey(event.getUId(),
                        event.getProgram(),
                        event.getOrgUnit(),
                        event.getDataValues().get(0).getStoredBy());
                completionList.add(survey);
            }
        }
        return completionList;
    }
}

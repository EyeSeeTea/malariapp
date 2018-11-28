package org.eyeseetea.malariacare.data.remote.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiMapper {

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?event=%s";

    public static final String EVENT = "events";

    public static String getFilteredEventPath(List<String> uids) {
        String endpoint = getUidFilter(uids);
        String url = String.format(DHIS_CHECK_EVENT_API, endpoint);
        return url;
    }

    private static String getUidFilter(List<String> uids) {
        String endpoint = "";
        for(int i=0; i<uids.size();i++){
            endpoint+=uids.get(i);
            if(i!=uids.size()-1) {
                endpoint += ";";
            }
        }
        return endpoint;
    }

    private static List<Event> getEventsFromJson(JSONObject jsonObject) throws IOException {
        List<Event> events = new ArrayList<>();
        TypeReference<List<Event>> typeRef =
                new TypeReference<List<Event>>() {
                };

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.convertValue(mapper.readTree(jsonObject.toString()),
                    JsonNode.class);
            if (jsonNode.has(EVENT)) {
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new JodaModule());
                events = objectMapper.
                        readValue(jsonNode.get(EVENT).traverse(), typeRef);
            } else {
                events = new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }

    public static List<Survey> mapSurveysFromJson(String response) throws IOException, JSONException {
        JSONObject eventAsJsonObject = null;
        eventAsJsonObject = new JSONObject(response);

        List<Event> events = getEventsFromJson(eventAsJsonObject);

        List<Survey> completionList = addSurveysFromEventsWithCreationDate(events);
        return completionList;
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

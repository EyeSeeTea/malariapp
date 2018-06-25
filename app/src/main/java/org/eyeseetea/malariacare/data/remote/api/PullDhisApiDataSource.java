package org.eyeseetea.malariacare.data.remote.api;


import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.exception.ClosedUserDateNotFoundException;
import org.eyeseetea.malariacare.domain.exception.PullApiParsingException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class PullDhisApiDataSource extends OkHttpClientDataSource{

    private static final String DHIS_PULL_API="/api/";

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
                    + "&fields=event,orgUnit,program,dataValues";

    private static String QUERY_USER_ATTRIBUTES =
            "/%s?fields=attributeValues[value,attribute[code]]id&paging=false";


    private static final String TAG = ".PullDhisApiDataSource";

    public static final String EVENT = "event";
    public static final String ATTRIBUTEVALUES = "attributeValues";
    public static final String ATTRIBUTE = "attribute";
    public static final String VALUE = "value";
    public static final String CODE = "code";
    private static final String USER = "users";

    public PullDhisApiDataSource(Credentials credentials) {
        super(credentials);
    }

    public boolean isUserClosed(String userUid) {
        if (Session.getCredentials().isDemoCredentials()) {
            return false;
        }
        String data = USER + String.format(QUERY_USER_ATTRIBUTES, userUid);
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));

        Date closedDate = null;
        try {
            String response = executeCall(DHIS_PULL_API+data, "GET");
            JsonNode jsonNode = parseResponse(response);
            closedDate = getClosedDate(jsonNode.get(ATTRIBUTEVALUES));
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
            return false;
        }
        return closedDate.before(new Date());
    }

    public UserDB pullUserAttributes(UserDB appUser) {
        String lastMessage = appUser.getAnnouncement();

        String data = USER + String.format(QUERY_USER_ATTRIBUTES, appUser.getUid());
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));
        try {
            String response = executeCall(DHIS_PULL_API+data, "GET");
            JsonNode jsonNode = parseResponse(response);
            JsonNode jsonNodeArray = jsonNode.get(ATTRIBUTEVALUES);
            String newMessage = "";
            String closeDate = "";
            for (int i = 0; i < jsonNodeArray.size(); i++) {
                newMessage =
                        getUserAnnouncement(jsonNodeArray, newMessage, i,
                                UserDB.ATTRIBUTE_USER_ANNOUNCEMENT);
                closeDate = getUserCloseDate(jsonNodeArray, closeDate, i);
            }
            saveNewAnnoucement(appUser, lastMessage, newMessage);
            saveClosedDate(appUser, closeDate);

        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
        }
        return appUser;
    }

    /**
     * Get events filtered by program orgUnit and between dates.
     */
    public List<EventExtended> getEvents(String program, String orgUnit, Date minDate,
            Date maxDate) throws Exception {
        String startDate = EventExtended.format(minDate, EventExtended.AMERICAN_DATE_FORMAT);
        String endDate = EventExtended.format(
                new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)),
                EventExtended.AMERICAN_DATE_FORMAT);
        String url = String.format(DHIS_CHECK_EVENT_API, program, orgUnit, startDate,
                endDate);
        Log.d(TAG, url);
        return pullQuarantineEvents(url);
    }

    private List<EventExtended> pullQuarantineEvents(String url) throws Exception {
        String response = executeCall(url, "GET");
        JSONObject events = new JSONObject(response);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(mapper.readTree(events.toString()),
                JsonNode.class);
        return EventExtended.fromJsonToEvents(jsonNode);
    }

    private static void saveNewAnnoucement(UserDB appUser, String lastMessage, String newMessage) {
        if ((lastMessage == null && newMessage != null) || (newMessage != null
                && !newMessage.equals("") && !lastMessage.equals(newMessage))) {
            appUser.setAnnouncement(newMessage);
            PreferencesState.getInstance().setUserAccept(false);
        }
    }

    private static void saveClosedDate(UserDB appUser, String closeDate) {
        if (closeDate == null || closeDate.equals("")) {
            appUser.setCloseDate(null);
        } else {
            appUser.setCloseDate(EventExtended.parseNewLongDate(closeDate));
        }
    }

    private static String getUserCloseDate(JsonNode jsonNodeArray, String closeDate, int i) {
        if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                UserDB.ATTRIBUTE_USER_CLOSE_DATE)) {
            closeDate = jsonNodeArray.get(i).get(VALUE).textValue();
        }
        return closeDate;
    }

    private static String getUserAnnouncement(JsonNode jsonNodeArray, String newMessage, int i,
            String attributeUserAnnouncement) {
        if (jsonNodeArray.get(i).get(ATTRIBUTE).get(CODE).textValue().equals(
                attributeUserAnnouncement)) {
            newMessage = jsonNodeArray.get(i).get(VALUE).textValue();
        }
        return newMessage;
    }

    private static Date getClosedDate(JsonNode jsonNodeArray)
            throws ClosedUserDateNotFoundException {

        String closeDateAsString = "";
        for (int i = 0; i < jsonNodeArray.size(); i++) {
            closeDateAsString = getUserCloseDate(jsonNodeArray, closeDateAsString, i);
        }
        if (closeDateAsString == null || closeDateAsString.equals("")) {
            throw new ClosedUserDateNotFoundException();
        }
        return EventExtended.parseNewLongDate(closeDateAsString);
    }

    private static JsonNode parseResponse(String responseData)throws Exception{
        try{
            JSONObject jsonResponse=new JSONObject(responseData);
            Log.i("JsonCommonParser", "parseResponse: " + jsonResponse);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = jsonResponse.toString();
            try {
                return objectMapper.readValue(jsonString, JsonNode.class);
            }catch(Exception ex){
                throw new PullApiParsingException();
            }
        }catch(Exception ex){
            throw new PullApiParsingException();
        }
    }

}

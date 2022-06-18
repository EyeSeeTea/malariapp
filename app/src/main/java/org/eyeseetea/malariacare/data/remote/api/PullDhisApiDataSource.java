package org.eyeseetea.malariacare.data.remote.api;


import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.exception.ClosedUserDateNotFoundException;
import org.eyeseetea.malariacare.utils.DateParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.executeCall;
import static org.eyeseetea.malariacare.data.remote.api.OkHttpClientDataSource.parseResponse;

public class PullDhisApiDataSource {

    private static final String DHIS_PULL_API="api/";

    private static final String DHIS_CHECK_EVENT_API =
            "api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
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

    public PullDhisApiDataSource() {
    }

    public static UserDB pullUserAttributes(UserDB appUser) {
        if (appUser != null) {
            String lastMessage = appUser.getAnnouncement();

            String data = USER + String.format(QUERY_USER_ATTRIBUTES, appUser.getUid());
            Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));
            try {
                Response response = executeCall(DHIS_PULL_API + data);
                JsonNode jsonNode = parseResponse(response.body().string());
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
        }

        return appUser;
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
            DateParser dateParser = new DateParser();
            appUser.setCloseDate(dateParser.parseDate(closeDate, DateParser.LONG_DATE_FORMAT));
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

    public static boolean isUserClosed(String userUid) {
        if (Session.getCredentials().isDemoCredentials()) {
            return false;
        }
        String data = USER + String.format(QUERY_USER_ATTRIBUTES, userUid);
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));

        Date closedDate = null;
        try {
            Response response = executeCall(DHIS_PULL_API+data);
            JsonNode jsonNode = parseResponse(response.body().string());
            closedDate = getClosedDate(jsonNode.get(ATTRIBUTEVALUES));
        } catch (Exception ex) {
            Log.e(TAG, "Cannot read user last updated from server with");
            ex.printStackTrace();
            return false;
        }
        return closedDate.before(new Date());
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
        DateParser dateParser = new DateParser();
        return dateParser.parseDate(closeDateAsString, DateParser.LONG_DATE_FORMAT);
    }

    public static List<EventExtended> pullQuarantineEvents(String url) throws IOException, JSONException {
        Response response = executeCall(url);
        JSONObject events = new JSONObject(response.body().string());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(mapper.readTree(events.toString()),
                JsonNode.class);
        return EventExtended.fromJsonToEvents(jsonNode);
    }


    /**
     * Get events filtered by program orgUnit and between dates.
     */
    public static List<EventExtended> getEvents(String program, String orgUnit, Date minDate,
            Date maxDate) throws IOException, JSONException {
        DateParser dateParser = new DateParser();
        String startDate = dateParser.format(minDate, DateParser.AMERICAN_DATE_FORMAT);
        String endDate = dateParser.format(
                new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)),
                DateParser.AMERICAN_DATE_FORMAT);
        String url = String.format(DHIS_CHECK_EVENT_API, program, orgUnit, startDate,
                endDate);
        Log.d(TAG, url);
        return PullDhisApiDataSource.pullQuarantineEvents(url);
    }

}

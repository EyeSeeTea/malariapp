package org.eyeseetea.malariacare.data.remote.api;


import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.exception.ClosedUserDateNotFoundException;
import org.eyeseetea.malariacare.domain.exception.PullApiParsingException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PullDhisApiDataSource {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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

    public PullDhisApiDataSource() {
    }

    public static UserDB pullUserAttributes(UserDB appUser) {
        String lastMessage = appUser.getAnnouncement();

        String data = USER + String.format(QUERY_USER_ATTRIBUTES, appUser.getUid());
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));
        try {
            Response response = executeCall(DHIS_PULL_API+data, "GET");
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

    public static boolean isUserClosed(String userUid) {
        if (Session.getCredentials().isDemoCredentials()) {
            return false;
        }
        String data = USER + String.format(QUERY_USER_ATTRIBUTES, userUid);
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", USER, data));

        Date closedDate = null;
        try {
            Response response = executeCall(DHIS_PULL_API+data, "GET");
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
        return EventExtended.parseNewLongDate(closeDateAsString);
    }

    public static List<EventExtended> pullQuarantineEvents(String url) throws IOException, JSONException {
        Response response = executeCall(url, "GET");
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
        String startDate = EventExtended.format(minDate, EventExtended.AMERICAN_DATE_FORMAT);
        String endDate = EventExtended.format(
                new Date(maxDate.getTime() + (8 * 24 * 60 * 60 * 1000)),
                EventExtended.AMERICAN_DATE_FORMAT);
        String url = String.format(DHIS_CHECK_EVENT_API, program, orgUnit, startDate,
                endDate);
        Log.d(TAG, url);
        return PullDhisApiDataSource.pullQuarantineEvents(url);
    }


    /**
     * Call to DHIS Server
     * @param data
     * @param method
     * @param url
     */
    public static Response executeCall(JSONObject data, String url, String method) throws IOException {
        final String DHIS_URL=PreferencesState.getInstance().getServerUrl() + url.replace(" ", "%20");

        Log.d(TAG, "executeCall Url" + DHIS_URL + "");

        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        client.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(30, TimeUnit.SECONDS);    // socket timeout
        client.setWriteTimeout(30, TimeUnit.SECONDS);    // write timeout
        client.setRetryOnConnectionFailure(false); // Cancel retry on failure

        BasicAuthenticator basicAuthenticator=new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);

        Request.Builder builder = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_URL);

        switch (method){
            case "POST":
                RequestBody postBody = RequestBody.create(JSON, data.toString());
                builder.post(postBody);
                break;
            case "PUT":
                RequestBody putBody = RequestBody.create(JSON, data.toString());
                builder.put(putBody);
                break;
            case "PATCH":
                RequestBody patchBody = RequestBody.create(JSON, data.toString());
                builder.patch(patchBody);
                break;
            case "GET":
                builder.get();
                break;
        }

        Request request = builder.build();
        Response response = client.newCall(request).execute();;
        if (!response.isSuccessful()) {
            Log.e(TAG, "pushData (" + response.code() + "): " + response.body().string());
            throw new IOException(response.message());
        }
        return response;
    }

    /**
     * Call to DHIS Server
     * @param url
     * @param method
     */
    public static Response executeCall(String url, String method) throws IOException {
        return executeCall(null, url, method);
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

    /**
     * Basic
     */
    static class BasicAuthenticator implements Authenticator {

        public final String AUTHORIZATION_HEADER="Authorization";
        private String credentials;
        private int mCounter = 0;

        org.eyeseetea.malariacare.domain.entity.Credentials userCredentials =
                PreferencesState.getInstance().getCreedentials();
        BasicAuthenticator(){
            credentials = Credentials.basic(userCredentials.getUsername(), userCredentials.getPassword());
        }

        @Override
        public Request authenticate(Proxy proxy, Response response) throws IOException {

            if (mCounter++ > 0) {
                throw new IOException(response.message());
            }
            return response.request().newBuilder().header(AUTHORIZATION_HEADER, credentials).build();
        }

        @Override
        public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
            return null;
        }

        public String getCredentials(){
            return credentials;
        }

    }

}

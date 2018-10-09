package org.eyeseetea.malariacare.data.remote.api;


import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.repositories.SettingsRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.Settings;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PullDhisApiDataSource {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String DHIS_CHECK_EVENT_API =
            "/api/events.json?program=%s&orgUnit=%s&startDate=%s&endDate=%s&skipPaging=true"
                    + "&fields=event,orgUnit,program,dataValues";

    private static final String TAG = ".PullDhisApiDataSource";

    public static final String EVENT = "event";

    public PullDhisApiDataSource() {
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
        //todo this method is refactorized in other branch, we should call the settings activity repository in activity layer and dont use Preferencestate.
        ISettingsRepository settingsRepository = new SettingsRepository(PreferencesState.getInstance().getContext());
        Settings settings = settingsRepository.getSettings();
        final String DHIS_URL=settings.getServer().getUrl() + url.replace(" ", "%20");

        Log.d(TAG, "executeCall Url" + DHIS_URL + "");

        BasicAuthenticator basicAuthenticator = new BasicAuthenticator();

        OkHttpClient client = UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient(basicAuthenticator);

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


}

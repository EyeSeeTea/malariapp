package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eyeseetea.malariacare.data.remote.api.BasicAuthenticator;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.exception.PullApiParsingException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpClientDataSource {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String TAG = ".PullDhisApiDataSource";

    /**
     * Call to DHIS Server
     * @param apiCall
     */
    public static Response executeCall(String apiCall) throws IOException {
        return executeCall(new BasicAuthenticator(PreferencesState.getInstance().getCreedentials()), PreferencesState.getInstance().getServer().getUrl(), apiCall);
    }

    /**
     * Call to DHIS Server
     * @param url
     */
    public static Response executeCall(BasicAuthenticator basicAuthenticator, String url, String apiCall) throws IOException {
        if (!url.substring(url.length()-1).equals("/")) {
            url = url + "/";
        }
        url = url + apiCall;
        url = url.replace(" ", "%20");
        Log.d(TAG, "executeCall Url " + url + "");

        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        client.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(30, TimeUnit.SECONDS);    // socket timeout
        client.setWriteTimeout(30, TimeUnit.SECONDS);    // write timeout
        client.setRetryOnConnectionFailure(false); // Cancel retry on failure
        client.setAuthenticator(basicAuthenticator);

        Request.Builder builder = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(url);

        builder.get();

        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            Log.e(TAG, "pushData (" + response.code() + "): " + response.body().string());
            throw new IOException(response.message());
        }
        return response;
    }

    public static JsonNode parseResponse(String responseData)throws Exception{
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

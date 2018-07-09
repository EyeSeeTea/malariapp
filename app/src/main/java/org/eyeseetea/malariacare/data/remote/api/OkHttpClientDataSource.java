package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.exception.PullApiParsingException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class OkHttpClientDataSource {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String TAG = ".PullDhisApiDataSource";
    private org.eyeseetea.malariacare.domain.entity.Credentials mCredentials;

    public OkHttpClientDataSource(Credentials credentials) {
        mCredentials = credentials;
    }

    /**
     * Call to DHIS Server
     */
    public String executeCall(String url)
            throws Exception {
        Response response = executeCall(null, url);
        return response.body().string();
    }

    private Response executeCall(JSONObject data, String url) throws
            IOException {
        final String DHIS_URL = mCredentials.getServerURL() +
                url.replace(" ", "%20");

        Log.d(TAG, "executeCall Url" + DHIS_URL + "");

        OkHttpClient client = UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        client.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(30, TimeUnit.SECONDS);    // socket timeout
        client.setWriteTimeout(30, TimeUnit.SECONDS);    // write timeout
        client.setRetryOnConnectionFailure(false); // Cancel retry on failure

        BasicAuthenticator basicAuthenticator = new BasicAuthenticator(mCredentials.getUsername(),
                mCredentials.getPassword());
        client.setAuthenticator(basicAuthenticator);

        Request.Builder builder = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER,
                        basicAuthenticator.getCredentials())
                .url(DHIS_URL);

        builder.get();

        Request request = builder.build();
        Response response = client.newCall(request).execute();
        ;
        if (!response.isSuccessful()) {
            Log.e(TAG, "pushData (" + response.code() + "): " + response.body().string());
            throw new IOException(response.message());
        }
        return response;
    }

    protected JsonNode parseResponse(String responseData) throws Exception {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            Log.i("JsonCommonParser", "parseResponse: " + jsonResponse);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = jsonResponse.toString();
            try {
                return objectMapper.readValue(jsonString, JsonNode.class);
            } catch (Exception ex) {
                throw new PullApiParsingException();
            }
        } catch (Exception ex) {
            throw new PullApiParsingException();
        }
    }

    /**
     * Basic
     */
    private class BasicAuthenticator implements Authenticator {

        public final String AUTHORIZATION_HEADER = "Authorization";
        private String credentials;
        private int mCounter = 0;

        BasicAuthenticator(String user, String password) {
            credentials = com.squareup.okhttp.Credentials.basic(user, password);
        }

        @Override
        public Request authenticate(Proxy proxy, Response response) throws IOException {

            if (mCounter++ > 0) {
                throw new IOException(response.message());
            }
            return response.request().newBuilder().header(AUTHORIZATION_HEADER,
                    credentials).build();
        }

        @Override
        public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
            return null;
        }

        public String getCredentials() {
            return credentials;
        }

    }
}

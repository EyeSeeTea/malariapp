package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpClientDataSource {

    private static final String TAG = ".PullDhisApiDataSource";
    private org.eyeseetea.malariacare.domain.entity.Credentials mCredentials;

    public OkHttpClientDataSource(org.eyeseetea.malariacare.domain.entity.Credentials credentials) {
        mCredentials = credentials;
    }

    /**
     * Call to DHIS Server
     * @param url
     */
    public String executeCall(String url) throws
            IOException {
        final String DHIS_URL= mCredentials.getServerURL() + url.replace(" ", "%20");

        Log.d(TAG, "executeCall Url" + DHIS_URL + "");

        BasicAuthenticator basicAuthenticator = new BasicAuthenticator(mCredentials);
        OkHttpClient client = UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient(basicAuthenticator);


        Request.Builder builder = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_URL);

        builder.get();

        Request request = builder.build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            Log.e(TAG, "pushData (" + response.code() + "): " + response.body().string());
            throw new IOException(response.message());
        }
        return  response.body().string();
    }
}

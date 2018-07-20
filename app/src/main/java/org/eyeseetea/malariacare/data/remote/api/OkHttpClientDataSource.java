package org.eyeseetea.malariacare.data.remote.api;

import android.util.Log;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

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

        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        client.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(30, TimeUnit.SECONDS);    // socket timeout
        client.setWriteTimeout(30, TimeUnit.SECONDS);    // write timeout
        client.setRetryOnConnectionFailure(false); // Cancel retry on failure

        BasicAuthenticator basicAuthenticator=new BasicAuthenticator(mCredentials.getUsername(), mCredentials.getPassword());
        client.setAuthenticator(basicAuthenticator);

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
    /**
     * Basic
     */
    private class BasicAuthenticator implements Authenticator {

        public final String AUTHORIZATION_HEADER="Authorization";
        private String credentials;
        private int mCounter = 0;

        BasicAuthenticator(String user, String password){
            credentials = Credentials.basic(user, password);
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

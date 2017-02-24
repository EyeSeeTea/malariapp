/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import org.eyeseetea.malariacare.R;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;

/**
 * Created by idelcano on 04/04/2016.
 */
public class NetworkUtils {
    private String TAG=".NetworkUtils";

    private Context applicationContext;

    private static String DHIS_PUSH_API="/api/events";
    private static String DHIS_PULL_API="/api/";

    private static String DHIS_SERVER ="https://www.psi-mis.org";

    String user;
    String password;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public NetworkUtils(Context context){
        applicationContext=context;
    }

    public void setDhisServer(String dhisServer){
        DHIS_SERVER=dhisServer;
    }
    public void setUser(String user){
        this.user=user;
    }
    public void setPassword(String password){
        this.password=password;
    }

    /**
     * Pushes data to DHIS Server
     * @param data
     */
    public JSONObject pushData(JSONObject data)throws Exception {
        Response response = null;

        final String DHIS_URL = getDhisURL()+DHIS_PUSH_API;

        OkHttpClient client = UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);

        Log.d(TAG, "Url" + DHIS_URL + "");
        RequestBody body = RequestBody.create(JSON, data.toString());
        Request request = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_URL)
                .post(body)
                .build();

        response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            Log.e(TAG, "pushData (" + response.code() + "): " + response.body().string());
            throw new IOException(response.message());
        }
        return  parseResponse(response.body().string());
    }

    /**
     * Pull data from DHIS Server
     * @param data
     */
    public JSONObject getData(String data)throws Exception {
        Response response = null;

        final String DHIS_URL = getDhisURL()+DHIS_PULL_API+data;

        OkHttpClient client = UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);

        Log.d(TAG, "Url" + DHIS_URL + "");
        Request request = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_URL)
                .get()
                .build();

        response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            Log.e(TAG, "getData (" + response.code() + "): " + response.body().string());
            throw new IOException(response.message());
        }
        return  parseResponse(response.body().string());
    }

    public JsonNode toJsonNode(JSONObject jsonObject){
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = jsonObject.toString();
        try {
            return objectMapper.readValue(jsonString, JsonNode.class);
        }catch(Exception ex){
            return null;
        }
    }

    /**
     * Call to DHIS Server
     * @param data
     * @param url
     */
    private Response executeCall(JSONObject data, String url, String method) throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        final String DHIS_URL=sharedPreferences.getString(applicationContext.getString(R.string.dhis_url), applicationContext.getString(R.string.login_info_dhis_default_server_url)) + url;

        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

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
        return client.newCall(request).execute();
    }

    private JSONObject parseResponse(String responseData)throws Exception{
        try{
            JSONObject jsonResponse=new JSONObject(responseData);
            Log.i(TAG, "parseResponse: " + jsonResponse);
            return jsonResponse;
        }catch(Exception ex){
            throw new Exception(applicationContext.getString(R.string.dialog_info_push_bad_credentials));
        }
    }

    public String getDhisURL() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url),"");
    }

    /**
     * Basic
     */
    class BasicAuthenticator implements Authenticator {

        public final String AUTHORIZATION_HEADER="Authorization";
        private String credentials;
        private int mCounter = 0;

        BasicAuthenticator(){
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

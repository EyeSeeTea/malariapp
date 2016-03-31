/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Utils;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Event$Table;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushClient {

    private static final String TAG=".PushClient";

    private static String DHIS_PUSH_API="/api/events";

    private static String DHIS_SERVER ="https://www.psi-mis.org";

    public static String DHIS_UID_PROGRAM="";

    public static String DHIS_ORG_NAME ="";
    private static String DHIS_ORG_UID ="";


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");



//PictureQuestion


    Survey survey;
    String user;
    String password;
    Context applicationContext;

    public PushClient(Survey survey, Context applicationContext, String user, String password) {
        this.survey = survey;
        this.applicationContext = applicationContext;
        this.user = user;
        this.password = password;
        DHIS_UID_PROGRAM=survey.getTabGroup().getProgram().getUid();
        DHIS_ORG_NAME=survey.getOrgUnit().getName();
        DHIS_ORG_UID=survey.getOrgUnit().getUid();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        DHIS_SERVER =sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url),"");
        Log.d(TAG,"User: "+this.user+" Program: "+DHIS_UID_PROGRAM+" OrgUnit:"+DHIS_ORG_NAME+"OrgUnitUid:"+DHIS_ORG_UID+"Survey:"+survey.getId_survey());
    }

    public PushClient(Context applicationContext, String user, String password) {
        this.applicationContext = applicationContext;
        this.user = user;
        this.password = password;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        DHIS_SERVER =sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url),"");
    }
    private boolean launchPush(Survey survey) {
        //fixme the survey is saved in session. But in other places too.
        Session.setSurvey(survey);
        //Pushing selected survey via sdk
        List<Survey> surveys = new ArrayList<>();
        surveys.add(survey);
        return PushController.getInstance().push(PreferencesState.getInstance().getContext(), surveys);
    }

    public PushClient(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void pushSDK() {
        if (Utils.isNetworkAvailable()) {
            malariaSdkPush();
        }
    }

    public PushResult pushAPI() {
        if (Utils.isNetworkAvailable()) {
               return malariaApiPush();
        }
        return new PushResult();
    }

    public void malariaSdkPush() {
        try{

            if(launchPush(survey)){
                //TODO: This should be removed once DHIS bug is solved
                //pushControlDataElements(controlData);
                survey.setSentSurveyState();
                AlarmPushReceiver.setFail(false);
            }
            else{
                AlarmPushReceiver.setFail(true);
            }
        }catch(Exception ex){
            AlarmPushReceiver.setFail(true);
            Log.e(TAG, ex.getMessage());
        }
        finally {
            //Success or not the dashboard must be reloaded
            updateDashboard();
        }
    }

    public PushResult malariaApiPush() {
        PushResult pushResult;
        try{
            //TODO: This should be removed once DHIS bug is solved
            //Map<String, JSONObject> controlData = prepareControlData();
            survey.prepareSurveyUploadedDate();
            JSONObject data = PushUtils.getInstance().prepareMetadata(survey);
            //TODO: This should be removed once DHIS bug is solved
            //data = PushUtilsElements(data, controlData.get(""));
            data = PushUtils.getInstance().PushUtilsElements(data, survey);
            pushResult = new PushResult(pushData(data));
            if(pushResult.isSuccessful() && !pushResult.getImported().equals("0")){
                //TODO: This should be removed once DHIS bug is solved
                //pushControlDataElements(controlData);
                survey.setSentSurveyState();
                AlarmPushReceiver.setFail(false);
            }
            else{
                AlarmPushReceiver.setFail(true);
            }
        }catch(Exception ex){
            AlarmPushReceiver.setFail(true);
            Log.e(TAG, ex.getMessage());
            pushResult=new PushResult(ex);
        }
        finally {
            //Success or not the dashboard must be reloaded
            updateDashboard();
        }
        return  pushResult;
    }

    public Event getLastEvent(OrgUnit orgUnit,Program program){
        Event lastEvent=new Select().from(Event.class)
                .where(Condition.column(Event$Table.PROGRAMID).eq(program.getUid()))
                .and(Condition.column(Event$Table.ORGANISATIONUNITID).eq(orgUnit.getUid()))
                .groupBy(new QueryBuilder().appendQuotedArray(Event$Table.PROGRAMID, Event$Table.ORGANISATIONUNITID))
                .having(Condition.columnsWithFunction("max", "lastUpdated")).querySingle();
        Date lastLocalDate = null;
        try {
            lastLocalDate= EventExtended.parseDate(lastEvent.getLastUpdated(),EventExtended.DHIS2_DATE_FORMAT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(lastLocalDate!=null) {
            String data = PushUtils.getInstance().prepareLastEventData(orgUnit.getUid(), program.getUid(), lastLocalDate);
            try {
                JSONObject lastEventsList= getData(data);
                String eventuid="";
                Date lastDate=null;
                JSONArray jsonArrayResponse=new JSONArray(lastEventsList.getString("events"));
                for(int i=0;i<jsonArrayResponse.length();i++) {
                    JSONObject event = new JSONObject(jsonArrayResponse.getString(i));
                    if(eventuid.equals("")){
                        eventuid=event.getString("event");
                        lastDate=EventExtended.parseDate(event.getString("lastUpdated"),EventExtended.DHIS2_DATE_FORMAT);
                    }
                    else if(lastDate.after(lastDate=EventExtended.parseDate(event.getString("lastUpdated"),EventExtended.DHIS2_DATE_FORMAT))){
                        eventuid=event.getString("event");
                        lastDate=EventExtended.parseDate(event.getString("lastUpdated"), EventExtended.DHIS2_DATE_FORMAT);
                    }
                }
                return pullEvent(eventuid,lastLocalDate);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,"Error getting the data "+ data);
                return null;
            }
        }
        return null;
        //https://hnqis-dev-ci.psi-mis.org/api/events?orgUnit=QS7sK8XzdQc&program=wK0958s1bdj&startDate=2016-1-01&fields=[event,eventDate]&order=eventDate:desc
    }

    private Event pullEvent(String eventuid, Date lastUpdated) {
        Event localEvent= new Select().from(Event.class).where(Condition.column(Event$Table.ID).eq(eventuid)).querySingle();
        if(localEvent==null && localEvent.getLastUpdated().equals(EventExtended.format(lastUpdated,EventExtended.DHIS2_DATE_FORMAT)))

        Log.d(TAG,"we need donwload event" +eventuid);
                return null;
    }

    /**
     * Pushes data to DHIS Server
     * @param data
     */
    private JSONObject getData(String data)throws Exception {
        Response response = null;

        final String DHIS_URL = getDhisURL()+DHIS_PUSH_API+data;

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

    /**
     * Pushes data to DHIS Server
     * @param data
     */
    private JSONObject pushData(JSONObject data)throws Exception {
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

    public void updateDashboard(){
        //Reload data using service
        Intent surveysIntent=new Intent(applicationContext, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        applicationContext.startService(surveysIntent);
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
    class BasicAuthenticator implements  Authenticator{

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

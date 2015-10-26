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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushClient {

    private static final String TAG=".PushClient";

    private static String DHIS_PUSH_API="/api/events";

    private static String DHIS_ANALYTICS_CONTROL_DATA ="/api/analytics/events/query/";
    private static String DHIS_PUSH_CONTROL_DATA ="/api/events/";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static String COMPLETED="COMPLETED";

    private static String TAG_PROGRAM="program";
    private static String TAG_ORG_UNIT="orgUnit";
    private static String TAG_EVENTDATE="eventDate";
    private static String TAG_STATUS="status";
    private static String TAG_STOREDBY="storedBy";
    private static String TAG_COORDINATE="coordinate";
    private static String TAG_COORDINATE_LAT="latitude";
    private static String TAG_COORDINATE_LNG="longitude";
    private static String TAG_DATAVALUES="dataValues";
    private static String TAG_DATAELEMENT="dataElement";
    private static String TAG_VALUE="value";

    Survey survey;
    Activity activity;
    String user;
    String password;

    public PushClient(Survey survey, Activity activity, String user, String password) {
        this.survey = survey;
        this.activity = activity;
        this.user = user;
        this.password = password;
    }

    public PushResult push() {
        PushResult pushResult;
        try{
            //TODO: This should be removed once DHIS bug is solved
            //Map<String, JSONObject> controlData = prepareControlData();
            JSONObject data = prepareMetadata();
            //TODO: This should be removed once DHIS bug is solved
            //data = prepareDataElements(data, controlData.get(""));
            data = prepareDataElements(data, null);
            pushResult = new PushResult(pushData(data));
            if(pushResult.isSuccessful()){
                //TODO: This should be removed once DHIS bug is solved
                //pushControlDataElements(controlData);
                updateSurveyState();
            }
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage());
            pushResult=new PushResult(ex);
        }
        finally {
            //Success or not the dashboard must be reloaded
            updateDashboard();
        }
        return  pushResult;
    }

    public void updateSurveyState(){
        //Change status and save mainScore
        this.survey.setStatus(Constants.SURVEY_SENT);
        this.survey.update();
        this.survey.saveMainScore();
    }

    public void updateDashboard(){
        //Reload data using service
        Intent surveysIntent=new Intent(activity, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        activity.startService(surveysIntent);
    }

    /**
     * Adds metadata info to json object
     * @return JSONObject with program, orgunit, eventdate and so on...
     * @throws Exception
     */
    private JSONObject prepareMetadata() throws Exception{
        Log.d(TAG, "prepareMetadata for survey: " + survey.getId_survey());

        JSONObject object=new JSONObject();
        object.put(TAG_PROGRAM, survey.getTabGroup().getProgram().getUid());
        object.put(TAG_ORG_UNIT, survey.getOrgUnit().getUid());
        object.put(TAG_EVENTDATE, android.text.format.DateFormat.format("yyyy-MM-dd", survey.getCompletionDate()));
        object.put(TAG_STATUS,COMPLETED );
        object.put(TAG_STOREDBY, survey.getUser().getName());

        Location lastLocation = LocationMemory.get(survey.getId_survey());
        //If location is required but there is no location -> exception
        if(PreferencesState.getInstance().isLocationRequired() && lastLocation==null){
            throw new Exception(activity.getString(R.string.dialog_error_push_no_location_and_required));
        }
        //Otherwise (not required or there are coords)
        object.put(TAG_COORDINATE, prepareCoordinates(lastLocation));

        Log.d(TAG, "prepareMetadata: " + object.toString());
        return object;
    }

    private JSONObject prepareCoordinates(Location location) throws Exception{

        JSONObject coordinate = new JSONObject();

        if(location==null){
            coordinate.put(TAG_COORDINATE_LAT, JSONObject.NULL);
            coordinate.put(TAG_COORDINATE_LNG, JSONObject.NULL);
        }else{
            coordinate.put(TAG_COORDINATE_LAT, location.getLatitude());
            coordinate.put(TAG_COORDINATE_LNG, location.getLongitude());
        }

        return coordinate;
    }

    /**
     * Adds questions and scores values to the JSON object
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     * @param data JSON object to update
     * @throws Exception
     */
    private JSONObject prepareDataElements(JSONObject data, JSONObject controlDataElements)throws Exception{
        Log.d(TAG, "prepareDataElements for survey: " + survey.getId_survey());

        //Add dataElement per values
        //TODO: This should be removed once DHIS bug is solved
        //JSONArray values=prepareValues(new JSONArray(), controlDataElements.getJSONArray("root"));
        JSONArray values=prepareValues(new JSONArray(), null);

        //Add dataElement per compositeScores
        values=prepareCompositeScores(values);

        //Add main scores values
        values=prepareMainScoreValues(values);

        data.put(TAG_DATAVALUES, values);
        Log.d(TAG, "prepareDataElements result: " + data.toString());
        return data;
    }

    /**
     * Adds 4 additional values:
     *  - Main score
     *  - Boolean flag is type A
     *  - Boolean flag is type B
     *  - Boolean flag is type C
     * @param values
     * @return
     */
    private JSONArray prepareMainScoreValues(JSONArray values) throws Exception{
        JSONObject dataElement;
        //Main score
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, activity.getString(R.string.main_score));
        dataElement.put(TAG_VALUE, survey.getType());
        values.put(dataElement);

        //Type A
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, activity.getString(R.string.main_score_a));
        dataElement.put(TAG_VALUE, survey.isTypeA() ? "true" : "false");
        values.put(dataElement);

        //Type B
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, activity.getString(R.string.main_score_b));
        dataElement.put(TAG_VALUE, survey.isTypeB() ? "true" : "false");
        values.put(dataElement);

        //Type C
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, activity.getString(R.string.main_score_c));
        dataElement.put(TAG_VALUE, survey.isTypeC() ? "true" : "false");
        values.put(dataElement);

        return values;
    }

    /**
     * Add a dataElement per value (answer)
     * @param values
     * @return
     * @throws Exception
     */
    private JSONArray prepareValues(JSONArray values,JSONArray controlDataElements) throws Exception{
        List<Value> surveyValues=survey.getValues();
        if(surveyValues==null || surveyValues.size()==0){
            throw new Exception(activity.getString(R.string.dialog_info_push_empty_survey));
        }

        for (Value value : surveyValues) {
            values.put(prepareValue(value));
        }

        //TODO: This should be removed once DHIS bug is solved
        if (controlDataElements != null) {
            for (int i = 0; i < controlDataElements.length(); i++) {
                values.put(controlDataElements.get(i));
            }
        }
        return values;
    }

    private JSONArray prepareCompositeScores(JSONArray values) throws Exception{

        //Prepare scores info
        List<CompositeScore> compositeScoreList=ScoreRegister.loadCompositeScores(survey);

        //Calculate main score to push later
        survey.setMainScore(ScoreRegister.calculateMainScore(compositeScoreList));

        //1 CompositeScore -> 1 dataValue
        for(CompositeScore compositeScore:compositeScoreList){
            values.put(prepareValue(compositeScore));
        }
        return values;
    }

    /**
     * Adds a pair dataElement|value according to the passed value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     * @param value
     * @return
     * @throws Exception
     */
    private JSONObject prepareValue(Value value) throws Exception{
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, value.getQuestion().getUid());

        if (value.getOption()!=null)
            elementObject.put(TAG_VALUE, value.getOption().getCode());
        else
            elementObject.put(TAG_VALUE, value.getValue());

        return elementObject;
    }

    /**
     * Adds a pair dataElement|value according to the 'compositeScore' of the value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     * @param compositeScore
     * @return
     * @throws Exception
     */
    private JSONObject prepareValue(CompositeScore compositeScore) throws Exception{
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, compositeScore.getUid());
        elementObject.put(TAG_VALUE, Utils.round(ScoreRegister.getCompositeScore(compositeScore)));
        return elementObject;
    }

    /**
     * Pushes data to DHIS Server
     * @param data
     */
    private JSONObject pushData(JSONObject data)throws Exception {

        Response response = executeCall(data, DHIS_PUSH_API, "POST");

        if(!response.isSuccessful()){
            Log.e(TAG, "pushData (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }
        return parseResponse(response.body().string());
    }

    /**
     * Call to DHIS Server
     * @param data
     * @param url
     */
    private Response executeCall(JSONObject data, String url, String method) throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        final String DHIS_URL=sharedPreferences.getString(activity.getString(R.string.dhis_url), activity.getString(R.string.login_info_dhis_default_server_url)) + url;

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
            throw new Exception(activity.getString(R.string.dialog_info_push_bad_credentials));
        }
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

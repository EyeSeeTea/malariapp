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
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushClient {

    private static String TAG=".PushClient";


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
        try{
            //TODO: This should be removed once DHIS bug is solved
            //Map<String, JSONObject> controlData = prepareControlData();
            JSONObject data = prepareMetadata();
            //TODO: This should be removed once DHIS bug is solved
            //data = prepareDataElements(data, controlData.get(""));
            data = prepareDataElements(data, null);

            PushResult result = new PushResult(pushData(data));
            if(result.isSuccessful()){
                //TODO: This should be removed once DHIS bug is solved
                //pushControlDataElements(controlData);
                updateSurveyState();
            }
            return result;
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage());
            return new PushResult(ex);
        }
    }

    public void updateSurveyState(){
        //Change status
        this.survey.setStatus(Constants.SURVEY_SENT);
        this.survey.update();

        //Reload data using service
        Intent surveysIntent=new Intent(activity, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        activity.startService(surveysIntent);
    }

    /**
     * Retrieve existing control data element
     * @return JSONObject with forward order, reverse order, last survey, overall score and overall class
     * @throws Exception
     */
    private Map<String, JSONObject> prepareControlData() throws Exception{
        Log.d(TAG,"prepareControlData for survey: " + survey.getId_survey());
        Map<String, JSONObject> controlDataMap = new HashMap<>();

        //Get control data elements for existing surveys
        String url = DHIS_ANALYTICS_CONTROL_DATA + survey.getTabGroup().getProgram().getUid() + ".json?dimension=pe:LAST_12_MONTHS&dimension=ou:" + survey.getOrgUnit().getUid() +
                "&dimension=" + activity.getString(R.string.forward_order) + "&dimension=" + activity.getString(R.string.reverse_order) + "&dimension=" + activity.getString(R.string.last_survey);
        Response response = executeCall(null, url, "GET");
        if(!response.isSuccessful()){
            Log.e(TAG, "getAnalytics (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }
        JSONObject responseBody = parseResponse(response.body().string());

        // If there are previous surveys we prepare the metadata in order to update it
        Integer maxReverseOrder = 1;
        if (responseBody.getJSONArray("rows").length()>0)
            maxReverseOrder = prepareControlDataForExistingSurveys(controlDataMap, responseBody);

        // Prepare data for new survey
        prepareControlDataForNewSurvey(controlDataMap, maxReverseOrder);

        Log.d(TAG, "prepareControlData: " + controlDataMap.toString());
        return controlDataMap;
    }

    /**
     * Prepare control data for new survey
     * @param controlDataMap control data map
     * @param maxReverseOrder new reverse order
     * @throws Exception
     */
    private void prepareControlDataForNewSurvey(Map<String, JSONObject> controlDataMap, Integer maxReverseOrder) throws Exception {
        JSONArray controlDataElementsValue = new JSONArray();

        // Forward Order
        JSONObject forwardOrderObject = new JSONObject();
        forwardOrderObject.put(TAG_DATAELEMENT, activity.getString(R.string.forward_order));
        forwardOrderObject.put(TAG_VALUE, maxReverseOrder);
        controlDataElementsValue.put(forwardOrderObject);

        //Reverse Order
        JSONObject reverseOrderObject = new JSONObject();
        reverseOrderObject.put(TAG_DATAELEMENT, activity.getString(R.string.reverse_order));
        reverseOrderObject.put(TAG_VALUE, 1);
        controlDataElementsValue.put(reverseOrderObject);

        //Last Survey
        JSONObject lastSurveyObject = new JSONObject();
        lastSurveyObject.put(TAG_DATAELEMENT, activity.getString(R.string.last_survey));
        lastSurveyObject.put(TAG_VALUE, true);
        controlDataElementsValue.put(lastSurveyObject);

        // Main Score
        JSONObject overallScoreObject = new JSONObject();
        overallScoreObject.put(TAG_DATAELEMENT, activity.getString(R.string.overall_score));
        overallScoreObject.put(TAG_VALUE, ScoreRegister.calculateMainScore(survey));
        controlDataElementsValue.put(overallScoreObject);

        // Overall class
        JSONObject overallClassObject = new JSONObject();
        overallClassObject.put(TAG_DATAELEMENT, activity.getString(R.string.overall_class));
        overallClassObject.put(TAG_VALUE, calculateOverallClass(survey.getMainScore()));
        controlDataElementsValue.put(overallClassObject);

        // Keep in the control data map
        JSONObject newSurveyControlDataElementsValue = new JSONObject();
        newSurveyControlDataElementsValue.put("root",controlDataElementsValue);
        controlDataMap.put("", newSurveyControlDataElementsValue);
    }

    /**
     * Prepare control data for existing surveys
     * @param controlDataMap conrtrol data map
     * @param responseBody response from analytics
     * @return Integer with maximun reverse order
     * @throws Exception
     */
    private Integer prepareControlDataForExistingSurveys(Map<String, JSONObject> controlDataMap, JSONObject responseBody) throws JSONException {
        Integer maxReverseOrder = 1;

        //TODO: We can hide this logic in sth like PushResult.java
        //Read the header to extract the reverse order, last survey and survey id (psi) position in the rows field
        Integer reverseOrderPosition = 0;
        Integer lastSurveyPosition = 0;
        Integer surveyUidPosition = 0;
        JSONArray headers = responseBody.getJSONArray("headers");
        for (int i = 0; i < headers.length(); i++) {
            String controlDEUid = headers.getJSONObject(i).getString("name");
            if (controlDEUid.equals(activity.getString(R.string.reverse_order))) {
                reverseOrderPosition = i;
            } else if (controlDEUid.equals(activity.getString(R.string.last_survey))) {
                lastSurveyPosition = i;
            } else if (controlDEUid.equals("psi")) {
                surveyUidPosition = i;
            }
        }

        //TODO: We can hide this logic in sth like PushResult.java
        // Read the rows and prepare control data for updating
        JSONArray rows = responseBody.getJSONArray("rows");
        for (int i = 0; i < rows.length(); i++) {
            JSONObject controlDataObject = new JSONObject();
            // General info: program and org unit
            controlDataObject.put(TAG_PROGRAM, survey.getTabGroup().getProgram().getUid());
            controlDataObject.put(TAG_ORG_UNIT, survey.getOrgUnit().getUid());

            JSONArray controlDataElementsValue = new JSONArray();

            // Reverse order
            JSONObject reserveOrderObject = new JSONObject();
            reserveOrderObject.put(TAG_DATAELEMENT, activity.getString(R.string.reverse_order));
            Integer newReverseOrder = rows.getJSONArray(i).getInt(reverseOrderPosition) + 1;
            reserveOrderObject.put(TAG_VALUE, newReverseOrder);
            controlDataElementsValue.put(reserveOrderObject);

            // Keep track of the maximum reverse order for the new order
            if (newReverseOrder > maxReverseOrder) {
                maxReverseOrder = newReverseOrder;
            }

            // Change last survey from true to false (the new survey will be set to true)
            if (rows.getJSONArray(i).getBoolean(lastSurveyPosition)) {
                JSONObject lastSurveyObject = new JSONObject();
                lastSurveyObject.put(TAG_DATAELEMENT, activity.getString(R.string.last_survey));
                lastSurveyObject.put(TAG_VALUE, false);
                controlDataElementsValue.put(lastSurveyObject);
            }
            controlDataObject.put(TAG_DATAVALUES, controlDataElementsValue);

            // Store in the control data map
            controlDataMap.put(rows.getJSONArray(i).getString(surveyUidPosition), controlDataObject);
        }
        return maxReverseOrder;
    }

    private String calculateOverallClass(Float mainScore) throws Exception {
        String overallClass = "A";
        if (mainScore < LayoutUtils.MAX_AMBER){
            overallClass = "B";
        }
        if (mainScore < LayoutUtils.MAX_FAILED){
            overallClass = "C";
        }
        return overallClass;
    }


    private JSONObject pushControlDataElements(Map<String, JSONObject> controlDataElementsMap) throws Exception{

        for (Map.Entry<String, JSONObject> controlDataElementEntry : controlDataElementsMap.entrySet()) {

            if (controlDataElementEntry.getKey() != "") {
                //Update control data elements
                String url = DHIS_PUSH_CONTROL_DATA + controlDataElementEntry.getKey();
                Response response = executeCall(controlDataElementEntry.getValue(), url, "PUT");
                if (!response.isSuccessful()) {
                    Log.e(TAG, "pushControlDataElement (" + response.code() + "): " + response.body().string());
                    throw new IOException(response.message());
                }
                //FIXME: Check output
                //JSONObject responseBody = parseResponse(response.body().string());
            }
        }
        return null;
    }

    /**
     * Adds metadata info to json object
     * @return JSONObject with program, orgunit, eventdate and so on...
     * @throws Exception
     */
    private JSONObject prepareMetadata() throws Exception{
        Log.d(TAG,"prepareMetadata for survey: " + survey.getId_survey());

        JSONObject object=new JSONObject();
        object.put(TAG_PROGRAM, survey.getTabGroup().getProgram().getUid());
        object.put(TAG_ORG_UNIT, survey.getOrgUnit().getUid());
        object.put(TAG_EVENTDATE, android.text.format.DateFormat.format("yyyy-MM-dd", survey.getCompletionDate()));
        object.put(TAG_STATUS,COMPLETED );
        object.put(TAG_STOREDBY, survey.getUser().getName());

        Location lastLocation = Session.getLocation();
        //If location is required but there is no location -> exception
        if(PreferencesState.getInstance().isLocationRequired() && lastLocation==null){
            throw new Exception(activity.getString(R.string.dialog_error_push_no_location_and_required));
        }
        //Otherwise (not required or there are coords)
        if (lastLocation!=null)
            object.put(TAG_COORDINATE, prepareCoordinates(lastLocation));

        Log.d(TAG, "prepareMetadata: " + object.toString());
        return object;
    }

    private JSONObject prepareCoordinates(Location location) throws Exception{

        JSONObject coordinate = new JSONObject();

        coordinate.put(TAG_COORDINATE_LAT, location.getLatitude());
        coordinate.put(TAG_COORDINATE_LNG, location.getLongitude());

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

        data.put(TAG_DATAVALUES, values);
        Log.d(TAG, "prepareDataElements result: " + data.toString());
        return data;
    }

    /**
     * Add a dataElement per value (answer)
     * @param values
     * @return
     * @throws Exception
     */
    private JSONArray prepareValues(JSONArray values,JSONArray controlDataElements) throws Exception{
        for (Value value : survey.getValues()) {
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

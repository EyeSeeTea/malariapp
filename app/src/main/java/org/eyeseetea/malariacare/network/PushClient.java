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
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushClient {



    private static String TAG=".PushClient";

    //FIXME This should change for a sharedpreferences url that is selected from the login screen
    private static String DHIS_DEFAULT_SERVER="https://malariacare.psi.org";
    private static String DHIS_PUSH_API="/api/events";
    private static String DHIS_USERNAME="testing";
    private static String DHIS_PASSWORD="Testing2015";


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static String COMPLETED="COMPLETED";

    private static String TAG_PROGRAM="program";
    private static String TAG_ORG_UNIT="orgUnit";
    private static String TAG_EVENTDATE="eventDate";
    private static String TAG_STATUS="status";
    private static String TAG_STOREDBY="storedBy";
    private static String TAG_DATAVALUES="dataValues";
    private static String TAG_DATAELEMENT="dataElement";
    private static String TAG_VALUE="value";


    Survey survey;
    Activity activity;

    public PushClient(Survey survey, Activity activity) {
        this.survey = survey;
        this.activity = activity;
    }

    public PushResult push() {
        try{
            JSONObject data = prepareMetadata();
            data = prepareDataElements(data);
            return new PushResult(pushData(data));
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage());
            return new PushResult(ex);
        }
    }

    /**
     * Adds metadata info to json object
     * @return JSONObject with progra, orgunit, eventdate and so on...
     * @throws Exception
     */
    private JSONObject prepareMetadata() throws Exception{
        Log.d(TAG,"prepareMetadata for survey: "+survey.getId());

        JSONObject object=new JSONObject();
        object.put(TAG_PROGRAM, survey.getProgram().getUid());
        object.put(TAG_ORG_UNIT, survey.getOrgUnit().getUid());
        object.put(TAG_EVENTDATE, android.text.format.DateFormat.format("yyyy-MM-dd", survey.getCompletionDate()));
        object.put(TAG_STATUS,COMPLETED );
        object.put(TAG_STOREDBY, survey.getUser().getName());
        return object;
    }


    /**
     * Adds questions and scores values to the JSON object
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     * @param data JSON object to update
     * @throws Exception
     */
    private JSONObject prepareDataElements(JSONObject data)throws Exception{
        Log.d(TAG, "prepareDataElements for survey: " + survey.getId());

        //Add dataElement per values
        JSONArray values=prepareValues(new JSONArray());

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
    private JSONArray prepareValues(JSONArray values) throws Exception{
        for (Value value : survey.getValues()) {
            values.put(prepareValue(value));
        }
        return values;
    }

    private JSONArray prepareCompositeScores(JSONArray values) throws Exception{

        //Cleans scores
        ScoreRegister.clear();

        //Register scores for tabs
        List<Tab> tabs=survey.getProgram().getTabs();
        ScoreRegister.registerTabScores(tabs);

        //Register scores for composites
        List<CompositeScore> compositeScoreList=CompositeScore.listAllByProgram(survey.getProgram());
        ScoreRegister.registerCompositeScores(compositeScoreList);

        //Initialize scores x question
        for(Question question : Question.listAllByProgram(survey.getProgram())){
            if(!question.isHiddenBySurvey(survey)) {
                question.initScore(survey);
            }else{
                ScoreRegister.addRecord(question, 0F, ScoreRegister.calcDenum(question));
            }
        }

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
        elementObject.put(TAG_VALUE, ScoreRegister.getCompositeScore(compositeScore));
        return elementObject;
    }
    /**
     * Pushes data to DHIS Server
     * @param data
     */
    private JSONObject pushData(JSONObject data)throws Exception {

        final String DHIS_URL=DHIS_DEFAULT_SERVER + DHIS_PUSH_API;

        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        BasicAuthenticator basicAuthenticator=new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);

        RequestBody body = RequestBody.create(JSON, data.toString());
        Request request = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER,basicAuthenticator.getCredentials())
                .url(DHIS_URL)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            Log.e(TAG, "pushData (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }
        return parseResponse(response.body().string());
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

        BasicAuthenticator(){
            credentials = Credentials.basic(DHIS_USERNAME, DHIS_PASSWORD);
        }

        @Override
        public Request authenticate(Proxy proxy, Response response) throws IOException {
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

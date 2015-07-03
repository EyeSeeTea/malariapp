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

package org.eyeseetea.malariacare.utils;

import android.app.Activity;
import android.util.Log;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Challenge;
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
import java.net.URL;
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

    private String credentials = Credentials.basic(DHIS_USERNAME,DHIS_PASSWORD);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

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

    public void push() throws Exception{
        JSONObject data=prepareMetadata();
        data= prepareDataElements(data);
        pushData(data);
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

        ScoreRegister.clear();
        List<Tab> tabs=survey.getProgram().getTabs();
        ScoreRegister.registerTabScores(tabs);

        List<CompositeScore> compositeScoreList=CompositeScore.listAllByProgram(survey.getProgram());
        ScoreRegister.registerCompositeScores(compositeScoreList);

        JSONArray values = new JSONArray();
        for (Value value : survey.getValues()) {
            values.put(prepareValue(value));

            Question question=value.getQuestion();
            Float num=ScoreRegister.calcNum(question, survey);
            Float den=ScoreRegister.calcDenum(question, survey);
            ScoreRegister.addRecord(value.getQuestion(), num,den);
        }

        for(CompositeScore compositeScore:compositeScoreList){
            values.put(prepareValue(compositeScore));
        }
        data.put(TAG_DATAVALUES, values);
        Log.d(TAG, "prepareDataElements result: " + data.toString());
        return data;
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
    private void pushData(JSONObject data)throws Exception {
        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                return response.request().newBuilder().header("Authorization", credentials).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        RequestBody body = RequestBody.create(JSON, data.toString());
        Request request = new Request.Builder()
                .header("Authorization",credentials)
                .url(DHIS_DEFAULT_SERVER+DHIS_PUSH_API)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            Log.e(TAG, "pushData (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }
        parseResponse(response.body().string());
    }

    private void parseResponse(String responseData)throws Exception{
        try{
            JSONObject jsonResponse=new JSONObject(responseData);
            Log.e(TAG, "parseResponse: " + jsonResponse);
        }catch(Exception ex){
            throw new Exception(activity.getString(R.string.dialog_info_push_bad_credentials));
        }
    }

}

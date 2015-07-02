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

import android.util.Log;

import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jose on 20/06/2015.
 */
public class PushClient {

    private static String TAG=".PushClient";

    //FIXME This should change for a sharedpreferences url that is selected from the login screen
    private static String DHIS_SERVER="http://localhost:8080/dhis2";

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

    public PushClient(Survey survey) {
        this.survey = survey;
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
     * @throws Exceptionuser
     */
    private JSONObject prepareValue(Value value) throws Exception{
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, value.getQuestion().getUid());
        elementObject.put(TAG_VALUE, value.getValue());
        return elementObject;
    }

    private JSONObject prepareValue(CompositeScore compositeScore) throws Exception{
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, compositeScore.getUid());
        elementObject.put(TAG_VALUE, ScoreRegister.getCompositeScore(compositeScore));
        return elementObject;
    }

    /**
     * Adds a pair dataElement|value according to the 'compositeScore' of the value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     * @param value
     * @return
     * @throws Exception
     */
    private JSONObject prepareCompositeScore(Value value) throws Exception{
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, value.getQuestion().getUid());
        elementObject.put(TAG_VALUE, value.getValue());
        return elementObject;
    }

    /**
     * Pushes data to DHIS Server
     * @param data
     */
    private void pushData(JSONObject data)throws Exception{
        //TODO do a real push to DHIS_SERVER
        Thread.sleep(3000);
    }

}

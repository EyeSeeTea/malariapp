/*
 * Copyright (c) 2015.
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
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.phonemetadata.PhoneMetaData;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by ignac on 21/12/2015.
 */
public class PrepareData {
    private static final String TAG = ".PrepareData";
    private static String COMPLETED = "COMPLETED";

    private static String TAG_PROGRAM = "program";
    private static String TAG_ORG_UNIT = "orgUnit";
    private static String TAG_EVENTDATE = "eventDate";
    private static String TAG_STATUS = "status";
    private static String TAG_STOREDBY = "storedBy";
    private static String TAG_COORDINATE = "coordinate";
    private static String TAG_COORDINATE_LAT = "latitude";
    private static String TAG_COORDINATE_LNG = "longitude";


    private static final String DHIS_PATCH_DESCRIPTIONCLOSED_DATE = "[%s] - Android Surveillance App set the closing date to %s because over 30 surveys were pushed within 1 hour.";

    private static String DHIS_ANALYTICS_CONTROL_DATA = "/api/analytics/events/query/";
    private static String DHIS_PUSH_CONTROL_DATA = "/api/events/";


    private static String TAG_DATAVALUES = "dataValues";
    private static String TAG_DATAELEMENT = "dataElement";
    private static String TAG_VALUE = "value";
    private static String TAG_PHONEMETADA = "RuNZUhiAmlv";

    private static final String TAG_CLOSEDATA = "closedDate";
    private static final String TAG_DESCRIPTIONCLOSEDATA = "description";

    /**
     * Singleton reference
     */
    private static PrepareData instance;

    static Context applicationContext;

    public PrepareData(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static PrepareData getInstance() {
        if (instance == null) {
            instance = new PrepareData(PreferencesState.getInstance().getContext());
        }
        return instance;
    }

    /**
     * Adds metadata info to json object
     *
     * @return JSONObject with program, orgunit, eventdate and so on...
     * @throws Exception
     */
    public JSONObject prepareMetadata(Survey survey) throws Exception {
        Log.d(TAG, "prepareMetadata for survey: " + survey.getId_survey());
        JSONObject object = new JSONObject();
        object.put(TAG_PROGRAM, survey.getTabGroup().getProgram().getUid());
        object.put(TAG_ORG_UNIT, survey.getOrgUnit().getUid());
        object.put(TAG_EVENTDATE, android.text.format.DateFormat.format("yyyy-MM-dd", survey.getCompletionDate()));
        object.put(TAG_STATUS, COMPLETED);
        object.put(TAG_STOREDBY, survey.getUser().getName());

        Location lastLocation = LocationMemory.get(survey.getId_survey());
        //If location is required but there is no location -> exception
        if (PreferencesState.getInstance().isLocationRequired() && lastLocation == null) {
            throw new Exception(applicationContext.getString(R.string.dialog_error_push_no_location_and_required));
        }
        //Otherwise (not required or there are coords)
        object.put(TAG_COORDINATE, prepareCoordinates(lastLocation));

        //Fixme create phone metadata value
        //PhoneMetaData phoneMetaData= Session.getPhoneMetaData();
        //object.put(TAG_PHONEMETADA, phoneMetaData.getPhone_metaData());

        Log.d(TAG, "prepareMetadata: " + object.toString());
        return object;
    }

    /**
     * Adds a pair dataElement|value according to the passed value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     *
     * @param value
     * @return
     * @throws Exception
     */
    public JSONObject preparePhoneValue(String uid, String value) throws Exception {
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, uid);
        elementObject.put(TAG_VALUE, value);
        return elementObject;
    }

    private JSONObject prepareCoordinates(Location location) throws Exception {

        JSONObject coordinate = new JSONObject();

        if (location == null) {
            coordinate.put(TAG_COORDINATE_LAT, JSONObject.NULL);
            coordinate.put(TAG_COORDINATE_LNG, JSONObject.NULL);
        } else {
            coordinate.put(TAG_COORDINATE_LAT, location.getLatitude());
            coordinate.put(TAG_COORDINATE_LNG, location.getLongitude());
        }

        return coordinate;
    }

    /**
     * Adds questions and scores values to the JSON object
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     *
     * @param data JSON object to update
     * @throws Exception
     */
    public JSONObject prepareDataElements(JSONObject data, Survey survey) throws Exception {
        Log.d(TAG, "prepareDataElements for survey: " + survey.getId_survey());

        //Add dataElement per values
        //TODO: This should be removed once DHIS bug is solved
        //JSONArray values=prepareValues(new JSONArray(), controlDataElements.getJSONArray("root"));
        JSONArray values = prepareValues(new JSONArray(), survey);

        values = prepareControlDataElementsValues(values, null);
        //Add dataElement per compositeScores
        values = prepareCompositeScores(values, survey);

        //Add main scores values
        if (!PreferencesState.isPictureQuestion())
            values = prepareControlDataElementValues(values, survey);

        data.put(TAG_DATAVALUES, values);
        Log.d(TAG, "prepareDataElements result: " + data.toString());
        return data;
    }

    /**
     * Adds 4 additional values:
     * - Main score
     * - Boolean flag is type A
     * - Boolean flag is type B
     * - Boolean flag is type C
     *
     * @param values
     * @return
     */
    public JSONArray prepareControlDataElementValues(JSONArray values, Survey survey) throws Exception {
        JSONObject dataElement;
        //Main score
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, applicationContext.getString(R.string.main_score));
        dataElement.put(TAG_VALUE, survey.getType());
        values.put(dataElement);

        //Type A
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, applicationContext.getString(R.string.main_score_a));
        dataElement.put(TAG_VALUE, survey.isTypeA() ? "true" : "false");
        values.put(dataElement);

        //Type B
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, applicationContext.getString(R.string.main_score_b));
        dataElement.put(TAG_VALUE, survey.isTypeB() ? "true" : "false");
        values.put(dataElement);

        //Type C
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, applicationContext.getString(R.string.main_score_c));
        dataElement.put(TAG_VALUE, survey.isTypeC() ? "true" : "false");
        values.put(dataElement);

        //Forward Order
        dataElement = new JSONObject();
        dataElement.put(TAG_DATAELEMENT, applicationContext.getString(R.string.forward_order));
        dataElement.put(TAG_VALUE, applicationContext.getString(R.string.forward_order_value));
        values.put(dataElement);

        return values;
    }


    /**
     * Pull the current description and adds new closed organization description.
     *
     * @return new description.
     * @url url for pull the current description
     */
    public JSONObject prepareClosingDescriptionValue(String url, String actualDescription) throws Exception {
        String dateFormatted = Utils.getClosingDateString("dd-MM-yyyy");
        String dateTimestamp = Utils.getClosingDateTimestamp(Utils.getClosingDateString("dd-MM-yyyy")).getTime() + "";
        String description = String.format(DHIS_PATCH_DESCRIPTIONCLOSED_DATE, dateTimestamp, dateFormatted);
        StringBuilder sb = new StringBuilder();
        sb.append(actualDescription);
        sb.append("");//next line
        sb.append("");//next line
        sb.append(description);
        description = sb.toString();
        sb = null;
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DESCRIPTIONCLOSEDATA, description);
        Log.d(TAG, "closingDateURL:Description:" + description);
        return elementObject;
    }

    /**
     * Prepare the closing value.
     *
     * @return Closing value as Json.
     */
    public JSONObject prepareClosingDateValue() throws Exception {
        String dateFormatted = Utils.getClosingDateString("yyyy-MM-dd");
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_CLOSEDATA, dateFormatted);
        Log.d("closingDateURL", "closingDateURL:EndDate:" + dateFormatted);
        return elementObject;
    }

    /**
     * Prepare the closing value.
     *
     * @return Closing value as Json.
     */
    public JSONObject prepareTodayDateValue() throws Exception {
        String dateFormatted = Utils.geTodayDataString("yyyy-MM-dd");
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_CLOSEDATA, dateFormatted);
        Log.d("closingDateURL", "closingDateURL:EndDate:" + dateFormatted);
        return elementObject;
    }


    /**
     * Add a dataElement per value (answer)
     *
     * @param values
     * @return
     * @throws Exception
     */
    private JSONArray prepareValues(JSONArray values, Survey survey) throws Exception {
        List<Value> surveyValues = survey.getValues();
        if (surveyValues == null || surveyValues.size() == 0) {
            throw new Exception(applicationContext.getString(R.string.dialog_info_push_empty_survey));
        }

        for (Value value : surveyValues) {
            values.put(prepareValue(value));
        }

        //TODO: This should be removed once DHIS bug is solved
        return values;
    }

    private JSONArray prepareCompositeScores(JSONArray values, Survey survey) throws Exception {

        if (PreferencesState.isPictureQuestion()) {
            //Cleans score
            ScoreRegister.clear();

            //Register scores for tabs
            List<Tab> tabs = survey.getTabGroup().getProgram().getTabs();
            ScoreRegister.registerTabScores(tabs);

            //Register scores for composites
            List<CompositeScore> compositeScoreList = CompositeScore.listAllByProgram(survey.getTabGroup().getProgram());
            ScoreRegister.registerCompositeScores(compositeScoreList);

            //Initialize scores x question
            ScoreRegister.initScoresForQuestions(Question.listAllByProgram(survey.getTabGroup().getProgram()), survey);

            //1 CompositeScore -> 1 dataValue
            for (CompositeScore compositeScore : compositeScoreList) {
                values.put(prepareValue(compositeScore));
            }

            PhoneMetaData phoneMetaData = Session.getPhoneMetaData();
            values.put(preparePhoneValue(TAG_PHONEMETADA, phoneMetaData.getPhone_metaData()));
        } else {
            //Prepare scores info
            List<CompositeScore> compositeScoreList = ScoreRegister.loadCompositeScores(survey);

            //Calculate main score to push later
            survey.setMainScore(ScoreRegister.calculateMainScore(compositeScoreList));

            //1 CompositeScore -> 1 dataValue
            for (CompositeScore compositeScore : compositeScoreList) {
                values.put(prepareValue(compositeScore));
            }
        }
        return values;
    }


    private JSONArray prepareControlDataElementsValues(JSONArray values, JSONArray controlDataElements) throws JSONException {
        if (controlDataElements != null) {
            for (int i = 0; i < controlDataElements.length(); i++) {
                values.put(controlDataElements.get(i));
            }
        }
        return values;
    }


    /**
     * Adds a pair dataElement|value according to the passed value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     *
     * @param value
     * @return
     * @throws Exception
     */
    private JSONObject prepareValue(Value value) throws Exception {
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, value.getQuestion().getUid());
        if (PreferencesState.isPictureQuestion())
            elementObject.put(TAG_VALUE, value.getValue());

        if (value.getOption() != null)
            elementObject.put(TAG_VALUE, value.getOption().getCode());
        else
            elementObject.put(TAG_VALUE, value.getValue());

        return elementObject;
    }

    /**
     * Adds a pair dataElement|value according to the 'compositeScore' of the value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     *
     * @param compositeScore
     * @return
     * @throws Exception
     */
    private JSONObject prepareValue(CompositeScore compositeScore) throws Exception {
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, compositeScore.getUid());
        elementObject.put(TAG_VALUE, Utils.round(ScoreRegister.getCompositeScore(compositeScore)));
        return elementObject;
    }

    public void prepareSurveyCompletionDate(Survey survey) {
        if (!survey.isSent()) {
            survey.setCompletionDate(new Date());
            survey.save();
        }
    }


    public void updateSurveyState(Survey survey){
        //Change status and save mainScore
        survey.setStatus(Constants.SURVEY_SENT);
        if(PreferencesState.isPictureQuestion()){
            survey.save();
            //Reload data using service
            Intent surveysIntent=new Intent(applicationContext, SurveyService.class);
            surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
            applicationContext.startService(surveysIntent);
        }
        else{
            survey.saveMainScore();
            survey.save();
        }
    }

}

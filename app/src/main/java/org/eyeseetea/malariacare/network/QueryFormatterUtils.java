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
import android.location.Location;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.AUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by ignac on 21/12/2015.
 */
public class QueryFormatterUtils {
    private static final String TAG = ".QueryFormatterUtils";
    private static String COMPLETED = "COMPLETED";

    private static String TAG_PROGRAM = "program";
    private static String TAG_ORG_UNIT = "orgUnit";

    private static String TAG_EVENT = "event";
    private static String TAG_STATUS = "status";
    private static String TAG_STOREDBY = "storedBy";
    private static String TAG_COORDINATE = "coordinate";
    private static String TAG_COORDINATE_LAT = "latitude";
    private static String TAG_COORDINATE_LNG = "longitude";
    private static String TAG_EVENTDATE = "eventDate";

    private static String TAG_DATAVALUES = "dataValues";
    private static String TAG_DATAELEMENT = "dataElement";
    private static String TAG_VALUE = "value";

    private static String TAG_USER = "users";

    private static String QUERY_LAST_EVENTS_FROM_DATE =
            "?orgUnit=%s&program=%s&startDate=%s&fields=[event,eventDate,lastUpdated,"
                    + "created]&skipPaging=true";

    private static String QUERY_USER_LAST_UPDATED = "/%s?fields=lastUpdated&paging=false";

    private static String QUERY_USER_ATTRIBUTES =
            "/%s?fields=attributeValues[value,attribute[code]]id&paging=false";

    /**
     * Singleton reference
     */
    private static QueryFormatterUtils instance;

    static Context applicationContext;

    public QueryFormatterUtils(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static QueryFormatterUtils getInstance() {
        if (instance == null) {
            instance = new QueryFormatterUtils(PreferencesState.getInstance().getContext());
        }
        return instance;
    }

    /**
     * Adds metadata info to json object to get the last events
     * Ex: https://eds-dev-ci.psi-mis
     * .org/api/events?orgUnit=QS7sK8XzdQc&program=wK0958s1bdj&startDate=2016-1-01&fields=[event,
     * eventDate,lastUpdated,created]
     *
     * @return JSONObject with program, orgunit and lastDate
     */
    public String prepareLastEventData(String orgUnit, String program, Date lastDate) {
        String formattedQuery = String.format(QUERY_LAST_EVENTS_FROM_DATE, orgUnit, program,
                EventExtended.formatShort(lastDate));
        Log.d(TAG, "prepareLastEventData-> " + formattedQuery);
        return formattedQuery;
    }

    /**
     * Adds metadata info to json object
     *
     * @return JSONObject with program, orgunit, eventdate and so on...
     */
    public JSONObject prepareMetadata(SurveyDB survey) throws Exception {
        Log.d(TAG, "prepareMetadata for survey: " + survey.getId_survey());
        JSONObject object = new JSONObject();
        object.put(TAG_PROGRAM, survey.getProgram().getUid());
        object.put(TAG_ORG_UNIT, survey.getOrgUnit().getUid());
        object.put(TAG_EVENTDATE,
                android.text.format.DateFormat.format("yyyy-MM-dd", survey.getCompletionDate()));
        object.put(TAG_STATUS, COMPLETED);
        object.put(TAG_STOREDBY, survey.getUser().getName());

        Location lastLocation = LocationMemory.get(survey.getId_survey());
        //If location is required but there is no location -> exception
        if (PreferencesState.getInstance().isLocationRequired() && lastLocation == null) {
            throw new Exception(applicationContext.getString(
                    R.string.dialog_error_push_no_location_and_required));
        }
        //Otherwise (not required or there are coords)
        object.put(TAG_COORDINATE, prepareCoordinates(lastLocation));

        //Fixme create phone metadata value
        //PhoneMetaData phoneMetaData= Session.getPhoneMetaData();
        //object.put(TAG_PHONEMETADA, phoneMetaData.getPhone_metaData());

        Log.d(TAG, "prepareMetadata: " + object.toString());
        return object;
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
     */
    public JSONObject PushUtilsElements(JSONObject data, SurveyDB survey, String module)
            throws Exception {
        Log.d(TAG, "PushUtilsElements for survey: " + survey.getId_survey());

        //Add dataElement per values
        //TODO: This should be removed once DHIS bug is solved
        //JSONArray values=prepareValues(new JSONArray(), controlDataElements.getJSONArray("root"));
        JSONArray values = prepareValues(new JSONArray(), survey);

        values = prepareControlDataElementsValues(values, null);
        //Add dataElement per compositeScores
        values = prepareCompositeScores(values, survey, module);

        data.put(TAG_DATAVALUES, values);
        Log.d(TAG, "PushUtilsElements result: " + data.toString());
        return data;
    }

    /**
     * Add a dataElement per value (answer)
     */
    private JSONArray prepareValues(JSONArray values, SurveyDB survey) throws Exception {
        List<ValueDB> surveyValues = survey.getValues();
        if (surveyValues == null || surveyValues.size() == 0) {
            throw new Exception(
                    applicationContext.getString(R.string.dialog_info_push_empty_survey));
        }

        for (ValueDB value : surveyValues) {
            values.put(prepareValue(value));
        }

        //TODO: This should be removed once DHIS bug is solved
        return values;
    }

    private JSONArray prepareCompositeScores(JSONArray values, SurveyDB survey, String module)
            throws Exception {

        //Prepare scores info
        List<CompositeScoreDB> compositeScoreList = ScoreRegister.loadCompositeScores(survey.getId_survey(), survey.getProgram(),
                module);

        //Calculate main score to push later
        survey.setMainScore(
                ScoreRegister.calculateMainScore(compositeScoreList, survey.getId_survey(),
                        module));

        //1 CompositeScoreDB -> 1 dataValue
        for (CompositeScoreDB compositeScore : compositeScoreList) {
            values.put(prepareValue(compositeScore, survey.getId_survey(), module));
        }
        return values;
    }


    private JSONArray prepareControlDataElementsValues(JSONArray values,
            JSONArray controlDataElements) throws JSONException {
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
     */
    private JSONObject prepareValue(ValueDB value) throws Exception {
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, value.getQuestion().getUid());

        if (value.getOption() != null) {
            elementObject.put(TAG_VALUE, value.getOption().getCode());
        } else {
            elementObject.put(TAG_VALUE, value.getValue());
        }

        return elementObject;
    }

    /**
     * Adds a pair dataElement|value according to the 'compositeScore' of the value.
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     */
    private JSONObject prepareValue(CompositeScoreDB compositeScore, float idSurvey, String module)
            throws Exception {
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, compositeScore.getUid());
        elementObject.put(TAG_VALUE,
                AUtils.round(ScoreRegister.getCompositeScore(compositeScore, idSurvey, module)));
        return elementObject;
    }

    /**
     * Returns the right endpoint depending on the server version
     */
    static String getUserLastUpdatedApiCall(String userUid) {
        String endpoint = TAG_USER + String.format(QUERY_USER_LAST_UPDATED, userUid);

        endpoint = encodeBlanks(endpoint);
        Log.d(TAG, String.format("userLastUpdatedApiCall (%s) -> %s", TAG_USER, endpoint));
        return endpoint;
    }

    /**
     * Returns the right endpoint depending on the server version
     */
    static String getUserAttributesApiCall(String userUid) {
        String endpoint = TAG_USER + String.format(QUERY_USER_ATTRIBUTES, userUid);

        endpoint = encodeBlanks(endpoint);
        Log.d(TAG, String.format("getUserAttributesApiCall(%s) -> %s", TAG_USER, endpoint));
        return endpoint;
    }

    static String encodeBlanks(String endpoint) {
        return endpoint.replace(" ", "%20");
    }

}
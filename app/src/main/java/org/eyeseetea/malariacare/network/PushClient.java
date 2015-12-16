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
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import org.eyeseetea.malariacare.views.ShowException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
import java.util.Calendar;
import java.util.Date;
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

//PictureQuestion

    private static String DHIS_SERVER ="https://www.psi-mis.org";
    private static final String DHIS_PULL_PROGRAM="/api/programs/";
    private static final String DHIS_PULL_ORG_UNIT_API ="/api/organisationUnits.json?paging=false&fields=id,closedDate&filter=code:eq:%s&filter:programs:id:eq:%s";
    private static final String DHIS_PULL_ORG_UNITS_API=".json?fields=organisationUnits";
    private static final String DHIS_EXIST_PROGRAM=".json?fields=id";
    private static final String DHIS_PATCH_URL_CLOSED_DATE ="/api/organisationUnits/%s/closedDate";
    private static final String DHIS_PATCH_URL_DESCRIPTIONCLOSED_DATE="/api/organisationUnits/%s/description";
    private static final String DHIS_PATCH_DESCRIPTIONCLOSED_DATE ="[%s] - Android Surveillance App set the closing date to %s because over 30 surveys were pushed within 1 hour.";

    //The boolean BANNED and INVALID_SERVER control if the org unit is banned or the server is invalid
    private static Boolean BANNED=false;
    private static Boolean INVALID_SERVER=false;

    //The strings DHIS_INVALID_URL DHIS_UNEXISTENT_ORG_UNIT stored the last bad url and org unit.
    //They are used as a control to avoid requests to the server if no new values ​​for the url or to the organization.
    private static String DHIS_INVALID_URL="";
    private static String DHIS_UNEXISTENT_ORG_UNIT=null;

    public static String DHIS_UID_PROGRAM="";

    private static String DHIS_USERNAME="KHMCS";
    //Todo: introduce final password
    private static String DHIS_PASSWORD="KHMCSadmin1";

    public static String DHIS_ORG_NAME ="";
    private static String DHIS_ORG_UID ="";


    private static final String TAG_CLOSEDATA="closedDate";
    private static final String TAG_DESCRIPTIONCLOSEDATA="description";
    private static final String TAG_ORGANISATIONUNIT="organisationUnits";
    private static final String TAG_ID = "id";


    private static String TAG_PHONEMETADA="RuNZUhiAmlv";

    private static int DHIS_LIMIT_SENT_SURVEYS_IN_ONE_HOUR=30;
    private static int DHIS_LIMIT_HOURS=1;

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

    public PushClient(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public PushResult push() {
        PushResult pushResult;
        try{
            //TODO: This should be removed once DHIS bug is solved
            //Map<String, JSONObject> controlData = prepareControlData();
            prepareSurveyCompletionDate();
            JSONObject data = prepareMetadata();
            //TODO: This should be removed once DHIS bug is solved
            //data = prepareDataElements(data, controlData.get(""));
            data = prepareDataElements(data, null);
            pushResult = new PushResult(pushData(data));
            if(pushResult.isSuccessful() && !pushResult.getImported().equals("0")){
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

    public void prepareSurveyCompletionDate(){
        if(!this.survey.isSent()) {
            this.survey.setCompletionDate(new Date());
            this.survey.save();
        }
    }

    public void updateDashboard(){
        //Reload data using service
        Intent surveysIntent=new Intent(applicationContext, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        applicationContext.startService(surveysIntent);
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
     * @param value
     * @return
     * @throws Exception
     */
    private JSONObject preparePhoneValue(String uid, String value) throws Exception{
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DATAELEMENT, uid);
        elementObject.put(TAG_VALUE, value);
        return elementObject;
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
        if(!Utils.isPictureQuestion())
        values= prepareControlDataElementValues(values);

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
    private JSONArray prepareControlDataElementValues(JSONArray values) throws Exception{
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

    public PushResult pushBackground() {
        //Check if the static DHIS_UNEXISTENT_ORG_UNIT is the same than the used DHIS_ORG_NAME.
        //If DHIS_UNEXISTENT_ORG_UNIT!=DHIS_ORG_NAME is the same, the UID not exist, and it was be checked.
        //hasOrgUnitValidCode check the code the program and the closedDate
        //This if is evaluating every push from SurveyService.
        //if (isNetworkAvailable() && !INVALID_SERVER && isValidOrgUnit() &&  checkAll() && !BANNED  ) {
        if (isNetworkAvailable()) {
            try {
                JSONObject data = prepareMetadata();
                data = prepareDataElements(data);
                PushResult result = new PushResult(pushData(data));
                if (result.isSuccessful()) {
                    this.survey.setStatus(Constants.SURVEY_SENT);
                    this.survey.save();
                    //Change status
                    //check if the user was sent more than the limit
                    List<Survey> sentSurveys = Survey.getAllHideAndSentSurveys();
                    if(isSurveyOverLimit(sentSurveys))
                    {
                        banOrg(DHIS_ORG_NAME);
                    }
                }
                return result;
            } catch (Exception ex) {
                //Log.e(TAG, ex.getMessage());
                return new PushResult(ex);
            }
        }
        return new PushResult();
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

    /**
     * compares the dates of the surveys and checks if the dates are over the limit
     * @param surveyList all the sent surveys
     * @return true if the surveys are over the limit
     */
    private boolean isSurveyOverLimit(List<Survey> surveyList){
        if(surveyList.size()>=DHIS_LIMIT_SENT_SURVEYS_IN_ONE_HOUR) {
            for (int i = 0; i < surveyList.size(); i++) {
                int countDates = 0;
                Calendar actualSurvey=Utils.DateToCalendar(surveyList.get(i).getEventDate());
                for (int d = 0; d < surveyList.size(); d++) {
                    Calendar nextSurvey=Utils.DateToCalendar(surveyList.get(d).getEventDate());
                    if (actualSurvey.before(nextSurvey)) {
                        if (!Utils.isDateOverLimit(actualSurvey, nextSurvey, DHIS_LIMIT_HOURS)) {
                            countDates++;
                            Log.d(TAG, "Surveys sents in one hour:" + countDates);
                            if (countDates >= DHIS_LIMIT_SENT_SURVEYS_IN_ONE_HOUR) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Bans the organization for future requests , reducing one day the system date and keeping closedate on the server.
     */

    private void banOrg(String orgName) {

        String url= DHIS_SERVER;

        String orgid = null;
        try {
            orgid = DHIS_ORG_UID;
            patchClosedDate(getPatchClosedDateUrl(url, orgid));
            patchDescriptionClosedDate(getPatchClosedDescriptionUrl(url, orgid));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Patch the closedDate data in the server
    private void patchClosedDate(String url){
        //https://malariacare.psi.org/api/organisationUnits/u5jlxuod8xQ/closedDate
        try {
            String DHIS_PATCH_URL=url;
            JSONObject data =prepareTodayDateValue();
            Response response=executeCall(data, DHIS_PATCH_URL, "PATCH");
            Log.e(TAG, "closingDatePatch (" + response.code() + "): " + response.body().string());
            if(!response.isSuccessful()){
                Log.e(TAG, "closingDatePatch (" + response.code() + "): " + response.body().string());
                throw new IOException(response.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Patch in the server the new description in a closed organisation
     * @param url the patch url
     */

    private void patchDescriptionClosedDate(String url) throws Exception{
        //https://malariacare.psi.org/api/organisationUnits/Pg91OgEIKIm/description
        try {
            String DHIS_PATCH_URL = url;
            JSONObject data =prepareClosingDescriptionValue(url);

            Response response=executeCall(data, DHIS_PATCH_URL, "PATCH");
            if(!response.isSuccessful()){
                Log.e(TAG, "closingDateDescriptionPatch (" + response.code() + "): " + response.body().string());
                throw new IOException(response.message());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pull the current description and adds new closed organization description.
     * @url url for pull the current description
     * @return new description.
     */
    private JSONObject prepareClosingDescriptionValue(String url) throws Exception{
        String actualDescription= getCurrentDescription(url);
        String dateFormatted=Utils.getClosingDateString("dd-MM-yyyy");
        String dateTimestamp=Utils.getClosingDateTimestamp(Utils.getClosingDateString("dd-MM-yyyy")).getTime()+"";
        String description=String.format(DHIS_PATCH_DESCRIPTIONCLOSED_DATE,dateTimestamp, dateFormatted);
        StringBuilder sb = new StringBuilder();
        sb.append(actualDescription);
        sb.append("");//next line
        sb.append("");//next line
        sb.append(description);
        description=sb.toString();
        sb=null;
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_DESCRIPTIONCLOSEDATA, description);
        Log.d(TAG, "closingDateURL:Description:" + description);
        return elementObject;
    }
    /**
     * Prepare the closing value.
     * @return Closing value as Json.
     */
    private JSONObject prepareClosingDateValue() throws Exception {
        String dateFormatted=Utils.getClosingDateString("yyyy-MM-dd");
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_CLOSEDATA, dateFormatted);
        Log.d("closingDateURL", "closingDateURL:EndDate:" + dateFormatted);
        return elementObject;
    }

    /**
     * Prepare the closing value.
     * @return Closing value as Json.
     */
    private JSONObject prepareTodayDateValue() throws Exception {
        String dateFormatted=Utils.geTodayDataString("yyyy-MM-dd");
        JSONObject elementObject = new JSONObject();
        elementObject.put(TAG_CLOSEDATA, dateFormatted);
        Log.d("closingDateURL", "closingDateURL:EndDate:" + dateFormatted);
        return elementObject;
    }

    public void updateSurveyState(){
        //Change status and save mainScore
        this.survey.setStatus(Constants.SURVEY_SENT);
        if(Utils.isPictureQuestion()){
            this.survey.save();
            //Reload data using service
            Intent surveysIntent=new Intent(applicationContext, SurveyService.class);
            surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
            applicationContext.startService(surveysIntent);
        }
        else{
            this.survey.saveMainScore();
        }
    }

    /**
     * This method check the org_unit is valid and if the org unit is banned.
     * @return return true if all is correct.
     */
    private boolean checkAll(){
        try{
            DHIS_ORG_UID = getUIDCheckProgramClosedDate(DHIS_ORG_NAME);
            if (DHIS_ORG_UID!=null) {
                return true;
            } else {
                DHIS_UNEXISTENT_ORG_UNIT = DHIS_ORG_NAME;
                try {
                    throw new ShowException(applicationContext.getString(R.string.exception_org_unit_not_valid), applicationContext);
                } catch (ShowException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            //isValidServer only was checked here, if the getUIDCheckProgramClosedDate return exception, and if change the url in settingsapplicationContext.java.
            //it turns the INVALID_SERVER value to true.
            isValidServer();
        }
        return false;
    }
    /**
     * This method check the org_unit not is invalid, and is not banned, and later check if the server is valid.
     * @return return true if all is correct.
     */
    public boolean isNetworkAvailable(){
        ConnectivityManager conMgr = (ConnectivityManager) applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }
    /**
     * This method check the org_unit not is invalid, and is not banned, and later check if the server is valid.
     * @return return true if all is correct.
     */
    public boolean isValidOrgUnit() {
        boolean result=false;
        if(DHIS_UNEXISTENT_ORG_UNIT!=null) {
            if (!DHIS_ORG_NAME.equals(""))
                if (!(DHIS_UNEXISTENT_ORG_UNIT.equals(DHIS_ORG_NAME)))
                    if (!BANNED)
                        if (!INVALID_SERVER)
                            result = true;
        }
        else if(!DHIS_ORG_NAME.equals(""))
            if(!BANNED)
                if(!INVALID_SERVER)
                    result= true;

        return result;
    }

    /**
     * checks if the server is valid, checking the UID PROGRAM.
     * @return return true if all is correct.
     */
    public boolean isValidServer() {
        Log.d(TAG, "check server valid?");
        String url = "";
        if (!INVALID_SERVER) {
            url = getDhisURL();
            if (!DHIS_INVALID_URL.equals(url)) {
                if(isNetworkAvailable()) {
                    try {
                        if (isValidProgram()) {
                            return true;
                        }
                    } catch (Exception e) {
                    }
                    INVALID_SERVER = true;
                    DHIS_INVALID_URL = url;
                    return false;
                }
                else{
                    DHIS_INVALID_URL="";
                }
            }
        }
        return false;
    }
    /**
     * Only checks the orgUnit
     * @param orgUnit the organization unit
     * @return true if is correct.
     */
    public boolean checkOrgUnit(String orgUnit) {
        boolean result=false;
        try {
            if((getUIDCheckProgramClosedDate(orgUnit)!=null))
                result=true;
        } catch (Exception e) {
            result=false;
            e.printStackTrace();
        }

        return result;
    }
    public boolean getIsInvalidServer(){
        return INVALID_SERVER;
    }
    /**
     * This method resets the unit checks for invalid, the invalid_server, and banned organization.
     * @orgName is the DHIS_ORG_NAME
     */
    public static void newOrgUnitOrServer(){
        BANNED=false;
        DHIS_UNEXISTENT_ORG_UNIT=null;
        DHIS_INVALID_URL="";
        INVALID_SERVER=false;
    }

    /**
     * @code is the DHIS_ORG_NAME
     * @return If org_unit not valid or have no UID Returns null, else the UID
     */
    private String getUIDCheckProgramClosedDate(String code) throws Exception{
        //https://malariacare.psi.org/api/organisationUnits.json?paging=false&fields=id,name,openingDate,closedDate,programs&filter=code:eq:KH_Cambodia
        String DHIS_PULL_URL=getDhisOrgUnitURL(code);
        JSONArray responseArray=null;
        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        Log.d(TAG,"Url"+DHIS_PULL_URL+"");
        BasicAuthenticator basicAuthenticator =new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);
        Log.d("URL",DHIS_PULL_URL);
        Response response=null;
        Request request= new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_PULL_URL)
                .build();
        try {
            response = client.newCall(request).execute();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }

        try {
            if(!response.isSuccessful()){
                Log.e(TAG, "pullOrgUnitUID (" + response.code()+"): "+response.body().string());
                throw new IOException(response.message());
            }

            JSONObject responseJSON=parseResponse(response.body().string());
            responseArray = (JSONArray) responseJSON.get(TAG_ORGANISATIONUNIT);

            if (responseArray.length() == 0) {
                Log.e(TAG, "pullOrgUnitUID: No UID for code " + code);
                //Assign the used org_unit to the unexistent_org_unit for not make new pulls.
                // throw new IOException(applicationContext.getString(R.string.dialog_error_push_no_uid)+" "+code);
                return null;
            }
            try {
                String date = responseArray.getJSONObject(0).getString(TAG_CLOSEDATA);
                Calendar calendarDate = Utils.parseStringToCalendar(date);

                if(!Utils.isDateOverSystemDate(calendarDate)){
                    if(BANNED==false) {
                        BANNED = true;
                        try {
                            throw new ShowException(applicationContext.getString(R.string.exception_org_unit_banned), applicationContext);
                        } catch (ShowException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }catch(Exception e){
                //if the date is null is not need check
            }
        }catch(Exception e){
            return null;
        }
        //Return the org_unit id
        return responseArray.getJSONObject(0).getString("id");
    }
    /**
     * This method returns a String[] whit the Organitation codes
     * @throws Exception
     */
    public String[] pullOrgUnitsCodes() throws Exception{
        if(!INVALID_SERVER) {
            //https://malariacare.psi.org/api/programs/IrppF3qERB7.json?fields=organisationUnits
            final String DHIS_PULL_URL = getDhisOrgUnitsURL(DHIS_UID_PROGRAM);

            OkHttpClient client = UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

            BasicAuthenticator basicAuthenticator = new BasicAuthenticator();
            client.setAuthenticator(basicAuthenticator);

            Log.d(TAG, "Url" + DHIS_PULL_URL + "");
            Log.e(TAG, "pullOrgUnitUID URL (" + DHIS_PULL_URL);
            Request request = new Request.Builder()
                    .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                    .url(DHIS_PULL_URL)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                Log.e(TAG, "pullOrgUnitUID (" + response.code() + "): " + response.body().string());
                throw new IOException(response.message());
            }

            JSONObject responseJSON = parseResponse(response.body().string());
            JSONArray responseArray = (JSONArray) responseJSON.get(TAG_ORGANISATIONUNIT);
            if (responseArray.length() == 0) {
                Log.e(TAG, "pullOrgUnitUID: No org_unit ");
                throw new IOException(applicationContext.getString(R.string.dialog_error_push_no_uid));
            }
            return Utils.jsonArrayToStringArray(responseArray, "code");
        }
        else {
            INVALID_SERVER=true;
            String[] value = new String[1];
            value[0] = "";
            return value;
        }
    }
    /**
     * This method checks the UID_PROGRAM uid is in the server( and throws exception is not)
     * @throws Exception
     */
    public boolean isValidProgram() throws Exception{
        //https://malariacare.psi.org/api/programs/IrppF3qERB7.json?fields=organisationUnits
        final String DHIS_PULL_URL=getIsValidProgramUrl(DHIS_UID_PROGRAM);

        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        BasicAuthenticator basicAuthenticator =new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);

        Log.e(TAG, "pullOrgUnitUID URL (" + DHIS_PULL_URL);
        Request request = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_PULL_URL)
                .build();
        Response response =null;
        response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            Log.e(TAG, "pullOrgUnitUID (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }

        JSONObject responseJSON=parseResponse(response.body().string());
        String id = String.valueOf(responseJSON.get(TAG_ID));
        if(id.length()==0){
            Log.e(TAG, "pullOrgUnitUID: No org_unit ");
            throw new IOException(applicationContext.getString(R.string.dialog_error_push_no_uid));
        }
        return DHIS_UID_PROGRAM.equals(id);
    }

    /**
     * Get the current description for a org_unit from the server
     * @return return the description or "".
     */
    public String getCurrentDescription(String url)  throws Exception{
        //https://malariacare.psi.org/api/organisationUnits/Pg91OgEIKIm/description
        String DHIS_PULL_URL=url;
        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        Log.d(TAG,"Url"+DHIS_PULL_URL+"");
        BasicAuthenticator basicAuthenticator =new BasicAuthenticator();
        client.setAuthenticator(basicAuthenticator);

        Request request = new Request.Builder()
                .header(basicAuthenticator.AUTHORIZATION_HEADER, basicAuthenticator.getCredentials())
                .url(DHIS_PULL_URL)
                .build();

        Response response = client.newCall(request).execute();
        if(!response.isSuccessful()){
            Log.e(TAG, "closingDateURL (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }
        String jsonData = response.body().string();
        Log.d(TAG,"Response"+jsonData);
        String description="";
        JSONObject responseObject=new JSONObject(jsonData);
        if(responseObject.length()==0){
            Log.e(TAG, "closingDateURL: No UID for code " + DHIS_ORG_NAME);
//            throw new IOException(applicationContext.getString(R.string.dialog_error_push_no_uid)+" "+code);
            return description;
        }
        Log.d(TAG, "data description:" + responseObject.getString("description"));
        try {
            description =responseObject.getString("description");
        }
        catch(Exception e){
            description="";
            return description;
        }
        return description;
    }

    /**
     * Adds questions and scores values to the JSON object
     * Format: {dataValues: [{dataElement:'234567',value:'34'}, ...]}
     * @param data JSON object to update
     * @throws Exception
     */
    private JSONObject prepareDataElements(JSONObject data)throws Exception{
        Log.d(TAG, "prepareDataElements for survey: " + survey.getId_survey());

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
    private JSONArray prepareValues(JSONArray values,JSONArray controlDataElements) throws Exception{
        List<Value> surveyValues=survey.getValues();
        if(surveyValues==null || surveyValues.size()==0){
            throw new Exception(applicationContext.getString(R.string.dialog_info_push_empty_survey));
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
    /**
     *
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

        if(Utils.isPictureQuestion()){
            //Cleans score
            ScoreRegister.clear();

            //Register scores for tabs
            List<Tab> tabs=survey.getTabGroup().getProgram().getTabs();
            ScoreRegister.registerTabScores(tabs);

            //Register scores for composites
            List<CompositeScore> compositeScoreList=CompositeScore.listAllByProgram(survey.getTabGroup().getProgram());
            ScoreRegister.registerCompositeScores(compositeScoreList);

            //Initialize scores x question
            ScoreRegister.initScoresForQuestions(Question.listAllByProgram(survey.getTabGroup().getProgram()), survey);

            //1 CompositeScore -> 1 dataValue
            for(CompositeScore compositeScore:compositeScoreList){
                values.put(prepareValue(compositeScore));
            }

            PhoneMetaData phoneMetaData= Session.getPhoneMetaData();
            values.put(preparePhoneValue(TAG_PHONEMETADA, phoneMetaData.getPhone_metaData()));
        }
        else {
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
        if(Utils.isPictureQuestion())
            elementObject.put(TAG_VALUE, value.getValue());

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
    private JSONObject pushPictureData(JSONObject data)throws Exception {

        Response response = executeCall(data, DHIS_PUSH_API, "POST");

        if(!response.isSuccessful()){
            Log.e(TAG, "pushData (" + response.code()+"): "+response.body().string());
            throw new IOException(response.message());
        }
        return parseResponse(response.body().string());
    }

    /**
     * Returns the URL that points to the DHIS server (Pull) API according to preferences.
     * @return
     */
    private String getDhisOrgUnitURL(String code){
        String url= DHIS_SERVER;
        Log.d("uid", DHIS_UID_PROGRAM);
        url=url+String.format(DHIS_PULL_ORG_UNIT_API,code,DHIS_UID_PROGRAM);
        return url.replace(" ", "%20");
    }

    /**
     * Returns the ClosedDate that points to the DHIS server (Pull) API according to preferences.
     * @return
     */
    private String getPatchClosedDateUrl(String url, String orguid){
        //Get the org_ID
        url=url+String.format(DHIS_PATCH_URL_CLOSED_DATE,orguid);
        return url.replace(" ", "%20");
    }
    /**
     * Returns the Description of orgUnit that points to the DHIS server (Pull) API according to preferences.
     * @return
     */
    private String getPatchClosedDescriptionUrl(String url, String orguid){
        url=url+String.format(DHIS_PATCH_URL_DESCRIPTIONCLOSED_DATE,orguid);
        return url.replace(" ","%20");
    }

    /**
     * Returns the URL that points to the DHIS server (Pull) API according to preferences.
     * @return
     */
    private String getDhisOrgUnitsURL(String uid_program){
        String url= DHIS_SERVER +DHIS_PULL_PROGRAM+uid_program+DHIS_PULL_ORG_UNITS_API;
        return url.replace(" ", "%20");
    }
    /**
     *
     * This method returns the valid url for check the program
     * @return url for ask if the program uid exist with the UID_PROGRAM value.
     */
    public String getIsValidProgramUrl(String uid_program) {
        String url = DHIS_SERVER +DHIS_PULL_PROGRAM+uid_program+DHIS_EXIST_PROGRAM;
        Log.d(TAG,"validprogramurl"+url);
        return url.replace(" ", "%20");
    }

    /**
     * Call to DHIS Server
     * @param data
     * @param url
     */
    private Response executeCall(JSONObject data, String url, String method) throws IOException {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        String DHIS_URL=sharedPreferences.getString(getDhisURL(), applicationContext.getString(R.string.login_info_dhis_default_server_url)) + url;
        Log.d(TAG,"Url"+DHIS_URL+"");
        OkHttpClient client= UnsafeOkHttpsClientFactory.getUnsafeOkHttpClient();

        Log.d(TAG,"Url"+DHIS_URL+"");
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
        if(Utils.isPictureQuestion())
            return DHIS_SERVER;
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

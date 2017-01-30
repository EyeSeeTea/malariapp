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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.data.database.model.ServerMetadata;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.database.model.Value;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.network.PullClient;
import org.eyeseetea.malariacare.data.remote.SdkQueries;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.AUtils;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.FailedItemFlow_Table;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a given survey into its corresponding events+datavalues.
 */
public class ConvertToSDKVisitor implements
        org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.IConvertToSDKVisitor {

    private final static String TAG=".ConvertToSDKVisitor";

    /**
     * Context required to recover magic UID for mainScore dataElements
     */
    Context context;

    String overallScoreCode;
    String mainScoreClassCode;
    String mainScoreACode;
    String mainScoreBCode;
    String mainScoreCCode;
    String forwardOrderCode;
    String pushDeviceCode;
    String overallProductivityCode;
    String nextAssessmentCode;

    String createdOnCode;
    String updatedDateCode;
    String updatedUserCode;

    /**
     * List of surveys that are going to be pushed
     */
    List<Survey> surveys;

    /**
     * Map app surveys with sdk events (N to 1)
     */
    Map<Long,EventExtended> events;

    /**
     * Each survey is new|known modification|unknow modification.
     * If something goes wrong while pushing you need a mechanism to reset this to its original state.
     */
    Map<Long,String> originalSurveysUIDs;

    /**
     * The last survey that it is being translated
     */
    Survey currentSurvey;

    /**
     * The generated event
     */
    EventExtended currentEvent;

    /**
     * Timestamp that captures the moment when the survey is converted right before being sent
     */
    Date uploadedDate;

    /**
     * Used to control if the actual survey/event is new or update
     */
    boolean isAModification;


    ConvertToSDKVisitor(Context context){
        this.context=context;
        // FIXME: We should create a visitor to translate the ControlDataElement class
        overallScoreCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.overall_score_code));
        mainScoreClassCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.main_score_class_code));
        mainScoreACode = ServerMetadata.findControlDataElementUid(context.getString(R.string.main_score_a_code));
        mainScoreBCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.main_score_b_code));
        mainScoreCCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.main_score_c_code));
        forwardOrderCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.forward_order_code));
        pushDeviceCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.push_device_code));
        overallProductivityCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.overall_productivity_code));
        nextAssessmentCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.next_assessment_code));

        createdOnCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.created_on_code));
        updatedDateCode =ServerMetadata.findControlDataElementUid(context.getString(R.string.upload_date_code));
        updatedUserCode = ServerMetadata.findControlDataElementUid(context.getString(R.string.uploaded_by_code));
        surveys = new ArrayList<>();
        events = new HashMap<>();
        originalSurveysUIDs = new HashMap<>();
    }

    @Override
    public void visit(Survey survey) throws Exception{

        uploadedDate =new Date();

        //Turn survey into an event
        this.currentSurvey=survey;

        Log.d(TAG,String.format("Creating event for survey (%d) ...",survey.getId_survey()));
        Log.d(TAG,String.format("Creating event for survey (%s) ...", survey.toString()));


        this.currentEvent=buildEvent();
        Log.d(TAG,currentEvent.toString());

        //Calculates scores and update survey
        Log.d(TAG,"Registering scores...");
        List<CompositeScore> compositeScores = ScoreRegister.loadCompositeScores(survey, Constants.PUSH_MODULE_KEY);
        updateSurvey(compositeScores, currentSurvey.getId_survey(), Constants.PUSH_MODULE_KEY);

        //Turn score values into dataValues
        Log.d(TAG, "Creating datavalues from scores...");
        for(CompositeScore compositeScore:compositeScores){
            compositeScore.accept(this);
        }

        //Turn question values into dataValues
        Log.d(TAG, "Creating datavalues from questions... Values"+survey.getValues().size());
        for(Value value:currentSurvey.getValues()) {
            //in a modification an old value is skipped
            if(isAModification && value.getUploadDate().before(currentSurvey.getUploadDate())){
                continue;
            }
            //value -> datavalue
            value.accept(this);
        }


        survey.setUploadDate(uploadedDate);

        //Update all the dates after checks the new values
        updateEventDates();

        Log.d(TAG, "Creating datavalues from other stuff...");
        buildControlDataElements(survey);

        //Annotate both objects to update its state once the process is over
        annotateSurveyAndEvent();
    }

    /**
     * Inits current survey stuff
     * @param survey
     * @return
     */
    private Survey buildCurrentSurvey(Survey survey){
        Log.d(TAG,String.format("Init survey stuff for survey id: %d",survey.getId_survey()));
        this.uploadedDate =new Date();
        this.isAModification = survey.isAModification();
        this.originalSurveysUIDs.put(survey.getId_survey(),survey.getEventUid());
        return survey;
    }

    /**
     * Inits current event stuff.
     * This implies doing the right choosing the right action (modification or brand new)
     * @return
     * @throws Exception
     */
    private EventExtended buildCurrentEvent()throws Exception{
        Log.d(TAG,String.format("Init event stuff for survey id: %d",this.currentSurvey.getId_survey()));

        //Brand new event
        if(!this.isAModification){
            return buildNewEvent();
        }

        //A modification, look for a local built event
        List<EventExtended> eventsToBePushed= EventExtended.getExtendedList(
                SdkQueries.getEvents(currentSurvey.getOrgUnit().getUid(),currentSurvey.getProgram().getUid()));

        //No local events, try to build from server
        if(eventsToBePushed.isEmpty()){
            return buildFromServer();
        }

        //Adding values to an already built event that needs to be pushed
        return eventsToBePushed.get(0);
    }


    /**
     * Builds an event from a survey
     * @return
     */
    private EventExtended buildNewEvent() throws Exception{
        EventExtended newEvent=new EventExtended();
        newEvent = setBasicEventProperties(newEvent);
        Log.d(TAG, "Saving event " + newEvent.toString());
        newEvent.save();
        return newEvent;
    }

    /**
     * Look for an event to modify from the server.
     * @return
     */
    private EventExtended buildFromServer()throws Exception{
        PullClient pullClient = new PullClient(DashboardActivity.dashboardActivity);
        EventExtended eventFromServer=pullClient.getLastEventInServerWith(this.currentSurvey.getOrgUnit(), this.currentSurvey.getProgram());
        //No event to modify -> create a new one
        if(eventFromServer==null){
            return buildNewEvent();
        }

        //Found in server
        eventFromServer=setBasicEventProperties(eventFromServer);
        eventFromServer.save();
        return eventFromServer;
    }

    private EventExtended setBasicEventProperties(EventExtended eventToUpdate) throws Exception{
        eventToUpdate.setStatus(EventExtended.STATUS_COMPLETED);
        eventToUpdate.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        eventToUpdate.setProgramId(currentSurvey.getProgram().getUid());
        eventToUpdate.setProgramStageId(currentSurvey.getProgram().getUid());
        Location lastLocation=getEventLocation();
        //location -> set lat/lng
        if(lastLocation!=null) {
            eventToUpdate.setLatitude(lastLocation.getLatitude());
            eventToUpdate.setLongitude(lastLocation.getLongitude());
        }
        return eventToUpdate;
    }

    @Override
    public void visit(CompositeScore compositeScore) {
        List<Float> result=ScoreRegister.getCompositeScoreResult(compositeScore,currentSurvey.getId_survey(), Constants.PUSH_MODULE_KEY);
        //Checks if the result have at least one valid denominator.
        if(result!=null && result.get(1)==0)
            return;
        DataValueExtended dataValue=new DataValueExtended();
        dataValue.setDataElement(compositeScore.getUid());
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(AUtils.round(ScoreRegister.getCompositeScore(compositeScore,currentSurvey.getId_survey(), Constants.PUSH_MODULE_KEY)));
        dataValue.save();
    }

    @Override
    public void visit(Value value) {
        DataValueExtended dataValue=new DataValueExtended();
        dataValue.setDataElement(value.getQuestion().getUid());
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        if(value.getOption()!=null){
            dataValue.setValue(value.getOption().getCode());
        }else{
            dataValue.setValue(value.getValue());
        }
        dataValue.save();
    }

    /**
     * Builds an event from a survey
     * @return
     */
    private EventExtended buildEvent() throws Exception{
        currentEvent=new EventExtended();
        currentEvent.setStatus(EventExtended.STATUS_COMPLETED);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getProgram().getUid());
        currentEvent.setProgramStageId(currentSurvey.getProgram().getUid());
        updateEventLocation();
        Log.d(TAG, "Saving event " + currentEvent.toString());
        currentEvent.save();
        return currentEvent;
    }

    /**
     * Fulfills the dates of the event
     */
    private void updateEventDates() {

        // NOTE: do not try to set the event creation date. SDK will try to update the event in the next push instead of creating it and that will crash
        currentEvent.setEventDate(new DateTime(currentSurvey.getCompletionDate()));
        currentEvent.setDueDate(new DateTime(currentSurvey.getScheduledDate()));
        //Not used
        currentEvent.setLastUpdated(new DateTime(currentSurvey.getUploadDate()));
        currentEvent.save();
    }

    /**
     * Updates the location of the current event that it is being processed
     * @throws Exception
     */
    private void updateEventLocation() throws Exception{
        Location lastLocation = LocationMemory.get(currentSurvey.getId_survey());
        //If location is required but there is no location -> exception
        if(PreferencesState.getInstance().isLocationRequired() && lastLocation==null){
            throw new Exception(context.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //No location + not required -> done
        if(lastLocation==null){
            return;
        }

        //location -> set lat/lng
        currentEvent.setLatitude(lastLocation.getLatitude());
        currentEvent.setLongitude(lastLocation.getLongitude());
    }

    /**
     * Builds several datavalues from the mainScore of the survey
     * @param survey
     */
    private void buildControlDataElements(Survey survey) {

        //Overall score
        if(controlDataElementExistsInServer(overallScoreCode)  && survey.hasMainScore()){
            buildAndSaveDataValue(overallScoreCode, survey.getMainScore().toString());
        }

        //It Checks if the dataelement exists, before build and save the datavalue
        //Created date
        if(controlDataElementExistsInServer(createdOnCode)){
            addDataValue(createdOnCode, EventExtended.format(survey.getCreationDate(), EventExtended.DHIS2_GMT_DATE_FORMAT));
        }

        //Updated date
        if(controlDataElementExistsInServer(updatedDateCode)){
            addOrUpdateDataValue(updatedDateCode, EventExtended.format(survey.getUploadDate(), EventExtended.DHIS2_GMT_DATE_FORMAT));
        }

        //Updated by user
        if(controlDataElementExistsInServer(updatedUserCode)){
            addOrUpdateDataValue(updatedUserCode, Session.getUser().getUid());
        }

        //Forward order
        if(controlDataElementExistsInServer(forwardOrderCode)) {
            addOrUpdateDataValue(forwardOrderCode, context.getString(R.string.forward_order_value));
        }

        //Overall score
        if(!overallScoreCode.equals("") && survey.hasMainScore())
            buildAndSaveDataValue(overallScoreCode, survey.getMainScore().toString());

        //Forward order
        if(controlDataElementExistsInServer(pushDeviceCode)) {
            addOrUpdateDataValue(pushDeviceCode, Session.getPhoneMetaData().getPhone_metaData() + "###" + AUtils.getCommitHash(context));
        }

        //MainScoreUID
        if(controlDataElementExistsInServer(mainScoreClassCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreClassCode, survey.getType());
        }

        //MainScore A
        if(controlDataElementExistsInServer(mainScoreACode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreACode, survey.isTypeA() ? "true" : "false");
        }

        //MainScore B
        if(controlDataElementExistsInServer(mainScoreBCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreBCode, survey.isTypeB() ? "true" : "false");
        }

        //MainScoreC
        if(controlDataElementExistsInServer(mainScoreCCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreCCode, survey.isTypeC() ? "true" : "false");
        }

        //Forward Order
        if(controlDataElementExistsInServer(forwardOrderCode)) {
            addOrUpdateDataValue(forwardOrderCode, context.getString(R.string.forward_order_value));
        }

        //Push Device
        if(controlDataElementExistsInServer(pushDeviceCode)) {
            addOrUpdateDataValue(pushDeviceCode, Session.getPhoneMetaData().getPhone_metaData() + "###" + AUtils.getCommitHash(context));
        }

        //Overall productivity
        if(controlDataElementExistsInServer(overallProductivityCode)) {
            addOrUpdateDataValue(overallProductivityCode, Integer.toString(OrgUnitProgramRelation.getProductivity(survey)));
        }

        //Next assessment
        if(controlDataElementExistsInServer(nextAssessmentCode)) {
            addOrUpdateDataValue(nextAssessmentCode, EventExtended.format(SurveyPlanner.getInstance().findScheduledDateBySurvey(survey), EventExtended.AMERICAN_DATE_FORMAT));
        }
    }

    private boolean controlDataElementExistsInServer(String controlDataElementUID){
        return controlDataElementUID!=null && !controlDataElementUID.equals("");
    }

    /**
     * Adds a new datavalue for the current event only if it does NOT already exist. To avoid duplication.
     * @param dataElementUID
     * @param value
     */
    private void addDataValue(String dataElementUID,String value){
        DataValueExtended dataValue= DataValueExtended.findByEventAndUID(currentEvent.getEvent(),dataElementUID);
        //Already added
        if(dataValue!=null){
            return;
        }

        //Build a new value
        buildAndSaveDataValue(dataElementUID, value);
    }

    private void addOrUpdateDataValue(String dataElementUID,String value){
        DataValueExtended dataValue=null;
        try {
            dataValue = DataValueExtended.findByEventAndUID(
                    currentEvent.getEvent(), dataElementUID);
        }catch (Exception e){
            e.printStackTrace();}
        //Already added, update its value
        if(dataValue.getDataValue()!=null){
            dataValue.setValue(value);
            dataValue.save();
            return;
        }

        buildAndSaveDataValue(dataElementUID,value);
    }

    private void buildAndSaveDataValue(String uid, String value){
        DataValueExtended dataValue= new DataValueExtended();
        dataValue.setDataElement(uid);
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(value);
        dataValue.save();
    }

    /**
     * Several properties must be updated when a survey is about to be sent.
     * This changes will be saved just when process finish successfully.
     * @param compositeScores
     */
    private void updateSurvey(List<CompositeScore> compositeScores, float idSurvey, String module){
        currentSurvey.setMainScore(ScoreRegister.calculateMainScore(compositeScores, idSurvey, module));
        currentSurvey.setStatus(Constants.SURVEY_SENT);
        currentSurvey.setEventUid(currentEvent.getUid());
    }

    /**
     * Updates the location of the current event that it is being processed
     * @throws Exception
     */
    private Location getEventLocation() throws Exception{
        Location lastLocation = LocationMemory.get(currentSurvey.getId_survey());
        //If location is required but there is no location -> exception
        if(PreferencesState.getInstance().isLocationRequired() && lastLocation==null){
            throw new Exception(context.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //location -> set lat/lng
        currentEvent.setLatitude(lastLocation.getLatitude());
        currentEvent.setLongitude(lastLocation.getLongitude());

        return lastLocation;
    }

    /**
     * Annotates the survey and event that has been processed
     */
    private void annotateSurveyAndEvent() {
        surveys.add(currentSurvey);
        currentEvent.setLastUpdated(new DateTime(uploadedDate.getTime()));
        events.put(currentSurvey.getId_survey(),currentEvent);
        Log.d(TAG, String.format("%d surveys converted so far", surveys.size()));
    }

    /**
     * Saves changes in the survey (supposedly after a successfull push)
     */
    public void saveSurveyStatus(Map<String,ImportSummary> importSummaryMap){
        for(int i=0;i<surveys.size();i++){
            Survey iSurvey=surveys.get(i);
            EventExtended iEvent=new EventExtended(events.get(iSurvey.getId_survey()));
            ImportSummary importSummary=importSummaryMap.get(iEvent.getEvent().getUId());
            FailedItemFlow failedItem=  EventExtended.hasConflict(iEvent.getLocalId());

            //No errors -> Save and next
            if(!hasImportSummaryErrors(importSummary) && failedItem==null){
                saveSurveyFromImportSummary(iSurvey);
                continue;
            }

            if(importSummary==null){
                rollbackSurvey(iSurvey);
                return;
            }

            //Errors
            if(importSummary!=null) {
                Log.d(TAG, importSummary.toString());
            }
            //Some error happened -> move back to completed
            if(failedItem!=null) {
                rollbackSurvey(iSurvey);
                List<String> failedUids=getFailedUidQuestion(failedItem.getErrorMessage());
                for(String uid:failedUids) {
                    Log.d(TAG, "PUSH process...Conflict in "+uid+" dataelement pushing survey: "+iSurvey.getId_survey());
                    iSurvey.saveConflict(uid);
                    iSurvey.setStatus(Constants.SURVEY_CONFLICT);
                }
                iSurvey.save();
            }

            //XXX Whats this?
            if(iSurvey.getStatus()!=Constants.SURVEY_CONFLICT && org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary.Status.SUCCESS.equals(importSummary.getStatus())) {
                if(iEvent.getEventDate()==null || iEvent.getEventDate().equals("")) {
                    //the event is invalid. The event will be pushed but we need inform to the user.
                    DashboardActivity.showException(context.getString(R.string.error_message), String.format(context.getString(R.string.error_message_push), iEvent.getEvent()));
                }
                saveSurveyFromImportSummary(iSurvey);
                Log.d(TAG, "PUSH process...Survey uploaded: " + iSurvey.getId_survey());
            }
        }
    }

    private void rollbackSurvey(Survey survey){
        survey.setStatus(Constants.SURVEY_COMPLETED);
        survey.setEventUid(originalSurveysUIDs.get(survey.getId_survey()));
        survey.save();
    }

    private void saveSurveyFromImportSummary(Survey iSurvey) {
        iSurvey.setStatus(Constants.SURVEY_SENT);
        iSurvey.setUploadDate(uploadedDate);
        iSurvey.saveMainScore();
        iSurvey.save();

        Log.d(TAG, "PUSH process...OK. Survey saved");
    }

    /**
     * Checks whether the given event contains errors in SDK FailedItemExtended table or has been successful.
     * If not return null, it is becouse this item had a conflict.
     * @param localId
     * @return
     */
    private FailedItemFlow hasConflict(long localId){
        return new Select()
                .from(FailedItemFlow.class)
                .where(FailedItemFlow_Table.itemId
                        .is(localId))
                .querySingle();
    }

    /**
     * Get dataelement fails from errormessage JSON.
     * @param responseData
     * @return
     */
    private List<String> getFailedUidQuestion(String responseData){
        String message="";
        List<String> uid=new ArrayList<>();
        JSONArray jsonArrayResponse=null;
        JSONObject jsonObjectResponse= null;
        try {
            jsonObjectResponse = new JSONObject(responseData);
            message=jsonObjectResponse.getString("message");
            jsonObjectResponse=new JSONObject(jsonObjectResponse.getString("response"));
            jsonArrayResponse=new JSONArray(jsonObjectResponse.getString("importSummaries"));
            jsonObjectResponse=new JSONObject(jsonArrayResponse.getString(0));
            //conflicts
            jsonArrayResponse=new JSONArray(jsonObjectResponse.getString("conflicts"));
            //values
            for(int i=0;i<jsonArrayResponse.length();i++) {
                jsonObjectResponse = new JSONObject(jsonArrayResponse.getString(i));
                uid.add(jsonObjectResponse.getString("object"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(message!=""){
            DashboardActivity.showException(context.getString(R.string.error_message), message);
        }
        return  uid;
    }

    /**
     * Checks whether the given importSummary contains errors or has been successful.
     * An import with 0 importedItems is an error too.
     * @param importSummary
     * @return
     */
    private boolean hasImportSummaryErrors(ImportSummary importSummary){
        if(importSummary==null){
            return true;
        }

        if(importSummary.getImportCount()==null){
            return true;
        }
        return importSummary.getImportCount().getImported()==0;
    }

    /**
     * Returns the name of the username avoiding NPE
     * @return
     */
    private String getSafeUsername(){
        User user = Session.getUser();
        if(user!=null){
            return user.getName();
        }
        return "";
    }
}

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

package org.eyeseetea.malariacare.database.iomodules.dhis.exporter;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.network.PullClient;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.Utils;

import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.ImportSummary;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Turns a given survey into its corresponding events+datavalues.
 */
public class ConvertToSDKVisitor implements IConvertToSDKVisitor {

    private final static String TAG=".ConvertToSDKVisitor";

    /**
     * Context required to recover magic UID for mainScore dataElements
     */
    Context context;

    String mainScoreUID;
    String mainScoreAUID;
    String mainScoreBUID;
    String mainScoreCUID;
    String forwardOrderUID;

    String createdOnUID;
    String createdByUID;
    String updatedDateUID;
    String updatedUserUid;
    /**
     * List of surveys that are going to be pushed
     */
    List<Survey> surveys;

    /**
     * List of events that are going to be pushed
     */
    List<Event> events;


    /**
     * The last survey that it is being translated
     */
    Survey currentSurvey;

    /**
     * The generated event
     */
    Event currentEvent;

    /**
     * Timestamp that captures the moment when the survey is converted right before being sent
     */
    Date uploadedDate;

    /**
     * Used to control if the actual survey/event is new or update
     */
    boolean updateEvent;

    /**
     * Used to control if the actual survey/event is new or update
     */
    boolean skippedEvent;


    ConvertToSDKVisitor(Context context){
        this.context=context;
        mainScoreUID=context.getString(R.string.main_score);
        mainScoreAUID=context.getString(R.string.main_score_a);
        mainScoreBUID=context.getString(R.string.main_score_b);
        mainScoreCUID=context.getString(R.string.main_score_c);
        forwardOrderUID=context.getString(R.string.forward_order);

        createdOnUID =context.getString(R.string.createdOnUID);
        createdByUID =context.getString(R.string.createdByUid);
        updatedDateUID=context.getString(R.string.uploadedDateUID);
        updatedUserUid=context.getString(R.string.createdByUid);
        surveys = new ArrayList<>();
        events = new ArrayList<>();
    }

    @Override
    public void visit(Survey survey) throws Exception{
        updateEvent =false;

        uploadedDate =new Date();

        //Turn survey into an event
        this.currentSurvey=survey;
        Log.d(TAG,String.format("Creating event for survey (%d) ...",survey.getId_survey()));
        Log.d(TAG,String.format("Creating event for survey (%s) ...", survey.toString()));

        //if the event exist in the survey, it will be patched, else, created.
        if(survey.getEventUid()!=null) {
            buildUpgradeEvent(survey);
        }
        else
            this.currentEvent = buildEvent();
        Log.d(TAG,currentEvent.toString());

        //Calculates scores and update survey
        Log.d(TAG,"Registering scores...");
        List<CompositeScore> compositeScores = ScoreRegister.loadCompositeScores(survey);
        updateSurvey(compositeScores);

        //Turn score values into dataValues
        Log.d(TAG, "Creating datavalues from scores...");
        for(CompositeScore compositeScore:compositeScores){
            compositeScore.accept(this);
        }

        //Turn question values into dataValues
        Log.d(TAG, "Creating datavalues from questions... Values"+survey.getValues().size());


        for(Value value : survey.getValues()) {
            if(updateEvent) {
                if (value.getUploadedDate().after(currentSurvey.getUploadedDate())) {
                    value.accept(this);
                    Log.d(TAG, "Value saved: " + value);
                }
            }
            else
                    value.accept(this);
        }

        //Update all the dates after checks the new values
        updateEventDates();
        Log.d(TAG,"Saving dates in control dataelements");

        buildDateControlDataElements(survey);

        Log.d(TAG, "Creating datavalues from other stuff...");
        buildControlDataElements(survey);

        //Annotate both objects to update its state once the process is over
        annotateSurveyAndEvent();
    }

    private Event forceLastSurvey(Survey survey) {
        //download last survey uid
        PullClient pullClient = new PullClient((DashboardActivity) DashboardActivity.dashboardActivity);
        PullClient.EventInfo eventInfo = pullClient.getLastEventUid(survey.getOrgUnit(), survey.getTabGroup());
        if(!survey.getEventUid().equals(eventInfo.getEventUid())){
            survey.setEventUid(eventInfo.getEventUid());
        }
        if(survey.getEvent()==null)
            return buildFakeEvent(survey.getOrgUnit(),survey.getTabGroup(), eventInfo);
        return survey.getEvent();
    }

    @Override
    public void visit(CompositeScore compositeScore) {
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(compositeScore.getUid());
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setProvidedElsewhere(false);
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(Utils.round(ScoreRegister.getCompositeScore(compositeScore)));
        dataValue.save();
    }

    @Override
    public void visit(Value value) {
        if(updateEvent){
            if(value.getQuestion()==null) {
                //The controlDataelements values don't have question. It should be ignored  in a upload event.
                return;
            }
            if(value.getOption()==null){
                return;
            }
        }
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(value.getQuestion().getUid());
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setProvidedElsewhere(false);
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
    private Event buildEvent()throws Exception{
        currentEvent =new Event();
        currentEvent.setStatus(Event.STATUS_COMPLETED);
        currentEvent.setFromServer(false);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getTabGroup().getProgram().getUid());
        currentEvent.setProgramStageId(currentSurvey.getTabGroup().getUid());
        updateEventLocation();
        //updateEventDates();
        Log.d(TAG, "Saving event " + currentEvent.toString());
        currentEvent.save();
        return currentEvent;
    }

    /**
     * Builds an fake event only to send the new DataValues.
     * @return
     */
    public static Event buildFakeEvent(OrgUnit orgUnit, TabGroup tabGroup, PullClient.EventInfo eventInfo) {
        //a false event was created to path the event datavalues
        Log.d(TAG, "Recovering Event:" + eventInfo.getEventUid() + " not exist");
        Log.d(TAG, "Creating fake event to upgrade one event in the server");
        Event event = new Event();
        event.setUid(eventInfo.getEventUid());
        event.setLastUpdated(EventExtended.format(eventInfo.getEventDate(), EventExtended.DHIS2_DATE_FORMAT));
        event.setEventDate(EventExtended.format(eventInfo.getEventDate(), EventExtended.DHIS2_DATE_FORMAT));
        event.setOrganisationUnitId(orgUnit.getUid());
        event.setProgramId(tabGroup.getProgram().getUid());
        event.setProgramStageId(tabGroup.getUid());
        event.save();
        return event;
    }

    /**
     * Builds or modify a event to be upgraded.
     * @return
     */
    private void buildUpgradeEvent(Survey survey) {
        updateEvent =true;
        currentEvent = forceLastSurvey(survey);

        if(currentEvent != null){
            currentEvent.setCreated(null);
            uploadedDate = currentSurvey.getUploadedDate();
            //It's necesary, Set"from server" as false to upload the event
            currentEvent.setFromServer(false);
            currentEvent.setStatus(Event.STATUS_COMPLETED);
            currentEvent.save();
        }
        else
        {
            Log.d(TAG, "Error Creating/Recovering Event:"+survey.getEventUid()+" not exist");
        }
    }

    /**
     * Fulfills the dates of the event
     */
    private void updateEventDates() {

        // NOTE: do not try to set the event creation date. SDK will try to update the event in the next push instead of creating it and that will crash
        String date=EventExtended.format(currentSurvey.getCompletionDate(), EventExtended.DHIS2_DATE_FORMAT);
        currentEvent.setEventDate(date);
        currentEvent.setDueDate(EventExtended.format(currentSurvey.getScheduledDate(), EventExtended.DHIS2_DATE_FORMAT));
        //Not used
        currentEvent.setLastUpdated(EventExtended.format(currentSurvey.getUploadedDate(), EventExtended.DHIS2_DATE_FORMAT));
        }

    /**
     * Builds several datavalues from the mainScore of the survey
     * @param survey
     */
    private void buildControlDataElements(Survey survey) {

        //MainScoreUID
        buildAndSaveDataValue(mainScoreUID, survey.getType());

        //MainScore A
        buildAndSaveDataValue(mainScoreAUID, survey.isTypeA() ? "true" : "false");

        //MainScore B
        buildAndSaveDataValue(mainScoreBUID, survey.isTypeB() ? "true" : "false");

        //MainScoreC
        buildAndSaveDataValue(mainScoreCUID, survey.isTypeC() ? "true" : "false");

        //Forward Order
        buildAndSaveDataValue(forwardOrderUID, context.getString(R.string.forward_order_value));
    }

    /**
     * Builds several datavalues from the Dates of the survey
     * @param survey
     */
    private void buildDateControlDataElements(Survey survey) {
        //Created date
        buildAndSaveDataValue(createdOnUID, EventExtended.format(survey.getCreationDate(), EventExtended.AMERICAN_DATE_FORMAT));

        //Updated date
        buildAndSaveDataValue(updatedDateUID, EventExtended.format(uploadedDate, EventExtended.AMERICAN_DATE_FORMAT));

        //Updated by user
        buildAndSaveDataValue(updatedUserUid, Session.getUser().getUid());

        //Updated by user
        buildAndSaveDataValue(createdByUID, Session.getUser().getUid());
    }

    private void buildAndSaveDataValue(String UID, String value){
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(UID);
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setProvidedElsewhere(false);
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(value);
        dataValue.save();
    }

    /**
     * Several properties must be updated when a survey is about to be sent.
     * This changes will be saved just when process finish successfully.
     * @param compositeScores
     */
    private void updateSurvey(List<CompositeScore> compositeScores){
        currentSurvey.setMainScore(ScoreRegister.calculateMainScore(compositeScores));
        currentSurvey.setStatus(Constants.SURVEY_SENT);
        currentSurvey.setEventUid(currentEvent.getUid());
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
     * Annotates the survey and event that has been processed
     */
    private void annotateSurveyAndEvent() {
        surveys.add(currentSurvey);
        currentEvent.setLastUpdated(EventExtended.format(uploadedDate, EventExtended.DHIS2_DATE_FORMAT));
        events.add(currentEvent);
        Log.d(TAG, String.format("%d surveys converted so far", surveys.size()));
    }

    /**
     * Saves changes in the survey (supposedly after a successfull push)
     */
    public void saveSurveyStatus(Map<Long,ImportSummary> importSummaryMap){
        for(int i=0;i<surveys.size();i++){
            Survey iSurvey=surveys.get(i);
            Event iEvent=events.get(i);
            ImportSummary importSummary=importSummaryMap.get(iEvent.getLocalId());
            FailedItem failedItem= EventExtended.hasConflict(iEvent.getLocalId());
            if(hasImportSummaryErrors(importSummary) || failedItem!=null){
                Log.d(TAG, importSummary.toString());
                //Some error happened -> move back to completed
                if(failedItem!=null) {
                    iSurvey.setStatus(Constants.SURVEY_COMPLETED);
                    iSurvey.setEventUid(null);
                    ImportSummary importSummary1=failedItem.getImportSummary();
                    List<String> failedUids=getFailedUidQuestion(failedItem.getErrorMessage());
                    for(String uid:failedUids) {
                        Log.d(TAG, "PUSH process...Conflict in "+uid+" dataelement pushing survey: "+iSurvey.getId_survey());
                        iSurvey.saveConflict(uid);
                        iSurvey.setStatus(Constants.SURVEY_CONFLICT);
                    }
                }
                iSurvey.save();

                //Generated event must be remove too (or saved in a modify import summary)
                if(iSurvey.getStatus()!=Constants.SURVEY_CONFLICT && importSummary.getStatus().equals("SUCCESS")) {
                    saveSurveyFromImportSummary(iSurvey, iEvent);
                    Log.d(TAG, "PUSH process...Survey uploaded: " + iSurvey.getId_survey());
                }
                else{
                    Log.d(TAG, "PUSH process...Fail pushing survey: " + iSurvey.getId_survey());
                    iEvent.delete();
                }
            }else{
                saveSurveyFromImportSummary(iSurvey, iEvent);
            }
        }
    }

    private void saveSurveyFromImportSummary(Survey iSurvey, Event iEvent) {
        iSurvey.setStatus(Constants.SURVEY_SENT);
        iSurvey.setUploadedDate(uploadedDate);
        iSurvey.saveMainScore();
        iSurvey.save();

        //To avoid several pushes
        iEvent.setFromServer(true);
        iEvent.save();

        Log.d(TAG, "PUSH process...OK. Survey and Event saved");
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
            //String status=jsonObjectResponse.getString("status");

            //String httpStatusCode=jsonObjectResponse.getString("httpStatusCode");

            //String httpStatus=jsonObjectResponse.getString("httpStatus");
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
        if(message!="")
            DashboardActivity.showException(context.getString(R.string.error_message), message);
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

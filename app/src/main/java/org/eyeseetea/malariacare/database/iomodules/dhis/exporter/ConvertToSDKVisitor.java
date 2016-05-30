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

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.ControlDataElement;
import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.LocationMemory;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Utils;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem$Table;
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
    String uploadedOnCode;
    String uploadedByCode;
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

    ConvertToSDKVisitor(Context context){
        this.context=context;
        // FIXME: We should create a visitor to translate the ControlDataElement class
        overallScoreCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.overall_score_code));
        mainScoreClassCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.main_score_class_code));
        mainScoreACode = ControlDataElement.findControlDataElementUid(context.getString(R.string.main_score_a_code));
        mainScoreBCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.main_score_b_code));
        mainScoreCCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.main_score_c_code));
        forwardOrderCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.forward_order_code));
        pushDeviceCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.push_device_code));
        overallProductivityCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.overall_productivity_code));
        nextAssessmentCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.next_assessment_code));

        createdOnCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.created_on_code));
        uploadedOnCode =ControlDataElement.findControlDataElementUid(context.getString(R.string.upload_date_code));
        uploadedByCode = ControlDataElement.findControlDataElementUid(context.getString(R.string.uploaded_by_code));
        surveys = new ArrayList<>();
        events = new ArrayList<>();
    }

    @Override
    public void visit(Survey survey) throws Exception{
        //Turn survey into an event
        this.currentSurvey=survey;

        Log.d(TAG,String.format("Creating event for survey (%d) ...",survey.getId_survey()));
        this.currentEvent=buildEvent();

        //Calculates scores and update survey
        Log.d(TAG,"Registering scores...");
        List<CompositeScore> compositeScores = ScoreRegister.loadCompositeScores(survey);
        updateSurvey(compositeScores, currentSurvey.getId_survey());

        //Turn score values into dataValues
        Log.d(TAG, "Creating datavalues from scores...");
        for(CompositeScore compositeScore:compositeScores){
            compositeScore.accept(this);
        }

        //Turn question values into dataValues
        Log.d(TAG, "Creating datavalues from questions... Values"+survey.getValues().size());
        for(Value value:currentSurvey.getValues()) {
            value.accept(this);
        }

        Log.d(TAG,"Saving control dataelements");
        buildControlDataElements(survey);

        //Annotate both objects to update its state once the process is over
        annotateSurveyAndEvent();
    }

    @Override
    public void visit(CompositeScore compositeScore) {
        DataValue dataValue=new DataValue();
        dataValue.setDataElement(compositeScore.getUid());
        dataValue.setLocalEventId(currentEvent.getLocalId());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setProvidedElsewhere(false);
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(AUtils.round(ScoreRegister.getCompositeScore(compositeScore,currentSurvey.getId_survey())));
        dataValue.save();
    }

    @Override
    public void visit(Value value) {
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
    private Event buildEvent() throws Exception{
        currentEvent=new Event();

        currentEvent.setStatus(Event.STATUS_COMPLETED);
        currentEvent.setFromServer(false);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getTabGroup().getProgram().getUid());
        currentEvent.setProgramStageId(currentSurvey.getTabGroup().getUid());
        updateEventLocation();
        updateEventDates();
        Log.d(TAG, "Saving event " + currentEvent.toString());
        currentEvent.save();
        return currentEvent;
    }

    /**
     * Fulfills the dates of the event
     */
    private void updateEventDates() {

        //Sent date 'now' (this change will be saves after successful push)
        currentSurvey.setUploadedDate(new Date());

        uploadedDate =currentSurvey.getUploadedDate();

        // NOTE: do not try to set the event creation date. SDK will try to update the event in the next push instead of creating it and that will crash
        currentEvent.setEventDate(EventExtended.format(currentSurvey.getCompletionDate(), EventExtended.DHIS2_DATE_FORMAT));
        currentEvent.setDueDate(EventExtended.format(currentSurvey.getScheduledDate(),EventExtended.DHIS2_DATE_FORMAT));
        //Not used
        currentEvent.setLastUpdated(EventExtended.format(currentSurvey.getUploadedDate(),EventExtended.DHIS2_DATE_FORMAT));
        }

    /**
     * Builds several datavalues from the mainScore of the survey
     * @param survey
     */
    private void buildControlDataElements(Survey survey) {

        //It Checks if the dataelement exists, before build and save the datavalue
        //Created date
        if(!createdOnCode.equals(""))
            buildAndSaveDataValue(createdOnCode, EventExtended.format(survey.getCreationDate(), EventExtended.AMERICAN_DATE_FORMAT));

        //Updated date
        if(!uploadedOnCode.equals(""))
            buildAndSaveDataValue(uploadedOnCode, EventExtended.format(survey.getUploadedDate(), EventExtended.AMERICAN_DATE_FORMAT));

        //Updated by user
        if(!uploadedByCode.equals(""))
            buildAndSaveDataValue(uploadedByCode, Session.getUser().getUsername());


        //Overall score
        if(!overallScoreCode.equals("") && survey.hasMainScore())
            buildAndSaveDataValue(overallScoreCode, survey.getMainScore().toString());

        //MainScoreUID
        if(!mainScoreClassCode.equals("") && survey.hasMainScore())
            buildAndSaveDataValue(mainScoreClassCode, survey.getType());

        //MainScore A
        if(!mainScoreACode.equals("") && survey.hasMainScore())
            buildAndSaveDataValue(mainScoreACode, survey.isTypeA() ? "true" : "false");

        //MainScore B
        if(!mainScoreBCode.equals("") && survey.hasMainScore())
            buildAndSaveDataValue(mainScoreBCode, survey.isTypeB() ? "true" : "false");

        //MainScoreC
        if(!mainScoreCCode.equals("") && survey.hasMainScore())
            buildAndSaveDataValue(mainScoreCCode, survey.isTypeC() ? "true" : "false");

        //Forward Order
        if(!forwardOrderCode.equals(""))
            buildAndSaveDataValue(forwardOrderCode, context.getString(R.string.forward_order_value));

        //Push Device
        if(!pushDeviceCode.equals(""))
            buildAndSaveDataValue(pushDeviceCode, Session.getPhoneMetaData().getPhone_metaData() + "###" + new Utils().getCommitHash(context));

        //Overall productivity
        if(!overallProductivityCode.equals(""))
            buildAndSaveDataValue(overallProductivityCode, Integer.toString(OrgUnitProgramRelation.getProductivity(survey)));

        //Next assessment
        if(!nextAssessmentCode.equals(""))
            buildAndSaveDataValue(nextAssessmentCode, EventExtended.format(SurveyPlanner.getInstance().findScheduledDateBySurvey(survey), EventExtended.AMERICAN_DATE_FORMAT));
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
    private void updateSurvey(List<CompositeScore> compositeScores, float idSurvey){
        currentSurvey.setMainScore(ScoreRegister.calculateMainScore(compositeScores, idSurvey));
        currentSurvey.setStatus(Constants.SURVEY_SENT);
        currentSurvey.setUploadedDate(uploadedDate);
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
            FailedItem failedItem= hasConflict(iEvent.getLocalId());
            if(hasImportSummaryErrors(importSummary) || failedItem!=null){
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

                //Generated event must be remove too
                iEvent.delete();
                Log.d(TAG, "PUSH process...Fail pushing survey: " + iSurvey.getId_survey());
            }else{
                PushController.getInstance().saveCreationDateInSDK(surveys);
                iSurvey.setStatus(Constants.SURVEY_SENT);
                iSurvey.saveMainScore();
                iSurvey.save();

                //To avoid several pushes
                iEvent.setFromServer(true);
                iEvent.save();

                Log.d(TAG, "PUSH process...OK. Survey and Event saved");
            }
        }
    }

    /**
     * Checks whether the given event contains errors in SDK FailedItem table or has been successful.
     * If not return null, it is becouse this item had a conflict.
     * @param localId
     * @return
     */
    private FailedItem hasConflict(long localId){
        return  new Select()
                        .from(FailedItem.class)
                        .where(Condition.column(FailedItem$Table.ITEMID)
                                .is(localId)).querySingle();
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

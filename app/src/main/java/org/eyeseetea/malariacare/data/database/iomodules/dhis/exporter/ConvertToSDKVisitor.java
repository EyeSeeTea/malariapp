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

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.strategies
        .ConvertToSDKVisitorStrategyFactory;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ObsActionPlanDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.joda.time.DateTime;

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

    private final static String TAG = ".ConvertToSDKVisitorB&D";

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
    String completionDateCode;

    //ObsActionPlan control dataelements
    String providerCode;
    String gapsCode;
    String planActionCode;
    String action1Code;
    String action2Code;

    /**
     * List of surveys that are going to be pushed
     */
    List<SurveyDB> surveys;
    /**
     * List of surveys with obsActionPlans that are going to be pushed
     */
    List<SurveyDB> obsActionPlanSurveys;

    /**
     * Map app surveys with sdk events (N to 1)
     */
    Map<Long, EventExtended> obsActionPlanEvents;
    /**
     * Map app surveys with sdk events (N to 1)
     */
    Map<Long, EventExtended> events;

    /**
     * The last survey that it is being translated
     */
    SurveyDB currentSurvey;

    /**
     * The generated event
     */
    EventExtended currentEvent;

    /**
     * Timestamp that captures the moment when the survey is converted right before being sent
     */
    Date uploadedDate;


    ConvertToSDKVisitor(Context context) {
        this.context = context;
        Log.d(TAG, "new convertToSdkVisitor");
        // FIXME: We should create a visitor to translate the ControlDataElement class
        overallScoreCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.overall_score_code));
        mainScoreClassCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.main_score_class_code));
        mainScoreACode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.main_score_a_code));
        mainScoreBCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.main_score_b_code));
        mainScoreCCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.main_score_c_code));
        forwardOrderCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.forward_order_code));
        pushDeviceCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.push_device_code));
        overallProductivityCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.overall_productivity_code));
        nextAssessmentCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.next_assessment_code));

        createdOnCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.created_on_code));
        updatedDateCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.upload_date_code));
        completionDateCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.completed_on_code));
        updatedUserCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.uploaded_by_code));
        providerCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.providerCode));
        gapsCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.gaps_code));
        planActionCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.action_plan_code));
        action1Code = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.action1_code));
        action2Code = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.action2_code));

        surveys = new ArrayList<>();
        obsActionPlanSurveys = new ArrayList<>();
        events = new HashMap<>();
        obsActionPlanEvents = new HashMap<>();
    }

    @Override
    public void visit(ObsActionPlanDB obsActionPlan) throws ConversionException {
        SurveyDB survey = SurveyDB.findById(obsActionPlan.getId_survey_obs_action_fk());
        currentSurvey=survey;
        String errorMessage = "Exception creating a new event from obsActionPlan. Removing survey from DB";
    try{
        this.currentEvent = buildSentEvent();
        currentEvent.setCreationDate(survey.getCreationDate());
        currentEvent.setEventUid(survey.getEventUid());
        currentEvent.setEventDate(new DateTime(currentSurvey.getCreationDate()));
        currentEvent.save();
        if(obsActionPlan.getProvider()!=null) {
            if (controlDataElementExistsInServer(providerCode)) {
                addOrUpdateDataValue(providerCode, obsActionPlan.getProvider());
            }
        }
        if(obsActionPlan.getGaps()!=null) {
            if (controlDataElementExistsInServer(gapsCode)) {
                addOrUpdateDataValue(gapsCode, obsActionPlan.getGaps());
            }
        }
        if(obsActionPlan.getAction_plan()!=null) {
            if (controlDataElementExistsInServer(planActionCode)) {
                addOrUpdateDataValue(planActionCode, obsActionPlan.getAction_plan());
            }
        }
        if(obsActionPlan.getAction1()!=null) {
            if (controlDataElementExistsInServer(action1Code)) {
                addOrUpdateDataValue(action1Code, obsActionPlan.getAction1());
            }
        }
        if(obsActionPlan.getAction2()!=null) {
            if (controlDataElementExistsInServer(action2Code)) {
                addOrUpdateDataValue(action2Code, obsActionPlan.getAction2());
            }
        }
        annotateSurveyAndEvent(IPushController.Kind.PLANS);
    } catch (Exception e) {
        e.printStackTrace();
        errorMessage = showErrorConversionMessage(errorMessage);
        removeSurveyAndEvent(IPushController.Kind.PLANS);
        throw new ConversionException(errorMessage);
    }
    }

    @Override
    public void visit(SurveyDB survey) throws ConversionException {

        uploadedDate = new Date();

        //Turn survey into an event
        this.currentSurvey = survey;

        Log.d(TAG, String.format("Creating event for survey (%d) ...", survey.getId_survey()));
        Log.d(TAG, String.format("Creating event for survey (%s) ...", survey.toString()));

        String errorMessage = "Exception creating a new event from survey. Removing survey from DB";
        try {
            this.currentEvent = buildEvent();
        } catch (Exception e) {
            showErrorConversionMessage(errorMessage);
            currentSurvey.delete();//invalid survey
            return;
        }
        try {
            currentEvent.setEventUid(currentSurvey.getEventUid());
            currentSurvey.save();
            currentEvent.save();
            Log.d(TAG, "Event created" + currentEvent.getUid());
            //Calculates scores and update survey
            Log.d(TAG, "Registering scores...");
            errorMessage = "Calculating compositeScores";
            List<CompositeScoreDB> compositeScores = ScoreRegister.loadCompositeScores(survey,
                    Constants.PUSH_MODULE_KEY);
            updateSurvey(compositeScores, currentSurvey.getId_survey(), Constants.PUSH_MODULE_KEY);

            //Turn score values into dataValues
            Log.d(TAG, "Creating datavalues from scores...");

            errorMessage = "compositeScores visitors";
            for (CompositeScoreDB compositeScore : compositeScores) {
                compositeScore.accept(this);
            }

            errorMessage = "datavalue visitors ";
            //Turn question values into dataValues
            Log.d(TAG, "Creating datavalues from questions... Values" + survey.getValues().size());
            for (ValueDB value : currentSurvey.getValues()) {
                //value -> datavalue
                value.accept(this);
            }


            errorMessage = "updating dates";
            survey.setUploadDate(uploadedDate);

            //Update all the dates after checks the new values
            updateEventDates();

            Log.d(TAG, "Creating datavalues from other stuff...");
            errorMessage = "building dataElements";
            buildControlDataElements(survey);
            //Annotate both objects to update its state once the process is over

            errorMessage = "annotating surveys and events";
            annotateSurveyAndEvent(IPushController.Kind.EVENTS);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = showErrorConversionMessage(errorMessage);
            removeSurveyAndEvent(IPushController.Kind.EVENTS);
            throw new ConversionException(errorMessage);
        }
    }

    private String showErrorConversionMessage(String errorMessage) {
        String programName = "", orgUnitName = "";
        if (currentSurvey.getProgram() != null
                && currentSurvey.getProgram().getName() != null) {
            programName = currentSurvey.getProgram().getName();
        }
        if (currentSurvey.getOrgUnit() != null
                && currentSurvey.getOrgUnit().getName() != null) {
            orgUnitName = currentSurvey.getOrgUnit().getName();
        }
        if (currentSurvey.getValues() != null) {
            for (ValueDB value : currentSurvey.getValues()) {
                Log.d(TAG, "DataValues:" + value.toString());
            }
        }
        return ": " + errorMessage + " surveyId: " + currentSurvey.getId_survey()
                                + "program: " + programName + " OrgUnit: "
                                + orgUnitName + "Survey: " + currentSurvey.toString();
    }

    private void removeSurveyAndEvent(IPushController.Kind kind) {
        //remove event from annotated event list and from db
        ConvertToSDKVisitorStrategyFactory convertToSDKVisitorStrategyFactory =
                new ConvertToSDKVisitorStrategyFactory(
                        obsActionPlanEvents, surveys, currentSurvey, currentEvent);
        convertToSDKVisitorStrategyFactory.get(kind).removeSurveyAndEvent();
    }

    @Override
    public void visit(CompositeScoreDB compositeScore) {
        List<Float> result = ScoreRegister.getCompositeScoreResult(compositeScore,
                currentSurvey.getId_survey(), Constants.PUSH_MODULE_KEY);
        //Checks if the result have at least one valid denominator.
        if (result != null && result.get(1) == 0) {
            return;
        }
        DataValueExtended dataValue = new DataValueExtended();
        dataValue.setDataElement(compositeScore.getUid());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(AUtils.round(
                ScoreRegister.getCompositeScore(compositeScore, currentSurvey.getId_survey(),
                        Constants.PUSH_MODULE_KEY)));
        dataValue.save();
    }

    @Override
    public void visit(ValueDB value) {
        DataValueExtended dataValue = new DataValueExtended();
        dataValue.setDataElement(value.getQuestion().getUid());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        if (value.getOption() != null) {
            dataValue.setValue(value.getOption().getCode());
        } else {
            dataValue.setValue(value.getValue());
        }
        dataValue.save();
    }

    /**
     * Builds an event from a survey
     */
    private EventExtended buildEvent() throws Exception {
        currentEvent = new EventExtended();
        currentEvent.setStatus(EventExtended.STATUS_COMPLETED);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getProgram().getUid());
        currentEvent.setProgramStageId(currentSurvey.getProgram().getUid());
        updateEventLocation();
        Log.d(TAG, "Saving event " + currentEvent.getUid());
        return currentEvent;
    }
    /**
     * Builds an event from a survey
     */
    private EventExtended buildSentEvent() throws Exception {
        currentEvent = new EventExtended();
        currentEvent.setStatus(EventExtended.STATUS_COMPLETED);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getProgram().getUid());
        currentEvent.setProgramStageId(currentSurvey.getProgram().getUid());
        Log.d(TAG, "Saving event " + currentEvent.getUid());
        return currentEvent;
    }
    /**
     * Fulfills the dates of the event
     */
    private void updateEventDates() {

        // NOTE: do not try to set the event creation date. SDK will try to update the event in
        // the next push instead of creating it and that will crash
        currentEvent.setEventDate(new DateTime(currentSurvey.getCreationDate()));
        currentEvent.setDueDate(new DateTime(currentSurvey.getScheduledDate()));
        //Not used
        currentEvent.setLastUpdated(new DateTime(currentSurvey.getUploadDate()));
        currentEvent.save();
    }

    /**
     * Updates the location of the current event that it is being processed
     */
    private void updateEventLocation() throws Exception {
        Location lastLocation = LocationMemory.get(currentSurvey.getId_survey());
        //If location is required but there is no location -> exception
        if (PreferencesState.getInstance().isLocationRequired() && lastLocation == null) {
            throw new Exception(
                    context.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //No location + not required -> done
        if (lastLocation == null) {
            return;
        }

        //location -> set lat/lng
        currentEvent.setLatitude(lastLocation.getLatitude());
        currentEvent.setLongitude(lastLocation.getLongitude());
    }

    /**
     * Builds several datavalues from the mainScore of the survey
     */
    private void buildControlDataElements(SurveyDB survey) {
        //Overall score
        if (controlDataElementExistsInServer(overallScoreCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(overallScoreCode, survey.getMainScore().getScore().toString());
        }

        //It Checks if the dataelement exists, before build and save the datavalue
        //Created date
        if (controlDataElementExistsInServer(createdOnCode)) {
            addOrUpdateDataValue(createdOnCode, EventExtended.format(survey.getCreationDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
        }

        //It Checks if the dataelement exists, before build and save the datavalue
        //Created date
        if (controlDataElementExistsInServer(completionDateCode)) {
            addOrUpdateDataValue(completionDateCode, EventExtended.format(survey.getCompletionDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
        }

        //Updated date
        if (controlDataElementExistsInServer(updatedDateCode)) {
            addOrUpdateDataValue(updatedDateCode, EventExtended.format(survey.getUploadDate(),
                    EventExtended.DHIS2_GMT_DATE_FORMAT));
        }

        //Updated by user
        if (controlDataElementExistsInServer(updatedUserCode)) {
            addOrUpdateDataValue(updatedUserCode, Session.getUser().getUsername());
        }

        //Forward order
        if (controlDataElementExistsInServer(forwardOrderCode)) {
            addOrUpdateDataValue(forwardOrderCode, context.getString(R.string.forward_order_value));
        }

        //Push Device
        if (controlDataElementExistsInServer(pushDeviceCode)) {
            addOrUpdateDataValue(pushDeviceCode,
                    Session.getPhoneMetaData().getPhone_metaData() + "###" + AUtils.getCommitHash(
                            context));
        }

        //init scoreType
        ScoreType scoreType = new ScoreType(survey.getMainScore().getScore());

        //MainScoreUID
        if (controlDataElementExistsInServer(mainScoreClassCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreClassCode, scoreType.getType());
        }

        //MainScore A
        if (controlDataElementExistsInServer(mainScoreACode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreACode, scoreType.isTypeA() ? "true" : "false");
        }

        //MainScore B
        if (controlDataElementExistsInServer(mainScoreBCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreBCode, scoreType.isTypeB() ? "true" : "false");
        }

        //MainScoreC
        if (controlDataElementExistsInServer(mainScoreCCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(mainScoreCCode, scoreType.isTypeC() ? "true" : "false");
        }

        //Overall productivity
        if (controlDataElementExistsInServer(overallProductivityCode)) {
            addOrUpdateDataValue(overallProductivityCode,
                    Integer.toString(OrgUnitProgramRelationDB.getProductivity(survey)));
        }

        //Next assessment
        if (controlDataElementExistsInServer(nextAssessmentCode)) {
            addOrUpdateDataValue(nextAssessmentCode, EventExtended.format(
                    SurveyPlanner.getInstance().findScheduledDateBySurvey(survey),
                    EventExtended.AMERICAN_DATE_FORMAT));
        }
    }

    private boolean controlDataElementExistsInServer(String controlDataElementUID) {
        return controlDataElementUID != null && !controlDataElementUID.equals("");
    }

    private void addOrUpdateDataValue(String dataElementUID, String value) {
        DataValueExtended dataValue = DataValueExtended.findByEventAndUID(
                currentEvent.getEvent(), dataElementUID);
        //Already added, update its value
        if (dataValue != null && dataValue.getDataValue() != null) {
            dataValue.setValue(value);
            dataValue.update();
            return;
        }

        buildAndSaveDataValue(dataElementUID, value);
    }

    private void buildAndSaveDataValue(String uid, String value) {
        DataValueExtended dataValue = new DataValueExtended();
        dataValue.setDataElement(uid);
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue(value);
        dataValue.save();
    }

    /**
     * Several properties must be updated when a survey is about to be sent.
     * This changes will be saved just when process finish successfully.
     */
    private void updateSurvey(List<CompositeScoreDB> compositeScores, float idSurvey, String module) {
        currentSurvey.setMainScore((long) idSurvey,
                ScoreRegister.getCompositeScoreRoot(compositeScores).getUid(),
                ScoreRegister.calculateMainScore(compositeScores, idSurvey, module));
        currentSurvey.setStatus(Constants.SURVEY_SENT);
        currentSurvey.setEventUid(currentEvent.getUid());
    }

    /**
     * Annotates the survey and event that has been processed
     * @param kind
     */
    private void annotateSurveyAndEvent(IPushController.Kind kind) {
        ConvertToSDKVisitorStrategyFactory convertToSDKVisitorStrategyFactory =
                new ConvertToSDKVisitorStrategyFactory(
                        obsActionPlanEvents, surveys, currentSurvey, currentEvent);
        convertToSDKVisitorStrategyFactory.get(kind).annotateSurveyAndEvent(uploadedDate);
    }

    /**
     * Saves changes in the survey (supposedly after a successfull push)
     */
    public void saveSurveyStatus(Map<String, PushReport> pushReportMap, final
    IPushController.IPushControllerCallback callback, IPushController.Kind kind) {
        ConvertToSDKVisitorStrategyFactory convertToSDKVisitorStrategyFactory =
                new ConvertToSDKVisitorStrategyFactory(
                        obsActionPlanEvents, surveys, currentSurvey, currentEvent);
        convertToSDKVisitorStrategyFactory.get(kind).saveSurveyStatus(pushReportMap, callback,
                context, uploadedDate);
    }

    /**
     * Returns the name of the username avoiding NPE
     */
    private String getSafeUsername() {
        UserDB user = Session.getUser();
        if (user != null) {
            return user.getUsername();
        }
        return "";
    }

    public void setSurveysAsQuarantine(IPushController.Kind kind) {
        ConvertToSDKVisitorStrategyFactory convertToSDKVisitorStrategyFactory =
                new ConvertToSDKVisitorStrategyFactory(
                        obsActionPlanEvents, surveys, currentSurvey, currentEvent);
        convertToSDKVisitorStrategyFactory.get(kind).setSurveysAsQuarantine();
    }
}

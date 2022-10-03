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

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ServerDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.metadata.PhoneMetaData;
import org.eyeseetea.malariacare.data.sync.IData;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.NextScheduleDateConfiguration;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushConflict;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.push.NullEventDateException;
import org.eyeseetea.malariacare.domain.exception.push.PushValueException;
import org.eyeseetea.malariacare.domain.service.SurveyNextScheduleDomainService;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.DateParser;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow_Table;
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

    String overallCompetencyCode;
    String overallCompetencyCompetentCode;
    String overallCompetencyNotCompetentCode;
    String overallCompetencyCompetentImprvCode;

    /**
     * List of surveys that are going to be pushed
     */
    List<SurveyDB> surveys;
    /**
     * List of surveys with obsActionPlans that are going to be pushed
     */
    List<SurveyDB> observationSurveys;

    /**
     * Map app surveys with sdk events (N to 1)
     */
    Map<Long, EventExtended> observationEvents;
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


        overallCompetencyCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.overall_competency_code));
        overallCompetencyCompetentCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.overall_competency_competent_code));
        overallCompetencyNotCompetentCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.overall_competency_not_competent_code));
        overallCompetencyCompetentImprvCode = ServerMetadataDB.findControlDataElementUid(
                context.getString(R.string.overall_competency_competent_improvement_code));

        surveys = new ArrayList<>();
        observationSurveys = new ArrayList<>();
        events = new HashMap<>();
        observationEvents = new HashMap<>();
    }

    @Override
    public void visit(ObservationDB observationDB) throws ConversionException {
        SurveyDB survey = SurveyDB.findById(observationDB.getId_survey_observation_fk());
        currentSurvey = survey;
        uploadedDate = currentSurvey.getUploadDate();
        String errorMessage =
                "Exception creating a new event from obsActionPlan. Removing survey from DB";

        try {
            this.currentEvent = buildSentEvent(survey, errorMessage);

            for (ObservationValueDB observationValueDB : observationDB.getValuesDB()) {
                //ObservationValueDB -> datavalue
                observationValueDB.accept(this);
            }

            Log.d(TAG, String.format("%d plan surveys converted so far", surveys.size()));
            annotateSurveyAndEvent(IPushController.Kind.OBSERVATIONS);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = showErrorConversionMessage(errorMessage);
            throw new ConversionException(observationDB, errorMessage);
        }

    }

    @Override
    public void visit(ObservationValueDB observationValueDB) {
        DataValueExtended dataValue = new DataValueExtended();
        dataValue.setDataElement(observationValueDB.getUid_observation_value());
        dataValue.setEvent(currentEvent.getEvent());
        dataValue.setStoredBy(getSafeUsername());
        dataValue.setValue (observationValueDB.getValue());
        dataValue.save();
    }

    @Override
    public void visit(SurveyDB survey) throws ConversionException {
        String errorMessage = "Exception creating a new event from survey. Removing survey from DB";
        try {
            this.currentEvent = buildEventFromSurvey(survey, errorMessage);
            if(currentEvent==null){
                Log.d(TAG,"invalid survey");
                return;
            }
            //Annotate both objects to update its state once the process is over
            errorMessage = "annotating surveys and events";

            annotateSurveyAndEvent(PushDataController.Kind.EVENTS);
            Log.d(TAG, String.format("%d surveys converted so far", surveys.size()));
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = showErrorConversionMessage(errorMessage);
            throw new ConversionException(survey, errorMessage);
        }
    }

    private EventExtended buildEventFromSurvey(SurveyDB survey, String errorMessage) throws Exception{
        currentSurvey = survey;
        uploadedDate = new Date();

        Log.d(TAG, String.format("Creating event for survey (%d) ...", currentSurvey.getId_survey()));
        Log.d(TAG, String.format("Creating event for survey (%s) ...", currentSurvey.toString()));
        currentEvent = new EventExtended(survey.getEventUid());
        currentEvent = buildEvent();

        currentSurvey.setEventUid(currentEvent.getUid());
        currentSurvey.save();
        currentEvent.save();
        Log.d(TAG, "Event created" + currentEvent.getUid());

        //Calculates scores and update survey
        Log.d(TAG, "Registering scores...");
        errorMessage = "Calculating compositeScores";
        List<CompositeScoreDB> compositeScores = ScoreRegister.loadCompositeScores(
                currentSurvey,
                Constants.PUSH_MODULE_KEY);
        updateSurvey(currentSurvey, compositeScores, currentSurvey.getId_survey(),
                Constants.PUSH_MODULE_KEY);

        //Turn score values into dataValues
        Log.d(TAG, "Creating datavalues from scores...");

        errorMessage = "compositeScores visitors";
        for (CompositeScoreDB compositeScore : compositeScores) {
            compositeScore.accept(this);
        }

        errorMessage = "datavalue visitors ";
        //Turn question values into dataValues
        Log.d(TAG, "Creating datavalues from questions... Values"
                + currentSurvey.getValues().size());
        for (ValueDB value : currentSurvey.getValues()) {
            //value -> datavalue
            value.accept(this);
        }

        errorMessage = "updating dates";
        currentSurvey.setUploadDate(uploadedDate);

        //Update all the dates after checks the new values
        updateEventDates(currentEvent, currentSurvey);

        Log.d(TAG, "Creating datavalues from other stuff...");
        errorMessage = "building dataElements";
        buildControlDataElements(currentSurvey);

        return currentEvent;
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
                        Constants.PUSH_MODULE_KEY),2));
        dataValue.save();
    }

    @Override
    public void visit(ValueDB value) {
        DataValueExtended dataValue = new DataValueExtended();
        if (value.getQuestion() == null) {
            //ignore ServerMetadata values
            return;
        }
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
        currentEvent.setStatus(EventExtended.STATUS_COMPLETED);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getProgram().getUid());

        currentEvent.setProgramStageId(
                ProgramStageExtended.getProgramStageUid(currentSurvey.getProgram().getUid()));

        updateEventLocation();
        Log.d(TAG, "Saving event " + currentEvent.getUid());
        return currentEvent;
    }

    /**
     * Builds an event from a survey
     */
    private EventExtended buildSentEvent(SurveyDB survey, String errorMessage) throws Exception {
        currentEvent = buildEventFromSurvey(survey, errorMessage);
        currentEvent.setStatus(EventExtended.STATUS_COMPLETED);
        currentEvent.setOrganisationUnitId(currentSurvey.getOrgUnit().getUid());
        currentEvent.setProgramId(currentSurvey.getProgram().getUid());
        currentEvent.setProgramStageId(
                ProgramStageExtended.getProgramStageUid(currentSurvey.getProgram().getUid()));
        Log.d(TAG, "Saving event " + currentEvent.getUid());
        return currentEvent;
    }

    /**
     * Fulfills the dates of the event
     */
    private void updateEventDates(EventExtended eventExtended, SurveyDB surveyDB) {

        // NOTE: do not try to set the event creation date. SDK will try to update the event in
        // the next push instead of creating it and that will crash
        eventExtended.setEventDate(new DateTime(surveyDB.getCreationDate()));
        eventExtended.setDueDate(new DateTime(surveyDB.getScheduledDate()));
        //Not used
        eventExtended.setLastUpdated(new DateTime(surveyDB.getUploadDate()));
        eventExtended.save();
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
            addOrUpdateDataValue(overallScoreCode, survey.getMainScoreValue().toString());
        }

        DateParser dateParser = new DateParser();
        //It Checks if the dataelement exists, before build and save the datavalue
        //Created date
        if (controlDataElementExistsInServer(createdOnCode)) {
            addOrUpdateDataValue(createdOnCode, dateParser.format(survey.getCreationDate(),
                    DateParser.LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE));
        }

        //It Checks if the dataelement exists, before build and save the datavalue
        //Created date
        if (controlDataElementExistsInServer(completionDateCode)) {
            addOrUpdateDataValue(completionDateCode, dateParser.format(survey.getCompletionDate(),
                    DateParser.LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE));
        }

        //Updated date
        if (controlDataElementExistsInServer(updatedDateCode)) {
            addOrUpdateDataValue(updatedDateCode, dateParser.format(survey.getUploadDate(),
                    DateParser.LONG_DATE_FORMAT_WITH_SPECIFIC_UTC_TIME_ZONE));
        }

        //Updated by user
        if (controlDataElementExistsInServer(updatedUserCode)) {
            addOrUpdateDataValue(updatedUserCode, Session.getUser().getName());
        }

        //Forward order
        if (controlDataElementExistsInServer(forwardOrderCode)) {
            addOrUpdateDataValue(forwardOrderCode, context.getString(R.string.forward_order_value));
        }

        //Push Device
        //Only It's possible until API 28
        //https://source.android.com/devices/tech/config/device-identifiers
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                controlDataElementExistsInServer(pushDeviceCode)) {
            addOrUpdateDataValue(pushDeviceCode,
                    Session.getPhoneMetaData().getPhone_metaData() + "###" + AUtils.getCommitHash(
                            context));
        }

        //init scoreType
        ScoreType scoreType = new ScoreType(survey.getMainScoreValue());

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
            addOrUpdateDataValue(nextAssessmentCode, dateParser.format(
                    findScheduledDateBySurvey(survey),
                    DateParser.AMERICAN_DATE_FORMAT));
        }

        CompetencyScoreClassification competency = CompetencyScoreClassification.get(
                survey.getCompetencyScoreClassification());

        //Competency
        if (controlDataElementExistsInServer(overallCompetencyCode) && survey.hasMainScore()) {
            addOrUpdateDataValue(overallCompetencyCode, competency.getCode());
        }

        //Competency competent
        if (controlDataElementExistsInServer(overallCompetencyCompetentCode) &&
                competency != CompetencyScoreClassification.NOT_AVAILABLE) {
            addOrUpdateDataValue(overallCompetencyCompetentCode,
                    competency == CompetencyScoreClassification.COMPETENT ? "true" : "false");
        }

        //Competency not competent
        if (controlDataElementExistsInServer(overallCompetencyNotCompetentCode) &&
                competency != CompetencyScoreClassification.NOT_AVAILABLE) {
            addOrUpdateDataValue(overallCompetencyNotCompetentCode,
                    competency == CompetencyScoreClassification.NOT_COMPETENT ? "true" : "false");
        }

        //Competency competent improvement
        if (controlDataElementExistsInServer(overallCompetencyCompetentImprvCode) &&
                competency != CompetencyScoreClassification.NOT_AVAILABLE) {
            addOrUpdateDataValue(overallCompetencyCompetentImprvCode,
                    competency == CompetencyScoreClassification.COMPETENT_NEEDS_IMPROVEMENT ? "true"
                            : "false");
        }
    }

    private Date findScheduledDateBySurvey(SurveyDB survey) {
        Date eventDate = survey.getCompletionDate();

        CompetencyScoreClassification competencyScoreClassification =
                CompetencyScoreClassification.get(survey.getCompetencyScoreClassification());

        NextScheduleDateConfiguration nextScheduleDateConfiguration =
                new NextScheduleDateConfiguration(survey.getProgram().getNextScheduleDeltaMatrix());

        SurveyNextScheduleDomainService surveyNextScheduleDomainService = new
                SurveyNextScheduleDomainService();

        ServerDB serverDB = ServerDB.getConnectedServerFromDB();

        Date nextScheduleDate = surveyNextScheduleDomainService.calculate(
                nextScheduleDateConfiguration,
                eventDate,
                competencyScoreClassification,
                survey.isLowProductivity(),
                survey.getMainScoreValue(),
                ServerClassification.Companion.get(serverDB.getClassification()));

        return nextScheduleDate;
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
    private void updateSurvey(SurveyDB currentSurvey, List<CompositeScoreDB> compositeScores,
            float idSurvey, String module) {
        currentSurvey.setMainScore(currentSurvey.getId_survey(),
                ScoreRegister.getCompositeScoreRoot(compositeScores).getUid(),
                ScoreRegister.calculateMainScore(compositeScores, idSurvey, module));
        currentSurvey.saveMainScore();

        currentSurvey.setStatus(Constants.SURVEY_SENT);
    }

    /**
     * Annotates the survey and event that has been processed
     */
    private void annotateSurveyAndEvent(PushDataController.Kind kind) {
        Map<Long, EventExtended> events = getEventsByKind(kind);
        List<SurveyDB> surveys = getSurveysByKind(kind);

        surveys.add(currentSurvey);

        currentEvent.setLastUpdated(new DateTime(uploadedDate.getTime()));

        events.put(currentSurvey.getId_survey(), currentEvent);
    }

    @SuppressLint("StringFormatInvalid")
    public void saveSurveyStatus(Map<String, PushReport> pushReportMap, final
    IPushController.IPushControllerCallback callback, PushDataController.Kind kind) {

        Map<Long, EventExtended> events = getEventsByKind(kind);
        List<SurveyDB> surveys = getSurveysByKind(kind);

        Log.d(TAG, String.format("pushReportMap %d surveys savedSurveyStatus", surveys.size()));
        for (int i = 0; i < surveys.size(); i++) {
            IData data = getDataByKind(surveys.get(i), kind);

            //Sets the survey status as quarantine to prevent wrong reports on unexpected exception.
            //F.E. if the app crash unexpected this survey will be checked again in the future push to prevent the duplicates
            // in the server.

            data.changeStatusToQuarantine();


            EventExtended event = new EventExtended(events.get(data.getSurveyId()));
            PushReport pushReport= pushReportMap.get(event.getEvent().getUId());

            if (pushReport == null) {
                //the survey was saved as quarantine.
                Log.d(TAG, "Error saving survey: report is null in this survey: "
                        + data.getSurveyId());
                //The loop should continue without throw the Exception.
                continue;
            }

            List<PushConflict> pushConflicts = pushReport.getPushConflicts();

            //If the pushResult has some conflict the survey was saved in the server but
            // never resend, the survey is saved as survey in conflict.
            if (pushConflicts != null && pushConflicts.size() > 0) {
                Log.d(TAG, "saveSurveyStatus: conflicts");
                data.changeStatusToConflict();

                for (PushConflict pushConflict : pushConflicts) {
                    Log.d(TAG, "saveSurveyStatus: Faileditem not null " + data.getSurveyId());

                    if (pushConflict.getUid() != null) {
                        Log.d(TAG, "saveSurveyStatus: PUSH process...PushConflict in "
                                + pushConflict.getUid() +
                                " with error " + pushConflict.getValue()
                                + " dataelement pushing survey: "
                                +  data.getSurveyId());

                        data.saveConflict(pushConflict.getUid());

                        callback.onInformativeError(new PushValueException(
                                String.format(context.getString(R.string.error_conflict_message)+"",
                                        event.getEvent().getUId(), pushConflict.getUid(),
                                        pushConflict.getValue()) + ""));
                    }
                }
                continue;
            }

            //No errors -> Save and next
            if (pushReport!=null && !pushReport.hasPushErrors(data instanceof ObservationDB)) {
                Log.d(TAG, "saveSurveyStatus: report without errors and status ok "
                        + data.getSurveyId());

                if (event.getEventDate() == null || event.getEventDate().equals("")) {
                    //If eventdate is null the event is invalid. The event is sent but we need inform to the user.
                    callback.onInformativeError(new NullEventDateException(
                            String.format(context.getString(R.string.error_message_push)+"",
                                    event.getEvent())));
                }
                data.changeStatusToSent();
            }
        }
    }


    /**
     * Returns the name of the username avoiding NPE
     */
    private String getSafeUsername() {
        UserDB user = Session.getUser();
        if (user != null) {
            return user.getName();
        }
        return "";
    }

    public void setSurveysAsQuarantine(PushDataController.Kind kind) {
        List<SurveyDB> surveys = getSurveysByKind(kind);

        for (SurveyDB survey : surveys) {
            IData data = getDataByKind(survey, kind);

            Log.d(TAG, "Set plan status as QUARANTINE" + data.toString());

            data.changeStatusToQuarantine();
        }
    }

    private IData getDataByKind(SurveyDB survey, IPushController.Kind kind) {
        if (kind.equals(IPushController.Kind.EVENTS))
            return survey;
        else
            return ObservationDB.getBySurveyId(survey.getId_survey());
    }

    private List<SurveyDB> getSurveysByKind(IPushController.Kind kind) {
        if (kind.equals(IPushController.Kind.EVENTS)) {
            return this.surveys;
        } else {
            return this.observationSurveys;
        }
    }

    private Map<Long, EventExtended> getEventsByKind(IPushController.Kind kind) {
        if (kind.equals(IPushController.Kind.EVENTS)) {
            return this.events;
        } else {
            return this.observationEvents;
        }
    }
}

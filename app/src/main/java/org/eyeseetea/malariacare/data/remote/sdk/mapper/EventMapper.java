package org.eyeseetea.malariacare.data.remote.sdk.mapper;

import static org.eyeseetea.malariacare.utils.DateParser.AMERICAN_DATE_FORMAT;
import static org.eyeseetea.malariacare.utils.DateParser.DHIS2_GMT_DATE_FORMAT;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.entity.IData;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.QuestionValue;
import org.eyeseetea.malariacare.domain.entity.ScoreType;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.CalculateNextScheduledDateException;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.DateParser;
import org.hisp.dhis.client.sdk.models.common.Coordinates;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EventMapper {

    private final ServerMetadata mServerMetadata;
    private String TAG = "EventMapper";

    protected final Context mContext;
    protected final String mUsername;

    private Map<String, Option> optionsMap;

    public EventMapper(Context context, String username,
            List<Option> options,
            ServerMetadata serverMetadata) {
        mContext = context;
        mUsername = username;
        mServerMetadata = serverMetadata;

        createMaps(options);
    }

    protected Event mapFromSurvey(Survey survey) throws ConversionException {
        String errorMessage = "Exception creating a new event from survey.";

        Log.d(TAG, String.format("Creating event for survey (%s) ...", survey.getSurveyUid()));


        try {
            Log.d(TAG, "build event " + survey.getSurveyUid());
            Event event = buildEvent(survey.getSurveyUid(), survey.getOrgUnitUId(),
                    survey.getProgramUId());

            event.setEventDate(new DateTime(survey.getCreationDate()));
            event.setDueDate(new DateTime(survey.getScheduledDate()));
            event.setLastUpdated(new DateTime(survey.getUploadDate()));

            event.setDataValues(new ArrayList<TrackedEntityDataValue>());

            //Calculates scores and update survey
            Log.d(TAG, "Registering scores...");
            errorMessage = "Calculating compositeScores";

            addCompositeScoresToSurvey(survey, event);


            errorMessage = "Calculating datavalues";

            //Turn question values into dataValues

            Log.d(TAG, "Creating datavalues from questions... Values" + survey.getValues().size());
            for (QuestionValue questionValue : survey.getValues()) {
                String value = "";
                Option option = null;

                if (questionValue.getOptionUId() != null &&
                        optionsMap.containsKey(questionValue.getOptionUId()) ) {
                    option = optionsMap.get(questionValue.getOptionUId());
                }

                if (option != null) {
                    value = option.getCode();
                } else {
                    value= questionValue.getValue();
                }

                TrackedEntityDataValue dataValue =
                        createDataValue(event, questionValue.getQuestionUId(), value);

                event.getDataValues().add(dataValue);
            }

            Log.d(TAG, "Creating datavalues from other stuff...");
            errorMessage = "building dataElements";
            buildControlDataElements(survey, event);

            return event;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = createErrorConversionMessage(errorMessage, survey,
                    survey.getOrgUnitUId(), survey.getProgramUId());
            throw new ConversionException(survey, errorMessage);
        }
    }

    private void addCompositeScoresToSurvey(Survey survey, Event event) {
        //TODO: jsanchez to refactor, here on future use only domain entities
        //composite scores should be calculated children inside survey domain entity as with values

        SurveyDB surveyDB = SurveyDB.getSurveyByUId(survey.getSurveyUid());
        List<CompositeScoreDB> compositeScores = ScoreRegister.loadCompositeScores(surveyDB,
                Constants.PUSH_MODULE_KEY);

        //Turn score values into dataValues
        Log.d(TAG, "Creating datavalues from scores...");

        for (CompositeScoreDB compositeScore : compositeScores) {
            List<Float> result = ScoreRegister.getCompositeScoreResult(compositeScore,
                    surveyDB.getId_survey(), Constants.PUSH_MODULE_KEY);
            //Checks if the result have at least one valid denominator.
            if (result != null && result.get(1) > 0) {
                TrackedEntityDataValue dataValue = new TrackedEntityDataValue();
                dataValue.setDataElement(compositeScore.getUid());
                dataValue.setEvent(event);
                dataValue.setStoredBy(mUsername);
                dataValue.setValue (AUtils.round(
                        ScoreRegister.getCompositeScore(compositeScore, surveyDB.getId_survey(),
                                Constants.PUSH_MODULE_KEY)));
                event.getDataValues().add(dataValue);
            }
        }
    }

    private void buildControlDataElements(Survey survey,
            Event event) throws CalculateNextScheduledDateException {

        if (mServerMetadata.getOverallScore() != null && survey.getScore() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getOverallScore().getUId(),
                    String.valueOf(survey.getScore().getScore()));
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getCreationDate() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getCreationDate().getUId(),
                    DateParser.format(survey.getCreationDate(),DHIS2_GMT_DATE_FORMAT));
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getCompletionDate() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getCompletionDate().getUId(),
                    DateParser.format(survey.getCompletionDate(),DHIS2_GMT_DATE_FORMAT));
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getUploadDate() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getUploadDate().getUId(),
                    DateParser.format(survey.getUploadDate(),DHIS2_GMT_DATE_FORMAT));
            event.getDataValues().add(dataValue);
        }


        if (mServerMetadata.getUploadBy() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getUploadBy().getUId(), mUsername);
            event.getDataValues().add(dataValue);
        }


        if (mServerMetadata.getForwardOrder() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getForwardOrder().getUId(),
                    mContext.getString(R.string.forward_order_value));
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getPushDevice() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getPushDevice().getUId(),
                    Session.getPhoneMetaData().getPhone_metaData() +
                            "###" + AUtils.getCommitHash(mContext));
            event.getDataValues().add(dataValue);
        }


        //init scoreType
        ScoreType scoreType = new ScoreType(survey.getScore().getScore());

        if (mServerMetadata.getMainScoreClass() != null && survey.getScore() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getMainScoreClass().getUId(), scoreType.getType());
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getMainScoreA() != null && survey.getScore() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getMainScoreA().getUId(),
                    scoreType.isTypeA() ? "true" : "false");
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getMainScoreB() != null && survey.getScore() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getMainScoreB().getUId(),
                    scoreType.isTypeB() ? "true" : "false");
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getMainScoreC() != null && survey.getScore() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getMainScoreC().getUId(),
                    scoreType.isTypeC() ? "true" : "false");
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getOverallProductivity() != null ){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getOverallProductivity().getUId(),
                    String.valueOf(survey.getProductivity()));
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getNextAssessment() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getNextAssessment().getUId(),
                    DateParser.format(survey.calculateNextScheduledDate(), AMERICAN_DATE_FORMAT));
            event.getDataValues().add(dataValue);
        }
    }


    private void createMaps(List<Option> options) {
        optionsMap = new HashMap<>();
        for (Option option : options) {
            optionsMap.put(option.getUId(), option);
        }
    }

    protected TrackedEntityDataValue createDataValue(Event event, String dataElement, String value) {
        TrackedEntityDataValue dataValue = new TrackedEntityDataValue();
        dataValue.setDataElement(dataElement);
        dataValue.setEvent(event);
        dataValue.setStoredBy(mUsername);
        dataValue.setValue(value);
        return dataValue;
    }

    private Event buildEvent(String eventUId, String orgUnitUid, String programUid) throws Exception {
        Event event = new Event();
        event.setUId(eventUId);
        event.setStatus(Event.EventStatus.COMPLETED);

        event.setOrgUnit(orgUnitUid);
        event.setProgram(programUid);

        updateEventLocation(event);

        return event;
    }

    protected String createErrorConversionMessage(String errorMessageBase, IData data,
            String orgUnitUid, String programUid) {

        return ": " + errorMessageBase + " surveyUid: " + data.getSurveyUid()
                + "program: " + programUid + " OrgUnit: "
                + orgUnitUid + "data: " + data.toString();
    }

    private void updateEventLocation(Event event) throws Exception {
        Location lastLocation = LocationMemory.get(event.getUId());
        //If location is required but there is no location -> exception
        if (PreferencesState.getInstance().isLocationRequired() && lastLocation == null) {
            throw new Exception(
                    mContext.getString(R.string.dialog_error_push_no_location_and_required));
        }

        //No location + not required -> done
        if (lastLocation == null) {
            return;
        }

        //location -> set lat/lng
        Coordinates coordinates =
                new Coordinates(lastLocation.getLatitude(),lastLocation.getLongitude());

        event.setCoordinate(coordinates);
    }
}

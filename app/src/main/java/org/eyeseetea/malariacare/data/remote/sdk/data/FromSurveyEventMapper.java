package org.eyeseetea.malariacare.data.remote.sdk.data;

import static org.eyeseetea.malariacare.utils.DateParser.DHIS2_GMT_DATE_FORMAT;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.LocationMemory;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
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

public class FromSurveyEventMapper extends EventMapper {
    private String TAG = "FromSurveyEventMapper";

    private Map<String, Option> optionsMap;
    private ServerMetadata mServerMetadata;

    public FromSurveyEventMapper(Context context, String username,
            List<Option> options, ServerMetadata serverMetadata) {
        super(context,username);

        mServerMetadata = serverMetadata;

        createMaps(options);
    }

    public List<Event> map(List<Survey> surveys) throws ConversionException {
        List<Event> events = new ArrayList<>();

        for (Survey survey : surveys) {
            try {
                Event event = mapFromSurvey(survey);

                events.add(event);
            } catch (Exception e) {
                System.out.println("An error occurred converting Survey " +
                        survey.getSurveyUid() + " to Event:" + e.getMessage());
                throw e;
            }
        }

        return events;
    }

    public Event mapFromSurvey(Survey survey) throws ConversionException {
        String errorMessage = "Exception creating a new event from survey.";

        Log.d(TAG, String.format("Creating event for survey (%s) ...", survey.getSurveyUid()));


        try {
            Log.d(TAG, "build event " + survey.getSurveyUid());
            Event event = buildEvent(survey.getOrgUnitUId(), survey.getProgramUId(), false);

            event.setUId(survey.getSurveyUid());

            //Update all the dates after checks the new values
            // NOTE: do not try to set the event creation date. SDK will try to update the event in
            // the next push instead of creating it and that will crash
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
        //TODO: to refactor, here on future use only domain entities
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
                    //Integer.toString(OrgUnitProgramRelationDB.getProductivity(survey));
            event.getDataValues().add(dataValue);
        }

        if (mServerMetadata.getNextAssessment() != null){
            TrackedEntityDataValue dataValue = createDataValue(event,
                    mServerMetadata.getNextAssessment().getUId(),
                    DateParser.format(survey.calculateNextScheduledDate(),
                            DHIS2_GMT_DATE_FORMAT));
            event.getDataValues().add(dataValue);
        }
    }

    @NonNull
    private TrackedEntityDataValue createDataValue(Event event, String dataElement, String value) {
        TrackedEntityDataValue dataValue = new TrackedEntityDataValue();
        dataValue.setDataElement(dataElement);
        dataValue.setEvent(event);
        dataValue.setStoredBy(mUsername);
        dataValue.setValue(value);
        return dataValue;
    }


    private void createMaps(List<Option> options) {
        optionsMap = new HashMap<>();
        for (Option option : options) {
            optionsMap.put(option.getUId(), option);
        }
    }
}

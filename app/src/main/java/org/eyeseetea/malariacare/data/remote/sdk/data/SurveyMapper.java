package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.util.Log;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.domain.entity.QuestionValue;
import org.eyeseetea.malariacare.domain.entity.Score;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityDataValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SurveyMapper {
    private final static String TAG = ".SurveyConversion";

    private ServerMetadata serverMetadata;
    private Map<String, CompositeScoreDB> compositeScoreDBMap;
    private Map<String, QuestionDB> questionsDBMap;
    private Map<String, OptionDB> optionsMap;

    //TODO compositeScoreMap, questionsMap, optionsMap on the future should be domain entities maps
    public SurveyMapper(
            ServerMetadata serverMetadata,
            Map<String, CompositeScoreDB> compositeScoreMap,
            Map<String, QuestionDB> questionsMap,
            Map<String, OptionDB> optionsMap) {
        this.serverMetadata = serverMetadata;
        this.compositeScoreDBMap = compositeScoreMap;
        this.questionsDBMap = questionsMap;
        this.optionsMap = optionsMap;
    }

    public List<Survey> mapSurveys(List<Event> events) {

        List<Survey> surveys = new ArrayList<>();

        for (Event event : events) {
            Survey survey = map(event);
            surveys.add(survey);
        }

        return surveys;
    }

    private Survey map(Event event) {

        String eventUid = event.getUId();
        String orgUnitUid = event.getOrgUnit();
        String programUid = event.getProgram();
        String userUid = "";
        Score score = null;
        //Set dates by default ( to prevent a null value, all take the getEventDate date
        Date creationDate = event.getEventDate().toDate();
        Date completionDate = event.getEventDate().toDate();
        Date uploadDate = event.getEventDate().toDate();
        Date nextAssessmentDate = event.getEventDate().toDate();
        ;
        Date scheduledDate = event.getDueDate().toDate();

        List<QuestionValue> values = new ArrayList<>();

        for (TrackedEntityDataValue dataValue : event.getDataValues()) {

            if (dataValue.getDataElement().equals(serverMetadata.getCreationDateUid())) {
                creationDate = EventExtended.parseLongDate(dataValue.getValue());
            } else if (dataValue.getDataElement().equals(serverMetadata.getCompletionDateUid())) {
                completionDate = EventExtended.parseLongDate(dataValue.getValue());
            } else if (dataValue.getDataElement().equals(serverMetadata.getUploadDateUid())) {
                uploadDate = EventExtended.parseLongDate(dataValue.getValue());
            } else if (dataValue.getDataElement().equals(serverMetadata.getNextAssessmentUid())) {
                if (dataValue.getValue() != null && !dataValue.getValue().isEmpty()) {
                    nextAssessmentDate =
                            convertStringToDate("yyyy-MM-dd", dataValue.getValue());
                }

                if (nextAssessmentDate != null) {
                    scheduledDate = nextAssessmentDate;
                }

            } else if (dataValue.getDataElement().equals(serverMetadata.getUploadByUid())) {
                userUid = dataValue.getValue();
            } else if (compositeScoreDBMap.containsKey(dataValue.getDataElement())) {
                CompositeScoreDB compositeScore = compositeScoreDBMap.get(
                        dataValue.getDataElement());

                if (CompositeScoreBuilder.isRootScore(compositeScore)) {
                    score = new Score(dataValue.getDataElement(),
                            Float.parseFloat(dataValue.getValue()));
                }
            } else {
                QuestionValue questionValue = mapValue(dataValue);

                if (questionValue != null) {
                    values.add(questionValue);
                } else {
                    Log.d(TAG, String.format(
                            "An error occurred converting data value (data: Event: %s, DataElement: %s, value: %s)",
                            eventUid, dataValue.getDataElement(), dataValue.getValue()));
                }
            }
        }

        Survey survey = Survey.createSentSurvey(eventUid, programUid, orgUnitUid,
                userUid, creationDate, uploadDate, scheduledDate, completionDate, values, score);

        return survey;
    }

    private QuestionValue mapValue(TrackedEntityDataValue dataValue) {

        QuestionValue questionValue = null;
        String questionUid = dataValue.getDataElement();

        if (questionsDBMap.containsKey(dataValue.getDataElement())) {
            QuestionDB question = questionsDBMap.get(dataValue.getDataElement());

            String optionUid = null;
            String optionName = "";

            //TODO find optionUid

            if (optionUid == null) {
                questionValue = QuestionValue.createSimpleValue(questionUid, dataValue.getValue());
            } else {
                questionValue = QuestionValue.createOptionValue(questionUid, dataValue.getValue(),optionName );
            }
        }

        return questionValue;
    }

    private Date convertStringToDate(String format, String dateText) {
        DateFormat df = new SimpleDateFormat(format);
        try {
            return df.parse(dateText);
        } catch (ParseException e) {
            Log.e(TAG, "Error converting date in pull: " + e.getMessage() + e);
            e.printStackTrace();
        }
        return null;
    }

}

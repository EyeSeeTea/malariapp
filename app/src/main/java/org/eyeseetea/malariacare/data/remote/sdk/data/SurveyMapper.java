package org.eyeseetea.malariacare.data.remote.sdk.data;

import android.util.Log;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.domain.entity.Option;
import org.eyeseetea.malariacare.domain.entity.Question;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyMapper {
    private final static String TAG = ".SurveyMapper";

    private ServerMetadata serverMetadata;
    private Map<String, CompositeScoreDB> compositeScoreMap;
    private Map<String, Question> questionsMap;
    private Map<String, List<Option>> optionsMap;

    //TODO: compositeScoreMap on the future should be a domain entity list
    public SurveyMapper(
            ServerMetadata serverMetadata,
            List<CompositeScoreDB> compositeScores,
            List<Question> questions,
            List<Option> options) {
        this.serverMetadata = serverMetadata;

        createMaps(compositeScores, questions, options);
    }

    public List<Survey> mapSurveys(List<Event> events) {

        List<Survey> surveys = new ArrayList<>();

        for (Event event : events) {
            try {
                Survey survey = map(event);

                surveys.add(survey);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred converting Event " + event.getUId() +
                        " to survey:" + e.getMessage());
            }
        }

        return surveys;
    }

    private Survey map(Event event) {

        String eventUid = event.getUId();
        String orgUnitUid = event.getOrgUnit();
        String programUid = event.getProgram();
        String userUid = null;
        Score score = null;
        //Set dates by default ( to prevent a null value, all take the getEventDate date
        Date creationDate = event.getEventDate().toDate();
        Date completionDate = event.getEventDate().toDate();
        Date uploadDate = event.getEventDate().toDate();
        Date nextAssessmentDate = event.getEventDate().toDate();

        Date scheduledDate = event.getDueDate().toDate();

        List<QuestionValue> values = new ArrayList<>();

        for (TrackedEntityDataValue dataValue : event.getDataValues()) {

            if (dataValue.getDataElement().equals(serverMetadata.getCreationDate().getUId())) {
                creationDate = EventExtended.parseLongDate(dataValue.getValue());
            } else if (serverMetadata.getCompletionDate() != null
                    && dataValue.getDataElement().equals(
                    serverMetadata.getCompletionDate().getUId())) {
                completionDate = EventExtended.parseLongDate(dataValue.getValue());
            } else if (dataValue.getDataElement().equals(serverMetadata.getUploadDate().getUId())) {
                uploadDate = EventExtended.parseLongDate(dataValue.getValue());
            } else if (dataValue.getDataElement().equals(
                    serverMetadata.getNextAssessment().getUId())) {
                if (dataValue.getValue() != null && !dataValue.getValue().isEmpty()) {
                    nextAssessmentDate =
                            convertStringToDate("yyyy-MM-dd", dataValue.getValue());
                }

                if (nextAssessmentDate != null) {
                    scheduledDate = nextAssessmentDate;
                }

            } else if (dataValue.getDataElement().equals(serverMetadata.getUploadBy().getUId())) {
                userUid = dataValue.getValue();
            } else if (compositeScoreMap.containsKey(dataValue.getDataElement())) {
                CompositeScoreDB compositeScore = compositeScoreMap.get(
                        dataValue.getDataElement());

                if (CompositeScoreBuilder.isRootScore(compositeScore)) {
                    score = new Score(dataValue.getDataElement(),
                            Float.parseFloat(dataValue.getValue()));
                }
            } else {
                QuestionValue questionValue = mapValue(dataValue);

                if (questionValue != null) {
                    values.add(questionValue);
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

        if (questionsMap.containsKey(dataValue.getDataElement())) {
            String optionUid = null;

            Option option = findOptionByValue(dataValue);

            if (option != null) {
                optionUid = option.getUId();
            }

            //No option -> text question (straight value)
            if (optionUid == null) {
                questionValue = QuestionValue.createSimpleValue(questionUid, dataValue.getValue());
            } else {
                //Option -> extract value from code
                questionValue = QuestionValue.createOptionValue(questionUid, optionUid,
                       option.getName());
            }
        } else {
            //There is data values assigned by dhis scripts that we should not converting
            //Examples:
            //<dataValue created="2017-10-03T08:00:35.266" lastUpdated="2018-04-24T04:00:40.560"
            // value="23" dataElement="deeu8rjsqvH" providedElsewhere="false"
            // storedBy="script_HNQIS"/>
            //<dataValue created="2017-10-03T08:00:35.269" lastUpdated="2018-04-24T04:00:40.564"
            // value="false" dataElement="iW2zVNwfDK6" providedElsewhere="false"
            // storedBy="script_HNQIS"/>
            Log.w(TAG, String.format(
                    "An error occurred converting data value because DataElement does no exists "
                            + "as Question. Event: %s, DataElement: %s)",
                    dataValue.getEvent().getUId(), dataValue.getDataElement()));
        }

        return questionValue;
    }

    private Option findOptionByValue(TrackedEntityDataValue dataValue) {
        Option optionResult = null;

        Question question = questionsMap.get(dataValue.getDataElement());

        for (Option option : optionsMap.get(question.getAnswerName())) {
            if (option.getCode().equals(dataValue.getValue())) {
                optionResult = option;
                break;
            }
        }

        return optionResult;
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

    private void createMaps(List<CompositeScoreDB> compositeScores, List<Question> questions,
            List<Option> options) {
        compositeScoreMap = new HashMap<>();
        for (CompositeScoreDB compositeScore : compositeScores) {
            compositeScoreMap.put(compositeScore.getUid(), compositeScore);
        }

        optionsMap = new HashMap<>();
        for (Option option : options) {
            if (!optionsMap.containsKey(option.getAnswerName())) {
                optionsMap.put(option.getAnswerName(), new ArrayList<Option>());
            }

            optionsMap.get(option.getAnswerName()).add(option);
        }


        questionsMap = new HashMap<>();
        for (Question question : questions) {
            questionsMap.put(question.getUId(), question);
        }
    }
}

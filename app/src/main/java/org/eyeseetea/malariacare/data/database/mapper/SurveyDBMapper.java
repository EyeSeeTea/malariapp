package org.eyeseetea.malariacare.data.database.mapper;

import androidx.annotation.NonNull;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.QuestionValue;
import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyDBMapper {
    private final static String TAG = ".SurveyDBMapper";

    private Map<String, QuestionDB> questionsDBMap;
    private Map<String, OptionDB> optionsDBMap;
    private Map<String, OrgUnitDB> orgUnitsDBMap;
    private Map<String, ProgramDB> programsDBMap;
    private Map<String, UserDB> usersDBMap;

    public SurveyDBMapper(
            List<OrgUnitDB> orgUnitsDB,
            List<ProgramDB> programsDB,
            List<QuestionDB> questionsDB,
            List<OptionDB> optionsDB,
            List<UserDB> usersDB) {

        createMaps(orgUnitsDB, programsDB, questionsDB, optionsDB, usersDB);
    }

    public SurveyDB mapToAdd(Survey survey) {
        try {

            SurveyDB surveyDB = new SurveyDB();

            surveyDB.setEventUid(survey.getUId());
            surveyDB.setStatus(survey.getStatus().getCode());
            surveyDB.setCompletionDate(survey.getCompletionDate());
            surveyDB.setCreationDate(survey.getCreationDate());
            surveyDB.setUploadDate(survey.getCreationDate());
            surveyDB.setScheduledDate(survey.getScheduledDate());


            if (orgUnitsDBMap.containsKey(survey.getOrgUnitUId())) {
                surveyDB.setOrgUnit(orgUnitsDBMap.get(survey.getOrgUnitUId()));
            }

            if (programsDBMap.containsKey(survey.getProgramUId())) {
                surveyDB.setProgram(programsDBMap.get(survey.getProgramUId()));
            }

            List<ValueDB> valuesDB = new ArrayList<>();

            for (QuestionValue questionValue : survey.getValues()) {
                ValueDB valueDB = mapValueDB(new ValueDB(),surveyDB, questionValue);

                valuesDB.add(valueDB);
            }

            surveyDB.setValues(valuesDB);

            if (survey.getScore() != null) {
                ScoreDB scoreDB = mapScore(survey, surveyDB);

                surveyDB.setScoreDB(scoreDB);
            }

            UserDB user = mapSurveyUser(survey);
            surveyDB.setUser(user);
            surveyDB.save();

            return surveyDB;
        } catch (Exception e) {
            Log.e(TAG, "An error occurred converting Survey " + survey.getUId() +
                    " to surveyDB:" + e.getMessage());
            throw e;
        }
    }

    public SurveyDB mapToModify(SurveyDB surveyDB, Survey survey){
        List<ValueDB> valuesCopy = new ArrayList<>(surveyDB.getValues());

        surveyDB.setStatus(survey.getStatus().getCode());
        surveyDB.setUploadDate(survey.getUploadDate());
        surveyDB.setCreationDate(survey.getCreationDate());
        surveyDB.setCompletionDate(survey.getCompletionDate());
        surveyDB.setEventUid(survey.getSurveyUid());

        for (ValueDB valueDB : valuesCopy) {
            QuestionValue questionValue =
                    getQuestionValue(valueDB.getId_question_fk(), survey);

            if (questionValue == null) {
                surveyDB.getValues().remove(valueDB);
            }
        }

        for (QuestionValue questionValue : survey.getValues()) {
            ValueDB valueDB = getQuestionValueDB(questionValue.getQuestionUId(), surveyDB);

            if (valueDB == null) {
                valueDB = new ValueDB();
                valueDB = mapValueDB(valueDB,surveyDB,questionValue);
                surveyDB.getValues().add(valueDB);
            } else {
                mapValueDB(valueDB,surveyDB,questionValue);
            }
        }

        return surveyDB;
    }

    private ValueDB getQuestionValueDB(String questionUId, SurveyDB surveyDB) {

        ValueDB existedValueDB = null;

        Long questionId = questionsDBMap.get(questionUId).getId_question();

        for (ValueDB valueDB : surveyDB.getValues()) {
            if (valueDB.getId_question_fk().equals(questionId)) {
                existedValueDB = valueDB;
            }
        }

        return existedValueDB;
    }

    private QuestionValue getQuestionValue(Long questionId, Survey survey) {

        QuestionValue existedQuestionValue = null;

        String questionUId = getQuestionUid(questionId);

        for (QuestionValue questionValue : survey.getValues()) {
            if (questionValue.getQuestionUId().equals(questionUId)) {
                existedQuestionValue = questionValue;
            }
        }

        return existedQuestionValue;
    }

    private String getQuestionUid(Long questionId) {
        String questionUId = null;

        for (QuestionDB questionDB:questionsDBMap.values()) {
            if (questionDB.getId_question().equals(questionId)){
                questionUId = questionDB.getUid();
                break;
            }
        }

        return questionUId;
    }

    @NonNull
    private ScoreDB mapScore(Survey survey, SurveyDB surveyDB) {
        ScoreDB scoreDB = new ScoreDB();
        scoreDB.setUid(survey.getScore().getUId());
        scoreDB.setScore(survey.getScore().getScore());
        scoreDB.setSurvey(surveyDB);
        return scoreDB;
    }

    @NonNull
    private UserDB mapSurveyUser(Survey survey) {
        UserDB userDB = findUserDB(survey.getUserUId());

        if (userDB == null) {
            userDB = new UserDB(survey.getUserUId(), survey.getUserUId());
            userDB.save();

            usersDBMap.put(userDB.getUid(), userDB);
        }
        return userDB;
    }

    private UserDB findUserDB(String text) {
        UserDB existedUser = null;

        //For Support old push find by uid, name and username
        for (UserDB userDB : usersDBMap.values()) {
            if ((userDB.getUid() != null && userDB.getUid().equals(text)) ||
                    (userDB.getName() != null && userDB.getName().equals(text)) ||
                    (userDB.getUsername() != null && userDB.getUsername().equals(text))) {
                existedUser = userDB;
            }
        }

        return existedUser;
    }

    private ValueDB mapValueDB(ValueDB valueDB, SurveyDB surveyDB, QuestionValue questionValue) {
        valueDB.setSurvey(surveyDB);
        valueDB.setQuestion(questionsDBMap.get(questionValue.getQuestionUId()));

        if (questionValue.getOptionUId() != null) {
            valueDB.setOption(optionsDBMap.get(questionValue.getOptionUId()));
        }

        valueDB.setValue(questionValue.getValue());
        valueDB.setUploadDate(new Date());
        valueDB.setConflict(questionValue.isConflict());

        return valueDB;
    }

    private void createMaps(List<OrgUnitDB> orgUnitsDB, List<ProgramDB> programsDB,
            List<QuestionDB> questionsDB, List<OptionDB> optionsDB, List<UserDB> usersDB) {
        orgUnitsDBMap = new HashMap<>();
        for (OrgUnitDB orgUnitDB : orgUnitsDB) {
            orgUnitsDBMap.put(orgUnitDB.getUid(), orgUnitDB);
        }

        programsDBMap = new HashMap<>();
        for (ProgramDB programDB : programsDB) {
            programsDBMap.put(programDB.getUid(), programDB);
        }

        questionsDBMap = new HashMap<>();
        for (QuestionDB questionDB : questionsDB) {
            questionsDBMap.put(questionDB.getUid(), questionDB);
        }

        optionsDBMap = new HashMap<>();
        for (OptionDB optionDB : optionsDB) {
            optionsDBMap.put(optionDB.getUid(), optionDB);
        }

        usersDBMap = new HashMap<>();
        for (UserDB userDB : usersDB) {
            usersDBMap.put(userDB.getUsername(), userDB);
        }
    }
}
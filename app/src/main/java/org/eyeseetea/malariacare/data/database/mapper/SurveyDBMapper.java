package org.eyeseetea.malariacare.data.database.mapper;

import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
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

    public SurveyDBMapper(
            List<OrgUnitDB> orgUnitsDB,
            List<ProgramDB> programsDB,
            List<QuestionDB> questionsDB,
            List<OptionDB> optionsDB) {

        createMaps(orgUnitsDB, programsDB, questionsDB, optionsDB);
    }

    public List<SurveyDB> mapSurveys(List<Survey> surveys) {

        List<SurveyDB> surveysDB = new ArrayList<>();

        for (Survey survey : surveys) {
            try {
                SurveyDB surveyDB = map(survey);

                surveysDB.add(surveyDB);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred converting Survey " + survey.getUId() +
                        " to surveyDB:" + e.getMessage());
            }
        }

        return surveysDB;
    }

    private SurveyDB map(Survey survey) {
        SurveyDB surveyDB = new SurveyDB();

        surveyDB.setEventUid(survey.getUId());
        surveyDB.setStatus(survey.getStatus().getCode());
        surveyDB.setCompletionDate(survey.getCompletionDate());
        surveyDB.setCreationDate(survey.getCreationDate());
        surveyDB.setUploadDate(survey.getCreationDate());
        surveyDB.setScheduledDate(survey.getScheduledDate());
        surveyDB.setOrgUnit(orgUnitsDBMap.get(survey.getOrgUnitUId()));
        surveyDB.setProgram(programsDBMap.get(survey.getProgramUId()));

        List<ValueDB> valuesDB = new ArrayList<>();

        for (QuestionValue questionValue : survey.getValues()) {
            ValueDB valueDB = mapValueDB(surveyDB, questionValue);

            valuesDB.add(valueDB);
        }

        surveyDB.setValues(valuesDB);

        ScoreDB scoreDB = new ScoreDB();
        scoreDB.setUid(survey.getScore().getUId());
        scoreDB.setScore(survey.getScore().getScore());
        scoreDB.setSurvey(surveyDB);

        surveyDB.setScoreDB(scoreDB);

        return surveyDB;
    }

    private ValueDB mapValueDB(SurveyDB surveyDB, QuestionValue questionValue) {

        ValueDB value = new ValueDB();

        value.setSurvey(surveyDB);
        value.setQuestion(questionsDBMap.get(questionValue.getQuestionUId()));

        if (questionValue.getOptionUId() != null) {
            value.setOption(optionsDBMap.get(questionValue.getOptionUId()));
        }

        value.setValue(questionValue.getValue());
        value.setUploadDate(new Date());

        return value;
    }

    private void createMaps(List<OrgUnitDB> orgUnitsDB, List<ProgramDB> programsDB,
            List<QuestionDB> questionsDB, List<OptionDB> optionsDB) {
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
    }
}

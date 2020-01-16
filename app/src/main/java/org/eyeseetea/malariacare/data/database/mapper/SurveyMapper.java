package org.eyeseetea.malariacare.data.database.mapper;

import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.entity.QuestionValue;
import org.eyeseetea.malariacare.domain.entity.Score;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyMapper {
    private Map<Long, QuestionDB> questionsDBMap;
    private Map<Long, OptionDB> optionsDBMap;
    private Map<Long, OrgUnitDB> orgUnitsDBMap;
    private Map<Long, ProgramDB> programsDBMap;
    private Map<Long, UserDB> usersDBMap;
    private Map<Long, ScoreDB> scoreDBMap;
    private Map<String, OrgUnitProgramRelationDB> orgUnitProgramRelationsMap;


    public SurveyMapper(
            List<OrgUnitDB> orgUnitsDB,
            List<ProgramDB> programsDB,
            List<QuestionDB> questionsDB,
            List<OptionDB> optionsDB,
            List<UserDB> usersDB,
            List<ScoreDB> scores,
            List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB) {

        createMaps(orgUnitsDB, programsDB, questionsDB, optionsDB, usersDB, scores,orgUnitProgramRelationsDB);
    }

    public List<Survey> mapSurveys(List<SurveyDB> surveyDBS) {
        List<Survey> surveys = new ArrayList<>();

        for (SurveyDB surveyDB : surveyDBS) {
            try {
                Survey survey = map(surveyDB);

                surveys.add(survey);
            } catch (Exception e) {
                System.out.println(
                        "An error occurred converting SurveyDB " + surveyDB.getId_survey() +
                                " to domain survey:" + e.getMessage());
            }
        }

        return surveys;
    }

    public Survey map(SurveyDB surveyDB) {

        String eventUid = surveyDB.getEventUid();
        String orgUnitUid = orgUnitsDBMap.get(surveyDB.getId_org_unit_fk()).getUid();
        String programUid = programsDBMap.get(surveyDB.getId_program_fk()).getUid();
        String userUid = null;

        if (usersDBMap.containsKey(surveyDB.getId_user_fk())) {
            userUid = usersDBMap.get(surveyDB.getId_user_fk()).getUid();
        }
        else{
            userUid = ((UserDB)usersDBMap.values().toArray()[0]).getUid();
        }

        ScoreDB scoreDB = scoreDBMap.get(surveyDB.getId_survey());

        Score score = null;

        if (scoreDB != null)
            score = new Score(scoreDB.getUid(), scoreDB.getScore());

        Date creationDate = surveyDB.getCreationDate();
        Date completionDate = surveyDB.getCompletionDate();
        Date uploadDate = surveyDB.getUploadDate();
        Date scheduledDate = surveyDB.getScheduledDate();

        List<QuestionValue> values = new ArrayList<>();

        for (ValueDB valueDB : surveyDB.getValues()) {
            QuestionValue questionValue = mapValue(valueDB, surveyDB);

            if (questionValue != null) {
                values.add(questionValue);
            }
        }

        OrgUnitProgramRelationDB orgUnitProgramRelationDB =
                orgUnitProgramRelationsMap.get(surveyDB.getId_org_unit_fk() + "_" +
                        surveyDB.getId_program_fk());

        int productivity = Survey.DEFAULT_PRODUCTIVITY;

        if (orgUnitProgramRelationDB != null)
            productivity = orgUnitProgramRelationDB.getProductivity();

        CompetencyScoreClassification competency =
                CompetencyScoreClassification.get(
                        surveyDB.getCompetencyScoreClassification());

        Survey survey = Survey.createStoredSurvey(SurveyStatus.get(surveyDB.getStatus()),
                eventUid, programUid, orgUnitUid, userUid, creationDate, uploadDate, scheduledDate,
                completionDate, values, score, productivity, competency);

        return survey;

    }

    private QuestionValue mapValue(ValueDB valueDB, SurveyDB surveyDB) {

        QuestionValue questionValue = null;
        String questionUid;

        if (questionsDBMap.containsKey(valueDB.getId_question_fk())) {
            questionUid = questionsDBMap.get(valueDB.getId_question_fk()).getUid();

            if (valueDB.getId_option_fk() != null &&
                    optionsDBMap.containsKey(valueDB.getId_option_fk())) {

                OptionDB optionDB = optionsDBMap.get(valueDB.getId_option_fk());

                //Option -> extract value from code
                questionValue = QuestionValue.createOptionValue(questionUid, optionDB.getUid(),
                        optionDB.getName());
            } else {

                questionValue = QuestionValue.createSimpleValue(questionUid,
                        valueDB.getValue());

            }
        } else {
            System.out.println(String.format(
                    "An error occurred converting data value. Survey: %s, Question: %s)",
                    surveyDB.getId_survey(), valueDB.getId_question_fk()));
        }

        return questionValue;
    }

    private void createMaps(List<OrgUnitDB> orgUnitsDB, List<ProgramDB> programsDB,
            List<QuestionDB> questionsDB, List<OptionDB> optionsDB, List<UserDB
            > usersDB,
            List<ScoreDB> scoresDB,
            List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB) {
        orgUnitsDBMap = new HashMap<>();
        for (OrgUnitDB orgUnitDB : orgUnitsDB) {
            orgUnitsDBMap.put(orgUnitDB.getId_org_unit(), orgUnitDB);
        }

        programsDBMap = new HashMap<>();
        for (ProgramDB programDB : programsDB) {
            programsDBMap.put(programDB.getId_program(), programDB);
        }

        questionsDBMap = new HashMap<>();
        for (QuestionDB questionDB : questionsDB) {
            questionsDBMap.put(questionDB.getId_question(), questionDB);
        }

        optionsDBMap = new HashMap<>();
        for (OptionDB optionDB : optionsDB) {
            optionsDBMap.put(optionDB.getId_option(), optionDB);
        }

        usersDBMap = new HashMap<>();
        for (UserDB userDB : usersDB) {
            usersDBMap.put(userDB.getId_user(), userDB);
        }

        scoreDBMap = new HashMap<>();
        for (ScoreDB scoreDB : scoresDB) {
            scoreDBMap.put(scoreDB.getId_survey_fk(), scoreDB);
        }

        orgUnitProgramRelationsMap = new HashMap<>();
        for (OrgUnitProgramRelationDB orgUnitProgramRelationDB : orgUnitProgramRelationsDB) {
            orgUnitProgramRelationsMap.put(orgUnitProgramRelationDB.getId_org_unit_fk() + "_" +
                    orgUnitProgramRelationDB.getProgram(), orgUnitProgramRelationDB);
        }
    }
}
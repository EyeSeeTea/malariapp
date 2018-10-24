package org.eyeseetea.malariacare.data.database.datasources;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.eyeseetea.malariacare.data.boundaries.IDataLocalDataSource;
import org.eyeseetea.malariacare.data.database.mapper.SurveyDBMapper;
import org.eyeseetea.malariacare.data.database.mapper.SurveyMapper;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB_Table;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB_Table;
import org.eyeseetea.malariacare.domain.entity.IData;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyLocalDataSource implements IDataLocalDataSource {
    private final static String TAG = ".SurveyLocalDataSource";


    @Override
    public List<? extends IData> getDataToSync() throws Exception {
        List<Survey> surveys = getSurveys(SurveyStatus.COMPLETED);
        return surveys;
    }

    @Override
    public List<? extends IData> getAll() {
        List<Survey> surveys = getSurveys(null);
        return surveys;
    }

    @Override
    public IData getByUId(String uid) {
        return getSurveyByUid(uid);
    }

    @Override
    public void save(List<? extends IData> dataList) throws Exception {
        List<Survey> surveys = (List<Survey>) dataList;
        saveSurveys(surveys);
    }

    @Override
    public void save(IData data) {
        Survey survey = (Survey) data;

        saveSurvey(survey);
    }

    private void saveSurveys(List<Survey> surveys) {
        for (Survey survey : surveys) {
            try {
                save(survey);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred saving Survey " + survey.getUId() + ":" +
                        e.getMessage());
            }

        }
    }

    private SurveyDBMapper createMapper() {
        SurveyDBMapper surveyDBMapper = new SurveyDBMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list());
        return surveyDBMapper;
    }


    private List<Survey> getSurveys(SurveyStatus status) {

        List<SurveyDB> surveyDBS = getSurveysDB(status);

        SurveyMapper surveyMapper = new SurveyMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list(), ScoreDB.list(), OrgUnitProgramRelationDB.list());

        List<Survey> surveys = surveyMapper.mapSurveys(surveyDBS);

        return surveys;
    }

    private Survey getSurveyByUid(String eventUId) {

        List<SurveyDB> surveyDBS = getSurveysDB(eventUId);

        SurveyMapper surveyMapper = new SurveyMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list(), ScoreDB.list(), OrgUnitProgramRelationDB.list());

        List<Survey> surveys = surveyMapper.mapSurveys(surveyDBS);

        return surveys.get(0);
    }

    private void saveSurvey(Survey survey) {
        SurveyDB surveyDB = getSurveyDB(survey.getUId());

        if (surveyDB == null) {
            add(survey);
        } else {
            modify(surveyDB, survey);
        }
    }

    private SurveyDB getSurveyDB(String surveyUid) {
        SurveyDB surveyDB = new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.uid_event_fk.is(surveyUid)).querySingle();

        if (surveyDB != null) {
            List<ValueDB> valuesDB =
                    getSurveyValuesDB(surveyDB.getId_survey());

            surveyDB.setValues(valuesDB);
        }

        return surveyDB;
    }

    private List<ValueDB> getSurveyValuesDB(Long surveyId) {
        return new Select().from(ValueDB.class)
                .where(ValueDB_Table.id_survey_fk.is(surveyId)).queryList();
    }

    private void add(Survey survey) {
        SurveyDB surveyDB = createMapper().mapToAdd(survey);

        saveChanges(surveyDB);
    }

    private void modify(SurveyDB surveyDB, Survey survey) {
        surveyDB = createMapper().mapToModify(surveyDB, survey);

        saveChanges(surveyDB);

        deleteNonExistedValuesInModifiedSurvey(surveyDB);
    }

    private void deleteNonExistedValuesInModifiedSurvey(SurveyDB surveyDB) {
        List<Long> existedQuestionsInSurvey = new ArrayList<>();

        for (ValueDB valueDB:surveyDB.getValues()) {
            existedQuestionsInSurvey.add(valueDB.getId_question_fk());
        }

        new Delete().from(ValueDB.class)
                .where(ValueDB_Table.id_survey_fk.is(surveyDB.getId_survey()))
                .and(ValueDB_Table.id_question_fk.notIn(existedQuestionsInSurvey)).execute();
    }

    private void saveChanges(SurveyDB surveyDB) {
        surveyDB.save();

        if (surveyDB.getScoreDB() != null) {
            surveyDB.getScoreDB().setSurvey(surveyDB);
            surveyDB.getScoreDB().save();
        }

        for (ValueDB valueDB : surveyDB.getValues()) {
            valueDB.setSurvey(surveyDB);
            valueDB.save();
        }
    }

    private List<SurveyDB> getSurveysDB(String eventUId){
        List<SurveyDB> surveyDBS = null;

        From from = new Select().from(SurveyDB.class);

        Where where = from.where(SurveyDB_Table.uid_event_fk.eq(eventUId));

        surveyDBS = where.orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();

        if (surveyDBS.size() > 0)
            loadValuesInSurveys(surveyDBS);

        return surveyDBS;
    }

    private List<SurveyDB> getSurveysDB(SurveyStatus status){
        List<SurveyDB> surveyDBS = null;

        From from = new Select().from(SurveyDB.class);

        Where where = from.where(SurveyDB_Table.status.isNotNull());

        if (status == SurveyStatus.COMPLETED){
            where = from.where(SurveyDB_Table.status.in(Constants.SURVEY_COMPLETED));
        }

        surveyDBS = where.orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();

        if (surveyDBS.size() > 0)
            loadValuesInSurveys(surveyDBS);

        return surveyDBS;
    }

    private void loadValuesInSurveys(List<SurveyDB> surveyDBS) {
        List<ValueDB> allValues = new Select().from(ValueDB.class).queryList();

        Map<Long, List<ValueDB>> valuesMap = new HashMap<>();
        for (ValueDB valueDB : allValues) {
            if (!valuesMap.containsKey(valueDB.getId_survey_fk()))
                valuesMap.put(valueDB.getId_survey_fk(), new ArrayList<ValueDB>());

            valuesMap.get(valueDB.getId_survey_fk()).add(valueDB);
        }

        for (SurveyDB surveyDB : surveyDBS) {
            if (valuesMap.containsKey(surveyDB.getId_survey())){
                surveyDB.setValues(valuesMap.get(surveyDB.getId_survey()));
            }
        }
    }
}

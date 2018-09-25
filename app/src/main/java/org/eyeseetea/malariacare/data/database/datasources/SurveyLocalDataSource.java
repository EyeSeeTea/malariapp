package org.eyeseetea.malariacare.data.database.datasources;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.eyeseetea.malariacare.data.boundaries.ISyncDataLocalDataSource;
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
import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyLocalDataSource implements ISyncDataLocalDataSource {
    private final static String TAG = ".SurveyLocalDataSource";


    @Override
    public List<? extends ISyncData> getDataToSync() throws Exception {
        List<Survey> surveys = getSurveys(SurveyStatus.COMPLETED);
        return surveys;
    }

    @Override
    public List<? extends ISyncData> getAll() {
        List<Survey> surveys = getSurveys(null);
        return surveys;
    }

    @Override
    public void save(List<? extends ISyncData> syncData) throws Exception {
        List<Survey> surveys = (List<Survey>) syncData;
        saveSurveys(surveys);
    }

    @Override
    public void save(ISyncData syncData) {
        Survey survey = (Survey) syncData;

        SurveyDB surveyDB = createMapper().map(survey);

        saveSurveyAndDependencies(surveyDB);
    }

    private void saveSurveys(List<Survey> surveys) {

        List<SurveyDB> surveysDB = createMapper().mapSurveys(surveys);

        for (SurveyDB surveyDB : surveysDB) {
            try {
                saveSurveyAndDependencies(surveyDB);
            } catch (Exception e) {
                Log.e(TAG, "An error occurred saving Survey " + surveyDB.getEventUid() + ":" +
                         e.getMessage());
            }
        }
    }

    private SurveyDBMapper createMapper() {
        SurveyDBMapper mSurveyDBMapper;
        mSurveyDBMapper = new SurveyDBMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list());
        return mSurveyDBMapper;
    }


    private List<Survey> getSurveys(SurveyStatus status) {

        List<SurveyDB> surveyDBS = getSurveysDB(status);

        SurveyMapper surveyMapper = new SurveyMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list(), ScoreDB.list(), OrgUnitProgramRelationDB.list());

        List<Survey> surveys = surveyMapper.mapSurveys(surveyDBS);

        return surveys;
    }

    private void saveSurveyAndDependencies(SurveyDB surveyDB) {
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

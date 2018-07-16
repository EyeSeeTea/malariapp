package org.eyeseetea.malariacare.data.database.datasources;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
import org.eyeseetea.malariacare.data.database.mapper.SurveyDBMapper;
import org.eyeseetea.malariacare.data.database.mapper.SurveyMapper;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB_Table;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyLocalDataSource implements ISurveyDataSource {
    private final static String TAG = ".SurveyLocalDataSource";

    @Override
    public List<Survey> getSurveys(SurveyFilter filter) {

        List<SurveyDB> surveyDBS = getSurveysDB(filter);

        SurveyMapper surveyMapper = new SurveyMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list(), ScoreDB.list());

        List<Survey> surveys = surveyMapper.mapSurveys(surveyDBS);

        return surveys;
    }

    @Override
    public void Save(List<Survey> surveys) {
        SurveyDBMapper surveyDBMapper = new SurveyDBMapper(
                OrgUnitDB.list(), ProgramDB.getAllPrograms(), QuestionDB.list(),
                OptionDB.list(), UserDB.list());

        List<SurveyDB> surveysDB = surveyDBMapper.mapSurveys(surveys);

        for (SurveyDB surveyDB : surveysDB) {
            try {
                surveyDB.save();

                if (surveyDB.getScoreDB() != null) {
                    surveyDB.getScoreDB().setSurvey(surveyDB);
                    surveyDB.getScoreDB().save();
                }

                for (ValueDB valueDB : surveyDB.getValues()) {
                    valueDB.setSurvey(surveyDB);
                    valueDB.save();
                }
            } catch (Exception e) {
                Log.e(TAG, "An error occurred saving Survey " + surveyDB.getEventUid() + ":" +
                         e.getMessage());
            }
        }
    }

    private List<SurveyDB> getSurveysDB(SurveyFilter surveyFilter){
        List<SurveyDB> surveyDBS = new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.status.isNot(Constants.SURVEY_PLANNED)).queryList();

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

        return surveyDBS;
    }
}

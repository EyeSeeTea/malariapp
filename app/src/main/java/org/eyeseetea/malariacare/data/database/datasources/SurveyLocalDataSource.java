package org.eyeseetea.malariacare.data.database.datasources;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.eyeseetea.malariacare.data.boundaries.ISurveyDataSource;
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
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.entity.SurveyStatusFilter;
import org.eyeseetea.malariacare.domain.usecase.SurveysFilter;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyLocalDataSource implements ISurveyDataSource {
    private final static String TAG = ".SurveyLocalDataSource";

    List<OrgUnitDB> orgUnitsDB;
    List<ProgramDB> programsDB;
    List<QuestionDB> questionsDB;
    List<OptionDB> optionsDB;
    List<OrgUnitProgramRelationDB> orgUnitProgramRelationsDB;

    public SurveyLocalDataSource() {
        loadMetadata();
    }

    private void loadMetadata() {
        orgUnitsDB = OrgUnitDB.list();
        programsDB = ProgramDB.getAllPrograms();
        questionsDB = QuestionDB.list();
        optionsDB = OptionDB.list();
        orgUnitProgramRelationsDB = OrgUnitProgramRelationDB.list();
    }


    @Override
    public List<Survey> getSurveysByFilter(SurveysFilter filter) {
        Long programId = getProgramId(filter.getProgramUid());
        Long orgUnitId = getOrgUnitId(filter.getOrgUnitUid());

        List<SurveyDB> surveyDBS = getSurveysDB(filter.getStatus(), programId, orgUnitId, filter.getStartDate());

        List<Survey> surveys = mapSurveys(surveyDBS);

        return surveys;
    }

    @Override
    public List<Survey> getSurveysByUids(List<String> uids) throws Exception {
        List<SurveyDB> surveyDBS = getSurveysDBByUids(uids);

        List<Survey> surveys = mapSurveys(surveyDBS);

        return surveys;
    }

    @Override
    public void save(List<Survey> surveys) throws Exception {
        saveSurveys(surveys);
    }

    @Override
    public Survey getSurveyByUid(String uid) {
        SurveyDB surveyDB = getSurveyDB(uid);

        Survey survey = mapSurvey(surveyDB);

        return survey;
    }

    private List<Survey> mapSurveys(List<SurveyDB> surveyDBS) {
        SurveyMapper surveyMapper = new SurveyMapper(
                orgUnitsDB, programsDB, questionsDB,
                optionsDB, UserDB.list(), ScoreDB.list(), orgUnitProgramRelationsDB);

        return surveyMapper.mapSurveys(surveyDBS);
    }

    private Survey mapSurvey(SurveyDB surveyDB) {
        SurveyMapper surveyMapper = new SurveyMapper(
                orgUnitsDB, programsDB, questionsDB,
                optionsDB, UserDB.list(), ScoreDB.list(), orgUnitProgramRelationsDB);

        return surveyMapper.map(surveyDB);
    }

    private void saveSurveys(List<Survey> surveys) {
        for (Survey survey : surveys) {
            try {
                saveSurvey(survey);
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

        for (ValueDB valueDB : surveyDB.getValues()) {
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

    private List<SurveyDB> getSurveysDB(SurveyStatusFilter statusFilter, Long program, Long orgUnit, Date startDate) {
        List<SurveyDB> surveyDBS = null;

        List<Integer> statuses = getFilterStatus(statusFilter);

        From from = new Select().from(SurveyDB.class);

        Where where = from.where(SurveyDB_Table.status.isNotNull());

        if (statuses != null && statuses.size() > 0) {
            where = from.where(SurveyDB_Table.status.in(statuses));
        }

        if (program != null) {
            where = where.and(SurveyDB_Table.id_program_fk.in(program));
        }

        if (orgUnit != null) {
            where = where.and(SurveyDB_Table.id_org_unit_fk.in(orgUnit));
        }

        if (startDate != null) {
            where = where.and(SurveyDB_Table.completion_date.greaterThan(startDate));
        }

        surveyDBS = where.orderBy(OrderBy.fromProperty(SurveyDB_Table.completion_date))
                .orderBy(OrderBy.fromProperty(SurveyDB_Table.id_org_unit_fk)).queryList();

        if (surveyDBS.size() > 0)
            loadValuesInSurveys(surveyDBS);

        return surveyDBS;
    }

    private List<Integer> getFilterStatus(SurveyStatusFilter statusFilter) {
        List<Integer> statuses = new ArrayList<>();

        if (statusFilter != null) {
            if (statusFilter == SurveyStatusFilter.IN_PROGRESS) {
                statuses.add(SurveyStatus.IN_PROGRESS.getCode());
            } else {

                statuses.add(SurveyStatus.SENT.getCode());
                statuses.add(SurveyStatus.COMPLETED.getCode());
                statuses.add(SurveyStatus.CONFLICT.getCode());
                statuses.add(SurveyStatus.QUARANTINE.getCode());
                statuses.add(SurveyStatus.SENDING.getCode());
            }
        }

        return statuses;
    }

    private List<SurveyDB> getSurveysDBByUids(List<String> uids) {
        List<SurveyDB> surveyDBS = new Select().from(SurveyDB.class)
                .where(SurveyDB_Table.uid_event_fk.in(uids)).queryList();

        if (surveyDBS.size() > 0)
            loadValuesInSurveys(surveyDBS);

        return surveyDBS;
    }

    private void loadValuesInSurveys(List<SurveyDB> surveyDBS) {
        List<String> surveysUid = extractSurveyUIds(surveyDBS);

        List<ValueDB> allValues =
                new Select().from(ValueDB.class)
                        .leftOuterJoin(SurveyDB.class).on(ValueDB_Table.id_survey_fk.eq(SurveyDB_Table.id_survey))
                        .where(SurveyDB_Table.uid_event_fk.in(surveysUid)).queryList();


        Map<Long, List<ValueDB>> valuesMap = new HashMap<>();
        for (ValueDB valueDB : allValues) {
            if (!valuesMap.containsKey(valueDB.getId_survey_fk()))
                valuesMap.put(valueDB.getId_survey_fk(), new ArrayList<ValueDB>());

            valuesMap.get(valueDB.getId_survey_fk()).add(valueDB);
        }

        for (SurveyDB surveyDB : surveyDBS) {
            if (valuesMap.containsKey(surveyDB.getId_survey())) {
                surveyDB.setValues(valuesMap.get(surveyDB.getId_survey()));
            }
        }
    }

    private List<String> extractSurveyUIds(List<SurveyDB> surveyDBS) {
        List<String> surveysUIds = new ArrayList<>();

        for (SurveyDB surveyDB : surveyDBS) {
            surveysUIds.add(surveyDB.getEventUid());
        }

        return surveysUIds;
    }

    private Long getProgramId(String uid) {
        Long id = null;

        for (ProgramDB programDB : programsDB) {
            if (programDB.getUid().equals(uid)) {
                return programDB.getId_program();
            }
        }

        return id;
    }

    private Long getOrgUnitId(String uid) {
        Long id = null;

        for (OrgUnitDB orgUnitsDB : orgUnitsDB) {
            if (orgUnitsDB.getUid().equals(uid)) {
                return orgUnitsDB.getId_org_unit();
            }
        }

        return id;
    }
}
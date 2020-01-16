package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.eyeseetea.malariacare.data.database.mapper.ObservationDBMapper;
import org.eyeseetea.malariacare.data.database.mapper.ObservationMapper;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationDB_Table;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB_Table;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB_Table;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB_Table;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB_Table;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.exception.ObservationNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationLocalDataSource {
    private ObservationMapper mObservationMapper;

    public List<Observation> getSentObservations(
            String programUid,
            String orgUnitUid,
            List<ObservationStatus> observationStatuses) {
        List<ObservationDB> observationDBS =
                getObservationsDBByStatus(programUid, orgUnitUid, observationStatuses);

        List<SurveyDB> surveyDBS = new Select().from(SurveyDB.class).queryList();

        mObservationMapper = new ObservationMapper(surveyDBS);
        List<Observation> observations = mObservationMapper.map(observationDBS);

        return observations;
    }

    public void save(List<Observation> observations) {
        saveObservations(observations);
    }

    public Observation getObservation(String surveyUId) throws Exception {
        ObservationDB observationDB = getObservationDB(surveyUId);

        if (observationDB != null) {

            List<SurveyDB> surveyDBS = new Select().from(SurveyDB.class)
                    .where(SurveyDB_Table.uid_event_fk.eq(surveyUId)).queryList();

            mObservationMapper = new ObservationMapper(surveyDBS);
            Observation observation = mObservationMapper.map(observationDB);

            return observation;
        } else {
            throw new ObservationNotFoundException();
        }
    }

    public void saveObservations(List<Observation> observations) {
        for (Observation observation : observations) {
            save(observation);
        }
    }

    public void save(Observation observation) {
        saveObservation(observation);
    }

    public void saveObservation(Observation observation) {
        ObservationDB observationDB = getObservationDB(observation.getSurveyUid());

        if (observationDB == null) {
            add(observation);
        } else {
            modify(observationDB, observation);
        }

    }

    private List<ObservationDB> getObservationsDBByStatus(
            String programUid,
            String orgUnitUid,
            List<ObservationStatus> observationStatuses) {

        List<ObservationDB> observationDBS;

        From from = new Select().from(ObservationDB.class)
                .leftOuterJoin(SurveyDB.class)
                .on(SurveyDB_Table.id_survey.eq(ObservationDB_Table.id_survey_observation_fk))
                .leftOuterJoin(ProgramDB.class)
                .on(SurveyDB_Table.id_program_fk.eq(ProgramDB_Table.id_program))
                .leftOuterJoin(OrgUnitDB.class)
                .on(SurveyDB_Table.id_org_unit_fk.eq(OrgUnitDB_Table.id_org_unit));

        Where basicWhere = from.where(ObservationDB_Table.status_observation.isNotNull());

        if (programUid != null && !programUid.isEmpty()){
            basicWhere = basicWhere.and(ProgramDB_Table.uid_program.eq(programUid));
        }

        if (orgUnitUid != null && !orgUnitUid.isEmpty()){
            basicWhere = basicWhere.and(OrgUnitDB_Table.uid_org_unit.eq(orgUnitUid));
        }

        if (observationStatuses != null && observationStatuses.size() > 0) {
            List<Integer> statusCodes = new ArrayList<>();

            for (ObservationStatus observationStatus : observationStatuses) {
                statusCodes.add(observationStatus.getCode());
            }

            basicWhere = basicWhere.and(ObservationDB_Table.status_observation.in(statusCodes));
        }

        observationDBS = basicWhere.queryList();

        if (observationDBS.size() > 0) {
            loadValuesInObservation(observationDBS);
        }

        return observationDBS;
    }

    private void loadValuesInObservation(List<ObservationDB> observationDBS) {
        List<ObservationValueDB> allValues =
                new Select().from(ObservationValueDB.class).queryList();

        Map<Long, List<ObservationValueDB>> valuesMap = new HashMap<>();
        for (ObservationValueDB observationValueDB : allValues) {
            if (!valuesMap.containsKey(observationValueDB.getId_observation_fk())) {
                valuesMap.put(observationValueDB.getId_observation_fk(),
                        new ArrayList<ObservationValueDB>());
            }

            valuesMap.get(observationValueDB.getId_observation_fk()).add(observationValueDB);
        }

        for (ObservationDB observationDB : observationDBS) {
            if (valuesMap.containsKey(observationDB.getId_observation())) {
                observationDB.setValuesDB(valuesMap.get(observationDB.getId_observation()));
            }
        }
    }

    private ObservationDB getObservationDB(String surveyUId) {
        ObservationDB observationDB = new Select().from(ObservationDB.class)
                .leftOuterJoin(SurveyDB.class)
                .on(ObservationDB_Table.id_survey_observation_fk.eq(SurveyDB_Table.id_survey))
                .where(SurveyDB_Table.uid_event_fk.is(surveyUId)).querySingle();

        if (observationDB != null) {
            List<ObservationValueDB> valuesDB =
                    getObservationValuesDB(observationDB.getId_observation());

            observationDB.setValuesDB(valuesDB);
        }

        return observationDB;
    }

    private List<ObservationValueDB> getObservationValuesDB(long observationId) {
        return new Select().from(ObservationValueDB.class)
                .where(ObservationValueDB_Table.id_observation_fk.is(observationId)).queryList();
    }


    private void add(Observation observation) {
        SurveyDB surveyDB = SurveyDB.getSurveyByUId(observation.getSurveyUid());

        ObservationDB observationDB = createMapper().mapToAdd(observation, surveyDB.getId_survey());

        saveChanges(observationDB);
    }

    private void modify(ObservationDB observationDB, Observation observation) {
        observationDB = createMapper().mapToModify(observationDB, observation);

        saveChanges(observationDB);

        deleteNonExistedValuesInModifiedObservation(observationDB);
    }

    private ObservationDBMapper createMapper() {
        ObservationDBMapper observationDBMapper = new ObservationDBMapper();
        return observationDBMapper;
    }

    private void saveChanges(ObservationDB observationDB) {
        observationDB.save();

        for (ObservationValueDB observationValueDB : observationDB.getValuesDB()) {
            observationValueDB.setId_observation_fk(observationDB.getId_observation());
            observationValueDB.save();
        }
    }

    private void deleteNonExistedValuesInModifiedObservation(ObservationDB observationDB) {
        List<String> existedValuesInSurvey = new ArrayList<>();

        for (ObservationValueDB observationValueDB : observationDB.getValuesDB()) {
            existedValuesInSurvey.add(observationValueDB.getUid_observation_value());
        }

        new Delete().from(ObservationValueDB.class)
                .where(ObservationValueDB_Table.id_observation_fk.is(
                        observationDB.getId_observation()))
                .and(ObservationValueDB_Table.uid_observation_value.notIn(
                        existedValuesInSurvey)).execute();
    }

    @NotNull
    private List<SurveyDB> getSurveysByObservationsDB(List<ObservationDB> observationDBS) {
        List<Long> surveyIds = new ArrayList<>();

        for (ObservationDB observationDB : observationDBS) {
            surveyIds.add(observationDB.getId_survey_observation_fk());
        }

        return new Select(SurveyDB_Table.id_survey, SurveyDB_Table.uid_event_fk)
                .from(SurveyDB.class)
                .leftOuterJoin(ObservationDB.class)
                .on(SurveyDB_Table.id_survey.eq(ObservationDB_Table.id_survey_observation_fk))
                .where(ObservationDB_Table.id_survey_observation_fk.in(surveyIds))
                .queryList();
    }
}
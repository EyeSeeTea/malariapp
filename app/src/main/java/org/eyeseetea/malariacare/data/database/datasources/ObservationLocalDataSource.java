package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.eyeseetea.malariacare.data.boundaries.IObservationDataSource;
import org.eyeseetea.malariacare.data.boundaries.ISyncDataLocalDataSource;
import org.eyeseetea.malariacare.data.database.mapper.ObservationMapper;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationDB_Table;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB_Table;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB_Table;
import org.eyeseetea.malariacare.domain.entity.ISyncData;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.exception.ObservationNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationLocalDataSource implements IObservationDataSource, ISyncDataLocalDataSource {
    private final ObservationMapper mObservationMapper;

    public ObservationLocalDataSource(){
        List<SurveyDB> surveyDBS = new Select().from(SurveyDB.class).queryList();

        mObservationMapper = new ObservationMapper(surveyDBS);
    }

    @Override
    public List<? extends ISyncData> getDataToSync() throws Exception {
        List<Observation> observations = getObservations(ObservationStatus.COMPLETED);
        return observations;
    }

    @Override
    public List<? extends ISyncData> getAll() {
        List<Observation> observations = getObservations(null);
        return observations;
    }

    @Override
    public void save(List<? extends ISyncData> syncData) throws Exception {
        List<Observation> observations = (List<Observation>) syncData;
        saveObservations(observations);
    }

    @Override
    public void save(ISyncData syncData){
        Observation observation = (Observation) syncData;

        saveObservation(observation);
    }

    public List<Observation> getObservations(ObservationStatus status) {
        List<ObservationDB> observationDBS = getObservationsDB(status);

        List<Observation> observations = mObservationMapper.map(observationDBS);

        return observations;
    }

    @Override
    public Observation getObservation(String surveyUId) throws Exception {
        ObservationDB observationDB = getObservationDB(surveyUId);

        if (observationDB != null) {

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

    @Override
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



    private List<ObservationDB> getObservationsDB(ObservationStatus observationStatus){

        List<ObservationDB> observationDBS = null;

        From from = new Select().from(ObservationDB.class).leftOuterJoin(SurveyDB.class)
                .on(SurveyDB_Table.id_survey.eq(ObservationDB_Table.id_survey_observation_fk));

        Where where = from.where(ObservationDB_Table.status_observation.isNotNull());

        if (observationStatus == ObservationStatus.COMPLETED){
            where = from.where(SurveyDB_Table.status.eq(SurveyStatus.SENT.getCode()))
                    .and(ObservationDB_Table.status_observation.eq(
                            ObservationStatus.COMPLETED.getCode()));
        }

        observationDBS = where.queryList();

        if (observationDBS.size() > 0)
            loadValuesInObservation(observationDBS);

        return observationDBS;
    }

    private void loadValuesInObservation(List<ObservationDB> observationDBS) {
        List<ObservationValueDB> allValues =
                new Select().from(ObservationValueDB.class).queryList();

        Map<Long, List<ObservationValueDB>> valuesMap = new HashMap<>();
        for (ObservationValueDB observationValueDB : allValues) {
            if (!valuesMap.containsKey(observationValueDB.getId_observation_fk()))
                valuesMap.put(observationValueDB.getId_observation_fk(),
                        new ArrayList<ObservationValueDB>());

            valuesMap.get(observationValueDB.getId_observation_fk()).add(observationValueDB);
        }

        for (ObservationDB observationDB : observationDBS) {
            if (valuesMap.containsKey(observationDB.getId_observation())){
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

        ObservationDB observationDB = new ObservationDB();
        observationDB.setId_survey_observation_fk(surveyDB.getId_survey());
        observationDB.setStatus_observation(observation.getStatus().getCode());
        observationDB.save();


        for (ObservationValue observationValue : observation.getValues()) {
            ObservationValueDB observationValueDB = new ObservationValueDB();
            observationValueDB.setId_observation_fk(observationDB.getId_observation());
            observationValueDB.setValue(observationValue.getValue());
            observationValueDB.setUid_observation_value(observationValue.getObservationValueUid());

            observationValueDB.save();
        }
    }

    private void modify(ObservationDB observationDB, Observation observation) {
        observationDB.setStatus_observation(observation.getStatus().getCode());
        observationDB.save();

        for (ObservationValueDB observationValueDB : observationDB.getValuesDB()) {
            ObservationValue observationValue =
                    getObservationValue(observationValueDB.getUid_observation_value(), observation);

            if (observationValue == null) {
                observationValueDB.delete();
            }
        }

        for (ObservationValue observationValue : observation.getValues()) {
            ObservationValueDB observationValueDB =
                    getObservationValueDB(observationValue.getObservationValueUid(), observationDB);

            if (observationValueDB == null) {
                observationValueDB = new ObservationValueDB();
            }

            observationValueDB.setId_observation_fk(observationDB.getId_observation());
            observationValueDB.setValue(observationValue.getValue());
            observationValueDB.setUid_observation_value(observationValue.getObservationValueUid());

            observationValueDB.save();
        }
    }

    private ObservationValueDB getObservationValueDB(String observationValueUid,
            ObservationDB observationDB) {

        ObservationValueDB existedObservationValueDB = null;

        for (ObservationValueDB observationValueDB : observationDB.getValuesDB()) {
            if (observationValueDB.getUid_observation_value().equals(observationValueUid)) {
                existedObservationValueDB = observationValueDB;
            }
        }

        return existedObservationValueDB;
    }

    private ObservationValue getObservationValue(String observationValueUid,
            Observation observation) {

        ObservationValue existedObservationValue = null;

        for (ObservationValue observationValue : observation.getValues()) {
            if (observationValue.getObservationValueUid().equals(observationValueUid)) {
                existedObservationValue = observationValue;
            }
        }

        return existedObservationValue;
    }
}

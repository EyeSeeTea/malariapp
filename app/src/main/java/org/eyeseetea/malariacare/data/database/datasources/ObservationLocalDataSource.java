package org.eyeseetea.malariacare.data.database.datasources;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.eyeseetea.malariacare.data.boundaries.IObservationDataSource;
import org.eyeseetea.malariacare.data.database.mapper.SurveyMapper;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationDB_Table;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB_Table;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB_Table;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.exception.ObservationNotFoundException;
import org.eyeseetea.malariacare.domain.usecase.pull.SurveyFilter;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationLocalDataSource implements IObservationDataSource {

    @Override
    public Observation getObservation(String surveyUId) throws Exception {
        ObservationDB observationDB = getObservationDB(surveyUId);

        if (observationDB != null) {

            Observation observation = map(surveyUId, observationDB);

            return observation;
        } else {
            throw new ObservationNotFoundException();
        }

    }

    @Override
    public List<Observation> getObservations(ObservationsToRetrieve observationsToRetrieve) {
        return null;
    }

    @Override
    public void save(Observation observation) {
        ObservationDB observationDB = getObservationDB(observation.getSurveyUid());

        if (observationDB == null) {
            add(observation);
        } else {
            modify(observationDB, observation);
        }

    }

    @Override
    public void save(List<Observation> observations) {
        for (Observation observation : observations) {
            save(observation);
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

    private Observation map(String surveyUId, ObservationDB observationDB) {
        List<ObservationValue> observationValues = new ArrayList<>();

        for (ObservationValueDB observationValueDB : observationDB.getValuesDB()) {
            observationValues.add(
                    new ObservationValue(observationValueDB.getValue(),
                            observationValueDB.getUid_observation_value()));
        }

        Observation observation =
                Observation.createStoredObservation(surveyUId,
                        ObservationStatus.get(observationDB.getStatus_observation()),
                        observationValues);

        return observation;
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

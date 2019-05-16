package org.eyeseetea.malariacare.data.database.mapper;

import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;

import java.util.ArrayList;
import java.util.List;

public class ObservationDBMapper {
    public ObservationDB mapToAdd(Observation observation, Long relatedSurveyId) {
        ObservationDB observationDB = new ObservationDB();
        observationDB.setId_survey_observation_fk(relatedSurveyId);
        observationDB.setStatus_observation(observation.getStatus().getCode());

        for (ObservationValue observationValue : observation.getValues()) {
            ObservationValueDB observationValueDB = new ObservationValueDB();
            observationValueDB.setId_observation_fk(observationDB.getId_observation());
            observationValueDB.setValue(observationValue.getValue());
            observationValueDB.setUid_observation_value(observationValue.getObservationValueUid());
        }

        return observationDB;
    }

    public ObservationDB mapToModify(ObservationDB observationDB, Observation observation){
        List<ObservationValueDB> observationValuesCopy = new ArrayList<>(observationDB.getValuesDB());

        observationDB.setStatus_observation(observation.getStatus().getCode());


        for (ObservationValueDB observationValueDB : observationValuesCopy) {
            ObservationValue observationValue =
                    getObservationValue(observationValueDB.getUid_observation_value(), observation);

            if (observationValue == null) {
                observationDB.getValuesDB().remove(observationValueDB);
            }
        }

        for (ObservationValue observationValue : observation.getValues()) {
            ObservationValueDB observationValueDB =
                    getObservationValueDB(observationValue.getObservationValueUid(), observationDB);

            if (observationValueDB == null) {
                observationValueDB = new ObservationValueDB();
                observationDB.getValuesDB().add(observationValueDB);
            }

            observationValueDB.setId_observation_fk(observationDB.getId_observation());
            observationValueDB.setValue(observationValue.getValue());
            observationValueDB.setUid_observation_value(observationValue.getObservationValueUid());
        }

        return observationDB;
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
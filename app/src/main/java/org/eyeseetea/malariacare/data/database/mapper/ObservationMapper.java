package org.eyeseetea.malariacare.data.database.mapper;

import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.ObservationValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationMapper {
    private Map<Long, SurveyDB> surveysDBMap;

    public ObservationMapper(List<SurveyDB> surveyDBS) {

        createMaps(surveyDBS);
    }

    public List<Observation> map(List<ObservationDB> observationDBs) {

        List<Observation> observations = new ArrayList<>();

        for (ObservationDB observationDB : observationDBs) {
            try {
                Observation observation = map(observationDB);
                observations.add(observation);
            } catch (Exception e) {
                System.out.println(
                        "An error occurred converting ObservationDB " + observationDB.getId_observation() +
                                " to domain survey:" + e.getMessage());
            }
        }

        return observations;
    }

    public Observation map(ObservationDB observationDB) {
        String surveyUId = null;

        if (surveysDBMap.containsKey(observationDB.getId_survey_observation_fk()))
            surveyUId = surveysDBMap.get(observationDB.getId_survey_observation_fk()).getEventUid();

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

    private void createMaps(List<SurveyDB> surveyDBS) {
        surveysDBMap = new HashMap<>();
        for (SurveyDB surveyDB : surveyDBS) {
            surveysDBMap.put(surveyDB.getId_survey(), surveyDB);
        }
    }
}
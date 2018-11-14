package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

import java.util.List;

public interface IObservationDataSource {
    Observation getObservation(String surveyUId) throws Exception;
    void save(Observation observation);

    List<Observation> getObservations(ObservationStatus observationStatus);
    void save(List<Observation> observations);
}

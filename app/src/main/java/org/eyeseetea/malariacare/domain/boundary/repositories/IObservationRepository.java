package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

import java.util.List;

public interface IObservationRepository {

    Observation getObservation(String surveyId) throws Exception;
    void save(Observation observation) throws Exception;

    List<Observation> getObservations(ObservationStatus observationStatus) throws Exception;
    void save(List<Observation> observations) throws Exception;
}

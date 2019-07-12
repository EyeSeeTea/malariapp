package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Observation;

public interface IObservationRepository {
    Observation getObservation(String surveyId) throws Exception;
    void save(Observation observation);
}
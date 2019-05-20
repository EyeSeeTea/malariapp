package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

import java.util.List;

public interface IObservationRepository {
    Observation getObservation(String surveyId) throws Exception;
    List<Observation> getObservations(
            String programUid,
            String orgUnitUid,
            List<ObservationStatus> observationStatuses) throws Exception;
    void save(Observation observation);
}
package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;

import java.util.Optional;

public interface IObservationRepository {
    Observation getObservation(String surveyId) throws Exception;
    void save(Observation observation);
}

package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Observation;

import java.util.List;

public interface IObservationDataSource {
    enum ObservationsToRetrieve {ALL, COMPLETED}

    Observation getObservation(String surveyUId) throws Exception;
    List<Observation> getObservations(ObservationsToRetrieve observationsToRetrieve);
    void save(Observation observation);
    void save(List<Observation> observations) throws Exception;
}

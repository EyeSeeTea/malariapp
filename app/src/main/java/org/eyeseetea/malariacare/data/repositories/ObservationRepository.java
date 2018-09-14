package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IObservationDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.entity.Observation;

public class ObservationRepository implements IObservationRepository {

    private final IObservationDataSource localDataSource;

    public ObservationRepository(
            IObservationDataSource localDataSource) {
        this.localDataSource = localDataSource;
    }

    @Override
    public Observation getObservation(String surveyId) throws Exception {
        return localDataSource.getObservation(surveyId);
    }

    @Override
    public void save(Observation observation) {
        localDataSource.save(observation);
    }
}

package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.entity.Observation;

public class ObservationRepository implements IObservationRepository {

    private final ObservationLocalDataSource localDataSource;

    public ObservationRepository(
            ObservationLocalDataSource localDataSource) {
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

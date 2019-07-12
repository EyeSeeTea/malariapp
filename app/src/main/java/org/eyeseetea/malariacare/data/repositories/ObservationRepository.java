package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.database.datasources.ObservationLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IObservationRepository;
import org.eyeseetea.malariacare.domain.entity.Observation;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;

import java.util.List;

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
    public List<Observation> getObservations(
            String programUid,
            String orgUnitUid,
            List<ObservationStatus> observationStatuses) throws Exception {
        return localDataSource.getSentObservations(programUid, orgUnitUid, observationStatuses);
    }

    @Override
    public void save(Observation observation) {
        localDataSource.save(observation);
    }
}

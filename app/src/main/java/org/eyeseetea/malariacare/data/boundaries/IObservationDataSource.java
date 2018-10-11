package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Observation;

import java.util.List;

public interface IObservationDataSource {
    Observation getObservation(String surveyUId) throws Exception;
    void save(Observation observation);
}

package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.List;

public interface ISurveyDataSource{
    List<Survey> getSurveysByStatus(SurveyStatus surveyStatus) throws Exception;

    List<Survey> getSurveysByObservationStatus(ObservationStatus observationStatus)
            throws Exception;
    void save(List<Survey> surveys) throws Exception;
}
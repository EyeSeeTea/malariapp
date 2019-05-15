package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyRepository;
import org.eyeseetea.malariacare.domain.entity.ObservationStatus;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.List;

public class GetSurveysUseCase {
    private ISurveyRepository surveyRepository;

    public GetSurveysUseCase(ISurveyRepository surveyRepository){
        this.surveyRepository = surveyRepository;
    }

    List<Survey> execute(SurveyStatus surveyStatus) throws Exception {
        return surveyRepository.getSurveysByStatus(surveyStatus);
    }

    List<Survey> execute(ObservationStatus observationStatus) throws Exception {
        return surveyRepository.getSurveysByObservationStatus(observationStatus);
    }
}

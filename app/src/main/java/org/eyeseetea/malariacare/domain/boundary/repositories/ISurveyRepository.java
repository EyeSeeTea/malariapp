package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;
import org.eyeseetea.malariacare.domain.usecase.LocalSurveyFilter;

import java.util.List;

public interface ISurveyRepository {
    List<Survey> getSurveys(LocalSurveyFilter surveyStatus) throws Exception;
    void save(List<Survey> surveys) throws Exception;
}

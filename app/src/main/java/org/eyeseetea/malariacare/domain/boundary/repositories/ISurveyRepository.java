package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.entity.SurveyFilter;
import org.eyeseetea.malariacare.domain.entity.SurveyStatus;

import java.util.List;

public interface ISurveyRepository {
    List<Survey> getSurveys(SurveyFilter surveyStatus) throws Exception;
    void save(List<Survey> surveys) throws Exception;
}

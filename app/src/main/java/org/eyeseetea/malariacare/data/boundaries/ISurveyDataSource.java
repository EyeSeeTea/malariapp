package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.domain.usecase.SurveyFilter;

import java.util.List;

public interface ISurveyDataSource{
    List<Survey> getSurveys(SurveyFilter surveyStatus) throws Exception;
    void save(List<Survey> surveys) throws Exception;
}

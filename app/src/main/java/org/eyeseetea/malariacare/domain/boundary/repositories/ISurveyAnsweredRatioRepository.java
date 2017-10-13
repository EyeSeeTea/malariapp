package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public interface ISurveyAnsweredRatioRepository {
    void saveSurveyAnsweredRatio(SurveyAnsweredRatio surveyAnsweredRatio);

    SurveyAnsweredRatio getSurveyAnsweredRatioBySurveyId(long id_survey);
}

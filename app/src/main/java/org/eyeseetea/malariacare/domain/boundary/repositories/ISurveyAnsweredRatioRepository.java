package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.domain.usecase.ISurveyAnsweredRatioCallback;

public interface ISurveyAnsweredRatioRepository {
    void saveSurveyAnsweredRatio(SurveyAnsweredRatio surveyAnsweredRatio);

    SurveyAnsweredRatio getSurveyAnsweredRatioBySurveyId(long id_survey);
    SurveyAnsweredRatio getSurveyAnsweredRatioBySurveyUId(String surveyUId);

    SurveyAnsweredRatio loadSurveyAnsweredRatio(ISurveyAnsweredRatioCallback callback, SurveyDB surveyDb);
}

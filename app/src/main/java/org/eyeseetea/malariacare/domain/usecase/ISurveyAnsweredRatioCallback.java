package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public interface ISurveyAnsweredRatioCallback {
    void nextProgressMessage();
    void onComplete(SurveyAnsweredRatio surveyAnsweredRatio);
}

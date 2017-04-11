package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class GetSurveyAnsweredRatioUseCase {
    public interface Callback{
        void nextProgressMessage();
        void onComplete(SurveyAnsweredRatio surveyAnsweredRatio);
    }

    public enum RecoveryFrom {DATABASE, MEMORY_FIRST }

    public GetSurveyAnsweredRatioUseCase() {
    }

    SurveyAnsweredRatio mSurveyAnsweredRatio;
    RecoveryFrom mRecoveryFrom;

    org.eyeseetea.malariacare.data.database.model.Survey surveyDB;

    public void execute(long idSurvey, RecoveryFrom recoveryFrom, GetSurveyAnsweredRatioUseCase.Callback callback) {
        this.mRecoveryFrom = recoveryFrom;
        SurveyAnsweredRatio surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatio getSurveyWithStatusAndAnsweredRatio(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        surveyDB = org.eyeseetea.malariacare.data.database.model.Survey.findById(idSurvey);
        if(mRecoveryFrom.equals(RecoveryFrom.DATABASE)) {
            mSurveyAnsweredRatio = surveyDB.reloadSurveyAnsweredRatio(callback);
        }else if(mRecoveryFrom.equals(RecoveryFrom.MEMORY_FIRST)){
            mSurveyAnsweredRatio = surveyDB.getAnsweredQuestionRatio(callback);
        }
        return mSurveyAnsweredRatio;
    }
}

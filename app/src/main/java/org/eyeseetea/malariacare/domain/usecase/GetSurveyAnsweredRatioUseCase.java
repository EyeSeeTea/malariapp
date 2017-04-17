package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.data.database.model.Value;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatioEntity;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatioCache;

public class GetSurveyAnsweredRatioUseCase {
    public interface Callback{
        void nextProgressMessage();
        void onComplete(SurveyAnsweredRatioEntity surveyAnsweredRatio);
    }

    public enum RecoveryFrom {DATABASE, MEMORY_FIRST }

    /**
     * Calculated answered ratio for this survey according to its values
     */
    SurveyAnsweredRatioEntity answeredQuestionRatio;

    public GetSurveyAnsweredRatioUseCase() {
    }

    SurveyAnsweredRatioEntity mSurveyAnsweredRatio;
    RecoveryFrom mRecoveryFrom;

    org.eyeseetea.malariacare.data.database.model.Survey surveyDB;

    public void execute(long idSurvey, RecoveryFrom recoveryFrom, GetSurveyAnsweredRatioUseCase.Callback callback) {
        this.mRecoveryFrom = recoveryFrom;
        SurveyAnsweredRatioEntity surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatioEntity getSurveyWithStatusAndAnsweredRatio(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        surveyDB = org.eyeseetea.malariacare.data.database.model.Survey.findById(idSurvey);
        if(mRecoveryFrom.equals(RecoveryFrom.DATABASE)) {
            mSurveyAnsweredRatio = reloadSurveyAnsweredRatio(callback);
        }else if(mRecoveryFrom.equals(RecoveryFrom.MEMORY_FIRST)){
            mSurveyAnsweredRatio = getAnsweredQuestionRatio(callback);
        }
        return mSurveyAnsweredRatio;
    }


    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     */
    public SurveyAnsweredRatioEntity getAnsweredQuestionRatio(GetSurveyAnsweredRatioUseCase.Callback callback) {
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = SurveyAnsweredRatioCache.get(surveyDB.getId_survey());
            if (answeredQuestionRatio == null) {
                answeredQuestionRatio = reloadSurveyAnsweredRatio(callback);
            }
        }
        return answeredQuestionRatio;
    }

    /**
     * Calculates the current ratio of completion for this survey
     *
     * @return SurveyAnsweredRatioEntity that hold the total & answered questions.
     */
    public SurveyAnsweredRatioEntity reloadSurveyAnsweredRatio(GetSurveyAnsweredRatioUseCase.Callback callback) {
        //TODO Review
        SurveyAnsweredRatioEntity surveyAnsweredRatioEntity=null;
        Program surveyProgram = surveyDB.getProgram();
        int numRequired = Question.countRequiredByProgram(surveyProgram);
        int numCompulsory = Question.countCompulsoryByProgram(surveyProgram);
        int numOptional = (int) surveyDB.countNumOptionalQuestionsToAnswer();
        if(callback!=null) {
            callback.nextProgressMessage();
        }
        int numActiveChildrenCompulsory = Question.countChildrenCompulsoryBySurvey(
                surveyDB.getId_survey(), callback);
        int numAnswered = Value.countBySurvey(surveyDB);
        int numCompulsoryAnswered = Value.countCompulsoryBySurvey(surveyDB);
        surveyAnsweredRatioEntity = new SurveyAnsweredRatioEntity(surveyDB.getId_survey(),
                numRequired + numOptional,
                numAnswered, numCompulsory + numActiveChildrenCompulsory,
                numCompulsoryAnswered);
        SurveyAnsweredRatioCache.put(surveyDB.getId_survey(), surveyAnsweredRatioEntity);
        SurveyAnsweredRatio.saveEntityToModel(surveyAnsweredRatioEntity);
        return surveyAnsweredRatioEntity;
    }
}

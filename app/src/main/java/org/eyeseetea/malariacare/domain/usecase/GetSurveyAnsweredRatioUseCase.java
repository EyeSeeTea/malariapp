package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatioEntity;

public class GetSurveyAnsweredRatioUseCase {
    public interface Callback{
        void nextProgressMessage();
        void onComplete(SurveyAnsweredRatioEntity surveyAnsweredRatio);
    }

    public enum Action {FORCE_UPDATE, GET}

    /**
     * Calculated answered ratio for this survey according to its values
     */
    SurveyAnsweredRatioEntity answeredQuestionRatio;

    public GetSurveyAnsweredRatioUseCase() {
    }

    SurveyAnsweredRatioEntity mSurveyAnsweredRatio;
    Action mAction;

    SurveyDB surveyDB;

    public void execute(long idSurvey, Action action, GetSurveyAnsweredRatioUseCase.Callback callback) {
        this.mAction = action;
        SurveyAnsweredRatioEntity surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatioEntity getSurveyWithStatusAndAnsweredRatio(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        surveyDB = SurveyDB.findById(idSurvey);
        if(mAction.equals(Action.FORCE_UPDATE)) {
            mSurveyAnsweredRatio = reloadSurveyAnsweredRatio(callback);
        }else if(mAction.equals(Action.GET)){
            mSurveyAnsweredRatio = getAnsweredQuestionRatio(idSurvey, callback);
        }
        return mSurveyAnsweredRatio;
    }


    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     */
    public SurveyAnsweredRatioEntity getAnsweredQuestionRatio(Long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = SurveyAnsweredRatioEntity.getModelToEntity(idSurvey);
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
        ProgramDB surveyProgram = surveyDB.getProgram();
        int numRequired = QuestionDB.countRequiredByProgram(surveyProgram);
        int numCompulsory = QuestionDB.countCompulsoryByProgram(surveyProgram);
        int numOptional = (int) surveyDB.countNumOptionalQuestionsToAnswer();
        if(callback!=null) {
            callback.nextProgressMessage();
        }
        int numActiveChildrenCompulsory = QuestionDB.countChildrenCompulsoryBySurvey(
                surveyDB.getId_survey(), callback);
        int numAnswered = ValueDB.countBySurvey(surveyDB);
        int numCompulsoryAnswered = ValueDB.countCompulsoryBySurvey(surveyDB);
        surveyAnsweredRatioEntity = new SurveyAnsweredRatioEntity(surveyDB.getId_survey(),
                numRequired + numOptional,
                numAnswered, numCompulsory + numActiveChildrenCompulsory,
                numCompulsoryAnswered);
        SurveyAnsweredRatio.saveEntityToModel(surveyAnsweredRatioEntity);
        return surveyAnsweredRatioEntity;
    }
}

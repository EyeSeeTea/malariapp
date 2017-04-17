package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatio;
import org.eyeseetea.malariacare.data.database.model.Value;
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

    org.eyeseetea.malariacare.data.database.model.Survey surveyDB;

    public void execute(long idSurvey, Action action, GetSurveyAnsweredRatioUseCase.Callback callback) {
        this.mAction = action;
        SurveyAnsweredRatioEntity surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatioEntity getSurveyWithStatusAndAnsweredRatio(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        surveyDB = org.eyeseetea.malariacare.data.database.model.Survey.findById(idSurvey);
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
        SurveyAnsweredRatio.saveEntityToModel(surveyAnsweredRatioEntity);
        return surveyAnsweredRatioEntity;
    }
}

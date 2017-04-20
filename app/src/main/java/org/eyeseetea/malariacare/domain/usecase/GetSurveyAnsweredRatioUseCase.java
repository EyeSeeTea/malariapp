package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class GetSurveyAnsweredRatioUseCase {
    public interface Callback{
        void nextProgressMessage();
        void onComplete(SurveyAnsweredRatio surveyAnsweredRatio);
    }

    public enum Action {FORCE_UPDATE, GET}

    /**
     * Calculated answered ratio for this survey according to its values
     */
    SurveyAnsweredRatio answeredQuestionRatio;

    public GetSurveyAnsweredRatioUseCase() {
    }

    SurveyAnsweredRatio mSurveyAnsweredRatio;
    Action mAction;

    SurveyDB surveyDB;

    public void execute(long idSurvey, Action action, GetSurveyAnsweredRatioUseCase.Callback callback) {
        this.mAction = action;
        SurveyAnsweredRatio surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatio getSurveyWithStatusAndAnsweredRatio(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
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
    public SurveyAnsweredRatio getAnsweredQuestionRatio(Long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = SurveyAnsweredRatio.getModelToEntity(idSurvey);
            if (answeredQuestionRatio == null) {
                answeredQuestionRatio = reloadSurveyAnsweredRatio(callback);
            }
        }
        return answeredQuestionRatio;
    }

    /**
     * Calculates the current ratio of completion for this survey
     *
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    public SurveyAnsweredRatio reloadSurveyAnsweredRatio(GetSurveyAnsweredRatioUseCase.Callback callback) {
        //TODO Review
        SurveyAnsweredRatio surveyAnsweredRatio =null;
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
        surveyAnsweredRatio = new SurveyAnsweredRatio(surveyDB.getId_survey(),
                numRequired + numOptional,
                numAnswered, numCompulsory + numActiveChildrenCompulsory,
                numCompulsoryAnswered);
        SurveyAnsweredRatioDB.saveEntityToModel(surveyAnsweredRatio);
        return surveyAnsweredRatio;
    }
}

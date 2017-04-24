package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyAnsweredRatioDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.entity.Question;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

import java.util.List;

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
    GetSurveyAnsweredRatioUseCase.Callback callback;
    SurveyDB surveyDB;

    public void execute(long idSurvey, Action action, GetSurveyAnsweredRatioUseCase.Callback callback) {
        this.mAction = action;
        this.callback = callback;
        SurveyAnsweredRatio surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatio getSurveyWithStatusAndAnsweredRatio(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        surveyDB = SurveyDB.findById(idSurvey);
        if(mAction.equals(Action.FORCE_UPDATE)) {
            mSurveyAnsweredRatio = reloadSurveyAnsweredRatio();
        }else if(mAction.equals(Action.GET)){
            mSurveyAnsweredRatio = getAnsweredQuestionRatio(idSurvey);
        }
        return mSurveyAnsweredRatio;
    }


    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(Long idSurvey) {
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = SurveyAnsweredRatio.getModelToEntity(idSurvey);
            if (answeredQuestionRatio == null) {
                answeredQuestionRatio = reloadSurveyAnsweredRatio();
            }
        }
        return answeredQuestionRatio;
    }

    /**
     * Calculates the current ratio of completion for this survey
     *
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    public SurveyAnsweredRatio reloadSurveyAnsweredRatio() {
        //TODO Review
        SurveyAnsweredRatio surveyAnsweredRatio =null;
        ProgramDB surveyProgram = surveyDB.getProgram();
        int numRequired = QuestionDB.countRequiredByProgram(surveyProgram);
        int numCompulsory = QuestionDB.countCompulsoryByProgram(surveyProgram);
        int numOptional = (int) surveyDB.countNumOptionalQuestionsToAnswer();
        if(callback!=null) {
            callback.nextProgressMessage();
        }

        List<QuestionDB> questionDBList =  QuestionDB.getChildrenCompulsoryBySurvey(
                surveyDB.getId_survey());
        int numActiveChildrenCompulsory = calculateCompulsoryChild(QuestionDB.convertListFromModelToEntity(questionDBList),
                surveyDB.getId_survey());
        int numAnswered = ValueDB.countBySurvey(surveyDB);
        int numCompulsoryAnswered = ValueDB.countCompulsoryBySurvey(surveyDB);
        surveyAnsweredRatio = new SurveyAnsweredRatio(surveyDB.getId_survey(),
                numRequired + numOptional,
                numAnswered, numCompulsory + numActiveChildrenCompulsory,
                numCompulsoryAnswered);
        SurveyAnsweredRatioDB.saveEntityToModel(surveyAnsweredRatio);
        return surveyAnsweredRatio;
    }

    public int calculateCompulsoryChild(List<Question> questions, long idSurvey){
        int numActiveChildren=0;

        //checks if the children questions are active by UID
        // Note: the question id_question is wrong because dbflow query overwrites the children id_question with the parent id_question.
        for(int i=0; i<questions.size();i++) {
            if(questions.get(i).isCompulsory() && !QuestionDB.isHiddenQuestionByUidAndSurvey(questions.get(i).getUId(), idSurvey)) {
                numActiveChildren++;
            }
            if(i == (Math.round((Float.parseFloat(questions.size()+"")*0.25)))
                    || i == (Math.round((Float.parseFloat(questions.size()+"")*0.5)))
                    || i == (Math.round((Float.parseFloat(questions.size()+"")*0.75)))){
                if (callback != null) {
                    callback.nextProgressMessage();
                }
            }
        }
        // Return number of active compulsory children
        return numActiveChildren;
    }
}

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class GetSurveyAnsweredRatioUseCase{

    public interface Callback{
        void nextProgressMessage();
        void onComplete(SurveyAnsweredRatio surveyAnsweredRatio);
    }

    private ISurveyAnsweredRatioRepository mSurveyAnsweredRatioRepository;

    private SurveyAnsweredRatio answeredQuestionRatio;

    public GetSurveyAnsweredRatioUseCase(
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository) {
        mSurveyAnsweredRatioRepository = surveyAnsweredRatioRepository;
    }

    SurveyAnsweredRatio mSurveyAnsweredRatio;

    SurveyDB surveyDB;

    public void execute(long idSurvey, Callback callback, boolean forceUpdate) {
        final SurveyAnsweredRatio surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback, forceUpdate);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatio getSurveyWithStatusAndAnsweredRatio(long idSurvey,
            GetSurveyAnsweredRatioUseCase.Callback callback, boolean forceUpdate) {
        surveyDB = SurveyDB.findById(idSurvey);
            mSurveyAnsweredRatio = getAnsweredQuestionRatio(idSurvey, callback, forceUpdate);
        return mSurveyAnsweredRatio;
    }

    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(Long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback, boolean forceUpdate) {
        if(!forceUpdate) {
            answeredQuestionRatio = mSurveyAnsweredRatioRepository.getSurveyAnsweredRatioBySurveyId(
                    idSurvey);
        }
        if (answeredQuestionRatio == null) {
            answeredQuestionRatio = reloadSurveyAnsweredRatio(callback);
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
        mSurveyAnsweredRatioRepository.saveSurveyAnsweredRatio(surveyAnsweredRatio);
        return surveyAnsweredRatio;
    }
}
package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class GetSurveyAnsweredRatioUseCase{

    private ISurveyAnsweredRatioRepository mSurveyAnsweredRatioRepository;

    private SurveyAnsweredRatio answeredQuestionRatio;

    public GetSurveyAnsweredRatioUseCase(
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository) {
        mSurveyAnsweredRatioRepository = surveyAnsweredRatioRepository;
    }

    SurveyAnsweredRatio mSurveyAnsweredRatio;

    SurveyDB surveyDB;

    public void execute(long idSurvey, ISurveyAnsweredRatioCallback callback) {
        final SurveyAnsweredRatio surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(idSurvey, callback);
        callback.onComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatio getSurveyWithStatusAndAnsweredRatio(long idSurvey,
            ISurveyAnsweredRatioCallback callback) {
        surveyDB = SurveyDB.findById(idSurvey);
        mSurveyAnsweredRatio = getAnsweredQuestionRatio(idSurvey, callback);
        return mSurveyAnsweredRatio;
    }

    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     */
    public SurveyAnsweredRatio getAnsweredQuestionRatio(Long idSurvey, ISurveyAnsweredRatioCallback callback) {
        answeredQuestionRatio = mSurveyAnsweredRatioRepository.getSurveyAnsweredRatioBySurveyId(idSurvey);
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
    public SurveyAnsweredRatio reloadSurveyAnsweredRatio(ISurveyAnsweredRatioCallback callback) {
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



    public void fixTotalQuestion(QuestionDB question, boolean visible) {
        if(question.getCompulsory()){
            if(visible) {
                mSurveyAnsweredRatio.incrementTotalCompulsory();
            }else{
                mSurveyAnsweredRatio.decrementTotalCompulsory();
            }
        }else{
            if(visible) {
                mSurveyAnsweredRatio.incrementTotal();
            }else{
                mSurveyAnsweredRatio.decrementTotal();
            }
        }
        save();
    }

    public void removeQuestion(QuestionDB question) {
        if(question.getCompulsory()){
            mSurveyAnsweredRatio.decrementCompulsoryAnswered();
        }else{
            mSurveyAnsweredRatio.decrementAnswered();
        }
        save();
    }


    public void addQuestion(QuestionDB question) {
        if(question.getCompulsory()){
            mSurveyAnsweredRatio.incrementCompulsoryAnswered();
        }else{
            mSurveyAnsweredRatio.incrementAnswered();
        }
        save();
    }

    public void save() {
        SaveSurveyAnsweredRatioUseCase saveSurveyAnsweredRatioUseCase = new SaveSurveyAnsweredRatioUseCase(mSurveyAnsweredRatioRepository);
        saveSurveyAnsweredRatioUseCase.execute(
                new ISurveyAnsweredRatioCallback() {
                    @Override
                    public void nextProgressMessage() {

                    }

                    @Override
                    public void onComplete(SurveyAnsweredRatio surveyAnsweredRatio) {

                    }
                }, mSurveyAnsweredRatio);
    }
}
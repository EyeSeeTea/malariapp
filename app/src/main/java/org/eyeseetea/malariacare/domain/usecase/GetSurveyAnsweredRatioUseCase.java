package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class GetSurveyAnsweredRatioUseCase implements UseCase{

    private ISurveyAnsweredRatioRepository mSurveyAnsweredRatioRepository;

    private SurveyAnsweredRatio answeredQuestionRatio;

    private IMainExecutor mMainExecutor;
    private IAsyncExecutor mAsyncExecutor;
    private ISurveyAnsweredRatioCallback mCallback;
    private long idSurvey;
    private String surveyUid;

    public GetSurveyAnsweredRatioUseCase(
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mSurveyAnsweredRatioRepository = surveyAnsweredRatioRepository;
    }

    public SurveyAnsweredRatio mSurveyAnsweredRatio;

    SurveyDB surveyDB;

    public void execute(long idSurvey, ISurveyAnsweredRatioCallback callback) {
        this.idSurvey=idSurvey;
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    public void execute(String surveyUId, ISurveyAnsweredRatioCallback callback) {
        this.surveyUid=surveyUId;
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        SurveyAnsweredRatio surveyAnsweredRatio = getSurveyWithStatusAndAnsweredRatio(mCallback);
        onGetSurveyComplete(surveyAnsweredRatio);
    }

    private SurveyAnsweredRatio getSurveyWithStatusAndAnsweredRatio(
            ISurveyAnsweredRatioCallback callback) {
        surveyDB = SurveyDB.findById(idSurvey);
        mSurveyAnsweredRatio = getAnsweredQuestionRatio( callback);
        return mSurveyAnsweredRatio;
    }

    /**
     * Ratio of completion is cached into answeredQuestionRatio in order to speed up loading
     */
    private SurveyAnsweredRatio getAnsweredQuestionRatio( ISurveyAnsweredRatioCallback callback) {

        if (surveyUid != null){
            answeredQuestionRatio = mSurveyAnsweredRatioRepository.getSurveyAnsweredRatioBySurveyUId(surveyUid);
        } else {
            answeredQuestionRatio = mSurveyAnsweredRatioRepository.getSurveyAnsweredRatioBySurveyId(idSurvey);
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
    private SurveyAnsweredRatio reloadSurveyAnsweredRatio(ISurveyAnsweredRatioCallback callback) {
        return mSurveyAnsweredRatioRepository.loadSurveyAnsweredRatio(callback, surveyDB);
    }


    private void onGetSurveyComplete(final SurveyAnsweredRatio surveyAnsweredRatio) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete(surveyAnsweredRatio);
            }
        });
    }


    private void notifyProgressMessage() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.nextProgressMessage();
            }
        });
    }

}
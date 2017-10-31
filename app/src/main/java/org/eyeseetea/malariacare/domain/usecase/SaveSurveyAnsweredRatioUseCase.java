package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class SaveSurveyAnsweredRatioUseCase implements UseCase{
    private ISurveyAnsweredRatioRepository mSurveyAnsweredRatioRepository;
    private IMainExecutor mMainExecutor;
    private IAsyncExecutor mAsyncExecutor;
    private ISurveyAnsweredRatioCallback mCallback;
    private SurveyAnsweredRatio mSurveyAnsweredRatio;

    public SaveSurveyAnsweredRatioUseCase(
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mSurveyAnsweredRatioRepository = surveyAnsweredRatioRepository;
    }

    public void execute(final long idSurvey, final ISurveyAnsweredRatioCallback callback) {
        mAsyncExecutor.run(new Runnable() {
            @Override
            public void run() {
                mSurveyAnsweredRatio = reloadSurveyAnsweredRatio(idSurvey, callback);
                execute(callback, mSurveyAnsweredRatio);
            }
        });
    }

    public void execute(ISurveyAnsweredRatioCallback callback, SurveyAnsweredRatio surveyAnsweredRatio) {
        mSurveyAnsweredRatio = surveyAnsweredRatio;
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    private void save(SurveyAnsweredRatio surveyAnsweredRatio) {
        mSurveyAnsweredRatioRepository.saveSurveyAnsweredRatio(surveyAnsweredRatio);
    }

    /**
     * Calculates the current ratio of completion for this survey
     *
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    public SurveyAnsweredRatio reloadSurveyAnsweredRatio(long idSurvey,
            ISurveyAnsweredRatioCallback callback) {
        SurveyDB surveyDB = SurveyDB.findById(idSurvey);
        SurveyAnsweredRatio surveyAnsweredRatio = null;
        ProgramDB surveyProgram = surveyDB.getProgram();
        int numRequired = QuestionDB.countRequiredByProgram(surveyProgram);
        int numCompulsory = QuestionDB.countCompulsoryByProgram(surveyProgram);
        int numOptional = (int) surveyDB.countNumOptionalQuestionsToAnswer();
        if (callback != null) {
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
        return surveyAnsweredRatio;
    }

    @Override
    public void run() {
        mAsyncExecutor.run(new Runnable() {
            @Override
            public void run() {
                save(mSurveyAnsweredRatio);
                notifySucessSave();
            }
        });
    }

    private void notifySucessSave() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete(mSurveyAnsweredRatio);
            }
        });
    }
}

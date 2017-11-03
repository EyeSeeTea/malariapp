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
    private long idSurvey;

    public SaveSurveyAnsweredRatioUseCase(
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mSurveyAnsweredRatioRepository = surveyAnsweredRatioRepository;
    }

    public void execute(long idSurvey, final ISurveyAnsweredRatioCallback callback) {
        this.idSurvey=idSurvey;
        mCallback = callback;
        run();
    }

    public void execute(ISurveyAnsweredRatioCallback callback, SurveyAnsweredRatio surveyAnsweredRatio) {
        mSurveyAnsweredRatio = surveyAnsweredRatio;
        mCallback = callback;
        run();
    }

    private void save(SurveyAnsweredRatio surveyAnsweredRatio) {
        mSurveyAnsweredRatioRepository.saveSurveyAnsweredRatio(surveyAnsweredRatio);
    }

    /**
     * Calculates the current ratio of completion for this survey
     *
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    private SurveyAnsweredRatio reloadSurveyAnsweredRatio(long idSurvey) {
        SurveyDB surveyDB = SurveyDB.findById(idSurvey);
        SurveyAnsweredRatio surveyAnsweredRatio = null;
        ProgramDB surveyProgram = surveyDB.getProgram();
        int numRequired = QuestionDB.countRequiredByProgram(surveyProgram);
        int numCompulsory = QuestionDB.countCompulsoryByProgram(surveyProgram);
        int numOptional = (int) surveyDB.countNumOptionalQuestionsToAnswer();
        if (mCallback != null) {
            notifyProgressMessage();
        }
        int numActiveChildrenCompulsory = QuestionDB.countChildrenCompulsoryBySurvey(
                surveyDB.getId_survey(), new IProgressCallback() {
                    @Override
                    public void onProgressMessage() {
                        if (mCallback != null) {
                            notifyProgressMessage();
                        }
                    }
                });
        int numAnswered = ValueDB.countBySurvey(surveyDB);
        int numCompulsoryAnswered = ValueDB.countCompulsoryBySurvey(surveyDB);
        surveyAnsweredRatio = new SurveyAnsweredRatio(surveyDB.getId_survey(),
                numRequired + numOptional,
                numAnswered, numCompulsory + numActiveChildrenCompulsory,
                numCompulsoryAnswered);
        return surveyAnsweredRatio;
    }

    private void notifyProgressMessage() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.nextProgressMessage();
            }
        });
    }

    @Override
    public void run() {
        mAsyncExecutor.run(new Runnable() {
            @Override
            public void run() {
                if(mSurveyAnsweredRatio==null) {
                    notifyProgressMessage();
                    mSurveyAnsweredRatio = reloadSurveyAnsweredRatio(idSurvey);
                }
                save(mSurveyAnsweredRatio);
                notifySuccessSave();
                }
            });
    }

    private void notifySuccessSave() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete(mSurveyAnsweredRatio);
            }
        });
    }
}

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISurveyAnsweredRatioRepository;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

public class SaveSurveyAnsweredRatioUseCase {
    private ISurveyAnsweredRatioRepository mSurveyAnsweredRatioRepository;

    public SaveSurveyAnsweredRatioUseCase(
            ISurveyAnsweredRatioRepository surveyAnsweredRatioRepository) {
        mSurveyAnsweredRatioRepository = surveyAnsweredRatioRepository;
    }

    public void execute(long idSurvey, GetSurveyAnsweredRatioUseCase.Callback callback) {
        SurveyAnsweredRatio surveyAnsweredRatio = reloadSurveyAnsweredRatio(idSurvey, callback);
        save(callback, surveyAnsweredRatio);
    }

    public void execute(GetSurveyAnsweredRatioUseCase.Callback callback, SurveyAnsweredRatio surveyAnsweredRatio) {
        save(callback, surveyAnsweredRatio);
    }

    private void save(GetSurveyAnsweredRatioUseCase.Callback callback,
            SurveyAnsweredRatio surveyAnsweredRatio) {
        mSurveyAnsweredRatioRepository.saveSurveyAnsweredRatio(surveyAnsweredRatio);
        callback.onComplete(surveyAnsweredRatio);
    }

    /**
     * Calculates the current ratio of completion for this survey
     *
     * @return SurveyAnsweredRatio that hold the total & answered questions.
     */
    public SurveyAnsweredRatio reloadSurveyAnsweredRatio(long idSurvey,
            GetSurveyAnsweredRatioUseCase.Callback callback) {
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

    public interface Callback {
        void onComplete(SurveyAnsweredRatio surveyAnsweredRatio);
    }
}

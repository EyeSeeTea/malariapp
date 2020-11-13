package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.CompetencyScoreClassification;
import org.eyeseetea.malariacare.domain.service.CompetencyScoreCalculationDomainService;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.Date;
import java.util.List;

public class CompleteSurveyUseCase implements UseCase {
    public interface Callback {
        void onCompleteSurveySuccess();

        void onCompleteSurveyError(Exception ex);
    }

    //TODO: this use case wrap complete survey actions and It's a code extracted from old code in
    // SurveyDB.setCompleteSurveyState. Create this class is the first step but
    //there is many dependencies to database model that we should refactor little by little

    private final IMainExecutor mMainExecutor;
    private final IAsyncExecutor mAsyncExecutor;
    private Callback callback;
    private SurveyDB surveyDB;

    public CompleteSurveyUseCase(
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
    }

    public void execute(SurveyDB surveyDB, Callback callback) {
        this.surveyDB = surveyDB;
        this.callback = callback;

        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        try {
            surveyDB.setStatus(Constants.SURVEY_COMPLETED);
            surveyDB.setCompletionDate(new Date());

            CompetencyScoreCalculationDomainService competencyScoreCalculationDomainService =
                    new CompetencyScoreCalculationDomainService();

            List<QuestionDB> criticalFailedQuestions =
                    QuestionDB.getFailedQuestions(surveyDB.getId_survey(), true);

            float nonCriticalStepsScore =
                    ScoreRegister.calculateScoreForNonCriticalsSteps(surveyDB,
                            "nonCriticalStepsScore");

            List<QuestionDB> nonCriticalAnsweredQuestions =
                    QuestionDB.getNonCriticalAnsweredQuestions(surveyDB.getId_survey());

            CompetencyScoreClassification competencyScoreClassification =
                    competencyScoreCalculationDomainService.calculateClassification(
                            criticalFailedQuestions.size() > 0, nonCriticalStepsScore,
                            nonCriticalAnsweredQuestions.size() == 0);

            surveyDB.setCompetencyScoreClassification(competencyScoreClassification.getId());

            surveyDB.saveScore(getClass().getSimpleName());
            surveyDB.save();

            //Plan a new survey for the future
            SurveyPlanner.getInstance().buildNext(surveyDB);

            notifyOnCompleteSurveySuccess();

        } catch (Exception e) {
            notifyOnCompleteSurveyError(e);
        }
    }

    private void notifyOnCompleteSurveySuccess() {
        mMainExecutor.run(() -> callback.onCompleteSurveySuccess());
    }

    private void notifyOnCompleteSurveyError(Exception e) {
        mMainExecutor.run(() -> callback.onCompleteSurveyError(e));
    }
}

package org.eyeseetea.malariacare.presentation.mapper;

import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.CompositeScoreViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.MissedCriticalStepViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.Observations.QuestionViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MissedStepMapper {
    public static List<MissedCriticalStepViewModel> mapToViewModel(
            List<QuestionDB> criticalQuestions, List<CompositeScoreDB> compositeScoresTree) {

        List<MissedCriticalStepViewModel> missedCriticalStepViewModels = new ArrayList<>();

        if (criticalQuestions != null && criticalQuestions.size() > 0) {

            for (Iterator<CompositeScoreDB> iterator = compositeScoresTree.iterator();
                    iterator.hasNext(); ) {
                CompositeScoreDB compositeScore = iterator.next();

                CompositeScoreViewModel compositeScoreViewModel = new CompositeScoreViewModel(
                        compositeScore.getId_composite_score(),
                        compositeScore.getId_composite_score_parent(),
                        compositeScore.getHierarchical_code(),
                        compositeScore.getLabel());

                missedCriticalStepViewModels.add(compositeScoreViewModel);

                for (QuestionDB question : criticalQuestions) {
                    if (question.getCompositeScoreFk()
                            == (compositeScore.getId_composite_score())) {
                        QuestionViewModel questionViewModel = new QuestionViewModel(
                                question.getId_question(),
                                question.getCompositeScoreFk(),
                                question.getForm_name());

                        missedCriticalStepViewModels.add(questionViewModel);
                    }
                }
            }
        }

        return missedCriticalStepViewModels;
    }
}

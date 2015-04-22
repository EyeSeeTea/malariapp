package org.eyeseetea.malariacare.layout.score;

import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;

import java.util.List;

public class ScoreUtils {

    public static float calculateScoreFromNumDen(List<Float> numDenTotal){
        float score = 0;
        if (numDenTotal.get(1) != 0){
            score = (numDenTotal.get(0) / numDenTotal.get(1)) * 100;
        }
        return score;
    }

    public static float calculateNum(Question question, Option option){
        if (option.getFactor() == null) return 0;
        return option.getFactor() * question.getNumerator_w();
    }

    public static float calculateDen(Question question, Option option){
        if (question.getNumerator_w().floatValue() == question.getDenominator_w().floatValue()) return question.getDenominator_w();
        if (question.getNumerator_w().floatValue() == 0 && question.getDenominator_w().floatValue() != 0){
            if (option.getFactor() == null) return 0;
            return option.getFactor() * question.getDenominator_w();
        }
        return 0;
    }
}

package org.eyeseetea.malariacare.layout.score;

import java.util.List;

public class ScoreUtils {

    public static float calculateScoreFromNumDen(List<Float> numDenTotal){
        float score = 0;
        if (numDenTotal.get(1) != 0){
            score = (numDenTotal.get(0) / numDenTotal.get(1)) * 100;
        }
        return score;
    }
}

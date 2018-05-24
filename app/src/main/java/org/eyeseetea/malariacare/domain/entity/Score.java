package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Score {
    private final String uId;
    private final Float score;

    public Score(String uId, float score){
        this.uId = required(uId, "Score UID is required");
        validateScore(score);
        this.score = score;
    }

    private void validateScore(float score) {
        if(score<0 || score>100){
            throw new IllegalArgumentException("Invalid score. Score should be a number between 0 and 100");
        }
    }

    public String getUId() {
        return uId;
    }

    public float getScore() {
        return score;
    }
}

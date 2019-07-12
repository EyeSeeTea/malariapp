package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Score score1 = (Score) o;

        if (!uId.equals(score1.uId)) return false;
        return score.equals(score1.score);
    }

    @Override
    public int hashCode() {
        int result = uId.hashCode();
        result = 31 * result + score.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Score{" +
                "uId='" + uId + '\'' +
                ", score=" + score +
                '}';
    }
}
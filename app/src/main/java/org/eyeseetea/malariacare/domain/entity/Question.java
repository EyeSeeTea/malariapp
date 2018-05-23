package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.utils.Constants;
import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Question {
    private String uId;
    private boolean isCompulsory;
    private boolean removed;
    private int questionType;

    public Question() {
    }

    public Question(String uId, int questionType, boolean isCompulsory) {
        this.uId = required(uId, "question uId is required");
        this.questionType = required(questionType, "question questionType is required");
        this.isCompulsory = required(isCompulsory, "question isCompulsory is required");
    }

    public String getUId() {
        return uId;
    }

    public boolean isCompulsory() {
        return isCompulsory;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isComputable(){
        return questionType != Constants.NO_ANSWER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (uId != question.uId) return false;
        if (isCompulsory != question.isCompulsory) return false;
        if (removed != question.removed) return false;
        return questionType == question.questionType;
    }

    @Override
    public int hashCode() {
        int result = (int) (questionType ^ (questionType >>> 32));
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        result = 31 * result + (uId != null ? uId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + uId +
                ", isCompulsory=" + isCompulsory +
                ", removed=" + removed +
                ", questionType=" + questionType +
                '}';
    }
}

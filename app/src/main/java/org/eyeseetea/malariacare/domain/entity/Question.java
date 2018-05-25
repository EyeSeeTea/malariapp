package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Question {
    private String uId;
    private boolean isCompulsory;
    private boolean removed;
    private QuestionType questionType;

    public Question(String uId, int questionType, boolean isCompulsory) {
        this.uId = required(uId, "question uId is required");
        this.questionType = required(QuestionType.get(questionType), "valid question type is required");
        this.isCompulsory = isCompulsory;
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
        return questionType != QuestionType.NO_ANSWER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (isCompulsory != question.isCompulsory) return false;
        if (removed != question.removed) return false;
        if (uId != null ? !uId.equals(question.uId) : question.uId != null) return false;
        return questionType == question.questionType;
    }

    @Override
    public int hashCode() {
        int result = uId != null ? uId.hashCode() : 0;
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        result = 31 * result + (questionType != null ? questionType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + uId +
                ", isCompulsory=" + isCompulsory +
                ", removed=" + removed +
                ", questionType=" + questionType.toString() +
                '}';
    }
}

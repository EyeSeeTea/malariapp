package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class Question {
    private String uId;
    private boolean isCompulsory;
    private boolean removed;
    private QuestionType questionType;
    private String answerName;

    public Question(String uId, int questionType, boolean isCompulsory) {
        this(uId,   questionType, isCompulsory, null);
    }

    public Question(String uId, int questionType, boolean isCompulsory, String answerName) {
        this.uId = required(uId, "question uId is required");
        this.questionType = required(QuestionType.get(questionType), "valid question type is required");
        this.isCompulsory = isCompulsory;
        this.answerName = answerName;
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

    public String getAnswerName() {
        return answerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (isCompulsory != question.isCompulsory) return false;
        if (removed != question.removed) return false;
        if (!uId.equals(question.uId)) return false;
        if (questionType != question.questionType) return false;
        return answerName != null ? !answerName.equals(question.answerName)
                : question.answerName != null;
    }

    @Override
    public int hashCode() {
        int result = uId.hashCode();
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        result = 31 * result + questionType.hashCode();
        result = 31 * result + (answerName != null ? answerName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "uId='" + uId + '\'' +
                ", isCompulsory=" + isCompulsory +
                ", removed=" + removed +
                ", questionType=" + questionType +
                ", answerName='" + answerName + '\'' +
                '}';
    }
}

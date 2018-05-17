package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.utils.Constants;

public class Question {
    private long id;
    private boolean cachedVisibility;
    private boolean isCompulsory;
    private boolean removed;
    private int questionType;

    public Question() {
    }

    public Question(long id, int questionType, boolean isCompulsory) {
        this.id = id;
        this.questionType = questionType;
        this.isCompulsory = isCompulsory;
    }

    public Question(long id, int questionType, boolean isCompulsory,  boolean cachedVisibility) {
        this.id = id;
        this.questionType = questionType;
        this.cachedVisibility = cachedVisibility;
        this.isCompulsory = isCompulsory;
    }

    public long getId() {
        return id;
    }

    public boolean isCachedVisibility() {
        return cachedVisibility;
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

    public int getQuestionType() {
        return questionType;
    }

    public boolean isComputable(){
        return questionType != Constants.NO_ANSWER;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (id != question.id) return false;
        if (cachedVisibility != question.cachedVisibility) return false;
        if (isCompulsory != question.isCompulsory) return false;
        if (removed != question.removed) return false;
        return questionType == question.questionType;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (cachedVisibility ? 1 : 0);
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        result = 31 * result + questionType;
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", cachedVisibility=" + cachedVisibility +
                ", isCompulsory=" + isCompulsory +
                ", removed=" + removed +
                ", questionType=" + questionType +
                '}';
    }
}

package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.data.database.QuestionType;

import java.util.ArrayList;
import java.util.List;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Question {
    private String uId;
    private boolean isCompulsory;
    private boolean removed;
    private QuestionType questionTypeMapper;
    private List<String> optionUIds;

    public Question(String uId, int questionType, boolean isCompulsory) {
        this(uId, new ArrayList(),  questionType, isCompulsory);
    }

    public Question(String uId, List<String> optionUIds, int questionType, boolean isCompulsory) {
        this.uId = required(uId, "question uId is required");
        this.optionUIds = required(optionUIds, "list of option uId is required");
        this.questionTypeMapper = required(QuestionType.get(questionType), "valid question type is required");
        this.isCompulsory = isCompulsory;
    }

    public List<String> getOptionUIds(){
        return optionUIds;
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
        return questionTypeMapper != QuestionType.NO_ANSWER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (isCompulsory != question.isCompulsory) return false;
        if (removed != question.removed) return false;
        if (uId != null ? !uId.equals(question.uId) : question.uId != null) return false;
        if (questionTypeMapper != question.questionTypeMapper) return false;
        return optionUIds != null ? optionUIds.equals(question.optionUIds) : question.optionUIds == null;
    }

    @Override
    public int hashCode() {
        int result = uId != null ? uId.hashCode() : 0;
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        result = 31 * result + (questionTypeMapper != null ? questionTypeMapper.hashCode() : 0);
        result = 31 * result + (optionUIds != null ? optionUIds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "uId='" + uId + '\'' +
                ", isCompulsory=" + isCompulsory +
                ", removed=" + removed +
                ", questionTypeMapper=" + questionTypeMapper +
                ", optionUIds=" + optionUIds +
                '}';
    }
}

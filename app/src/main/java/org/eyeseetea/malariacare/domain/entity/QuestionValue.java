package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class QuestionValue {
    private final String questionUId;
    private String optionUId;
    private String value;
    private boolean conflict;

    private QuestionValue(String questionUId, String optionUId, String value){
        this(questionUId, value);

        this.optionUId = required(optionUId, "OptionUID is required");
    }

    private QuestionValue(String questionUId, String value){
        this.questionUId = required(questionUId, "QuestionUID is required");
        this.value = required(value, "value is required");
    }

    public static QuestionValue createSimpleValue(String questionUId, String value) {
        QuestionValue questionValue = new QuestionValue(questionUId, value);

        return questionValue;
    }

    public static QuestionValue createOptionValue(String questionUId, String optionUId, String value) {
        QuestionValue questionValue = new QuestionValue(questionUId, optionUId, value);
        return questionValue;
    }

    public String getQuestionUId() {
        return questionUId;
    }

    public String getOptionUId() {
        return optionUId;
    }

    public String getValue() {
        return value;
    }


    public void markAsConflict() {
        this.conflict = true;
    }

    public boolean isConflict() {
        return conflict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionValue that = (QuestionValue) o;

        if (conflict != that.conflict) return false;
        if (!questionUId.equals(that.questionUId)) return false;
        if (optionUId != null ? !optionUId.equals(that.optionUId) : that.optionUId != null) {
            return false;
        }
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = questionUId.hashCode();
        result = 31 * result + (optionUId != null ? optionUId.hashCode() : 0);
        result = 31 * result + value.hashCode();
        result = 31 * result + (conflict ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "QuestionValue{" +
                "questionUId='" + questionUId + '\'' +
                ", optionUId='" + optionUId + '\'' +
                ", value='" + value + '\'' +
                ", conflict=" + conflict +
                '}';
    }
}

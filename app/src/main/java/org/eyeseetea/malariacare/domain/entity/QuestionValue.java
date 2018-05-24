package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import android.icu.text.PluralFormat;

public class QuestionValue {
    private final String questionUId;
    private String optionUId;
    private String value;

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
}

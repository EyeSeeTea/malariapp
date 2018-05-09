package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import android.icu.text.PluralFormat;

public class SurveyValue {
    private final String questionUId;
    private String optionUId;
    private String value;

    public SurveyValue(String questionUId, String optionUId, String value){
        this.questionUId = required(questionUId, "SurveyValue question UID is required");
        this.optionUId = required(optionUId, "SurveyValue option UID is required");
        this.value = required(value, "SurveyValue not null value is required");
    }

    public SurveyValue(String questionUId, String value){
        this.questionUId = required(questionUId, "SurveyValue question UID is required");
        this.value = required(value, "SurveyValue not null value is required");
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

package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Option {
    private String uId;
    private String code;
    private String name;
    private float factor;
    private String answerUId;

    public Option(String uId, String code, String name, float factor, String answerUId) {
        this.uId = validate(uId, "UId is required");
        this.code = validate(code, "Code is required");
        this.name = validate(name, "Name is required");
        this.factor = validateFactor(factor, "Invalid factor");
        this.answerUId = validate(answerUId, "Answer is required");
    }

    private String validate(String value, String message) {
        required(value, "UId is required");
        if(value.isEmpty()){
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private float validateFactor(float factor, String message) {
        if(factor<0){
            throw new IllegalArgumentException(message);
        }
        return factor;
    }

    public String getUId() {
        return uId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public float getFactor() {
        return factor;
    }

    public String getAnswerName() {
        return answerUId;
    }
}

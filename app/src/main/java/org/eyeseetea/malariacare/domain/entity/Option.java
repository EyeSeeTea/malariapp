package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.common.RequiredChecker.required;

public class Option {
    private String uId;
    private String code;
    private String name;
    private float factor;
    private String answerName;

    public Option(String uId, String code, String name, float factor, String answerName) {
        this.uId = required(uId, "UId is required");
        this.code = required(code, "Code is required");
        this.name = required(name, "Name is required");
        this.factor = validateFactor(factor, "Invalid factor");
        this.answerName = required(answerName, "Answer is required");
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
        return answerName;
    }
}

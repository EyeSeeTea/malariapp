package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class QuestionOption {
    String questionUId;
    String optionUId;
    boolean match;

    public QuestionOption(String questionUId, String optionUId){
        this.questionUId = required(questionUId, "Question uid is required");
        this.optionUId = required(optionUId, "Option uid is required");
        match = false;
    }

    public void doMatch(){
        match = true;
    }
    public void doUnMatch(){
        match = false;
    }

    public boolean isMatch() {
         return match;
    }

    public String getKey() {
        return questionUId+optionUId;
    }
}


package org.eyeseetea.malariacare.domain.entity;

import java.util.HashMap;

public class QuestionParentRelations {

    HashMap<String, QuestionOption> questionOptions;

    public QuestionParentRelations(){
        this.questionOptions = new HashMap<>();
    }

    public void addQuestionOptionRelation(QuestionOption questionOption){
        questionOptions.put(questionOption.getKey(), questionOption);
    }

    public void updateQuestionOption(QuestionOption questionOption) {
        if(questionOptions.containsKey(questionOption.getKey())){
            questionOptions.put(questionOption.getKey(), questionOption);
        }
    }

    public boolean checkIfExist(QuestionOption questionOption) {
        return questionOptions.containsKey(questionOption.getKey());
    }

    public boolean hasActiveMatches() {
        for(String key :questionOptions.keySet()){
            if(questionOptions.get(key).isMatch()){
                return true;
            }
        }
        return false;
    }
}

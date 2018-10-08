package org.eyeseetea.malariacare.domain.entity;

import android.support.annotation.NonNull;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Question {
    private String uId;
    private boolean isCompulsory;
    private boolean removed;
    private QuestionType questionType;
    private String answerName;
    private List<Question> children;
    private List<Question> parents;
    private Map<String, Map<String, Boolean>> questionOptionMatches;

    public Question(String uId, int questionType, boolean isCompulsory) {
        this(uId, questionType, isCompulsory, null);
    }

    public Question(String uId, int questionType, boolean isCompulsory, String answerName) {
        this.uId = required(uId, "question uId is required");
        this.questionType = required(QuestionType.get(questionType), "valid question type is required");
        this.isCompulsory = isCompulsory;
        this.answerName = answerName;
        children = new ArrayList<>();
        parents = new ArrayList<>();
        questionOptionMatches = new HashMap<>();
    }


    public String getUId() {
        return uId;
    }

    public boolean isCompulsory() {
        return isCompulsory;
    }

    public boolean isVisible() {
        if(!hasParents()) {
            return true;
        }else{
            if (hasActiveParent()) {
                return true;
            }
        }
        return false;
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

    public void addQuestionParentAndOptionMatch(Question question, String optionUid) {
        addParentIfNotExist(question);
        Map<String, Boolean> matches = new HashMap<>();
        matches = addNotTriggeredMatch(question, optionUid, matches);
        questionOptionMatches.put(question.getUId(), matches);
    }

    public void addChildren(Question question) {
        children.add(question);
    }

    public boolean hasChildren() {
        return  children.size()>0;
    }
    public boolean hasParents() {
        return  parents.size()>0;
    }

    public List<Question> getChildren(){
        return children;
    }

    public boolean shouldActivateQuestion(QuestionValue value) {
        Map<String, Boolean> matches = questionOptionMatches.get(value.getQuestionUId());
        return !isVisible() && matches!=null && matches.containsKey(value.getOptionUId());
    }

    public void addActiveParentMatch(QuestionValue questionValue) {
        if(questionValue.getQuestionUId()!=null && questionValue.getOptionUId()!=null){
            Map<String, Boolean> match = questionOptionMatches.get(questionValue.getQuestionUId());
            match.put(questionValue.getOptionUId(), true);
            questionOptionMatches.put(questionValue.getQuestionUId(), match);
        }
    }

    public void removeActiveParentMatch(QuestionValue questionValue) {
        if(questionValue.getQuestionUId()!=null && questionValue.getOptionUId()!=null){
            Map<String, Boolean> match = questionOptionMatches.get(questionValue.getQuestionUId());
            match.put(questionValue.getOptionUId(), false);
            questionOptionMatches.put(questionValue.getQuestionUId(), match);
        }
    }

    @NonNull
    private Map<String, Boolean> addNotTriggeredMatch(Question question, String optionUid1, Map<String, Boolean> matches) {
        if(questionOptionMatches.containsKey(question.getUId())) {
            matches = questionOptionMatches.get(question.getUId());
        }
        matches.put(optionUid1, false);
        return matches;
    }

    @NonNull
    private void addParentIfNotExist(Question question){
        if(!parents.contains(question)) {
            parents.add(question);
        }
    }

    private boolean hasActiveParent() {
        for(String questionUId : questionOptionMatches.keySet()){
            Map<String, Boolean> optionMatcher = questionOptionMatches.get(questionUId);
            if(optionMatcher==null){
                continue;
            }
            for(String optionUId : optionMatcher.keySet()) {
                if (optionMatcher.get(optionUId).booleanValue()) {
                    return true;
                }
            }
        }
        return false;
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

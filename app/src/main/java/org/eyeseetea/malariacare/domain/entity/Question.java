package org.eyeseetea.malariacare.domain.entity;

import android.support.annotation.NonNull;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private String uId;
    private boolean isCompulsory;
    private boolean removed;
    private QuestionType questionType;
    private String answerName;
    private List<String> children;
    private List<String> parents;
    private QuestionParentRelations questionOptionRelations;

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
        questionOptionRelations = new QuestionParentRelations();
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
            if (checkIfParentHasActiveMatches()) {
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
        addParentIfNotExist(question.getUId());
        QuestionOption questionOption = new QuestionOption(question.getUId(), optionUid);
        questionOptionRelations.addQuestionOptionRelation(questionOption);
    }

    public void addChildren(String questionUId) {
        children.add(questionUId);
    }

    public boolean hasChildren() {
        return  children.size()>0;
    }
    public boolean hasParents() {
        return  parents.size()>0;
    }

    public List<String> getChildren(){
        return children;
    }

    public boolean shouldActivateQuestion(String questionUId, String optionUId) {
        return !isVisible() && isValidQuestionOptionMatch(questionUId, optionUId);
    }

    //this method is triggered when a option parent is matched, setting a positive match for question option relation
    public void activateQuestionOptionMatch(String questionUId, String optionUId) {
        if(questionUId!=null && optionUId!=null && isValidQuestionOptionMatch(questionUId, optionUId)){
            questionOptionRelations.updateQuestionOption(getMatchedQuestionOption(questionUId, optionUId));
        }
    }

    //this method is triggered when a option parent is disabled, setting a negative match  for question option relation
    public void deactivateQuestionOptionMatch(String questionUId, String optionUId) {
        if(questionUId!=null && optionUId!=null && isValidQuestionOptionMatch(questionUId, optionUId)) {
            questionOptionRelations.updateQuestionOption(getUnMatchedQuestionOption(questionUId, optionUId));
        }
    }

    //Check if the question + option is on matches combinations.
    private boolean isValidQuestionOptionMatch(String questionUId, String optionUId) {
        if(optionUId==null) {
            return false;
        }
        QuestionOption questionOption = new QuestionOption(questionUId, optionUId);
        return questionOptionRelations.checkIfExist(questionOption);
    }

    @NonNull
    private void addParentIfNotExist(String questionUId){
        if(!parents.contains(questionUId)) {
            parents.add(questionUId);
        }
    }

    //this method search an active option doMatch for each question parent option doMatch relations.
    private boolean checkIfParentHasActiveMatches() {
        return questionOptionRelations.hasActiveMatches();
    }

    @NonNull
    private QuestionOption getMatchedQuestionOption(String questionUId, String optionUId) {
        QuestionOption questionOption = new QuestionOption(questionUId, optionUId);
        questionOption.doMatch();
        return questionOption;
    }

    private QuestionOption getUnMatchedQuestionOption(String questionUId, String optionUId) {
        QuestionOption questionOption = new QuestionOption(questionUId, optionUId);
        questionOption.doUnMatch();
        return questionOption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;

        if (isCompulsory != question.isCompulsory) return false;
        if (removed != question.removed) return false;
        if (uId != null ? !uId.equals(question.uId) : question.uId != null) return false;
        if (questionType != question.questionType) return false;
        if (answerName != null ? !answerName.equals(question.answerName) : question.answerName != null)
            return false;
        if (children != null ? !children.equals(question.children) : question.children != null)
            return false;
        if (parents != null ? !parents.equals(question.parents) : question.parents != null)
            return false;
        return questionOptionRelations != null ? questionOptionRelations.equals(question.questionOptionRelations) : question.questionOptionRelations == null;
    }

    @Override
    public int hashCode() {
        int result = uId != null ? uId.hashCode() : 0;
        result = 31 * result + (isCompulsory ? 1 : 0);
        result = 31 * result + (removed ? 1 : 0);
        result = 31 * result + (questionType != null ? questionType.hashCode() : 0);
        result = 31 * result + (answerName != null ? answerName.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (parents != null ? parents.hashCode() : 0);
        result = 31 * result + (questionOptionRelations != null ? questionOptionRelations.hashCode() : 0);
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
                ", children=" + children +
                ", parents=" + parents +
                ", questionOptionRelations=" + questionOptionRelations +
                '}';
    }
}

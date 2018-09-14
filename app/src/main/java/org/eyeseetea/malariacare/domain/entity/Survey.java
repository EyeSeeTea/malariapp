package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Survey {

    private final String uId;
    private final String programUId;
    private final String orgUnitUId;
    private Score score;
    private String userUId;
    private Date creationDate;
    private Date completionDate;
    private Date uploadDate;
    private Date scheduledDate;
    private SurveyStatus status;
    private HashMap<String, QuestionValue> questionValues;
    private HashMap<String, Question> questions;
    private SurveyAnsweredRatio surveyAnsweredRatio;

    private Survey(String uId, String programUId, String orgUnitUId, String userUId) {
        this.uId=required(uId, "Survey uid is required");
        this.programUId=required(programUId, "Survey programUId is required");
        this.orgUnitUId=required(orgUnitUId, "Survey orgUnitUId is required");
        this.userUId=required(userUId, "Survey userUId is required");
        this.questionValues = new HashMap<>();
        this.questions = new HashMap<>();
        creationDate = new Date();
        status = SurveyStatus.IN_PROGRESS;
    }

    public static Survey createEmptySurvey(String uId, String programUId, String orgUnitUId,
            String userUId, HashMap<String, Question> questions) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        survey.startQuestionSurvey(questions);
        return survey;
    }

    public static Survey createSentSurvey(String uId, String programUId, String orgUnitUId,
            String userUId, Date creationDate, Date uploadDate, Date scheduledDate,
            Date completionDate, HashMap<String, QuestionValue> questionValues, Score score) {
        Survey survey = new Survey(uId, programUId, orgUnitUId, userUId);
        survey.changeStatus(SurveyStatus.SENT);
        survey.assignCreationDate(creationDate);
        survey.changeUploadDate(uploadDate);
        survey.changeScheduledDate(scheduledDate);
        survey.assignCompletionDate(completionDate);
        survey.addQuestionValues(questionValues);
        survey.assignScore(score);
        return survey;
    }

    public String getUId() {
        return uId;
    }

    public String getProgramUId() {
        return programUId;
    }

    public String getOrgUnitUId() {
        return orgUnitUId;
    }

    public String getUserUId() {
        return userUId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public List<QuestionValue> getValues() {
        return new ArrayList<>(questionValues.values());
    }

    public Score getScore() {
        return score;
    }

    public void assignScore(Score score) {
        this.score = score;
    }

    private void addQuestionValues(HashMap<String, QuestionValue> values) {
        questionValues.putAll(values);
    }

    public void assignCreationDate(Date date) {
        this.creationDate = date;
    }

    public void changeStatus(SurveyStatus status) {
        this.status = status;
    }

    public void assignCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public void changeUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public void changeScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Question getQuestion(String uid) {
        return questions.get(uid);
    }

    public SurveyAnsweredRatio getSurveyAnsweredRatio() {
        return surveyAnsweredRatio;
    }

    public void setSurveyAnsweredRatio(SurveyAnsweredRatio surveyAnsweredRatio) {
        this.surveyAnsweredRatio = surveyAnsweredRatio;
    }

    public void addValue(QuestionValue questionValue, Question question){
        boolean isRepeated = false;
        if(questionValues.containsKey(question.getUId())){
            isRepeated = true;
        }
        //update
        questionValues.put(question.getUId(), questionValue);
        if(isRepeated) {
            //value count is already added
            return;
        }
        if(question.hasChildren()) {
            for(Question childQuestion : question.getChildren()){
                Question updatedQuestion = questions.get(childQuestion.getUId());
                if(updatedQuestion.shouldActivateQuestion(questionValue)) {
                    updatedQuestion.addActiveParentMatch(questionValue);
                    getSurveyAnsweredRatio().fixTotalQuestion(updatedQuestion.isCompulsory(), updatedQuestion.isVisible());
                }
            }
        }
        getSurveyAnsweredRatio().addQuestion(question.isCompulsory());
    }

    public void removeValue(QuestionValue questionValue, Question question){
        if(!questionValues.containsKey(question.getUId())){
            //value not exist
            return;
        }
        questionValues.remove(question.getUId());
        getSurveyAnsweredRatio().removeQuestion(question.isCompulsory());
        if(question.hasChildren()) {
            for(Question childQuestion : question.getChildren()){
                QuestionValue childQuestionValue = questionValues.get(childQuestion.getUId());
                Question updatedQuestion = questions.get(childQuestion.getUId());
                updatedQuestion.removeActiveParentMatch(questionValue);
                if(childQuestionValue!=null && !childQuestion.isVisible()) {
                    removeValue(childQuestionValue, childQuestion);
                    getSurveyAnsweredRatio().fixTotalQuestion(childQuestion.isCompulsory(), childQuestion.isVisible());
                }
            }
        }
    }

    private void startQuestionSurvey(HashMap<String, Question> questions) {
        int totalCompulsory = countTotalCompulsoryQuestions(questions);
        int total = countTotalActiveQuestions(questions) + totalCompulsory;
        setSurveyAnsweredRatio(SurveyAnsweredRatio.startSurvey(total, totalCompulsory));
        this.questions = questions;
    }

    private int countTotalCompulsoryQuestions(HashMap<String, Question> questions) {
        int total = 0;
        for(Question question : questions.values()){
            if(!question.hasParents() && question.isComputable()  && question.isCompulsory()){
                total++;
            }
        }
        return total;
    }

    private int countTotalActiveQuestions(HashMap<String, Question> questions) {
        int total = 0;
        for(Question question : questions.values()){
            if(!question.hasParents() && question.isComputable() && !question.isCompulsory()){
                total++;
            }
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Survey survey = (Survey) o;

        if (uId != null ? !uId.equals(survey.uId) : survey.uId != null) return false;
        if (programUId != null ? !programUId.equals(survey.programUId) : survey.programUId != null)
            return false;
        if (orgUnitUId != null ? !orgUnitUId.equals(survey.orgUnitUId) : survey.orgUnitUId != null)
            return false;
        if (score != null ? !score.equals(survey.score) : survey.score != null) return false;
        if (userUId != null ? !userUId.equals(survey.userUId) : survey.userUId != null)
            return false;
        if (creationDate != null ? !creationDate.equals(survey.creationDate) : survey.creationDate != null)
            return false;
        if (completionDate != null ? !completionDate.equals(survey.completionDate) : survey.completionDate != null)
            return false;
        if (uploadDate != null ? !uploadDate.equals(survey.uploadDate) : survey.uploadDate != null)
            return false;
        if (scheduledDate != null ? !scheduledDate.equals(survey.scheduledDate) : survey.scheduledDate != null)
            return false;
        if (status != survey.status) return false;
        return surveyAnsweredRatio != null ? surveyAnsweredRatio.equals(survey.surveyAnsweredRatio) : survey.surveyAnsweredRatio == null;
    }

    @Override
    public int hashCode() {
        int result = uId != null ? uId.hashCode() : 0;
        result = 31 * result + (programUId != null ? programUId.hashCode() : 0);
        result = 31 * result + (orgUnitUId != null ? orgUnitUId.hashCode() : 0);
        result = 31 * result + (score != null ? score.hashCode() : 0);
        result = 31 * result + (userUId != null ? userUId.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (completionDate != null ? completionDate.hashCode() : 0);
        result = 31 * result + (uploadDate != null ? uploadDate.hashCode() : 0);
        result = 31 * result + (scheduledDate != null ? scheduledDate.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (surveyAnsweredRatio != null ? surveyAnsweredRatio.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "uId='" + uId + '\'' +
                ", programUId='" + programUId + '\'' +
                ", orgUnitUId='" + orgUnitUId + '\'' +
                ", score=" + score +
                ", userUId='" + userUId + '\'' +
                ", creationDate=" + creationDate +
                ", completionDate=" + completionDate +
                ", uploadDate=" + uploadDate +
                ", scheduledDate=" + scheduledDate +
                ", status=" + status +
                ", questionValues=" + questionValues +
                ", questions=" + questions +
                ", surveyAnsweredRatio=" + surveyAnsweredRatio +
                '}';
    }
}

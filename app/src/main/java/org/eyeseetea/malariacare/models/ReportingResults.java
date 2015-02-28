package org.eyeseetea.malariacare.models;

/**
 * Created by Jose on 28/02/2015.
 */
public class ReportingResults {

    String question;
    String answer1;
    String answer2;
    String score;

    public ReportingResults(String question) {
        this.question = question;
        answer1="";
        answer2="";
        score="";
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer1() {
        return answer1;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

}

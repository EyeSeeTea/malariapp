package org.psi.malariacare;

/**
 * Created by ignacio on 2/14/15.
 */
public class Question {
    private String statement;
    private String optionSet;
    private String answer;

    public Question(){
        this.statement = "";
        this.optionSet = "Asked";
        this.answer = "NOT ANSWERED";
    }

    public Question(String statement){
        this.statement = statement;
        this.optionSet = "Asked";
        this.answer = "NOT ANSWERED";
    }

    public Question(String statement, String optionSet){
        this.statement = statement;
        this.optionSet = optionSet;
        this.answer = "NOT ANSWERED";
    }

    public Question(String statement, String optionSet, String defaultAnser){
        this.statement = statement;
        this.optionSet = optionSet;
        this.answer = defaultAnser;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getOptionSet() {
        return optionSet;
    }

    public void setOptionSet(String optionSet) {
        this.optionSet = optionSet;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String toString(){
        return "QUESTION:\n statement=" + this.getStatement() + "\n optionSet=" + this.getOptionSet() + "\n answer=" + this.getAnswer();
    }
}

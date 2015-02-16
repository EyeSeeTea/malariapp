package org.eyeseetea.malariacare.testing;

/**
 * Created by ignacio on 2/14/15.
 */
public class TestQuestion {
    private String statement;
    private String optionSet;
    private String answer;

    public TestQuestion(){
        this.statement = "";
        this.optionSet = "asked";
        this.answer = "NOT ANSWERED";
    }

    public TestQuestion(String statement){
        this.statement = statement;
        this.optionSet = "asked";
        this.answer = "NOT ANSWERED";
    }

    public TestQuestion(String statement, String optionSet){
        this.statement = statement;
        this.optionSet = optionSet;
        this.answer = "NOT ANSWERED";
    }

    public TestQuestion(String statement, String optionSet, String defaultAnser){
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

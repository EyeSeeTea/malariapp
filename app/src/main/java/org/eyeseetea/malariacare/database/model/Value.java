package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;

public class Value extends SugarRecord<Value> {

    Option option;
    Question question;
    String value;
    Survey survey;

    public Value() {
    }

    public Value(String value, Question question, Survey survey) {
        this.option = null;
        this.question = question;
        this.value = value;
        this.survey = survey;
    }

    public Value(Option option, Question question, Survey survey) {
        this.option = option;
        this.question = question;
        this.value = option.getName();
        this.survey = survey;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    @Override
    public String toString() {
        return "Value{" +
                "option=" + option +
                ", question=" + question +
                ", value='" + value + '\'' +
                ", survey=" + survey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;

        Value value1 = (Value) o;

        if (!option.equals(value1.option)) return false;
        if (!question.equals(value1.question)) return false;
        if (!survey.equals(value1.survey)) return false;
        if (!value.equals(value1.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = option.hashCode();
        result = 31 * result + question.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + survey.hashCode();
        return result;
    }
}

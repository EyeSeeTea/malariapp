package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Value extends SugarRecord<Value> {

    Option option;
    Question question;

    public Value() {
    }

    public Value(Option option, Question question) {
        this.option = option;
        this.question = question;
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

    @Override
    public String toString() {
        return "Value{" +
                "id='" + id + '\'' +
                ", option=" + option +
                ", question=" + question +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Value value = (Value) o;

        if (option != null ? !option.equals(value.option) : value.option != null) return false;
        if (question != null ? !question.equals(value.question) : value.question != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = option != null ? option.hashCode() : 0;
        result = 31 * result + (question != null ? question.hashCode() : 0);
        return result;
    }
}

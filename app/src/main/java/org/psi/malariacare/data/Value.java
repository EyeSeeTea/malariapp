package org.psi.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Value extends SugarRecord<Tab> {

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
}

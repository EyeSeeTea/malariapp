package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Option extends SugarRecord<Option> {

    String name;
    Float factor;
    Answer answer;

    public Option() {
    }

    public Option(String name, Float factor, Answer answer) {
        this.name = name;
        this.factor = factor;
        this.answer = answer;
    }

    public Option(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getFactor() {
        return factor;
    }

    public void setFactor(Float factor) {
        this.factor = factor;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    //This is not a standard to string method as this is going to be used for ddl
    @Override
    public String toString() {
        return name;
    }
}

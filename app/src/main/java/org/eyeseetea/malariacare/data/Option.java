package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;

/**
 * Created by adrian on 14/02/15.
 */
public class Option extends SugarRecord<Tab> {

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

    @Override
    public String toString() {
        return "Option{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", factor=" + factor +
                ", answer=" + answer +
                '}';
    }
}

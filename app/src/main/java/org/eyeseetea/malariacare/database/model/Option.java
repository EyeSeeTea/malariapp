package org.eyeseetea.malariacare.database.model;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (answer != null ? !answer.equals(option.answer) : option.answer != null) return false;
        if (factor != null ? !factor.equals(option.factor) : option.factor != null) return false;
        if (name != null ? !name.equals(option.name) : option.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (factor != null ? factor.hashCode() : 0);
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        return result;
    }
}

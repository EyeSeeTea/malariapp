package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;

import org.eyeseetea.malariacare.utils.Constants;


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

    /**
     * Checks if this option is a 'Yes'.
     *
     * @return true|false
     */
    public boolean isYes(){
        return Constants.CHECKBOX_YES_OPTION.equals(name);
    }

    @Override
    public String toString() {
        return "Option{" +
                "name='" + name + '\'' +
                ", factor=" + factor +
                ", answer=" + answer +
                '}';
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

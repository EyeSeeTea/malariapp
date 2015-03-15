package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class Answer extends SugarRecord<Answer> {

    String name;
    Integer output;

    public Answer() {
    }

    public Answer(String name, Integer output) {
        this.name = name;
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOutput() {
        return output;
    }

    public void setOutput(Integer output) {
        this.output = output;
    }

    public List<Option> getOptions(){
        return Option.find(Option.class, "answer = ?", String.valueOf(this.getId()));
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", output=" + output +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer = (Answer) o;

        if (name != null ? !name.equals(answer.name) : answer.name != null) return false;
        if (output != null ? !output.equals(answer.output) : answer.output != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (output != null ? output.hashCode() : 0);
        return result;
    }
}

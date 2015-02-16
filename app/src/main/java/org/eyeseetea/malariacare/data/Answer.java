package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class Answer extends SugarRecord<Tab> {

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
}

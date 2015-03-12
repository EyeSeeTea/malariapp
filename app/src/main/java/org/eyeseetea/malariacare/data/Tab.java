package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class Tab extends SugarRecord<Tab> {

    String name;
    Integer order_pos;

    public Tab() {
    }

    public Tab(String name, Integer order_pos) {
        this.name = name;
        this.order_pos = order_pos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder_pos() {
        return order_pos;
    }

    public void setOrder_pos(Integer order_pos) {
        this.order_pos = order_pos;
    }

    public List<Header> getHeaders(){
        return Header.find(Header.class, "tab = ?", String.valueOf(this.getId()));
    }

    public List<Header> getOrderedHeaders(){
        return Header.find(Header.class, "tab = ?", String.valueOf(this.getId()), "orderpos");
    }

    public List<Score> getScores(){
        return Score.find(Score.class, "tab = ?", String.valueOf(this.getId()));
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                '}';
    }
}

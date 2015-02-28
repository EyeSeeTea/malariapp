package org.eyeseetea.malariacare.data;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class Header extends SugarRecord<Header> {

    String short_name;
    String name;
    Integer order_header;
    Integer master;
    Tab tab;

    @Ignore
    List<Question> _questions;

    public Header() {
    }

    public Header(String short_name, String name, Integer order_header, Integer master, Tab tab) {
        this.short_name = short_name;
        this.name = name;
        this.order_header = order_header;
        this.master = master;
        this.tab = tab;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder_header() {
        return order_header;
    }

    public void setOrder_header(Integer order_header) {
        this.order_header = order_header;
    }

    public Integer getMaster() {
        return master;
    }

    public void setMaster(Integer master) {
        this.master = master;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public List<Question> getQuestions(){
        if (this._questions == null) {
            return Question.find(Question.class, "header = ?", String.valueOf(this.getId()));
        }
        return _questions;
    }

    @Override
    public String toString() {
        return "Header{" +
                "id='" + id + '\'' +
                ", short_name='" + short_name + '\'' +
                ", name='" + name + '\'' +
                ", order_header=" + order_header +
                ", master=" + master +
                ", tab=" + tab +
                '}';
    }
}

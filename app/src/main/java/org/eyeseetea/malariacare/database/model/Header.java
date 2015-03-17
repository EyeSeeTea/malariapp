package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

public class Header extends SugarRecord<Header> {

    String short_name;
    String name;
    Integer order_pos;
    Tab tab;

    @Ignore
    List<Question> _questions;

    public Header() {
    }

    public Header(String short_name, String name, Integer order_pos, Integer master, Tab tab) {
        this.short_name = short_name;
        this.name = name;
        this.order_pos = order_pos;
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

    public Integer getOrder_pos() {
        return order_pos;
    }

    public void setOrder_pos(Integer order_pos) {
        this.order_pos = order_pos;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public List<Question> getQuestions(){
        if (this._questions == null){
            return Select.from(Question.class)
                    .where(Condition.prop("header")
                            .eq(String.valueOf(this.getId())))
                    .orderBy("orderpos").list();
            //return Question.find(Question.class, "header = ? order by orderpos", String.valueOf(this.getId()));
        }
        return _questions;
    }

    @Override
    public String toString() {
        return "Header{" +
                "id='" + id + '\'' +
                ", short_name='" + short_name + '\'' +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", tab=" + tab +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (name != null ? !name.equals(header.name) : header.name != null) return false;
        if (order_pos != null ? !order_pos.equals(header.order_pos) : header.order_pos != null)
            return false;
        if (short_name != null ? !short_name.equals(header.short_name) : header.short_name != null)
            return false;
        if (tab != null ? !tab.equals(header.tab) : header.tab != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = short_name != null ? short_name.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        result = 31 * result + (tab != null ? tab.hashCode() : 0);
        return result;
    }
}

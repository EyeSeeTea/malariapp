package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

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
        return Select.from(Header.class)
                .where(Condition.prop("tab")
                        .eq(String.valueOf(this.getId())))
                .orderBy("orderpos").list();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tab tab = (Tab) o;

        if (name != null ? !name.equals(tab.name) : tab.name != null) return false;
        if (order_pos != null ? !order_pos.equals(tab.order_pos) : tab.order_pos != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (order_pos != null ? order_pos.hashCode() : 0);
        return result;
    }
}

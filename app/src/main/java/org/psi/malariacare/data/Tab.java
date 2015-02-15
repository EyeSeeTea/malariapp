package org.psi.malariacare.data;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by adrian on 14/02/15.
 */
public class Tab extends SugarRecord<Tab> {

    String name;
    Integer order_tab;

    public Tab() {
    }

    public Tab(String name, Integer order_tab) {
        this.name = name;
        this.order_tab = order_tab;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder_tab() {
        return order_tab;
    }

    public void setOrder_tab(Integer order_tab) {
        this.order_tab = order_tab;
    }

    public List<Header> getHeaders(){
        return Header.find(Header.class, "tab = ?", String.valueOf(this.getId()));
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", order_tab=" + order_tab +
                '}';
    }
}

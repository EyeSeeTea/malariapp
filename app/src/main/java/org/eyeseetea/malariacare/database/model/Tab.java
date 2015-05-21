package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import org.eyeseetea.malariacare.database.utils.Session;

import java.util.List;

public class Tab extends SugarRecord<Tab> {

    String name;
    Integer order_pos;
    Program program;
    Integer type;

    public Tab() {
    }

    public Tab(String name, Integer order_pos, Program program, Integer type) {
        this.name = name;
        this.order_pos = order_pos;
        this.program = program;
        this.type = type;
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

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    /*
     * Return tabs filter by program and order by orderpos field
     */
    public static List<Tab> getTabsBySession(){
        return Select.from(Tab.class).where(Condition.prop("program")
                .eq(String.valueOf(Session.getSurvey().getProgram().getId()))).orderBy("orderpos").list();
    }

    @Override
    public String toString() {
        return "Tab{" +
                "name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", program=" + program +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tab)) return false;

        Tab tab = (Tab) o;

        if (!name.equals(tab.name)) return false;
        if (!order_pos.equals(tab.order_pos)) return false;
        if (!program.equals(tab.program)) return false;
        if (!type.equals(tab.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + order_pos.hashCode();
        result = 31 * result + program.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

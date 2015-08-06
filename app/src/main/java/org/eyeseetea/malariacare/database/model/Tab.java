/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Tab extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;
    @Column
    String name;
    @Column
    Integer order_pos;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_program",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Program program;
    @Column
    Integer type;

    List<Header> headers;

    List<Score> scores;

    public Tab() {
    }

    public Tab(String name, Integer order_pos, Program program, Integer type) {
        this.name = name;
        this.order_pos = order_pos;
        this.program = program;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = OneToMany.Method.ALL, variableName = "headers")
    public List<Header> getHeaders(){
        return new Select().from(Header.class)
                .where(Condition.column(Header$Table.ID).is(this.getId()))
                .orderBy(Header$Table.ORDER_POS).queryList();
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = OneToMany.Method.ALL, variableName = "scores")
    public List<Score> getScores(){
        return new Select().from(Score.class)
                .where(Condition.column(Score$Table.ID).is(this.getId())).queryList();
    }

    /*
     * Return tabs filter by program and order by orderpos field
     */
    public static List<Tab> getTabsBySession(){
        return new Select().from(Tab.class)
                .where(Condition.column(Tab$Table.PROGRAM_ID_PROGRAM).is(String.valueOf(Session.getSurvey().getProgram().getId())))
                .orderBy(Tab$Table.ORDER_POS).queryList();
    }

    /**
     * Checks if this tab is a general score tab.
     * @return
     */
    public boolean isGeneralScore(){
        return getType() == Constants.TAB_SCORE_SUMMARY && !isCompositeScore();
    }

    /**
     * Checks if this tab is the composite score tab
     * @return
     */
    public boolean isCompositeScore(){
        return getName().equals(Constants.COMPOSITE_SCORE_TAB_NAME);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tab)) return false;

        Tab tab = (Tab) o;

        if (id != tab.id) return false;
        if (name != null ? !name.equals(tab.name) : tab.name != null) return false;
        if (!order_pos.equals(tab.order_pos)) return false;
        if (!program.equals(tab.program)) return false;
        return type.equals(tab.type);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + order_pos.hashCode();
        result = 31 * result + program.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", program=" + program +
                ", type=" + type +
                '}';
    }
}

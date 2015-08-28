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

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Header extends BaseModel{

    @Column
    @PrimaryKey(autoincrement = true)
    long id_header;
    @Column
    String short_name;
    @Column
    String name;
    @Column
    Integer order_pos;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_tab",
            columnType = Long.class,
            foreignColumnName = "id_tab")},
            saveForeignKeyModel = false)
    Tab tab;

    List<Question> questions;

//    @Ignore
//    List<Question> _parentQuestions;

    public Header() {
    }

    public Header(String short_name, String name, Integer order_pos, Integer master, Tab tab) {
        this.short_name = short_name;
        this.name = name;
        this.order_pos = order_pos;
        this.tab = tab;
    }

    public Long getId_header() {
        return id_header;
    }

    public void setId_header(Long id_header) {
        this.id_header = id_header;
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

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "questions")
    public List<Question> getQuestions(){
        if (this.questions == null){
            this.questions = new Select().from(Question.class)
                    .where(Condition.column(Question$Table.HEADER_ID_HEADER).eq(this.getId_header()))
                    .orderBy(Question$Table.ORDER_POS).queryList();
        }
        return questions;
    }

    /**
     * getNumber Of Question Parents Header
     * @return
     */
    public long getNumberOfQuestionParents() {
        return new Select().count().from(Question.class)
                .where(Condition.column(Question$Table.HEADER_ID_HEADER).eq(getId_header()))
                .and(Condition.column(Question$Table.QUESTION_ID_PARENT).isNull()).count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Header)) return false;

        Header header = (Header) o;

        if (id_header != header.id_header) return false;
        if (short_name != null ? !short_name.equals(header.short_name) : header.short_name != null)
            return false;
        if (name != null ? !name.equals(header.name) : header.name != null) return false;
        if (!order_pos.equals(header.order_pos)) return false;
        return tab.equals(header.tab);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_header ^ (id_header >>> 32));
        result = 31 * result + (short_name != null ? short_name.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + order_pos.hashCode();
        result = 31 * result + tab.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Header{" +
                "id=" + id_header +
                ", short_name='" + short_name + '\'' +
                ", name='" + name + '\'' +
                ", order_pos=" + order_pos +
                ", tab=" + tab +
                '}';
    }
}

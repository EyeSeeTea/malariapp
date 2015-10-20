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

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class TabGroup extends BaseModel implements Visitable {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_tab_group;
    @Column
    String name;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_program",
            columnType = Long.class,
            foreignColumnName = "id_program")},
            saveForeignKeyModel = false)
    Program program;

    //OneToMany Relations
    List<Tab> tabs;
    List<Survey> surveys;

    public TabGroup() {
    }

    public TabGroup(String name) {
        this.name = name;
    }

    public TabGroup(String name, Program program) {
        this.name = name;
        this.program = program;
    }

    public Long getId_tab_group() {
        return id_tab_group;
    }

    public void setId_tab_group(Long id_tab_group) {
        this.id_tab_group = id_tab_group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    //TODO: to enable lazy loading, here we need to set Method.SAVE and Method.DELETE and use the .toModel() to specify when do we want to load the models
    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tabs")
    public List<Tab> getTabs(){
        return new Select().from(Tab.class)
                .where(Condition.column(Tab$Table.TABGROUP_ID_TAB_GROUP).eq(this.getId_tab_group()))
                .orderBy(Tab$Table.ORDER_POS).queryList();
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "surveys")
    public List<Survey> getSurveys(){
        this.surveys = new Select().from(Survey.class)
                .where(Condition.column(Survey$Table.TABGROUP_ID_TAB_GROUP).eq(this.getId_tab_group())).queryList();
        return this.surveys;
    }

    /*
     * Return tabs filter by program and order by orderpos field
     */
    public static List<Tab> getTabsBySession(){
        return new Select().from(Tab.class)
                .where(Condition.column(Tab$Table.TABGROUP_ID_TAB_GROUP).eq(Session.getSurvey().getTabGroup().getProgram().getId_program()))
                .orderBy(Tab$Table.ORDER_POS).queryList();
    }

    @Override
    public void accept(IConvertToSDKVisitor IConvertToSDKVisitor) {
        IConvertToSDKVisitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TabGroup tabGroup = (TabGroup) o;

        if (id_tab_group != tabGroup.id_tab_group) return false;
        if (!name.equals(tabGroup.name)) return false;
        if (program != null ? !program.equals(tabGroup.program) : tabGroup.program != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id_tab_group ^ (id_tab_group >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + (program != null ? program.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TabGroup{" +
                "id=" + id_tab_group +
                ", name='" + name + '\'' +
                '}';
    }
}

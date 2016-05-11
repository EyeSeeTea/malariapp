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

import android.util.Log;

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
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;
import org.eyeseetea.malariacare.database.utils.Session;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class TabGroup extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_tab_group;
    @Column
    String name;

    @Column
    Long id_program;

    /**
     * Reference to parent program (loaded lazily)
     */
    Program program;

    @Column
    String uid;

    /**
     * List of tabs that belongs to this tabgroup
     */
    List<Tab> tabs;

    /**
     * List of surveys that belongs to this tabgroup
     */
    List<Survey> surveys;

    public TabGroup() {
    }

    public TabGroup(String name) {
        this.name = name;
    }

    public TabGroup(String name, Program program) {
        this.name = name;
        setProgram(program);
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

    public void setProgram(Long id_program){
        this.id_program=id_program;
        this.program=null;
    }
    public void setProgram(Program program) {
        this.program = program;
        this.id_program=(program!=null)?program.getId_program():null;
    }

    public Program getProgram() {
        if(program==null){
            if (id_program == null){
                return null;
            }
            program= new Select()
                    .from(Program.class)
                    .where(Condition.column(Program$Table.ID_PROGRAM)
                            .is(id_program)).querySingle();
        }
        return program;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Tab> getTabs(){
        if (tabs==null){
            tabs=new Select().from(Tab.class)
                    .where(Condition.column(Tab$Table.ID_TAB_GROUP).eq(this.getId_tab_group()))
                    .orderBy(Tab$Table.ORDER_POS).queryList();
        }
        return tabs;
    }

    public List<Survey> getSurveys(){
        if(surveys==null){
            this.surveys = new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.ID_TAB_GROUP).eq(this.getId_tab_group())).queryList();
        }
        return this.surveys;
    }


    public static TabGroup getTabGroup(String uid) {
        TabGroup tabGroup = new Select()
                .from(TabGroup.class)
                .where(Condition.column(TabGroup$Table.UID)
                        .is(uid)).querySingle();
        return tabGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TabGroup tabGroup = (TabGroup) o;

        if (id_tab_group != tabGroup.id_tab_group) return false;
        if (name != null ? !name.equals(tabGroup.name) : tabGroup.name != null) return false;
        if (id_program != null ? !id_program.equals(tabGroup.id_program) : tabGroup.id_program != null)
            return false;
        return !(uid != null ? !uid.equals(tabGroup.uid) : tabGroup.uid != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_tab_group ^ (id_tab_group >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id_program != null ? id_program.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TabGroup{" +
                "id_tab_group=" + id_tab_group +
                ", name='" + name + '\'' +
                ", id_program=" + id_program +
                ", uid='" + uid + '\'' +
                '}';
    }

}

/*
 * Copyright (c) 2015.
 *
 * This file is part of QA App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
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
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class Program extends BaseModel{

    @Column
    @PrimaryKey(autoincrement = true)
    long id_program;
    @Column
    String uid;
    @Column
    String name;

    List<TabGroup> tabGroups;

    public Program() {
    }

    public Program(String name) {
        this.name = name;
    }

    public Program(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public Long getId_program() {
        return id_program;
    }

    public void setId_program(Long id_program) {
        this.id_program = id_program;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tabGroups")
    public List<TabGroup> getTabGroups(){
        this.tabGroups = new Select().from(TabGroup.class)
                    .where(Condition.column(TabGroup$Table.PROGRAM_ID_PROGRAM).eq(this.getId_program()))
                    .queryList();
        return this.tabGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Program)) return false;

        Program program = (Program) o;

        if (id_program != program.id_program) return false;
        if (uid != null ? !uid.equals(program.uid) : program.uid != null) return false;
        return name.equals(program.name);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_program ^ (id_program >>> 32));
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Program{" +
                "id=" + id_program +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

}

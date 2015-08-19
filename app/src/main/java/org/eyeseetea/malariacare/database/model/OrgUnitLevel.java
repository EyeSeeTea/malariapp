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

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class OrgUnitLevel extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;
    @Column
    String name;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_parent",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    OrgUnitLevel orgUnit;

    List<Survey> surveys;

    List<OrgUnitLevel> children;

    public OrgUnitLevel() {
    }

    public OrgUnitLevel(String name) {
        this.name = name;
    }


    public OrgUnitLevel(String uid, String name, OrgUnitLevel orgUnit) {
        this.uid = uid;
        this.name = name;
        this.orgUnit = orgUnit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public OrgUnitLevel getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(OrgUnitLevel orgUnit) {
        this.orgUnit = orgUnit;
    }


    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "children")
    public List<OrgUnitLevel> getChildren(){
        this.children = new Select().from(OrgUnitLevel.class)
                .where(Condition.column(OrgUnit$Table.ORGUNIT_ID_PARENT).eq(this.getId())).queryList();
        return children;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "surveys")
    public List<Survey> getSurveys(){
        //if(this.surveys == null){
            this.surveys = new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.ORGUNIT_ID_ORG_UNIT).eq(this.getId())).queryList();
        //}
        return surveys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitLevel orgUnit1 = (OrgUnitLevel) o;

        if (id != orgUnit1.id) return false;
        if (name != null ? !name.equals(orgUnit1.name) : orgUnit1.name != null) return false;
        if (orgUnit != null ? !orgUnit.equals(orgUnit1.orgUnit) : orgUnit1.orgUnit != null)
            return false;
        if (uid != null ? !uid.equals(orgUnit1.uid) : orgUnit1.uid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (orgUnit != null ? orgUnit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", orgUnit='" + orgUnit + '\'' +
                '}';
    }
}

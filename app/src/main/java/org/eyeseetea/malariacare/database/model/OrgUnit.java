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
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class OrgUnit extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;
    @Column
    String uid;
    @Column
    String name;

    List<Survey> surveys;

    public OrgUnit() {
    }

    public OrgUnit(String name) {
        this.name = name;
    }


    public OrgUnit(String uid, String name) {
        this.uid = uid;
        this.name = name;
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

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "surveys")
    public List<Survey> getSurveys(){
        if(this.surveys == null){
            this.surveys = new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.ORGUNIT_ID_ORG_UNIT).is(this.getId())).queryList();
        }
        return surveys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrgUnit)) return false;

        OrgUnit orgUnit = (OrgUnit) o;

        if (id != orgUnit.id) return false;
        if (uid != null ? !uid.equals(orgUnit.uid) : orgUnit.uid != null) return false;
        return name.equals(orgUnit.name);

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "id=" + id +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

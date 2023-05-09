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

package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

@Table(database = AppDatabase.class, name = "OrgUnitLevel")
public class OrgUnitLevelDB extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_org_unit_level;
    @Column
    String name;

    @Column
    String uid_org_unit_level;


    List<OrgUnitDB> orgUnits;

    public OrgUnitLevelDB() {
    }

    public OrgUnitLevelDB(String name) {
        this.name = name;
    }


    public OrgUnitLevelDB(String uid, String name) {
        this.name = name;
    }

    public Long getId_org_unit_level() {
        return id_org_unit_level;
    }

    public void setId_org_unit_level(Long id_org_unit_level) {
        this.id_org_unit_level = id_org_unit_level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid_org_unit_level;
    }

    public void setUid(String uid) {
        this.uid_org_unit_level = uid;
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "orgUnits")
    public List<OrgUnitDB> getOrgUnits(){
        this.orgUnits = new Select().from(OrgUnitDB.class)
                .where(OrgUnitDB_Table.id_org_unit_parent.eq(this.getId_org_unit_level())).queryList();
        return orgUnits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitLevelDB that = (OrgUnitLevelDB) o;

        if (id_org_unit_level != that.id_org_unit_level) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (uid_org_unit_level != null ? !uid_org_unit_level.equals(that.uid_org_unit_level) : that.uid_org_unit_level != null) return false;
        return orgUnits != null ? orgUnits.equals(that.orgUnits) : that.orgUnits == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id_org_unit_level ^ (id_org_unit_level >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (uid_org_unit_level != null ? uid_org_unit_level.hashCode() : 0);
        result = 31 * result + (orgUnits != null ? orgUnits.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnitLevel{" +
                "id_org_unit_level=" + id_org_unit_level +
                ", name='" + name + '\'' +
                ", uid='" + uid_org_unit_level + '\'' +
                '}';
    }

    public static List<OrgUnitLevelDB> getByUIds(List<String> uids){
        return new Select().from(OrgUnitLevelDB.class).where(OrgUnitLevelDB_Table.uid_org_unit_level.in(uids))
                .orderBy(OrgUnitLevelDB_Table.name, true).queryList();
    }

    public static List<OrgUnitLevelDB> list() {
        return new Select().from(OrgUnitLevelDB.class).queryList();
    }
}

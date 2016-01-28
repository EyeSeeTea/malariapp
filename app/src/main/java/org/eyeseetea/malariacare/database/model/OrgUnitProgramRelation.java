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
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.database.AppDatabase;

import java.util.Date;

/**
 * Created by ivan.arrizabalaga on 14/02/15.
 */
@Table(databaseName = AppDatabase.NAME)
public class OrgUnitProgramRelation extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_orgunit_program_relation;

    @Column
    Long id_org_unit;

    /**
     * Reference to lazy orgUnit
     */
    OrgUnit orgUnit;

    @Column
    Long id_program;

    /**
     * Reference to lazy program
     */
    Program program;

    public OrgUnitProgramRelation() {
    }

    public OrgUnitProgramRelation(OrgUnit orgUnit, Program program) {
        setOrgUnit(orgUnit);
        setProgram(program);
    }

    public OrgUnit getOrgUnit() {
        if(orgUnit==null){
            if(id_org_unit==null) return null;
            orgUnit = new Select()
                    .from(OrgUnit.class)
                    .where(Condition.column(OrgUnit$Table.ID_ORG_UNIT)
                            .is(id_org_unit)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit= orgUnit;
        this.id_org_unit = (orgUnit!=null)?orgUnit.getId_org_unit():null;
    }

    public void setOrgUnit(Long id_org_unit){
        this.id_org_unit = id_org_unit;
        this.orgUnit = null;
    }

    public Program getProgram() {
        if(program==null){
            if(id_program==null) return null;
            program = new Select()
                    .from(Program.class)
                    .where(Condition.column(Program$Table.ID_PROGRAM)
                            .is(id_program)).querySingle();
        }
        return program;
    }

    public void setProgram(Program program) {
        this.program=program;
        this.id_program = (program!=null)?program.getId_program():null;
    }

    public void setProgram(Long id_program){
        this.id_program = id_program;
        this.program = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitProgramRelation that = (OrgUnitProgramRelation) o;

        if (id_orgunit_program_relation != that.id_orgunit_program_relation) return false;
        if (id_org_unit != null ? !id_org_unit.equals(that.id_org_unit) : that.id_org_unit != null)
            return false;
        return !(id_program != null ? !id_program.equals(that.id_program) : that.id_program != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_orgunit_program_relation ^ (id_orgunit_program_relation >>> 32));
        result = 31 * result + (id_org_unit != null ? id_org_unit.hashCode() : 0);
        result = 31 * result + (id_program != null ? id_program.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnitProgramRelation{" +
                "id_orgunit_program_relation=" + id_orgunit_program_relation +
                ", id_org_unit=" + id_org_unit +
                ", id_program=" + id_program +
                '}';
    }
}

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

package org.eyeseetea.malariacare.data.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

/**
 * Created by ivan.arrizabalaga on 14/02/15.
 */
@Table(database = AppDatabase.class)
public class OrgUnitProgramRelation extends BaseModel {

    public static final int DEFAULT_PRODUCTIVITY = 0;

    @Column
    @PrimaryKey(autoincrement = true)
    long id_orgunit_program_relation;

    @Column
    Long id_org_unit_fk;

    /**
     * Reference to lazy orgUnit
     */
    OrgUnit orgUnit;

    @Column
    Long id_program_fk;

    /**
     * Reference to lazy program
     */
    Program program;

    /**
     * Productivity of this relationship based on orgunit + program attributes
     */
    @Column
    Integer productivity;

    public OrgUnitProgramRelation() {
    }

    public OrgUnitProgramRelation(OrgUnit orgUnit, Program program) {
        setOrgUnit(orgUnit);
        setProgram(program);
        this.productivity = DEFAULT_PRODUCTIVITY;
    }

    public OrgUnitProgramRelation(OrgUnit orgUnit, Program program, Integer productivity) {
        this(orgUnit,program);
        this.productivity=productivity;
    }

    public OrgUnit getOrgUnit() {
        if(orgUnit==null){
            if(id_org_unit_fk==null) return null;
            orgUnit = new Select()
                    .from(OrgUnit.class)
                    .where(OrgUnit_Table.id_org_unit
                            .is(id_org_unit_fk)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit= orgUnit;
        this.id_org_unit_fk = (orgUnit!=null)?orgUnit.getId_org_unit():null;
    }

    public void setOrgUnit(Long id_org_unit){
        this.id_org_unit_fk = id_org_unit;
        this.orgUnit = null;
    }

    public Program getProgram() {
        if(program==null){
            if(id_program_fk==null) return null;
            program = new Select()
                    .from(Program.class)
                    .where(Program_Table.id_program
                            .is(id_program_fk)).querySingle();
        }
        return program;
    }

    public void setProgram(Program program) {
        this.program=program;
        this.id_program_fk = (program!=null)?program.getId_program():null;
    }

    public void setProgram(Long id_program){
        this.id_program_fk = id_program;
        this.program = null;
    }

    public Integer getProductivity() {
        return productivity;
    }

    public void setProductivity(Integer productivity) {
        this.productivity = productivity;
    }

    public static Integer getDefaultProductivity() {
        return DEFAULT_PRODUCTIVITY;
    }
    /**
     * Helper method to get the productivity for a given survey.
     * If its orgunit + program combination does NOT have a productivity value then returns 0
     * @return
     */
    public static Integer getProductivity(Long surveyId, Long orgUnitId, Long programId){
        if(surveyId==null){
            return DEFAULT_PRODUCTIVITY;
        }


        if(orgUnitId==null){
            return DEFAULT_PRODUCTIVITY;
        }


        if(programId==null){
            return DEFAULT_PRODUCTIVITY;
        }

        OrgUnitProgramRelation orgUnitProgramRelation = new Select().from(OrgUnitProgramRelation.class)
                .where(OrgUnitProgramRelation_Table.id_org_unit_fk.eq(orgUnitId))
                .and(OrgUnitProgramRelation_Table.id_program_fk.eq(programId)).querySingle();

        if(orgUnitProgramRelation==null){
            return DEFAULT_PRODUCTIVITY;
        }

        return orgUnitProgramRelation.getProductivity();
    }

    public long getId_orgunit_program_relation() {
        return id_orgunit_program_relation;
    }

    public void setId_orgunit_program_relation(long id_orgunit_program_relation) {
        this.id_orgunit_program_relation = id_orgunit_program_relation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitProgramRelation that = (OrgUnitProgramRelation) o;

        if (id_orgunit_program_relation != that.id_orgunit_program_relation) return false;
        if (id_org_unit_fk != null ? !id_org_unit_fk.equals(that.id_org_unit_fk) : that.id_org_unit_fk != null)
            return false;
        if (id_program_fk != null ? !id_program_fk.equals(that.id_program_fk) : that.id_program_fk != null)
            return false;
        return !(productivity != null ? !productivity.equals(that.productivity) : that.productivity != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_orgunit_program_relation ^ (id_orgunit_program_relation >>> 32));
        result = 31 * result + (id_org_unit_fk != null ? id_org_unit_fk.hashCode() : 0);
        result = 31 * result + (id_program_fk != null ? id_program_fk.hashCode() : 0);
        result = 31 * result + (productivity != null ? productivity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnitProgramRelation{" +
                "id_orgunit_program_relation=" + id_orgunit_program_relation +
                ", id_org_unit_fk=" + id_org_unit_fk +
                ", id_program_fk=" + id_program_fk +
                ", productivity=" + productivity +
                '}';
    }
}

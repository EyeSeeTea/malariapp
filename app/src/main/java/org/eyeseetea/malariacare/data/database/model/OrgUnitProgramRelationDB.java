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

import java.util.List;

/**
 * Created by ivan.arrizabalaga on 14/02/15.
 */
@Table(database = AppDatabase.class, name = "OrgUnitProgramRelation")
public class OrgUnitProgramRelationDB extends BaseModel {

    public static final int DEFAULT_PRODUCTIVITY = 0;

    @Column
    @PrimaryKey(autoincrement = true)
    long id_orgunit_program_relation;

    @Column
    Long id_org_unit_fk;

    /**
     * Reference to lazy orgUnit
     */
    OrgUnitDB orgUnit;

    @Column
    Long id_program_fk;

    /**
     * Reference to lazy program
     */
    ProgramDB program;

    /**
     * Productivity of this relationship based on orgunit + program attributes
     */
    @Column
    Integer productivity;

    public OrgUnitProgramRelationDB() {
    }

    public OrgUnitProgramRelationDB(OrgUnitDB orgUnit, ProgramDB program) {
        setOrgUnit(orgUnit);
        setProgram(program);
        this.productivity = DEFAULT_PRODUCTIVITY;
    }

    public OrgUnitProgramRelationDB(OrgUnitDB orgUnit, ProgramDB program, Integer productivity) {
        this(orgUnit, program);
        this.productivity = productivity;
    }

    public static List<OrgUnitProgramRelationDB> list() {
        return new Select().from(OrgUnitProgramRelationDB.class).queryList();
    }

    public Long getId_org_unit_fk() {
        return id_org_unit_fk;
    }

    public OrgUnitDB getOrgUnit() {
        if (orgUnit == null) {
            if (id_org_unit_fk == null) return null;
            orgUnit = new Select()
                    .from(OrgUnitDB.class)
                    .where(OrgUnitDB_Table.id_org_unit
                            .is(id_org_unit_fk)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnitDB orgUnit) {
        this.orgUnit = orgUnit;
        this.id_org_unit_fk = (orgUnit != null) ? orgUnit.getId_org_unit() : null;
    }

    public void setOrgUnit(Long id_org_unit) {
        this.id_org_unit_fk = id_org_unit;
        this.orgUnit = null;
    }

    public Long getId_program_fk() {
        return id_program_fk;
    }

    public ProgramDB getProgram() {
        if (program == null) {
            if (id_program_fk == null) return null;
            program = new Select()
                    .from(ProgramDB.class)
                    .where(ProgramDB_Table.id_program
                            .is(id_program_fk)).querySingle();
        }
        return program;
    }

    public void setProgram(ProgramDB program) {
        this.program = program;
        this.id_program_fk = (program != null) ? program.getId_program() : null;
    }

    public void setProgram(Long id_program) {
        this.id_program_fk = id_program;
        this.program = null;
    }

    public Integer getProductivity() {
        return productivity;
    }

    public void setProductivity(Integer productivity) {
        this.productivity = productivity;
    }

    /**
     * Helper method to get the productivity for a given survey.
     * If its orgunit + program combination does NOT have a productivity value then returns 0
     */
    public static Integer getProductivity(SurveyDB survey) {
        if (survey == null) {
            return DEFAULT_PRODUCTIVITY;
        }

        OrgUnitDB orgUnit = survey.getOrgUnit();
        if (orgUnit == null) {
            return DEFAULT_PRODUCTIVITY;
        }

        ProgramDB program = survey.getProgram();
        if (program == null) {
            return DEFAULT_PRODUCTIVITY;
        }

        OrgUnitProgramRelationDB
                orgUnitProgramRelation = new Select().from(OrgUnitProgramRelationDB.class)
                .where(OrgUnitProgramRelationDB_Table.id_org_unit_fk.eq(orgUnit.getId_org_unit()))
                .and(OrgUnitProgramRelationDB_Table.id_program_fk.eq(
                        program.getId_program())).querySingle();

        if (orgUnitProgramRelation == null) {
            return DEFAULT_PRODUCTIVITY;
        }

        return orgUnitProgramRelation.getProductivity();
    }

    public static List<OrgUnitProgramRelationDB> getAll() {
        return new Select()
                .from(OrgUnitProgramRelationDB.class)
                .queryList();
    }

    public long getId_orgunit_program_relation() {
        return id_orgunit_program_relation;
    }

    public void setId_orgunit_program_relation(long id_orgunit_program_relation) {
        this.id_orgunit_program_relation = id_orgunit_program_relation;
    }

    public static boolean existProgramAndOrgUnitRelation(String programUid, String orgUnitUid) {
        OrgUnitProgramRelationDB
                orgUnitProgramRelation =
                new Select().from(OrgUnitProgramRelationDB.class)
                        .leftOuterJoin(OrgUnitDB.class)
                        .on(OrgUnitProgramRelationDB_Table.id_org_unit_fk.eq(
                                OrgUnitDB_Table.id_org_unit))
                        .leftOuterJoin(ProgramDB.class)
                        .on(OrgUnitProgramRelationDB_Table.id_program_fk.eq(
                                ProgramDB_Table.id_program))
                        .where(OrgUnitDB_Table.uid_org_unit.eq(orgUnitUid))
                        .and(ProgramDB_Table.uid_program.eq(programUid)).querySingle();
        return (orgUnitProgramRelation != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitProgramRelationDB that = (OrgUnitProgramRelationDB) o;

        if (id_orgunit_program_relation != that.id_orgunit_program_relation) return false;
        if (id_org_unit_fk != null ? !id_org_unit_fk.equals(that.id_org_unit_fk)
                : that.id_org_unit_fk != null) {
            return false;
        }
        if (id_program_fk != null ? !id_program_fk.equals(that.id_program_fk)
                : that.id_program_fk != null) {
            return false;
        }
        return !(productivity != null ? !productivity.equals(that.productivity)
                : that.productivity != null);

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


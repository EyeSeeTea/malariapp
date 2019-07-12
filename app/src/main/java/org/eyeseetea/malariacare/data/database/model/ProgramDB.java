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
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

@Table(database = AppDatabase.class, name = "Program")
public class ProgramDB extends BaseModel{

    public static String DEFAULT_PROGRAM_DELTA_MATRIX = "6,6;4,4;4,2";

    @Column
    @PrimaryKey(autoincrement = true)
    long id_program;
    @Column
    String uid_program;
    @Column
    String name;
    @Column
    String stage_uid;

    @Column
    String next_schedule_delta_matrix;

    /**
     * List of tabs that belongs to this programstage
     */
    List<TabDB> tabs;

    /**
     * List of orgUnit authorized for this program
     */
    List<OrgUnitDB> orgUnits;

    public ProgramDB() {
    }

    public ProgramDB(String name) {
        this.name = name;
    }

    public ProgramDB(String uid, String name) {
        this.uid_program = uid;
        this.name = name;
    }

    public Long getId_program() {
        return id_program;
    }

    public void setId_program(Long id_program) {
        this.id_program = id_program;
    }

    public String getUid() {
        return uid_program;
    }

    public void setUid(String uid) {
        this.uid_program = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStageUid() {
        return stage_uid;
    }

    public void setStageUid(String stage_uid) {
        this.stage_uid = stage_uid;
    }

    public String getNextScheduleDeltaMatrix() {
        return next_schedule_delta_matrix;
    }

    public void setNextScheduleDeltaMatrix(String nextScheduleDeltaMatrix) {
        this.next_schedule_delta_matrix = nextScheduleDeltaMatrix;
    }

    public static List<ProgramDB> getAllPrograms(){
        return new Select().from(ProgramDB.class)
                .orderBy(ProgramDB_Table.name, true).queryList();
    }

    public static ProgramDB getProgram(String uid) {
        ProgramDB program = new Select()
                .from(ProgramDB.class)
                .where(ProgramDB_Table.uid_program
                        .is(uid)).querySingle();
        return program;
    }

    public List<OrgUnitDB> getOrgUnits(){
        if(orgUnits==null){
            List<OrgUnitProgramRelationDB> orgUnitProgramRelations = new Select().from(OrgUnitProgramRelationDB.class)
                    .where(OrgUnitProgramRelationDB_Table.id_program_fk.eq(this.getId_program()))
                    .queryList();
            this.orgUnits = new ArrayList<>();
            for(OrgUnitProgramRelationDB programRelation:orgUnitProgramRelations){
                orgUnits.add(programRelation.getOrgUnit());
            }
        }
        return orgUnits;
    }

    public List<TabDB> getTabs(){
        if (tabs==null){
            tabs=new Select().from(TabDB.class)
                    .where(TabDB_Table.id_program_fk.eq(this.getId_program()))
                    .orderBy(OrderBy.fromProperty(TabDB_Table.order_pos)).queryList();
        }
        return tabs;
    }

    public OrgUnitProgramRelationDB addOrgUnit(OrgUnitDB orgUnit){
        //Null -> nothing
        if(orgUnit==null){
            return null;
        }

        //Save a new relationship
        OrgUnitProgramRelationDB orgUnitProgramRelation = new OrgUnitProgramRelationDB(orgUnit,this);
        orgUnitProgramRelation.save();

        //Clear cache to enable reloading
        orgUnits=null;
        return orgUnitProgramRelation;
    }

    /**
     * List all programs
     * @return
     */
    public static List<ProgramDB> list() {
        return new Select().from(ProgramDB.class).orderBy(ProgramDB_Table.name, true).queryList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgramDB)) return false;

        ProgramDB program = (ProgramDB) o;

        if (id_program != program.id_program) return false;
        if (uid_program != null ? !uid_program.equals(program.uid_program) : program.uid_program != null) return false;
        return name.equals(program.name);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_program ^ (id_program >>> 32));
        result = 31 * result + (uid_program != null ? uid_program.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + (stage_uid != null ? stage_uid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Program{" +
                "id=" + id_program +
                ", uid_program='" + uid_program + '\'' +
                ", name='" + name + '\'' +
                ", stage_uid='" + stage_uid + '\'' +
                '}';
    }
}

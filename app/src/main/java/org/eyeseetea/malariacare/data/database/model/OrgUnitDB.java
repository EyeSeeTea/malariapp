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


import static org.eyeseetea.malariacare.data.database.AppDatabase.orgUnitProgramRelationAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.orgUnitProgramRelationName;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programAlias;
import static org.eyeseetea.malariacare.data.database.AppDatabase.programName;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.data.database.AppDatabase;

import java.util.List;

@Table(database = AppDatabase.class, name = "OrgUnit")
public class OrgUnitDB extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_org_unit;
    @Column
    String uid_org_unit;
    @Column
    String name;
    @Column
    Long id_org_unit_parent;

    /**
     * Refernce to parent orgUnit (loaded lazily)
     */
    OrgUnitDB orgUnit;

    @Column
    Long id_org_unit_level_fk;

    /**
     * Reference to the level of this orgUnit (loaded lazily)
     */
    OrgUnitLevelDB orgUnitLevel;

    /**
     * List of surveys that belong to this orgunit
     */
    List<SurveyDB> surveys;

    /**
     * List of orgUnits that belong to this one
     */
    List<OrgUnitDB> children;

    /**
     * List of program authorized for this orgunit
     */
    List<ProgramDB> programs;

    public OrgUnitDB() {
    }

    public OrgUnitDB(String name) {
        this();
        this.name = name;
    }


    public OrgUnitDB(String uid, String name, OrgUnitDB orgUnit, OrgUnitLevelDB orgUnitLevel) {
        this(name);
        this.uid_org_unit = uid;
        this.setOrgUnit(orgUnit);
        this.setOrgUnitLevel(orgUnitLevel);
    }

    public Long getId_org_unit() {
        return id_org_unit;
    }

    public void setId_org_unit(Long id_org_unit) {
        this.id_org_unit = id_org_unit;
    }

    public Long getId_org_unit_level_fk() {
        return id_org_unit_level_fk;
    }

    public String getUid() {
        return uid_org_unit;
    }

    public void setUid(String uid) {
        this.uid_org_unit = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrgUnitDB getOrgUnit() {
        if(orgUnit==null){
            if (this.id_org_unit_parent == null) return null;
            orgUnit = new Select()
                    .from(OrgUnitDB.class)
                    .where(OrgUnitDB_Table.id_org_unit
                            .is(id_org_unit_parent)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnitDB orgUnit) {
        this.orgUnit = orgUnit;
        this.id_org_unit_parent = (orgUnit!=null)?orgUnit.getId_org_unit():null;
    }

    public void setOrgUnit(Long id_parent){
        this.id_org_unit_parent = id_parent;
        this.orgUnit = null;
    }

    public OrgUnitLevelDB getOrgUnitLevel() {
        if(orgUnitLevel==null){
            if (this.id_org_unit_level_fk==null) return null;
            orgUnitLevel  = new Select()
                    .from(OrgUnitLevelDB.class)
                    .where(OrgUnitLevelDB_Table.id_org_unit_level
                            .is(id_org_unit_level_fk)).querySingle();
        }
        return orgUnitLevel;
    }

    public void setOrgUnitLevel(OrgUnitLevelDB orgUnitLevel) {
        this.orgUnitLevel = orgUnitLevel;
        this.id_org_unit_level_fk = (orgUnitLevel!=null)?orgUnitLevel.getId_org_unit_level():null;
    }

    public void setOrgUnitLevel(Long id_org_unit_level){
        this.id_org_unit_level_fk = id_org_unit_level;
        this.orgUnitLevel = null;
    }

    public List<OrgUnitDB> getChildren(){
        if(this.children==null){
            this.children = new Select().from(OrgUnitDB.class)
                    .where(OrgUnitDB_Table.id_org_unit_parent.eq(this.getId_org_unit())).queryList();
        }
        return children;
    }

    public List<OrgUnitDB> getChildrenOrderedByName(){
        if(this.children==null){
            this.children = new Select().from(OrgUnitDB.class)
                    .where(OrgUnitDB_Table.id_org_unit_parent.eq(this.getId_org_unit())).orderBy(
                            OrderBy.fromProperty(OrgUnitDB_Table.name)).queryList();
        }
        return children;
    }

    public List<SurveyDB> getSurveys(){
        if(this.surveys==null){
            this.surveys = new Select().from(SurveyDB.class)
                    .where(SurveyDB_Table.id_org_unit_fk.eq(this.getId_org_unit())).queryList();
        }
        return surveys;
    }

    /**
     * List of programs related to this orgunit order by name
     * @return
     */
    public List<ProgramDB> getPrograms(){
        if(programs==null){
            this.programs=new Select().from(ProgramDB.class).as(programName)
                    .join(OrgUnitProgramRelationDB.class, Join.JoinType.LEFT_OUTER).as(orgUnitProgramRelationName)
                    .on(ProgramDB_Table.id_program.withTable(programAlias)
                            .eq(OrgUnitProgramRelationDB_Table.id_program_fk.withTable(orgUnitProgramRelationAlias))
                    ).where(OrgUnitProgramRelationDB_Table.id_org_unit_fk.withTable(orgUnitProgramRelationAlias).eq(this.getId_org_unit()))
                    .orderBy(ProgramDB_Table.name.withTable(programAlias), true)
                    .queryList();
        }
        return programs;
    }

    public OrgUnitProgramRelationDB getRelation(ProgramDB program){
        return new Select().from(OrgUnitProgramRelationDB.class)
                .where(OrgUnitProgramRelationDB_Table.id_org_unit_fk.eq(this.getId_org_unit()))
                .and(OrgUnitProgramRelationDB_Table.id_program_fk.eq(program.getId_program())).querySingle();
    }

    public Integer getProductivity(ProgramDB program){
        if (getRelation(program) == null) return OrgUnitProgramRelationDB.DEFAULT_PRODUCTIVITY;
        return getRelation(program).getProductivity();
    }

    public void setProductivity(ProgramDB program, Integer productivity){
        getRelation(program).setProductivity(productivity);
    }

    public static List<OrgUnitDB> getAllOrgUnit() {
        return new Select().from(OrgUnitDB.class)
                .orderBy(OrgUnitDB_Table.name, true).queryList();
    }
    /**
     * Returns the UID of an orgUnit with the given name
     *
     * @param name Name of the orgunit
     */
    public static String findUIDByName(String name) {
        OrgUnitDB orgUnit = new Select().from(OrgUnitDB.class)
                .where(OrgUnitDB_Table.name.eq(name)).querySingle();
        if (orgUnit == null) {
            return null;
        }
        return orgUnit.getUid();
    }
    public OrgUnitProgramRelationDB addProgram(ProgramDB program){
        //Null -> nothing
        if(program==null){
            return null;
        }

        //Save a new relationship
        OrgUnitProgramRelationDB orgUnitProgramRelation = new OrgUnitProgramRelationDB(this,program);
        orgUnitProgramRelation.save();

        //Clear cache to enable reloading
        programs=null;
        return orgUnitProgramRelation;
    }

    /**
     * Returns all orgunits
     * @return
     */
    public static List<OrgUnitDB> list(){
        return new Select().from(OrgUnitDB.class).orderBy(OrgUnitDB_Table.id_org_unit_level_fk,true).orderBy(


                OrgUnitDB_Table.name, true).queryList();
    }

    public static OrgUnitDB getOrgUnit(String uid) {
            OrgUnitDB orgUnit = new Select()
                    .from(OrgUnitDB.class)
                    .where(OrgUnitDB_Table.uid_org_unit
                            .is(uid)).querySingle();
        return orgUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnitDB orgUnit = (OrgUnitDB) o;

        if (id_org_unit != orgUnit.id_org_unit) return false;
        if (uid_org_unit != null ? !uid_org_unit.equals(orgUnit.uid_org_unit) : orgUnit.uid_org_unit != null) return false;
        if (name != null ? !name.equals(orgUnit.name) : orgUnit.name != null) return false;
        if (id_org_unit_parent != null ? !id_org_unit_parent.equals(orgUnit.id_org_unit_parent) : orgUnit.id_org_unit_parent != null)
            return false;
        return !(id_org_unit_level_fk != null ? !id_org_unit_level_fk.equals(orgUnit.id_org_unit_level_fk) : orgUnit.id_org_unit_level_fk != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_org_unit ^ (id_org_unit >>> 32));
        result = 31 * result + (uid_org_unit != null ? uid_org_unit.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id_org_unit_parent != null ? id_org_unit_parent.hashCode() : 0);
        result = 31 * result + (id_org_unit_level_fk != null ? id_org_unit_level_fk.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "id_org_unit=" + id_org_unit +
                ", uid_org_unit='" + uid_org_unit + '\'' +
                ", name='" + name + '\'' +
                ", id_org_unit_parent=" + id_org_unit_parent +
                ", id_org_unit_level_fk=" + id_org_unit_level_fk +
                '}';
    }
}

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
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.IConvertToSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.exporter.VisitableToSDK;

import java.util.ArrayList;
import java.util.List;

@Table(databaseName = AppDatabase.NAME)
public class OrgUnit extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id_org_unit;
    @Column
    String uid;
    @Column
    String name;
    @Column
    Long id_parent;
    @Column
    Integer productivity;

    /**
     * Refernce to parent orgUnit (loaded lazily)
     */
    OrgUnit orgUnit;

    @Column
    Long id_org_unit_level;

    /**
     * Reference to the level of this orgUnit (loaded lazily)
     */
    OrgUnitLevel orgUnitLevel;

    /**
     * List of surveys that belong to this orgunit
     */
    List<Survey> surveys;

    /**
     * List of orgUnits that belong to this one
     */
    List<OrgUnit> children;

    /**
     * List of program authorized for this orgunit
     */
    List<Program> programs;

    public OrgUnit() {
    }

    public OrgUnit(String name) {
        this();
        this.name = name;
    }


    public OrgUnit(String uid, String name, OrgUnit orgUnit, OrgUnitLevel orgUnitLevel) {
        this(name);
        this.uid = uid;
        this.setOrgUnit(orgUnit);
        this.setOrgUnitLevel(orgUnitLevel);
    }

    public Long getId_org_unit() {
        return id_org_unit;
    }

    public void setId_org_unit(Long id_org_unit) {
        this.id_org_unit = id_org_unit;
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

    public OrgUnit getOrgUnit() {
        if(orgUnit==null){
            if (this.id_parent == null) return null;
            orgUnit = new Select()
                    .from(OrgUnit.class)
                    .where(Condition.column(OrgUnit$Table.ID_ORG_UNIT)
                            .is(id_parent)).querySingle();
        }
        return orgUnit;
    }

    public void setOrgUnit(OrgUnit orgUnit) {
        this.orgUnit = orgUnit;
        this.id_parent = (orgUnit!=null)?orgUnit.getId_org_unit():null;
    }

    public void setOrgUnit(Long id_parent){
        this.id_parent = id_parent;
        this.orgUnit = null;
    }

    public OrgUnitLevel getOrgUnitLevel() {
        if(orgUnitLevel==null){
            if (this.id_org_unit_level==null) return null;
            orgUnitLevel  = new Select()
                    .from(OrgUnitLevel.class)
                    .where(Condition.column(OrgUnitLevel$Table.ID_ORG_UNIT_LEVEL)
                            .is(id_org_unit_level)).querySingle();
        }
        return orgUnitLevel;
    }

    public void setOrgUnitLevel(OrgUnitLevel orgUnitLevel) {
        this.orgUnitLevel = orgUnitLevel;
        this.id_org_unit_level = (orgUnitLevel!=null)?orgUnitLevel.getId_org_unit_level():null;
    }

    public void setOrgUnitLevel(Long id_org_unit_level){
        this.id_org_unit_level = id_org_unit_level;
        this.orgUnitLevel = null;
    }

    public List<OrgUnit> getChildren(){
        if(this.children==null){
            this.children = new Select().from(OrgUnit.class)
                    .where(Condition.column(OrgUnit$Table.ID_PARENT).eq(this.getId_org_unit())).queryList();
        }
        return children;
    }

    public List<Survey> getSurveys(){
        if(this.surveys==null){
            this.surveys = new Select().from(Survey.class)
                    .where(Condition.column(Survey$Table.ID_ORG_UNIT).eq(this.getId_org_unit())).queryList();
        }
        return surveys;
    }

    public List<Program> getPrograms(){
        if(programs==null){
            List<OrgUnitProgramRelation> orgUnitProgramRelations = new Select().from(OrgUnitProgramRelation.class)
                    .where(Condition.column(OrgUnitProgramRelation$Table.ID_ORG_UNIT).eq(this.getId_org_unit()))
                    .queryList();
            this.programs= new ArrayList<>();
            for(OrgUnitProgramRelation programRelation:orgUnitProgramRelations){
                programs.add(programRelation.getProgram());
            }
        }
        return programs;
    }

    public static List<OrgUnit> getAllOrgUnit() {
        return new Select().all().from(OrgUnit.class).queryList();
    }

    public void addProgram(Program program){
        //Null -> nothing
        if(program==null){
            return;
        }

        //Save a new relationship
        OrgUnitProgramRelation orgUnitProgramRelation = new OrgUnitProgramRelation(this,program);
        orgUnitProgramRelation.save();

        //Clear cache to enable reloading
        programs=null;
    }

    public Integer getProductivity(){
        return productivity;
    }

    public void setProductivity(Integer productivity){
        this.productivity=productivity;
    }

    public boolean isLowProductivity(){
        return productivity<5;
    }

    /**
     * Returns all orgunits
     * @return
     */
    public static List<OrgUnit> list(){
        return new Select().from(OrgUnit.class).queryList();
    }

    public static OrgUnit getOrgUnit(String uid) {
            OrgUnit orgUnit = new Select()
                    .from(OrgUnit.class)
                    .where(Condition.column(OrgUnit$Table.UID)
                            .is(uid)).querySingle();
        return orgUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrgUnit orgUnit = (OrgUnit) o;

        if (id_org_unit != orgUnit.id_org_unit) return false;
        if (uid != null ? !uid.equals(orgUnit.uid) : orgUnit.uid != null) return false;
        if (name != null ? !name.equals(orgUnit.name) : orgUnit.name != null) return false;
        if (productivity != null ? !productivity.equals(orgUnit.productivity) : orgUnit.productivity != null) return false;
        if (id_parent != null ? !id_parent.equals(orgUnit.id_parent) : orgUnit.id_parent != null)
            return false;
        return !(id_org_unit_level != null ? !id_org_unit_level.equals(orgUnit.id_org_unit_level) : orgUnit.id_org_unit_level != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (id_org_unit ^ (id_org_unit >>> 32));
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (id_parent != null ? id_parent.hashCode() : 0);
        result = 31 * result + (productivity != null ? productivity.hashCode() : 0);
        result = 31 * result + (id_org_unit_level != null ? id_org_unit_level.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "OrgUnit{" +
                "id_org_unit=" + id_org_unit +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", id_parent=" + id_parent +
                ", id_org_unit_level=" + id_org_unit_level +
                ", productivity=" + productivity +
                '}';
    }
}

/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.eyeseetea.malariacare.database.iomodules.dhis.importer.models;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Question$Table;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.model.Value$Table;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.Attribute$Table;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit$Table;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitAttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitDataSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitDataSet$Table;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitGroup;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitGroup$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;

import java.util.List;

public class OrganisationUnitExtended implements VisitableFromSDK {

    private static final String TAG = ".OUExtended";

    /**
     * Hardcoded 'code' of the attribute that holds the array of productivities in the server
     */

    public static final String ORGANISATION_UNIT_PRODUCTIVITY_VALUE_ATTRIBUTE_CODE = "OUPV";

    /**
     * sdk organisation unit reference
     */
    OrganisationUnit orgUnit;

    /**
     * Productivity array values
     */
    String productivityArray;

    /**
     * App orgunit reference
     */
    private OrgUnit appOrgUnit;

    public OrganisationUnitExtended(){}

    public OrganisationUnitExtended(OrganisationUnit orgUnit){
        this.orgUnit = orgUnit;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public OrganisationUnit getOrgUnit() {
        return orgUnit;
    }

    /**
     * Returns the productivity value por the given position index
     * @param position
     * @return
     */
    public Integer getProductivity(Integer position){
        //No position -> no productivity
        if(position==null || position<0){
            return 0;
        }

        //First time array is loaded
        if(productivityArray==null){
            loadProductivityArray();
        }
        //Data is not configured properly
        if(position>=productivityArray.length()){
            return 0;
        }
        //Get value from position
        try{
            return Integer.parseInt(productivityArray.substring(position,position+1));
        }catch(Exception ex){
            Log.e(TAG, String.format("getProductivity(%d)-> %s", position, ex.getMessage()));
            return 0;
        }

    }

    /**
     * Loads the array value for later reuse
     */
    private void loadProductivityArray(){
        productivityArray=findOrganisationUnitAttributeValueByCode(ORGANISATION_UNIT_PRODUCTIVITY_VALUE_ATTRIBUTE_CODE);
    }

    /**
     * Finds the value of an attribute with the given code in a dataElement
     * @param code
     * @return
     */
    public  String findOrganisationUnitAttributeValueByCode(String code){

        OrganisationUnitAttributeValue organisationUnitAttributeValue = new Select().from(OrganisationUnitAttributeValue.class).as("o")
                .join(Attribute.class, Join.JoinType.LEFT).as("a")
                .on(Condition.column(ColumnAlias.columnWithTable("o", OrganisationUnitAttributeValue$Table.ATTRIBUTEID))
                        .eq(ColumnAlias.columnWithTable("a", Attribute$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("a", Attribute$Table.CODE))
                        .eq(code))
                .and(Condition.column(ColumnAlias.columnWithTable("o", OrganisationUnitAttributeValue$Table.ORGANISATIONUNIT)).is(this.getOrgUnit().getId()))
                .querySingle();

        if(organisationUnitAttributeValue==null){
            return "";
        }
        return organisationUnitAttributeValue.getValue();
    }

    /**
     * App orgUnit setter
     * @param appOrgUnit
     */
    public void setAppOrgUnit(OrgUnit appOrgUnit) {
        this.appOrgUnit = appOrgUnit;
    }

    /**
     * App orgUnit setter
     * @return
     */
    public OrgUnit getAppOrgUnit() {
        return appOrgUnit;
    }

    public static List<OrganisationUnit> getAllOrganisationUnits() {
        return new Select().all().from(OrganisationUnit.class).queryList();
    }

    /**
     * Get all the OU DataSets given a OU id
     * @param id
     * @return
     */
    public static List<OrganisationUnitDataSet> getOrganisationUnitDataSets(String id){
        return new Select()
                .from(OrganisationUnitDataSet.class)
                .where(Condition.column(OrganisationUnitDataSet$Table.ORGANISATIONUNITID)
                        .eq(id))
                .queryList();
    }

    /**
     * Get all the OU groups given a OU id
     * @param id
     * @return
     */
    public static List<OrganisationUnitGroup> getOrganisationUnitGroups(String id){
        return new Select()
                .from(OrganisationUnitGroup.class)
                .where(Condition.column(OrganisationUnitGroup$Table.ORGANISATIONUNITID)
                        .eq(id))
                .queryList();
    }

    /**
     * Get an OU given its id
     * @param id
     * @return
     */
    public static OrganisationUnit getOrganisationUnit(String id){
        return new Select()
                .from(OrganisationUnit.class)
                .where(Condition.column(OrganisationUnit$Table.ID)
                        .eq(id))
                .querySingle();
    }
}

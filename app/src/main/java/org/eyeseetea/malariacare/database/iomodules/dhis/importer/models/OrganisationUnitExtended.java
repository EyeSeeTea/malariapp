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

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitDataSetFlow;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitDataSetFlow_Table;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitGroupFlow;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitGroupFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow_Table;

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
    OrganisationUnitFlow organisationUnit;

    /**
     * Productivity array values
     */
    String productivityArray;

    /**
     * App orgunit reference
     */
    private OrgUnit appOrgUnit;

    public OrganisationUnitExtended(){}

    public OrganisationUnitExtended(OrganisationUnitFlow orgUnit){
        this.organisationUnit = orgUnit;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public OrganisationUnitFlow getOrganisationUnit() {
        return organisationUnit;
    }

    /**
     * Returns the productivity value por the given position index
     * @param position
     * @return
     */
    public Integer getProductivity(Integer position){
        //No position -> no productivity
        if(position==null || position<=0){
            return 0;
        }

        //First time array is loaded
        if(productivityArray==null){
            loadProductivityArray();
        }
        //Data is not configured properly
        if(position>productivityArray.length()){
            return 0;
        }
        //Get value from position
        try{
            return Integer.parseInt(productivityArray.substring(position-1,position));
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
        /*
        //// FIXME: 11/11/2016
        OrganisationUnitAttributeValue organisationUnitAttributeValue = new Select().from(OrganisationUnitAttributeValue.class).as("o")
                .join(Attribute.class, Join.JoinType.LEFT_OUTER).as("a")
                .on(Condition.column(ColumnAlias.columnWithTable("o", OrganisationUnitAttributeValue$Table.ATTRIBUTEID))
                        .eq(ColumnAlias.columnWithTable("a", Attribute$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("a", Attribute$Table.CODE))
                        .eq(code))
                .and(Condition.column(ColumnAlias.columnWithTable("o", OrganisationUnitAttributeValue$Table.ORGANISATIONUNIT)).is(this.getOrganisationUnit().getId()))
                .querySingle();

        if(organisationUnitAttributeValue==null){
            return "";
        }
        return organisationUnitAttributeValue.getValue();
        */
        return null;
    }

    /**
     * App organisationUnit setter
     * @param appOrgUnit
     */
    public void setAppOrgUnit(OrgUnit appOrgUnit) {
        this.appOrgUnit = appOrgUnit;
    }

    /**
     * App organisationUnit setter
     * @return
     */
    public OrgUnit getAppOrgUnit() {
        return appOrgUnit;
    }

    public static List<OrganisationUnitFlow> getAllOrganisationUnits() {
        return new Select().from(OrganisationUnitFlow.class).queryList();
    }

    /**
     * Get all the OU DataSets given a OU id
     * @param id
     * @return
     */
    public static List<OrganisationUnitDataSetFlow> getOrganisationUnitDataSets(String id){
        //// FIXME: 11/11/2016
        return new Select()
                .from(OrganisationUnitDataSetFlow.class)
                .where(OrganisationUnitDataSetFlow_Table.organisationUnitId
                        .eq(id))
                .queryList();
    }

    /**
     * Get all the OU groups given a OU id
     * @param id
     * @return
     */
    public static List<OrganisationUnitGroupFlow> getOrganisationUnitGroups(String id){
        return new Select()
                .from(OrganisationUnitGroupFlow.class)
                .where(OrganisationUnitGroupFlow_Table.organisationUnitId
                        .eq(id))
                .queryList();
    }

    /**
     * Get an OU given its id
     * @param id
     * @return
     */
    public static OrganisationUnitFlow getOrganisationUnit(String id){
        return new Select()
                .from(OrganisationUnitFlow.class)
                .where(OrganisationUnitFlow_Table.uId
                        .eq(id))
                .querySingle();
    }

    public int getLevel() {
        return organisationUnit.getLevel();
    }

    public String getLabel() {
        return organisationUnit.getDisplayName();
    }

    public String getName() {
        return organisationUnit.getName();
    }

    public String getUid() {
        return organisationUnit.getUId();
    }

    public String getPath() {
        //// TODO: 15/11/2016  create method in sdk
        //return organisationUnit.getPath();
        return null;
    }
}

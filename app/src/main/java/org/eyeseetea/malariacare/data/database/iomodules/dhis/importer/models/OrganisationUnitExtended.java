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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow_Table;

import java.util.ArrayList;
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
    private OrgUnitDB appOrgUnit;

    public OrganisationUnitExtended(){}

    public OrganisationUnitExtended(OrganisationUnitFlow orgUnit){
        this.organisationUnit = orgUnit;
    }

    public OrganisationUnitExtended(OrganisationUnitExtended orgUnit){
        this.organisationUnit = orgUnit.getOrganisationUnit();
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

    private List<AttributeValueFlow> getAttributeValues() {
        return organisationUnit.getAttributeValueFlow();
    }

    /**
     * Finds the value of an attribute with the given code in a dataElement
     * @param code
     * @return
     */
    public  String findOrganisationUnitAttributeValueByCode(String code){
        String value = AttributeValueExtended.findAttributeValueByCode(code, getAttributeValues());

        if(value==null){
            return "";
        }
        return value;
    }

    /**
     * App organisationUnit setter
     * @param appOrgUnit
     */
    public void setAppOrgUnit(OrgUnitDB appOrgUnit) {
        this.appOrgUnit = appOrgUnit;
    }

    /**
     * App organisationUnit setter
     * @return
     */
    public OrgUnitDB getAppOrgUnit() {
        return appOrgUnit;
    }

    public static List<OrganisationUnitFlow> getAllOrganisationUnits() {
        return new Select().from(OrganisationUnitFlow.class).queryList();
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
        return organisationUnit.getPath();
    }

    public String getId() {
        return organisationUnit.getUId();
    }

    public String getParent() {
        if(organisationUnit.getParent()==null)
            return null;
        return organisationUnit.getParent().getUId();
    }

    public static List<OrganisationUnitExtended> getExtendedList(
            List<OrganisationUnitFlow> flowList) {
        List <OrganisationUnitExtended> extendedsList = new ArrayList<>();
        for(OrganisationUnitFlow flowPojo:flowList){
            extendedsList.add(new OrganisationUnitExtended(flowPojo));
        }
        return extendedsList;
    }
}

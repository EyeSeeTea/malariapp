/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.data.remote.sdk.dataSources;

import android.content.Context;

import org.eyeseetea.dhis2.lightsdk.D2Response;
import org.eyeseetea.dhis2.lightsdk.attributes.AttributeValue;
import org.eyeseetea.dhis2.lightsdk.organisationunits.OrganisationUnit;
import org.eyeseetea.dhis2.lightsdk.organisationunits.OrganisationUnitLevel;
import org.eyeseetea.dhis2.lightsdk.programs.Program;
import org.eyeseetea.malariacare.data.boundaries.IMetadataRemoteDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.DhisFilter;
import org.eyeseetea.malariacare.domain.entity.OrgUnit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgUnitD2LightSDKDataSource
        extends D2LightSDKDataSource
        implements IMetadataRemoteDataSource<OrgUnit> {


    public OrgUnitD2LightSDKDataSource(Context context) {
        super(context);
    }

    @Override
    public List<OrgUnit> getAll(DhisFilter filter) throws Exception {

        D2Response<List<OrganisationUnit>> response =
                getD2Api().organisationUnits().getAll(filter.getUIds()).execute();

        if (response.isSuccess()) {
            D2Response.Success<List<OrganisationUnit>> success =
                    (D2Response.Success<List<OrganisationUnit>>) response;

            return mapToDomain(success.getValue());
        } else {
            D2Response.Error errorResponse = (D2Response.Error) response;

            handleError(errorResponse);
        }

        return null;
    }

    private List<OrgUnit> mapToDomain(
            List<OrganisationUnit> organisationUnits) throws Exception {
        List<OrgUnit> orgUnits = new ArrayList<>();

        Map<Integer, OrganisationUnitLevel> orgUnitLevelsMap = getAllOrgUnitLevels();

        for (OrganisationUnit organisationUnit : organisationUnits) {
            Map<String, Integer> productivityByProgram = getProductivityByProgram(organisationUnit);

            String orgUnitLevelUid = orgUnitLevelsMap.get(organisationUnit.getLevel()).getId();

            OrgUnit orgUnit = new OrgUnit(organisationUnit.getId(),
                    organisationUnit.getName(),
                    orgUnitLevelUid,
                    productivityByProgram);

            orgUnit.addRelatedPrograms(getRelatedProgramsUIds(organisationUnit));
            orgUnits.add(orgUnit);
        }

        return orgUnits;
    }

    private Map<Integer, OrganisationUnitLevel> getAllOrgUnitLevels() throws Exception {
        D2Response<List<OrganisationUnitLevel>> response =
                getD2Api().organisationUnitLevels().getAll().execute();

        Map<Integer, OrganisationUnitLevel> organisationUnitLevelsMap = new HashMap<>();

        if (response.isSuccess()) {
            D2Response.Success<List<OrganisationUnitLevel>> success =
                    (D2Response.Success<List<OrganisationUnitLevel>>) response;
            for (OrganisationUnitLevel organisationUnitLevel:success.getValue()) {
                organisationUnitLevelsMap.put(organisationUnitLevel.getLevel(), organisationUnitLevel);
            }
            return organisationUnitLevelsMap;
        } else {
            D2Response.Error errorResponse = (D2Response.Error) response;

            handleError(errorResponse);
        }

        return null;
    }

    private Map<String, Integer> getProductivityByProgram(OrganisationUnit organisationUnit) {
        Map<String, Integer> productivityByProgram = new HashMap<>();

        String productivityAttCodeProgram = "PPP";
        String productivityAttCodeOrgUnit = "OUPV";

        AttributeValue productivityAttValueOrgUnit =
                findAttributeValueByCode(organisationUnit.getAttributeValues(),
                        productivityAttCodeOrgUnit);

        String productivityArray = null;

        if (productivityAttValueOrgUnit != null){
            productivityArray = productivityAttValueOrgUnit.getValue();
        }

        for (Program program:organisationUnit.getPrograms()) {
            int productivity = 0;

            if (productivityArray != null){
                AttributeValue productivityAttValueProgram =
                        findAttributeValueByCode(program.getAttributeValues(),
                                productivityAttCodeProgram);

                int productivityPosition = 0;

                if ( productivityAttValueProgram != null){
                    productivityPosition = Integer.parseInt(productivityAttValueProgram.getValue());

                    if(productivityPosition >=0 && productivityPosition <= productivityArray.length()){
                        try{
                            productivity  = Integer.parseInt(
                                    productivityArray.substring(
                                            productivityPosition-1,productivityPosition));
                        }catch(Exception ex){
                            productivity = 0;
                        }
                    }

                    productivityByProgram.put(program.getId(), productivity);
                }
            }

            productivityByProgram.put(program.getId(), productivity);
        }

        return productivityByProgram;
    }

    private AttributeValue findAttributeValueByCode(
            List<AttributeValue> attributeValues, String attributeCode) {
        AttributeValue attributeValue = null;

        for (AttributeValue attValue : attributeValues) {
            if (attValue.getAttribute() != null && attValue.getAttribute().getCode() != null &&
                    attValue.getAttribute().getCode().equals(attributeCode)) {
                attributeValue = attValue;
            }
        }

        return attributeValue;
    }

    private List<String> getRelatedProgramsUIds(OrganisationUnit organisationUnit) {
        List<String> programUids = new ArrayList<>();

        for (Program program : organisationUnit.getPrograms()) {
            programUids.add(program.getId());
        }

        return programUids;
    }
}
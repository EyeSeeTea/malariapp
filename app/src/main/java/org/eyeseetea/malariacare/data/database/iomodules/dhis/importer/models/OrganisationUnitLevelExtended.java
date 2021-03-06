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

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitLevelFlow;

import java.util.ArrayList;
import java.util.List;

public class OrganisationUnitLevelExtended implements VisitableFromSDK {
    OrganisationUnitLevelFlow organisationUnitLevel;

    public OrganisationUnitLevelExtended(){}

    public OrganisationUnitLevelExtended(OrganisationUnitLevelFlow organisationUnitLevel){
        this.organisationUnitLevel = organisationUnitLevel;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public OrganisationUnitLevelFlow getOrganisationUnitLevel() {
        return organisationUnitLevel;
    }

    /**
     * Builds a synthetic key to use the map while converting levels
     * Ex: buildKey(3) -> "OrganisationUnitLevel3"
     * @param level Number of the level which key must be built (Ex: 3)
     * @return The synthetic key (Ex: "OrganisationUnitLevel3")
     */
    public static String buildKey(int level){
        return OrganisationUnitLevelFlow.class.getSimpleName()+level;
    }

    /**
     * Builds a synthetic key to the current level
     * @return (Ex: "OrganisationUnitLevel3")
     */
    public String buildKey(){
        return OrganisationUnitLevelExtended.buildKey(organisationUnitLevel.getLevel());
    }

    public String getUid() {
        return organisationUnitLevel.getUId();
    }

    public String getDisplayName() {
        return organisationUnitLevel.getDisplayName();
    }

    public static List<OrganisationUnitLevelExtended> getExtendedList(List<OrganisationUnitLevelFlow> flowList) {
        List <OrganisationUnitLevelExtended> extendedsList = new ArrayList<>();
        for(OrganisationUnitLevelFlow flowPojo:flowList){
            extendedsList.add(new OrganisationUnitLevelExtended(flowPojo));
        }
        return extendedsList;
    }
}

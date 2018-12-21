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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by arrizabalaga on 6/11/15.
 */
public class AttributeValueExtended {

    AttributeValueFlow attributeValueFlow;

    static HashMap<String, HashMap<String, AttributeValueFlow>> attributeMap = new HashMap();

    public AttributeValueExtended(AttributeValueFlow attributeValueFlow) {
        this.attributeValueFlow = attributeValueFlow;
    }

    public static AttributeValueFlow findAttributeValuefromDataElementCode(String attributeCode, String uid) {
        if(attributeMap.size()==0){
            attributeMap = SdkQueries.createHashMapCodeAndReferenceForAttributeValues();
        }
        if(!attributeMap.containsKey(attributeCode)){
            return null;
        }
        return attributeMap.get(attributeCode).get(uid);
    }

    public String getUid() {
        return attributeValueFlow.getAttributeUId();
    }


    public AttributeValueFlow getAttribute() {
        return attributeValueFlow;
    }

    public static List<AttributeValueExtended> getExtendedList(List<AttributeValueFlow> flowList) {
        List<AttributeValueExtended> extendedsList = new ArrayList<>();
        for (AttributeValueFlow flowPojo : flowList) {
            extendedsList.add(new AttributeValueExtended(flowPojo));
        }
        return extendedsList;
    }
}

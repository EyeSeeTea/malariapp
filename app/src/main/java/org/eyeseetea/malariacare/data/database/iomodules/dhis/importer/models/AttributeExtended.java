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

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeValueFlow;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by arrizabalaga on 6/11/15.
 */
public class AttributeExtended {

    AttributeFlow attributeFlow;

    public AttributeExtended(AttributeFlow attributeFlow) {
        this.attributeFlow = attributeFlow;
    }

    public AttributeExtended(AttributeExtended attributeFlow) {
        this.attributeFlow = attributeFlow.getAttribute();
    }

    public String getCode() {
        return attributeFlow.getCode();
    }

    public String getUid() {
        return attributeFlow.getAttributeUId();
    }

    public AttributeFlow getAttribute() {
        return attributeFlow;
    }

    public AttributeExtended() {
    }

    /**
     * Find an attribute by its code
     */
    public static AttributeFlow findAttributeByCode(String code) {
        return new Select().from(AttributeFlow.class)
                .where(AttributeFlow_Table.code.is(code))
                .querySingle();
    }


    /**
     * Find the attribute in a dataelement for the given code
     */
    public static AttributeValueFlow findAttributeValuefromCode(String code,
            List<AttributeValueFlow> attributeValueList) {
        if (code == null) {
            return null;
        }
        for (AttributeValueFlow attributeValue : attributeValueList) {
            if (attributeValue.getAttribute().getCode() == null) {
                throw new RuntimeException(String.format(
                        PreferencesState.getInstance().getContext().getResources().getString(
                                R.string.dialog_error_attribute_null),
                        attributeValue.getAttributeUId()));
            }
            if (attributeValue.getAttribute().getCode().equals(code)) {
                return attributeValue;
            }
        }
        return null;
    }


    /**
     * Finds the value of an attribute with the given code in a dataElement
     */
    public static  String findAttributeValueByCode(String code, List<AttributeValueFlow> attributeValueList) {

        //Find the right attribute
        AttributeFlow attribute = AttributeExtended.findAttributeByCode(code);
        //No such attribute -> done
        if (attribute == null) {
            Log.d("AttributeValueExtended",
                    String.format("findAttributeByCode(): Attribute with %s not found", code));
            return null;
        }

        //Find its value for the given dataelement
        AttributeValueFlow attributeValue = AttributeValueExtended.findAttributeValue(attribute, attributeValueList);
        if (attributeValue == null) {
            return null;
        }
        return attributeValue.getValue();
    }
    public static List<AttributeExtended> getExtendedList(List<AttributeFlow> flowList) {
        List<AttributeExtended> extendedsList = new ArrayList<>();
        for (AttributeFlow flowPojo : flowList) {
            extendedsList.add(new AttributeExtended(flowPojo));
        }
        return extendedsList;
    }
}

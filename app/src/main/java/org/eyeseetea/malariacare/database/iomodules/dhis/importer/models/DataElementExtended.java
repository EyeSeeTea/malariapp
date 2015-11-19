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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer.models;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.Attribute$Table;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.Program;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class DataElementExtended implements VisitableFromSDK {

    DataElement dataElement;

    public DataElementExtended(DataElement dataElement){
        this.dataElement =dataElement;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }


    public DataElement getDataElement() {
        return dataElement;
    }

    /**
     * Find the attributevalue in a dataelement for the given attribute
     * @param attribute
     * @return
     */
    public AttributeValue findAttributeValue(Attribute attribute){
        return new Select().from(AttributeValue.class).as("av")
                .join(DataElementAttributeValue.class, Join.JoinType.LEFT).as("dea")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.ATTRIBUTEVALUE_ATTRIBUTEVALUEID))
                                .eq(ColumnAlias.columnWithTable("av", AttributeValue$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.DATAELEMENTID)).eq(dataElement.getUid()))
                        //For the given survey
                .and(Condition.column(ColumnAlias.columnWithTable("av", AttributeValue$Table.ATTRIBUTE_ATTRIBUTEID)).eq(attribute.getUid())).querySingle();
    }


    /**
     * Finds the value of an attribute with the given code in a dataElement
     * @param code
     * @return
     */
    public  String findAttributeValueByCode(String code){

        //Find the right attribute
        Attribute attribute= AttributeExtended.findAttributeByCode(code);
        //No such attribute -> done
        if(attribute==null){
            return null;
        }

        //Find its value for the given dataelement
        AttributeValue attributeValue=findAttributeValue(attribute);
        if(attributeValue==null){
            return null;
        }
        return attributeValue.getValue();
    }

    /**
     * Find the attribute in a dataelement for the given attribute
     * @param dataElement
     * @param code
     * @return
     */
    public AttributeValue findAttributeValuefromDataElementCode(String code,DataElement dataElement){
        //select * from Attribute join AttributeValue on Attribute.id = attributeValue.attributeId join DataElementAttributeValue on attributeValue.id=DataElementAttributeValue.attributeValueId where DataElementAttributeValue.dataElementId="vWgsPN1RPLl" and code="Order"
        return new Select().from(AttributeValue.class).as("av")
                .join(Attribute.class, Join.JoinType.LEFT).as("at")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("at", Attribute$Table.ID))
                                .eq(ColumnAlias.columnWithTable("av", AttributeValue$Table.ATTRIBUTE_ATTRIBUTEID)))
                .join(DataElementAttributeValue.class, Join.JoinType.LEFT).as("dea")
                .on(
                        Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.ATTRIBUTEVALUE_ATTRIBUTEVALUEID))
                                .eq(ColumnAlias.columnWithTable("av", AttributeValue$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("dea", DataElementAttributeValue$Table.DATAELEMENTID)).eq(dataElement.getUid()))
                        //For the given survey
                .and(Condition.column(ColumnAlias.columnWithTable("at", Attribute$Table.CODE)).eq(code)).querySingle();
    }

}

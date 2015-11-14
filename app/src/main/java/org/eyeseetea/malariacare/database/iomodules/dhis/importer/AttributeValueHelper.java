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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Match$Table;
import org.eyeseetea.malariacare.database.model.Option$Table;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionOption$Table;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.QuestionRelation$Table;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.model.Value$Table;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.Attribute$Table;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataElement$Table;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.Option;

import java.util.List;

/**
 * Created by arrizabalaga on 14/11/15.
 */
public class AttributeValueHelper {

    /**
     * Some options have a 'hardcoded' name such as 'COMPOSITE_SCORE'. This method is a helper to recover the whole option with that name
     * @param name
     * @return
     */
    public Option findOptionByName(String name){
        return new Select().from(Option.class).where(Condition.column(Option$Table.NAME).
                is(name)).querySingle();
    }

    /**
     * Find an attribute by its code
     * @param code
     * @return
     */
    public Attribute findAttributeByCode(String code){
        return new Select().from(Attribute.class).where(Condition.column(Attribute$Table.CODE).
                is(code)).querySingle();
    }

    /**
     * Find the attributevalue in a dataelement for the given attribute
     * @param dataElement
     * @param attribute
     * @return
     */
    public AttributeValue findAttributeValue(DataElement dataElement, Attribute attribute){
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
     * @param dataElement
     * @return
     */
    public  String findAttributeValueByCode(String code, DataElement dataElement){

        //Find the right attribute
        Attribute attribute=findAttributeByCode(code);
        //No such attribute -> done
        if(attribute==null){
            return null;
        }

        //Find its value for the given dataelement
        AttributeValue attributeValue=findAttributeValue(dataElement,attribute);
        if(attributeValue==null){
            return null;
        }
        return attributeValue.getValue();
    }




}

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
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.sdk.models.Attribute;
import org.eyeseetea.malariacare.sdk.models.Attribute$Table;
import org.eyeseetea.malariacare.sdk.models.Option;

/**
 * Created by arrizabalaga on 6/11/15.
 */
public class AttributeExtended {

    Attribute attribute;

    public AttributeExtended(Attribute attribute){
        this.attribute=attribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public AttributeExtended(){}

    /**
     * Find an attribute by its code
     * @param code
     * @return
     */
    public static Attribute findAttributeByCode(String code){
        return new Select().from(Attribute.class).where(Condition.column(Attribute$Table.CODE).
                is(code)).querySingle();
    }


}

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

import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.utils.AUtils;
import org.eyeseetea.malariacare.sdk.models.Attribute;
import org.eyeseetea.malariacare.sdk.models.Option;
import org.eyeseetea.malariacare.sdk.models.OptionAttributeValue;
import org.eyeseetea.malariacare.sdk.models.OptionSet;

/**
 * Created by arrizabalaga on 6/11/15.
 */
public class OptionExtended implements VisitableFromSDK {

    private static final String TAG=".OptionExtended";

    /**
     * Name of the attribute that holds the factor value for each option
     */
    private static final String ATTRIBUTE_OPTION_FACTOR_NAME="OF";

    Option option;

    public OptionExtended(){}

    public OptionExtended(Option option){
        this.option=option;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public Option getOption() {
        return option;
    }

    /**
     * Some options have a 'hardcoded' name such as 'COMPOSITE_SCORE'. This method is a helper to recover the whole option with that name
     * @param name
     * @return
     */
    public static Option findOptionByName(String name){
        return new Select().from(Option.class).where(Condition.column(Option$Table.NAME).
                is(name)).querySingle();
    }

    /**
     * Some options have a 'hardcoded' name such as 'COMPOSITE_SCORE'. This method is a helper to recover the whole option with that name belonging to a given optionSet
     * @param optionSetUID
     * @param name
     * @return
     */
    public static Option findOptionByOptionSetAndName(String optionSetUID, String name){
        return new Select().from(Option.class).
                where(Condition.column(org.eyeseetea.malariacare.sdk.models.Option$Table.NAME).is(name)).
                and(Condition.column(org.eyeseetea.malariacare.sdk.models.Option$Table.OPTIONSET).is(optionSetUID)).querySingle();
    }

    /**
     * Finds the factor for this option (via OptionFactor attribute)
     * @return
     */
    public Float getFactor(){
        for(OptionAttributeValue optionAttributeValue:option.getAttributeValues()){
            Attribute attribute=optionAttributeValue.getAttribute();

            //Not OptionFactor -> ignore
            if(!ATTRIBUTE_OPTION_FACTOR_NAME.equals(attribute.getCode())){
                continue;
            }

            //Turn string value into float
            return AUtils.safeParseFloat(optionAttributeValue.getValue());
        }

        //Should not happen
        return 0f;
    }
}

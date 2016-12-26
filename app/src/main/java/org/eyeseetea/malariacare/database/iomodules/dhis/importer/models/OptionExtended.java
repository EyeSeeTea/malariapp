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


import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionAttributeValueFlow;
import org.eyeseetea.malariacare.utils.AUtils;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow_Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 6/11/15.
 */
public class OptionExtended implements VisitableFromSDK {

    private static final String TAG=".OptionExtended";

    /**
     * Name of the attribute that holds the factor value for each option
     */
    private static final String ATTRIBUTE_OPTION_FACTOR_NAME="OF";

    OptionFlow option;

    public OptionExtended(){}

    public OptionExtended(OptionFlow option){
        this.option=option;
    }

    public OptionExtended(OptionExtended option){
        this.option=option.getOption();
    }
    
    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public OptionFlow getOption() {
        return option;
    }

    /**
     * Some options have a 'hardcoded' name such as 'COMPOSITE_SCORE'. This method is a helper to recover the whole option with that name
     * @param name
     * @return
     */
    public static OptionFlow findOptionByName(String name){
        return new Select().from(OptionFlow.class).where(OptionFlow_Table.name.
                is(name)).querySingle();
    }

    /**
     * Some options have a 'hardcoded' name such as 'COMPOSITE_SCORE'. This method is a helper to recover the whole option with that name belonging to a given optionSet
     * @param optionSetUID
     * @param name
     * @return
     */
    public static OptionFlow findOptionByOptionSetAndName(String optionSetUID, String name){
        return new Select().from(OptionFlow.class).
                where(OptionFlow_Table.name.is(name)).
                and(OptionFlow_Table.optionSet.is(optionSetUID)).querySingle();
    }

    /**
     * Finds the factor for this option (via OptionFactor attribute)
     * @return
     */
    public Float getFactor(){

        for(OptionAttributeValueFlow optionAttributeValue:this.getOptionAttributeValuesFlow()){
            AttributeFlow attribute=optionAttributeValue.getAttribute();

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


    //// FIXME: 09/11/2016
    public List<OptionAttributeValueFlow> getAttributeValues() {
        //optionflow attributeValueFlow
        return  null;
    }
    public OptionAttributeValueFlow getAttribute() {
        //optionflow attributeFlow
        return  null;
    }

    public List<OptionAttributeValueFlow> getOptionAttributeValuesFlow() {
        //optionflow attributeFlow
        return  null;
    }

    public String getUid() {
        return option.getUId();
    }

    public String getOptionSet() {
        return option.getOptionSet().getUId();
    }

    public String getName() {
        return option.getName();
    }

    public String getCode() {
        return option.getCode();
    }


    public static List<OptionExtended> getExtendedList(List<OptionFlow> flowList) {
        List <OptionExtended> extendedsList = new ArrayList<>();
        for(OptionFlow flowPojo:flowList){
            extendedsList.add(new OptionExtended(flowPojo));
        }
        return extendedsList;
    }
}

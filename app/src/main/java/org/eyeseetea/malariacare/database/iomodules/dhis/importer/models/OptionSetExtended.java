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

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.sdk.models.OptionSet;
import org.eyeseetea.malariacare.sdk.models.OptionSet$Table;

/**
 * Created by arrizabalaga on 6/11/15.
 */
public class OptionSetExtended implements VisitableFromSDK {

    private final static String KEY_PREFFIX="@";

    /**
     * Code for optionset that holds options x each Type of Element
     */
    public final static String OPTION_SET_DATAELEMENT_TYPE_NAME="DB - DE Type";

    /**
     * Code for optionset that holds options x each Type of Question
     */
    public final static String OPTION_SET_QUESTION_TYPE_NAME="DB - Question Type";

    OptionSet optionSet;

    public OptionSetExtended(){}

    public OptionSetExtended(OptionSet optionSet){
        this.optionSet=optionSet;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public OptionSet getOptionSet() {
        return optionSet;
    }

    /**
     * Returns a sintetic UID for a given answer and an output
     * @param uid
     * @param output
     * @return
     */
    public static String getKeyWithOutput(String uid,int output){
        return uid+KEY_PREFFIX+output;
    }


    /**
     * Some optionSets have a 'hardcoded' name such as 'DB - DE Type'. This method is a helper to recover the whole option with that name
     * @param name
     * @return
     */
    public static OptionSet findOptionSetByName(String name){
        return new Select().from(OptionSet.class).where(Condition.column(OptionSet$Table.NAME).
                is(name)).querySingle();
    }

    /**
     * Returns the optionSet that holds info for 'DB - DE Type'.
     * @return
     */
    public static OptionSet findOptionSetForDataElementType(){
        return findOptionSetByName(OPTION_SET_DATAELEMENT_TYPE_NAME);
    }

    /**
     * Returns the optionSet that holds info for 'DB - Question Type'.
     * @return
     */
    public static OptionSet findOptionSetForQuestionType(){
        return findOptionSetByName(OPTION_SET_QUESTION_TYPE_NAME);
    }
}

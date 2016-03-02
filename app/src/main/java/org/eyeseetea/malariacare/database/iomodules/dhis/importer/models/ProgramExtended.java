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
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.Attribute$Table;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.ProgramAttributeValue$Table;

import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class ProgramExtended implements VisitableFromSDK {

    private static final String TAG = ".PRExtended";

    /**
     * Hardcoded 'code' of the attribute that holds the idx of productiviy in the orgunit array attribute
     */
    public static final String PROGRAM_PRODUCTIVITY_POSITION_ATTRIBUTE_CODE = "PPP";

    /**
     * Reference to sdk program
     */
    Program program;

    /**
     * Reference to app program (useful to create relationships with orgunits)
     */
    org.eyeseetea.malariacare.database.model.Program appProgram;

    public ProgramExtended(){}

    public ProgramExtended(Program program){
        this.program=program;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public Program getProgram(){
        return this.program;
    }

    public static List<Program> getAllPrograms(){
        return new Select().from(Program.class).queryList();
    }

    public void setAppProgram(org.eyeseetea.malariacare.database.model.Program appProgram) {
        this.appProgram = appProgram;
    }

    public org.eyeseetea.malariacare.database.model.Program getAppProgram(){
        return this.appProgram;
    }

    public Integer getProductivityPosition() {

        ProgramAttributeValue programAttributeValue = new Select().from(ProgramAttributeValue.class).as("p")
                .join(Attribute.class, Join.JoinType.LEFT).as("a")
                .on(Condition.column(ColumnAlias.columnWithTable("p", ProgramAttributeValue$Table.ATTRIBUTEID))
                        .eq(ColumnAlias.columnWithTable("a", Attribute$Table.ID)))
                .where(Condition.column(ColumnAlias.columnWithTable("a", Attribute$Table.CODE))
                        .eq(PROGRAM_PRODUCTIVITY_POSITION_ATTRIBUTE_CODE))
                .and(Condition.column(ColumnAlias.columnWithTable("p", ProgramAttributeValue$Table.PROGRAM)).is(this.getProgram().getUid()))
                .querySingle();

        if(programAttributeValue==null){
            return null;
        }

        try {
            return Integer.parseInt(programAttributeValue.getValue());
        }catch(Exception ex){
            Log.e(TAG, String.format("getProductivityPosition(%s) -> %s", this.getProgram().getUid(), ex.getMessage()));
            return null;
        }
    }
}

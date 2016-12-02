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

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.sdk.models.Program;
import org.eyeseetea.malariacare.sdk.models.ProgramAttributeValue;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow_Table;

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

    public void setAppProgram(org.eyeseetea.malariacare.database.model.Program appProgram) {
        this.appProgram = appProgram;
    }

    public org.eyeseetea.malariacare.database.model.Program getAppProgram(){
        return this.appProgram;
    }

    public Integer getProductivityPosition() {
        //// FIXME: 11/11/2016
        /*
        ProgramAttributeValue programAttributeValue = new Select().from(ProgramAttributeValue.class).as(programAttributeFlowName)
                .join(TrackedEntityAttributeFlow.class, Join.JoinType.LEFT_OUTER).as(attributeFlowName)
                .on(ProgramAttributeValueFlow_Table.attributeId.withTable(programAttributeFlowAlias)
                        .eq(TrackedEntityAttributeFlow_Table.id.withTable(attributeFlowAlias)))
                .where(TrackedEntityAttributeFlow_Table.code)
                        .eq(PROGRAM_PRODUCTIVITY_POSITION_ATTRIBUTE_CODE)
                .and(ProgramAttributeValueFlow_Table.program.withTable(programAttributeFlowAlias).is(this.getProgram().getUid()))
                .querySingle();
        */
        ProgramAttributeValue programAttributeValue = null;
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

    public static Program getProgramByDataElement(String dataElementUid) {
        Program program = null;
        //// FIXME: 11/11/2016
        /*
        List<Program> programs = getAllPrograms();
        for (Program program1 : programs) {
            for (ProgramStage programStage : program1.getProgramStages()) {
                for (ProgramStageSectionFlow programStageSection : programStage.getProgramStageSections()) {
                    for (ProgramStageDataElementFlow programStageDataElement : programStageSection.getProgramStageDataElements()) {
                        if (programStageDataElement.getDataElement().getUId().equals(dataElementUid)) {
                            return program1;
                        }
                    }
                }
            }
        }
        */
        return program;
    }
    
    public static List<Program> getAllPrograms(){
        return new Select().from(Program.class).queryList();
    }

    public static Program getProgram(String id){
        return new Select()
                .from(Program.class).where(ProgramFlow_Table.uId.eq(id)).querySingle();
    }
}

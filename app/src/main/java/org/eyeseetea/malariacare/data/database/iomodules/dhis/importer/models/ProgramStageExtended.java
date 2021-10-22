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

import com.raizlabs.android.dbflow.sql.language.Select;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.VisitableFromSDK;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow_Table;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class ProgramStageExtended implements VisitableFromSDK {
    ProgramStageFlow programStage;

    public ProgramStageExtended() {
    }

    public ProgramStageExtended(ProgramStageFlow programStage) {
        this.programStage = programStage;
    }

    public ProgramStageExtended(ProgramStageExtended programStage) {
        this.programStage = programStage.getProgramStage();
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public ProgramStageFlow getProgramStage() {
        return programStage;
    }

    public static ProgramStageFlow getProgramStage(String programStageUID) {
        return new Select().from(ProgramStageFlow.class)
                .where(ProgramStageFlow_Table.uId
                        .eq(programStageUID))
                .querySingle();
    }

    public String getProgramUid() {
        return programStage.getProgram().getUId();
    }

    public String getUid() {
        return programStage.getUId();
    }

    public List<ProgramStageSectionExtended> getProgramStageSections() {
        List<ProgramStageSectionFlow> programStageSections =
                SdkQueries.getProgramStageSectionFromProgramStage(programStage.getUId());
        List<ProgramStageSectionExtended> programStageSectionExtendedList = new ArrayList<>();
        for (ProgramStageSectionFlow programStageSectionFlow : programStageSections) {
            programStageSectionExtendedList.add(
                    new ProgramStageSectionExtended(programStageSectionFlow));
        }
        return programStageSectionExtendedList;
    }

    public List<ProgramStageDataElementExtended> getProgramStageDataElements() {
        List<ProgramStageDataElementFlow> programStageDataElementFlows =
                SdkQueries.getProgramStageDataElementFromProgramStage(programStage.getUId());
        List<ProgramStageDataElementExtended> programStageDataElementExtendedList =
                new ArrayList<>();
        for (ProgramStageDataElementFlow programStageDataElementFlow :
                programStageDataElementFlows) {
            programStageDataElementExtendedList.add(
                    new ProgramStageDataElementExtended(programStageDataElementFlow));
        }
        return programStageDataElementExtendedList;
    }

    public static List<ProgramStageExtended> getExtendedList(List<ProgramStageFlow> flowList) {
        List<ProgramStageExtended> extendedsList = new ArrayList<>();
        for (ProgramStageFlow flowPojo : flowList) {
            extendedsList.add(new ProgramStageExtended(flowPojo));
        }
        return extendedsList;
    }

    public static String getProgramStageUid(String programUid) {
        ProgramStageFlow programStage =  new Select()
                .from(ProgramStageFlow.class)
                .where(ProgramStageFlow_Table.program.eq(programUid))
                .querySingle();

        return programStage.getUId();
    }
}

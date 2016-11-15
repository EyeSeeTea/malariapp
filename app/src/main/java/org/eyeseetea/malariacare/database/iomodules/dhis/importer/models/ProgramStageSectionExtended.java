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

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.IConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.VisitableFromSDK;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class ProgramStageSectionExtended implements VisitableFromSDK {
    ProgramStageSectionFlow programStageSection;

    public ProgramStageSectionExtended(){}

    public ProgramStageSectionExtended(ProgramStageSectionFlow programStageSection){
        this.programStageSection=programStageSection;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public ProgramStageSectionFlow getProgramStageSection() {
        return programStageSection;
    }

    public ProgramStageExtended getProgramStage() {
        return new ProgramStageExtended(programStageSection.getProgramStage());
    }

    public String getDisplayName() {
        return programStageSection.getDisplayName();
    }

    public Integer getSortOrder() {
        return programStageSection.getSortOrder();
    }

    public String getUid() {
        return programStageSection.getUId();
    }
}

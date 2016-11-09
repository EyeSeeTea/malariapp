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
import org.eyeseetea.malariacare.sdk.models.ProgramStage;
import org.eyeseetea.malariacare.sdk.models.ProgramStage$Table;

/**
 * Created by arrizabalaga on 5/11/15.
 */
public class ProgramStageExtended implements VisitableFromSDK {
    ProgramStage programStage;

    public ProgramStageExtended(){}

    public ProgramStageExtended(ProgramStage programStage){
        this.programStage=programStage;
    }

    @Override
    public void accept(IConvertFromSDKVisitor visitor) {
        visitor.visit(this);
    }

    public ProgramStage getProgramStage() {
        return programStage;
    }

    public static ProgramStage getProgramStage(String programStageUID){
        return new Select().from(ProgramStage.class)
                .where(Condition.column(ProgramStage$Table.ID)
                        .eq(programStageUID))
                .querySingle();
    }

}

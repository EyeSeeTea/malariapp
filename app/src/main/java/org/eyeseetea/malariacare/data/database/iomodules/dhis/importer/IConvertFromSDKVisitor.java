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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ObservationExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ObservationValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OrganisationUnitLevelExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.UserAccountExtended;

public interface IConvertFromSDKVisitor {
    void visit(ProgramExtended sdkProgram);
    void visit(ProgramStageExtended sdkProgramStage);
    void visit(ProgramStageSectionExtended sdkProgramStageSection);
    void visit(OrganisationUnitExtended organisationUnit);
    void visit(OrganisationUnitLevelExtended organisationUnitLevel);
    void visit(OptionSetExtended optionSet);
    void visit(OptionExtended option);
    void visit(UserAccountExtended userAccount);
    void visit(DataElementExtended dataElement);
    void visit(EventExtended sdkEventExtended);
    void visit(DataValueExtended sdkDataValueExtended);
    void visit(ObservationExtended observationExtended);
    void visit(ObservationValueExtended observationValueExtended);
    void buildScores();
}

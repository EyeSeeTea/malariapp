/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.data.remote.sdk;

import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SdkModelUtils {
    public static Set<String> getProgramStageUids(List<Program> programs) {
        Set<String> programStagesUids = new HashSet<>();

        for (Program program : programs) {
            programStagesUids.addAll(
                    ModelUtils.toUidSet(program.getProgramStages()));
        }
        return programStagesUids;
    }

    public static Set<String> getProgramStageDataElementUids(List<ProgramStage> programStages) {
        Set<String> programStagesDataElementsUids = new HashSet<>();

        for (ProgramStage programStage : programStages) {
            programStagesDataElementsUids.addAll(ModelUtils.toUidSet(
                    programStage.getProgramStageDataElements()));
        }
        return programStagesDataElementsUids;
    }

    public static Set<String> getProgramStageSectionUids(List<ProgramStage> programStages) {
        Set<String> programStagesSectionUids = new HashSet<>();

        for (ProgramStage programStage : programStages) {
            programStagesSectionUids.addAll(
                    ModelUtils.toUidSet(programStage.getProgramStageSections()));
        }
        return programStagesSectionUids;
    }

    public static Set<String> getProgramStageSectionDataElementUids(
            List<ProgramStageSection> programStageSections) {
        Set<String> programStagesDataElementsUids = new HashSet<>();

        for (ProgramStageSection programStageSection : programStageSections) {
            programStagesDataElementsUids.addAll(ModelUtils.toUidSet(
                    programStageSection.getProgramStageDataElements()));
        }
        return programStagesDataElementsUids;
    }
}

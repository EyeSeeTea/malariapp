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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnit$Table;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel$Table;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.persistence.models.BaseMetaDataObject;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;

import java.util.HashMap;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    Map<String, Object> appMapObjects;

    public ConvertFromSDKVisitor() {
        appMapObjects = new HashMap();
    }

    /**
     * Turns a sdk Program into an app Program
     *
     * @param sdkProgram
     */
    public void visit(Program sdkProgram) {
        //Build program
        org.eyeseetea.malariacare.database.model.Program appProgram = new org.eyeseetea.malariacare.database.model.Program();
        appProgram.setUid(sdkProgram.getUid());
        appProgram.setName(sdkProgram.getDisplayName());
        appProgram.save();


        //Annotate built program
        appMapObjects.put(sdkProgram.getUid(), appProgram);

        //Visit children
        for (ProgramStage ps : sdkProgram.getProgramStages()) {
            new ProgramStageVisitableFromSDK(ps).accept(this);
        }
    }

    /**
     * Turns a sdk ProgramStage into a TabGroup
     *
     * @param sdkProgramStage
     */
    @Override
    public void visit(ProgramStage sdkProgramStage) {
        //Build tabgroup
        org.eyeseetea.malariacare.database.model.Program appProgram = (org.eyeseetea.malariacare.database.model.Program) appMapObjects.get(sdkProgramStage.getProgram().getUid());
        TabGroup appTabGroup = new TabGroup();
        //FIXME TabGroup has no UID right now
        appTabGroup.setName(sdkProgramStage.getDisplayName());
        appTabGroup.setProgram(appProgram);
        appTabGroup.save();

        //Annotate built tabgroup
        appMapObjects.put(sdkProgramStage.getUid(), appTabGroup);

        //Visit children
        for (ProgramStageSection pss : sdkProgramStage.getProgramStageSections()) {
            new ProgramStageSectionVisitableFromSDK(pss).accept(this);
        }
    }

    @Override
    public void visit(OrganisationUnit organisationUnit) {

        try {
            OrgUnit orgUnit = new OrgUnit();
            //Saving parent
            Long parent_id = null;
            try {
                parent_id = Long.parseLong(organisationUnit.getParent());
            } catch (Exception e) {
            }

            if (parent_id != null)
                orgUnit.setOrgUnit(new Select().from(OrgUnit.class).where(Condition.column(OrgUnit$Table.ID_ORG_UNIT)
                        .eq(parent_id)).querySingle());
            //Saving level
            Long level_id = null;
            try {
                level_id = (long) organisationUnit.getLevel();
            } catch (Exception e) {
            }

            if (level_id != null) {
                OrgUnitLevel orgUnitLevel = new Select().from(OrgUnitLevel.class)
                        .where(Condition.column(OrgUnitLevel$Table.ID_ORG_UNIT_LEVEL).eq(level_id)).querySingle();
                if (orgUnitLevel == null) {
                    orgUnitLevel = new OrgUnitLevel();
                    orgUnitLevel.setId_org_unit_level(level_id);
                    orgUnitLevel.setName("debug");
                }
                orgUnit.setOrgUnitLevel(orgUnitLevel);
            }
            //saving name
            orgUnit.setName(organisationUnit.getLabel());
            //Saving uid
            //saving id
            if (organisationUnit.getId().length() > 0)
                orgUnit.setId_org_unit(Long.parseLong((organisationUnit.getId())));
            orgUnit.setId_org_unit((long) 1000);
            orgUnit.save();
        } catch (Exception e) {
        }
    }

    public void visit(ProgramStageSection sdkProgramStageSection) {
        //Build Tab
        org.eyeseetea.malariacare.database.model.TabGroup appTabGroup = (org.eyeseetea.malariacare.database.model.TabGroup) appMapObjects.get(sdkProgramStageSection.getProgramStage());
        Tab appTab = new Tab();
        //FIXME TabGroup has no UID right now
        appTab.setName(sdkProgramStageSection.getDisplayName());
        appTab.setType(Constants.TAB_AUTOMATIC);
        appTab.setOrder_pos(sdkProgramStageSection.getSortOrder());
        appTab.setTabGroup(appTabGroup);
        appTab.save();

        //Annotate build tab
        appMapObjects.put(sdkProgramStageSection.getUid(), appTab);

        //TODO Headers,Questions,...
    }
}
/*
 * Copyright (c) 2016.
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

import android.util.Log;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.Value;
import org.hisp.dhis.android.sdk.persistence.models.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by idelcano on 18/04/2016.
 */
public class TabGroupBuilder {

    private static final String TAG = ".TabGroupBuilder";
    /**
     * Mapping headers(it is needed for not duplicate data)
     */
    static Map<String, TabGroup> mapTabGroup;

    TabGroupBuilder() {
        mapTabGroup = new HashMap<>();
    }

    /**
     * Finds the right Tabgroup according to the question (question ->header ->tab ->tabgroup)
     * @param question
     * @return
     */
    public TabGroup findTabgroupFromQuestion(Question question){
        //No question -> done
        if(question==null){
            return null;
        }

        Header header = question.getHeader();
        if(header==null){
            return null;
        }

        Tab tab = header.getTab();
        if(tab==null){
            return null;
        }
        return tab.getTabGroup();
    }

    public static Tab saveTabGroup(DataElementExtended sdkDataElementExtended) {
        TabGroup questionTabGroup;
        String attributeTabGroupValue = sdkDataElementExtended.getValue(DataElementExtended.ATTRIBUTE_TABGROUP_NAME);
        String tabUid = sdkDataElementExtended.findProgramStageSectionUIDByDataElementUID(sdkDataElementExtended.getDataElement().getUid());
        String tabGroupUid=TabGroup.class.getName();
        try {
            tabGroupUid = sdkDataElementExtended.findAttributeValuefromDataElementCode(DataElementExtended.ATTRIBUTE_TABGROUP_NAME, sdkDataElementExtended.getDataElement()).getAttributeId();
        }catch (NullPointerException e){
            Log.d(TAG, "The dataelement(header) "+sdkDataElementExtended.getDataElement().getUid()+" don't have Tabgroup attribute.");
        }

        Tab questionTab = (Tab) ConvertFromSDKVisitor.appMapObjects.get(tabUid);


        if(attributeTabGroupValue==null){
            Log.d(TAG, "Creating new tabgroup from dataelement: " +sdkDataElementExtended.getDataElement().getUid());
            String dataelementUid=sdkDataElementExtended.getDataElement().getUid();
            org.hisp.dhis.android.sdk.persistence.models.Program programSdk= ProgramExtended.getProgramByDataElement(dataelementUid);
            Program program =Program.getProgram(programSdk.getUid());
            Log.d(TAG, "With programUID: " + programSdk.getUid());

            if(mapTabGroup.containsKey(tabGroupUid+programSdk.getUid())){
                questionTabGroup=mapTabGroup.get(tabGroupUid+programSdk.getUid());
                questionTab.setTabGroup(questionTabGroup);
                questionTab.save();
            }
            else {
                questionTabGroup = new TabGroup();
                questionTabGroup.setName(program.getName());
                Log.d(TAG, "With local programUId " + program.getUid());
                questionTabGroup.setProgram(program.getId_program());
                questionTabGroup.save();
                questionTab.setTabGroup(questionTabGroup);
                questionTab.save();
                mapTabGroup.put(tabGroupUid + programSdk.getUid(), questionTabGroup);
            }
        }
        else if(!mapTabGroup.containsKey(tabGroupUid+attributeTabGroupValue)) {
            TabGroup tabGroup=new TabGroup();
            tabGroup.setName(attributeTabGroupValue);
            Log.d(TAG, "Creating new tab from: " +sdkDataElementExtended.getDataElement().getUid());
            String dataelementUid=sdkDataElementExtended.getDataElement().getUid();
            org.hisp.dhis.android.sdk.persistence.models.Program programSdk= ProgramExtended.getProgramByDataElement(dataelementUid);
            Log.d(TAG, "With programUID: " + programSdk.getUid());

            Program program =Program.getProgram(programSdk.getUid());

            Log.d(TAG, "With local programUId " + program.getUid());
            tabGroup.setProgram(program.getId_program());
            tabGroup.save();
            questionTab.setTabGroup(tabGroup);
            questionTab.save();
            mapTabGroup.put(tabGroupUid + attributeTabGroupValue, tabGroup);
        }
        else{
            questionTabGroup=mapTabGroup.get(tabGroupUid+attributeTabGroupValue);
            questionTab.setTabGroup(questionTabGroup);
            questionTab.save();
        }
        return questionTab;
    }

}

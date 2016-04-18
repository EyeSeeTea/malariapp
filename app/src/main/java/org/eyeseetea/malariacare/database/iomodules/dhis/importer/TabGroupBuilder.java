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

    public static void saveSurveyTabGroup(Survey survey){
        //Get tabgroup from values
        Log.d(TAG,"Building tabgroup for event survey "+survey.getEventUid());
        for (Value value : survey.getValues()) {
            Question question = value.getQuestion();
            if(question==null){
                Log.d(TAG, "This value don't have question. Value:"+value.getValue()+" Event Survey: "+survey.getEventUid());
                continue;
            }
            Log.d(TAG, "Adding survey tabgroup: question " + question.getUid());
            Header header = question.getHeader();
            if(header==null){
                Log.d(TAG, "This value don't have header. Value:"+value.getValue()+" Event Survey: "+survey.getEventUid());
                continue;
            }
            Log.d(TAG,"Adding survey tabgroup: header "+question.getHeader().getName());
            Tab tab = header.getTab();
            if(tab==null){
                Log.d(TAG, "This value don't have tab. Value:"+value.getValue()+" Event Survey: "+survey.getEventUid());
                continue;
            }
            Log.d(TAG,"Adding survey tabgroup: tab "+header.getTab().getName());
            TabGroup tabGroup = tab.getTabGroup();
            if(tabGroup!=null) {
                Log.d(TAG, "Adding survey tabgroup: tabgrouponame" + tabGroup.getName());
                survey.setTabGroup(tabGroup.getId_tab_group());
                survey.save();
                break;
            }
            if(tabGroup==null){
                Log.d(TAG, "This value don't have tabgroup. Value:"+value.getValue()+" Event Survey: "+survey.getEventUid());
            }
        }
        //IF the survey value don't have tabgroup
        TabGroup questionTabGroup=null;
        if(survey.getTabGroup()==null){
            String tabGroupUid=TabGroup.class.getName()+"";
            Log.d(TAG,"mapTabgroups"+tabGroupUid + "  -  " +mapTabGroup.toString());
            Event event=survey.getEvent();
            String programSdkUid= event.getProgramId();
            if(mapTabGroup.containsKey(tabGroupUid+programSdkUid)){
                questionTabGroup=mapTabGroup.get(tabGroupUid+programSdkUid);
                Log.d(TAG,"existe"+questionTabGroup.toString());
                Log.d(TAG,"existe"+questionTabGroup.getId_tab_group());
            }
            else {
                Log.d(TAG,"no existe");
                questionTabGroup = Survey.getFirstTabGroup(programSdkUid);
                org.eyeseetea.malariacare.database.model.Program program=org.eyeseetea.malariacare.database.model.Program.getProgram(programSdkUid);
                if(questionTabGroup==null){
                    questionTabGroup = new TabGroup(program.getName(),program);
                    questionTabGroup.save();
                    Log.d(TAG,"created" + questionTabGroup.toString() );
                }
            }
            if(questionTabGroup!=null) {
                survey.setTabGroup(questionTabGroup.getId_tab_group());
                survey.save();
                mapTabGroup.put(tabGroupUid + survey.getProgram().getUid(), questionTabGroup);
                Log.d(TAG, "The fake " + questionTabGroup.getName() + " tabgroup was saved as survey tabgroup");
            }
            else{
                Log.d(TAG,"Error: The tabgroup wasn't saved at the survey:"+survey.getId_survey());
            }
        }
        if(survey.getTabGroup()==null){
            Log.d(TAG,"Error: The survey don't have tabgroup :"+survey.getId_survey());
        }
    }

    public static Tab saveTabGroup(DataElementExtended sdkDataElementExtended) {
        TabGroup questionTabGroup;
        String attributeTabGroupValue = sdkDataElementExtended.getValue(DataElementExtended.ATTRIBUTE_TABGROUP_NAME);
        String tabUid = sdkDataElementExtended.findProgramStageSectionUIDByDataElementUID(sdkDataElementExtended.getDataElement().getUid());
        String tabGroupUid=TabGroup.class.getName()+"";
        try {
            tabGroupUid = sdkDataElementExtended.findAttributeValuefromDataElementCode(DataElementExtended.ATTRIBUTE_TABGROUP_NAME, sdkDataElementExtended.getDataElement()).getAttributeId();
        }catch (NullPointerException e){
            Log.d(TAG, "The dataelement(header) "+sdkDataElementExtended.getDataElement().getUid()+" don't have Tabgroup attribute.");
        }

        String attributeHeaderValue = sdkDataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HEADER_NAME);
        Tab questionTab;
        if(ConvertFromSDKVisitor.appMapObjects.containsKey(tabUid)) {
            questionTab = (Tab) ConvertFromSDKVisitor.appMapObjects.get(tabUid);
        }
        else
            questionTab=null;

        if(attributeTabGroupValue!=null && ConvertFromSDKVisitor.appMapObjects.containsKey(tabGroupUid+attributeTabGroupValue)) {
            questionTabGroup = (TabGroup) ConvertFromSDKVisitor.appMapObjects.get(tabGroupUid+attributeTabGroupValue);
        }
        else
            questionTabGroup=null;

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

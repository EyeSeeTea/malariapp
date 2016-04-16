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

import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ignac on 14/11/2015.
 */
public class QuestionBuilder {

    private static final String TAG = ".QuestionBuilder";

    /**
     * It is the factor needed in a option to create the questionRelation
     * */
    private final float MATCHFACTOR=1f;
    /**
     * Mapping all the questions
     */
    static Map<String, Question> mapQuestions;

    /**
     * Mapping all the question parents
     */
    static Map<String, String> mapParent;
    /**
     * Mapping all the question type(child/parent)
     */
    static Map<String, String> mapType;
    /**
     * Mapping all the question level(it is needed for know who are the parent)
     */
    static Map<String, String> mapLevel;
    /**
     * Mapping all the Match question parents
     */
    static Map<String, String> mapMatchType;
    /**
     * Mapping all the Match question type(child/parent)
     */
    static Map<String, String> mapMatchLevel;
    /**
     * Mapping all the Match question level(it is needed for know who are the parent)
     */
    static Map<String, String> mapMatchParent;
    /**
     * Mapping all the Match question level(it is needed for know who are the parent)
     */
    static Map<String, List<String>> mapMatchChilds;
    /**
     * Mapping headers(it is needed for not duplicate data)
     */
    static Map<String, Header> mapHeader;

    /**
     * Mapping headers(it is needed for not duplicate data)
     */
    static Map<String, TabGroup> mapTabGroup;
    /**
     * It is needed in the header order.
     */
    private int header_order = 0;

    QuestionBuilder() {
        mapQuestions = new HashMap<>();
        mapHeader = new HashMap<>();
        mapTabGroup = new HashMap<>();
        mapType = new HashMap<>();
        mapLevel = new HashMap<>();
        mapParent = new HashMap<>();
        mapMatchLevel = new HashMap<>();
        mapMatchParent = new HashMap<>();
        mapMatchChilds = new HashMap<>();
        mapMatchType = new HashMap<>();
    }

    /**
     * Registers a Question in builder
     *
     * @param question
     */
    public void add(Question question) {
        mapQuestions.put(question.getUid(), question);
    }

    /**
     * Save and return the Header the question represented by the given DataElement belongs to
     *
     * @param dataElementExtended
     * @return question header
     */
    public Header saveHeader(DataElementExtended dataElementExtended) {
        Header header = null;
        String attributeHeaderValue = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HEADER_NAME);
        if (attributeHeaderValue != null) {
            Tab questionTab;
            questionTab=saveTabGroup(dataElementExtended);
            String tabUid = dataElementExtended.findProgramStageSectionUIDByDataElementUID(dataElementExtended.getDataElement().getUid());

            if(!mapHeader.containsKey(tabUid+attributeHeaderValue)) {
                header = new Header();
                header.setName(attributeHeaderValue);
                header.setShort_name(attributeHeaderValue);
                header.setOrder_pos(header_order);
                header_order++;
                header.setTab(questionTab);
                header.save();
                mapHeader.put(tabUid+header.getName(), header);
            }
            else{
                header=mapHeader.get(tabUid+attributeHeaderValue);
            }
            if(questionTab==null) {
                header=null;
                return header;
            }

            if(questionTab.getTabGroup()==null){
                Program program=Program.getProgram(DataElementExtended.findProgramUIDByDataElementUID(dataElementExtended.getDataElement().getUid()));
                TabGroup tabGroup = new TabGroup(program.getName(),program);
                tabGroup.save();
                questionTab.setTabGroup(tabGroup);
                questionTab.save();
            }

        }
        return header;
    }

    /**
     * Registers a Parent/child and Match Parent/child relations in maps
     * Its need the programid to diferenciate between programs dataelements.
     *
     * @param dataElementExtended
     */
    public void registerParentChildRelations(DataElementExtended dataElementExtended) {
        DataElement dataElement = dataElementExtended.getDataElement();
        String questionRelationType = null;
        String questionRelationGroup = null;
        String matchRelationType = null;
        String matchRelationGroup = null;
        questionRelationType = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HIDE_TYPE);
        questionRelationGroup = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_HIDE_GROUP);
        matchRelationType = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_MATCH_TYPE);
        matchRelationGroup = dataElementExtended.getValue(DataElementExtended.ATTRIBUTE_MATCH_GROUP);
        if (questionRelationType != null) {
            String parentProgramUid = DataElementExtended.findProgramUIDByDataElementUID(dataElement.getUid());
            if (questionRelationType.equals(DataElementExtended.PARENT)) {
                mapParent.put(parentProgramUid + questionRelationGroup, dataElement.getUid());
            } else {
                mapType.put(parentProgramUid + dataElement.getUid(), questionRelationType);
                mapLevel.put(parentProgramUid + dataElement.getUid(), questionRelationGroup);
            }
        }
        if (matchRelationType != null) {
            String parentProgramUid = DataElementExtended.findProgramUIDByDataElementUID(dataElement.getUid());
            if (matchRelationType.equals(DataElementExtended.PARENT)) {
                mapMatchParent.put(parentProgramUid + matchRelationGroup, dataElement.getUid());
            } else if (matchRelationType.equals(DataElementExtended.CHILD)) {
                List<String> childsUids;
                if (mapMatchChilds.containsKey(parentProgramUid + matchRelationGroup)) {
                    childsUids = mapMatchChilds.get(parentProgramUid + matchRelationGroup);
                } else {
                    childsUids = new ArrayList<>();
                }
                childsUids.add(dataElement.getUid());
                mapMatchChilds.put(parentProgramUid + matchRelationGroup, childsUids);
            }
            mapMatchType.put(parentProgramUid + dataElement.getUid(), matchRelationType);
            mapMatchLevel.put(parentProgramUid + dataElement.getUid(), matchRelationGroup);

        }

    }

    /**
     * Save Question id_parent QuestionOption QuestionRelation and Match
     *
     * @param dataElementExtended
     */
    public void addRelations(DataElementExtended dataElementExtended) {
        if (mapQuestions.containsKey(dataElementExtended.getDataElement().getUid())) {
            addParent(dataElementExtended.getDataElement());
            addQuestionRelations(dataElementExtended.getDataElement());
            addCompositeScores(dataElementExtended);
        }
    }

    private void addCompositeScores(DataElementExtended dataElementExtended) {
        CompositeScore compositeScore = dataElementExtended.findCompositeScore();
        if (compositeScore != null) {
            Question appQuestion = mapQuestions.get(dataElementExtended.getDataElement().getUid());
            if (appQuestion != null) {
                appQuestion.setCompositeScore(compositeScore);
                appQuestion.save();
                add(appQuestion);
            }
        }
    }

    /**
     * Save Question id_parent in Question
     *
     * @param dataElement
     */
    private void addParent(DataElement dataElement) {
        String programUid = DataElementExtended.findProgramUIDByDataElementUID(dataElement.getUid());
        String questionRelationType = mapType.get(programUid + dataElement.getUid());
        String questionRelationGroup = mapLevel.get(programUid + dataElement.getUid());

        Question appQuestion = mapQuestions.get(dataElement.getUid());

        if (questionRelationType != null && questionRelationType.equals(DataElementExtended.CHILD)) {
            try {
                if (questionRelationType.equals(DataElementExtended.CHILD)) {
                    String parentuid = mapParent.get(programUid + questionRelationGroup);
                    if (parentuid != null) {
                        QuestionRelation questionRelation = new QuestionRelation();
                        questionRelation.setOperation(1);
                        questionRelation.setQuestion(appQuestion);
                        boolean isSaved=false;
                        Question parentQuestion = mapQuestions.get(parentuid);
                        List<Option> options = parentQuestion.getAnswer().getOptions();
                        for (Option option : options)
                        {
                            if (option.getFactor()==MATCHFACTOR) {
                                if(!isSaved) {
                                    questionRelation.save();
                                    isSaved=true;
                                }
                                Match match = new Match();
                                match.setQuestionRelation(questionRelation);
                                match.save();
                                new QuestionOption(option, parentQuestion, match).save();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create QuestionOption QuestionRelation and Match relations
     * <p/>
     * checks if the dataElement is a parent(if is a parent it have mapMatchType and mapMatchLevel)
     * Later get the two childs and create the relation
     * it needs check what Options factors do match, and check it with method getMatchOption() .
     *
     * @param dataElement
     */
    private void addQuestionRelations(DataElement dataElement) {

        String programUid = DataElementExtended.findProgramUIDByDataElementUID(dataElement.getUid());
        String matchRelationType = mapMatchType.get(programUid + dataElement.getUid());
        String matchRelationGroup = mapMatchLevel.get(programUid + dataElement.getUid());
        Question appQuestion = mapQuestions.get(dataElement.getUid());

        if (matchRelationType != null && matchRelationType.equals(DataElementExtended.PARENT)) {
            List<String> mapChilds = mapMatchChilds.get(programUid + matchRelationGroup);
            List<Question> children = new ArrayList<>();
            children.add(mapQuestions.get(mapChilds.get(0)));
            children.add(mapQuestions.get(mapChilds.get(1)));

            if (mapQuestions.get(mapChilds.get(0)) != null && mapQuestions.get(mapChilds.get(1)) != null) {
                QuestionRelation questionRelation = new QuestionRelation();
                questionRelation.setOperation(0);
                questionRelation.setQuestion(appQuestion);
                questionRelation.save();
                questionRelation.createMatchFromQuestions(children);
            }
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
            e.printStackTrace();
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
            Log.d(TAG, "Creating new tab from: " +sdkDataElementExtended.getDataElement().getUid());
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
            TabGroup tabGroup = new TabGroup();
            tabGroup.setName(program.getName());

            Log.d(TAG, "With local programUId " + program.getUid());
            tabGroup.setProgram(program.getId_program());
            tabGroup.save();
            questionTab.setTabGroup(tabGroup);
            questionTab.save();
            mapTabGroup.put(tabGroupUid + programSdk.getUid(), tabGroup);
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

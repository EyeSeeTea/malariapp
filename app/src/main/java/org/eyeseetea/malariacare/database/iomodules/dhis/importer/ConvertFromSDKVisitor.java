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
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.Attribute;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;

import java.util.HashMap;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    private final static String TAG=".ConvertFromSDKVisitor";
    static Map<String,Object> appMapObjects;

    /**
     * Builders that helps while linking compositeScores and questions
     */
    CompositeScoreBuilder compositeScoreBuilder;
    QuestionBuilder questionBuilder;
    private final String ATTRIBUTE_PRODUCTIVITY_CODE="OUProductivity";


    public ConvertFromSDKVisitor(){
        appMapObjects = new HashMap();
        compositeScoreBuilder = new CompositeScoreBuilder();
        questionBuilder = new QuestionBuilder();
    }

    /**
     * Turns a sdk Program into an app Program
     * @param sdkProgramExtended
     */
    public void visit(ProgramExtended sdkProgramExtended){
        //Build program
        Program program=sdkProgramExtended.getProgram();
        org.eyeseetea.malariacare.database.model.Program appProgram=new org.eyeseetea.malariacare.database.model.Program();
        appProgram.setUid(program.getUid());
        appProgram.setName(program.getDisplayName());
        appProgram.save();


        //Annotate built program
        appMapObjects.put(program.getUid(), appProgram);

        //Visit children
        for(ProgramStage ps:program.getProgramStages()){
            new ProgramStageExtended(ps).accept(this);
        }
    }

    /**
     * Turns a sdk ProgramStage into a TabGroup
     * @param sdkProgramStageExtended
     */
    @Override
    public void visit(ProgramStageExtended sdkProgramStageExtended) {
        //Build tabgroup
        ProgramStage programStage=sdkProgramStageExtended.getProgramStage();
        org.eyeseetea.malariacare.database.model.Program appProgram=(org.eyeseetea.malariacare.database.model.Program)appMapObjects.get(programStage.getProgram().getUid());
        TabGroup appTabGroup = new TabGroup();
        //FIXME TabGroup has no UID right now
        appTabGroup.setName(programStage.getDisplayName());
        appTabGroup.setProgram(appProgram);
        appTabGroup.setUid(programStage.getUid());
        appTabGroup.save();

        //Annotate built tabgroup
        appMapObjects.put(programStage.getUid(), appTabGroup);

        //Visit children
        for(ProgramStageSection pss:programStage.getProgramStageSections()){
            new ProgramStageSectionExtended(pss).accept(this);
        }
    }

    /**
     * Turns a sdk organisationUnit into an app OrgUnit
     *
     * @param sdkOrganisationUnitExtended
     */
    @Override
    public void visit(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        //Create and save OrgUnitLevel
        OrganisationUnit organisationUnit=sdkOrganisationUnitExtended.getOrgUnit();
        org.eyeseetea.malariacare.database.model.OrgUnitLevel orgUnitLevel = new org.eyeseetea.malariacare.database.model.OrgUnitLevel();
        if(!appMapObjects.containsKey(String.valueOf(organisationUnit.getLevel()))) {
            orgUnitLevel.setName(PreferencesState.getInstance().getContext().getResources().getString(R.string.create_info_zone));
            orgUnitLevel.save();
            appMapObjects.put(String.valueOf(organisationUnit.getLevel()), orgUnitLevel);
        }
        //create the orgUnit
        org.eyeseetea.malariacare.database.model.OrgUnit appOrgUnit= new org.eyeseetea.malariacare.database.model.OrgUnit();
        //Set name
        appOrgUnit.setName(organisationUnit.getLabel());
        //Set uid
        appOrgUnit.setUid(organisationUnit.getId());
        //Set orgUnitLevel
        appOrgUnit.setOrgUnitLevel((org.eyeseetea.malariacare.database.model.OrgUnitLevel) appMapObjects.get(String.valueOf(organisationUnit.getLevel())));
        //Set default productivity
        appOrgUnit.setProductivity(0);
        //if exist in the server set productivity
        for(OrganisationUnitAttributeValue organisationUnitAttributeValue:organisationUnit.getAttributeValues())
        {
            Attribute attribute= MetaDataController.getAttribute(organisationUnitAttributeValue.getAttributeId());
            if(attribute.getCode().equals(ATTRIBUTE_PRODUCTIVITY_CODE)) {
                appOrgUnit.setProductivity(Integer.parseInt(organisationUnitAttributeValue.getValue()));
            }
        }
        //Set the parent
        //At this moment, the parent is a UID of a not pulled Org_unit , without the full org_unit the OrgUnit.orgUnit(parent) is null.
        String parent_id=null;
        parent_id = organisationUnit.getParent();
        if(parent_id!=null && !parent_id.equals("")) {
            appOrgUnit.setOrgUnit((org.eyeseetea.malariacare.database.model.OrgUnit) appMapObjects.get(String.valueOf(parent_id)));
        }
        else
            appOrgUnit.setOrgUnit((OrgUnit)null);
        appOrgUnit.save();
        //Annotate built orgunit
        appMapObjects.put(organisationUnit.getId(), appOrgUnit);

        //Associate programs
        buildOrgUnitProgramRelationships(appOrgUnit);
    }

    /**
     * Turns a sdk ProgramStageSection into a Tab
     * @param sdkProgramStageSectionExtended
     */
    @Override
    public void visit(ProgramStageSectionExtended sdkProgramStageSectionExtended) {
        //Build Tab

        ProgramStageSection programStageSection=sdkProgramStageSectionExtended.getProgramStageSection();
        org.eyeseetea.malariacare.database.model.TabGroup appTabGroup=(org.eyeseetea.malariacare.database.model.TabGroup)appMapObjects.get(programStageSection.getProgramStage());
        Tab appTab = new Tab();
        //FIXME TabGroup has no UID right now
        appTab.setName(programStageSection.getDisplayName());
        appTab.setType(Constants.TAB_AUTOMATIC);
        appTab.setOrder_pos(programStageSection.getSortOrder());
        appTab.setTabGroup(appTabGroup);
        appTab.save();
        //Annotate build tab
        appMapObjects.put(appTab.getClass() + appTab.getName(), appTab);
        appMapObjects.put(programStageSection.getUid(), appTab);
    }


    /**
     * Turns a sdk OptionSet into an Answer
     * @param sdkOptionSetExtended
     */
    @Override
    public void visit(OptionSetExtended sdkOptionSetExtended) {
        //Build answer
        OptionSet sdkOptionSet=sdkOptionSetExtended.getOptionSet();
        Answer appAnswer = new Answer();
        appAnswer.setName(sdkOptionSet.getName());
        appAnswer.save();

        //Annotate built answer
        appMapObjects.put(sdkOptionSet.getUid(), appAnswer);

        //Visit children
        for(Option option:sdkOptionSet.getOptions()){
            new OptionExtended(option).accept(this);
        }
    }

    /**
     * Turns a sdk Option into an Option
     * @param sdkOptionExtended
     */
    @Override
    public void visit(OptionExtended sdkOptionExtended) {
        //Build option
        Option sdkOption=sdkOptionExtended.getOption();
        Answer appAnswer=(Answer)appMapObjects.get(sdkOption.getOptionSet());
        org.eyeseetea.malariacare.database.model.Option appOption= new org.eyeseetea.malariacare.database.model.Option();
        appOption.setName(sdkOption.getName());
        appOption.setCode(sdkOption.getCode());
        appOption.setAnswer(appAnswer);
        appOption.setFactor(DataValueExtended.extractFactor(sdkOption.getCode()));
        appOption.save();
    }

    /**
     * Turns a sdk userAccount into a User
     * @param sdkUserAccountExtended
     */
    @Override
    public void visit(UserAccountExtended sdkUserAccountExtended) {
        UserAccount userAccount=sdkUserAccountExtended.getUserAccount();
        User appUser = new User();
        appUser.setUid(userAccount.getUId());
        appUser.setName(userAccount.getName());
        appUser.save();
    }


    /**
     * Turns a dataElement into a question or a compositeScore
     * @param sdkDataElementExtended
     */
    @Override
    public void visit(DataElementExtended sdkDataElementExtended) {
        Object questionOrCompositeScore;
        if(compositeScoreBuilder.isACompositeScore(sdkDataElementExtended)){
            questionOrCompositeScore=buildCompositeScore(sdkDataElementExtended);
        }else if(sdkDataElementExtended.isQuestion()){
            questionOrCompositeScore=buildQuestion(sdkDataElementExtended);
            //Question type is annotated in 'answer' from an attribute of the question
        }else{
            return;
        }
        appMapObjects.put(sdkDataElementExtended.getDataElement().getUid(), questionOrCompositeScore);
        //Both questions and scores are annotated
    }

    /**
     * Turns an event into a sent survey
     * @param sdkEventExtended
     */
    @Override
    public void visit(EventExtended sdkEventExtended) {
        Event event=sdkEventExtended.getEvent();
        OrgUnit orgUnit =(OrgUnit)appMapObjects.get(event.getOrganisationUnitId());
        TabGroup tabGroup=(TabGroup)appMapObjects.get(event.getProgramStageId());

        Survey survey=new Survey();
        //Any survey that comes from the pull has been sent
        survey.setStatus(Constants.SURVEY_SENT);
        //Set dates
        survey.setCreationDate(sdkEventExtended.getCreationDate());
        survey.setCompletionDate(sdkEventExtended.getCompletionDate());
        survey.setEventDate(sdkEventExtended.getEventDate());
        survey.setScheduledDate(sdkEventExtended.getScheduledDate());
        //Set fks
        survey.setOrgUnit(orgUnit);
        survey.setTabGroup(tabGroup);
        survey.setEventUid(event.getUid());
        survey.save();

        //Annotate object in map
        appMapObjects.put(event.getUid(), survey);

        //Visit its values
        for(DataValue dataValue:event.getDataValues()){
            DataValueExtended dataValueExtended=new DataValueExtended(dataValue);
            dataValueExtended.accept(this);
        }

    }

    @Override
    public void visit(DataValueExtended sdkDataValueExtended) {

        DataValue dataValue=sdkDataValueExtended.getDataValue();
        Survey survey=(Survey)appMapObjects.get(dataValue.getEvent());
        //Data value is a value from compositeScore
        if(appMapObjects.get(dataValue.getDataElement()) instanceof CompositeScore){
            //CHeck if it is a root score -> score
            CompositeScore compositeScore = (CompositeScore)appMapObjects.get(dataValue.getDataElement());
            if(CompositeScoreBuilder.ROOT_NODE_CODE.equals(compositeScore.getHierarchical_code())){
                Score score = new Score();
                score.setScore(Float.parseFloat(dataValue.getValue()));
                score.setUid(dataValue.getDataElement());
                score.setSurvey(survey);
                score.save();
            }
            return;
        }

        //Datavalue is a value from a question
        Question question=(Question)appMapObjects.get(dataValue.getDataElement());

        Value value=new Value();
        value.setQuestion(question);
        value.setSurvey(survey);

        org.eyeseetea.malariacare.database.model.Option option=sdkDataValueExtended.findOptionByQuestion(question);
        value.setOption(option);
        //No option -> text question (straight value)
        if(option==null){
            value.setValue(dataValue.getValue());
        }else{
        //Option -> extract value from code
            value.setValue(sdkDataValueExtended.extractValue());
        }
        value.save();
    }


    /**
     * Turns a dataElement into a question
     * @param dataElementExtended
     */
    private Question buildQuestion(DataElementExtended dataElementExtended) {
        DataElement dataElement = dataElementExtended.getDataElement();
        Question appQuestion = new Question();
        appQuestion.setDe_name(dataElement.getName());
        appQuestion.setUid(dataElement.getUid());
        appQuestion.setShort_name(dataElement.getShortName());
        appQuestion.setForm_name(dataElement.getFormName());
        appQuestion.setFeedback(dataElement.getDescription());
        appQuestion.setCode(dataElement.getCode());
        appQuestion.setOrder_pos(dataElementExtended.findOrder());
        appQuestion.setNumerator_w(dataElementExtended.findNumerator());
        appQuestion.setDenominator_w(dataElementExtended.findDenominator());
        appQuestion.setOutput(compositeScoreBuilder.findAnswerOutput(dataElementExtended));

        //Label does not have an optionset
        if (dataElement.getOptionSet() != null) {
            appQuestion.setAnswer((Answer) appMapObjects.get(dataElement.getOptionSet()));
        }else{
            //A question with NO optionSet is a Label Question
            Log.d(TAG, String.format("Question (%s) is a LABEL", dataElement.getUid()));
            appQuestion.setAnswer(buildAnswerLabel());
        }

        ProgramStageDataElement programStageDataElement = DataElementExtended.findProgramStageDataElementByDataElementUID(dataElement.getUid());
        appQuestion.setCompulsory(programStageDataElement.getCompulsory());
        appQuestion.setHeader(questionBuilder.saveHeader(dataElementExtended));
        questionBuilder.registerParentChildRelations(dataElementExtended);
        appQuestion.save();
        questionBuilder.add(appQuestion);
        return appQuestion;
    }


    public void buildRelations(DataElementExtended dataElementExtended) {
        questionBuilder.addRelations(dataElementExtended);
    }

    /**
     * A dataElement (question) without optionSet is a Label.
     * This method inits the LABEL answer (the first time) and updates de question.answer to it
     */

    /**
     * Builds a synthetic answer 'LABEL'
     * @return
     */
    public Answer buildAnswerLabel() {

        //Build a sintetic Key (AnswerLABEL)
        final String key=Answer.class+Constants.LABEL;
        //Look for sintetic LABEL (answer) already created
        Answer answer=(Answer)appMapObjects.get(key);

        //First time no Label answer has been created
        if(answer==null){
            answer=new Answer(Constants.LABEL);
            answer.save();
            appMapObjects.put(key,answer);
        }

        return answer;
    }
    /**
     * Turns a dataElement into a question
     * @param sdkDataElementExtended
     */
    private CompositeScore buildCompositeScore(DataElementExtended sdkDataElementExtended){
        DataElement dataElement=sdkDataElementExtended.getDataElement();
        CompositeScore compositeScore = new CompositeScore();
        compositeScore.setUid(dataElement.getUid());
        compositeScore.setLabel(dataElement.getFormName());
        compositeScore.setHierarchical_code(compositeScoreBuilder.findHierarchicalCode(sdkDataElementExtended));

        //Parent score and Order can only be set once every score in saved
        compositeScore.save();

        compositeScoreBuilder.add(compositeScore);
        return compositeScore;
    }

    /**
     * Due to permissions programs 'belongs' to a given orgunit
     */
    public void buildOrgUnitProgramRelationships(OrgUnit appOrgUnit){
        Log.d(TAG,"buildOrgUnitProgramRelationships "+appOrgUnit.getName());
        //Each assigned program
        for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(appOrgUnit.getUid(), ProgramType.WITHOUT_REGISTRATION)) {
            org.eyeseetea.malariacare.database.model.Program appProgram = (org.eyeseetea.malariacare.database.model.Program) appMapObjects.get(program.getUid());
            appProgram.addOrgUnit(appOrgUnit);
        }
    }

    @Override
    public void buildScores() {
        compositeScoreBuilder.buildScores();
    }

}

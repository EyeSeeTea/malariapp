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

import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitLevelExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Media;
import org.eyeseetea.malariacare.database.model.ServerMetadata;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitLevel;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    private final static String TAG=".ConvertFromSDKVisitor";
    static Map<String,Object> appMapObjects;

    /**
     * Builders that helps while linking compositeScores and questions
     */
    CompositeScoreBuilder compositeScoreBuilder;
    QuestionBuilder questionBuilder;
    List<Question> questions;

    public ConvertFromSDKVisitor(){
        appMapObjects = new HashMap();
        compositeScoreBuilder = new CompositeScoreBuilder();
        questionBuilder = new QuestionBuilder();
        questions = new ArrayList<>();

        //Reload static dataElement codes
        DataElementExtended.reloadDataElementTypeCodes();
    }

    public void saveBatch(){
        //Save questions in batch
        new SaveModelTransaction<>(ProcessModelInfo.withModels(questions)).onExecute();

        //Refresh media references
        List<Media> medias = questionBuilder.getListMedia();
        for(Media media: medias){
            media.updateQuestion();
        }
        //Save media in batch
        new SaveModelTransaction<>(ProcessModelInfo.withModels(medias)).onExecute();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
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
        String sdkProgramUID = programStage.getProgram().getUid();
        org.eyeseetea.malariacare.database.model.Program appProgram = (org.eyeseetea.malariacare.database.model.Program) appMapObjects.get(sdkProgramUID);
        TabGroup appTabGroup = new TabGroup();
        appTabGroup.setName(programStage.getName());
        appTabGroup.setUid(programStage.getUid());
        appTabGroup.setProgram(appProgram);
        appTabGroup.save();

        appMapObjects.put(appTabGroup.getUid(),appTabGroup);

        //Visit children
        for(ProgramStageSection pss:programStage.getProgramStageSections()){
            new ProgramStageSectionExtended(pss).accept(this);
        }
    }

    /**
     * Turns a sdk level into an app level
     * @param sdkOrganisationUnitLevelExtended
     */
    @Override
    public void visit(OrganisationUnitLevelExtended sdkOrganisationUnitLevelExtended){
        OrganisationUnitLevel organisationUnitLevel = sdkOrganisationUnitLevelExtended.getOrganisationUnitLevel();
        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
        orgUnitLevel.setUid(organisationUnitLevel.getId());
        orgUnitLevel.setName(organisationUnitLevel.getDisplayName());
        orgUnitLevel.save();

        appMapObjects.put(sdkOrganisationUnitLevelExtended.buildKey(),orgUnitLevel);
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
        OrgUnitLevel appOrgUnitLevel = (OrgUnitLevel)appMapObjects.get(OrganisationUnitLevelExtended.buildKey(organisationUnit.getLevel()));
        //create the orgUnit
        org.eyeseetea.malariacare.database.model.OrgUnit appOrgUnit= new org.eyeseetea.malariacare.database.model.OrgUnit();
        //Set name
        if(organisationUnit.getLabel()==null)
            appOrgUnit.setName(organisationUnit.getName());
        else
            appOrgUnit.setName(organisationUnit.getLabel());
        //Set uid
        appOrgUnit.setUid(organisationUnit.getId());
        //Set orgUnitLevel
        appOrgUnit.setOrgUnitLevel(appOrgUnitLevel);
        //Since there is no guaranteed order in orgunits parent unit might not be yet converted or even pulled at all
        //Thus building hierarchy must be done in a second step

        appOrgUnit.save();
        //Annotate built orgunit
        appMapObjects.put(organisationUnit.getId(), appOrgUnit);

        //Associate programs
        sdkOrganisationUnitExtended.setAppOrgUnit(appOrgUnit);
        buildOrgUnitProgramRelation(sdkOrganisationUnitExtended);
    }

    /**
     * Turns a sdk ProgramStageSection into a Tab
     * @param sdkProgramStageSectionExtended
     */
    @Override
    public void visit(ProgramStageSectionExtended sdkProgramStageSectionExtended) {
        //Build Tab

        ProgramStageSection programStageSection=sdkProgramStageSectionExtended.getProgramStageSection();
        String tabGroupUID = programStageSection.getProgramStage();
        TabGroup tabGroup = (TabGroup)appMapObjects.get(tabGroupUID);
        Tab appTab = new Tab();
        appTab.setTabGroup(tabGroup);
        appTab.setName(programStageSection.getDisplayName());
        appTab.setType(Constants.TAB_AUTOMATIC);
        appTab.setOrder_pos(programStageSection.getSortOrder());
        appTab.save();
        //Annotate build tab
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
        appOption.setFactor(sdkOptionExtended.getFactor());
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
        if(sdkDataElementExtended.isCompositeScore()){
            questionOrCompositeScore=buildCompositeScore(sdkDataElementExtended);
        }else if(sdkDataElementExtended.isQuestion()){
            questionOrCompositeScore=buildQuestion(sdkDataElementExtended);
            //Question type is annotated in 'answer' from an attribute of the question
        }else if (sdkDataElementExtended.isControlDataElement()) {
            //The controlDataelements should be unique and only needs be saved one time
            if(appMapObjects.containsKey(sdkDataElementExtended.getDataElement().getUid()+sdkDataElementExtended.getProgramUid()))
                return;
            questionOrCompositeScore=buildControlDataElement(sdkDataElementExtended);
        } else {
            Log.d(TAG, "Error" + sdkDataElementExtended.getDataElement().toString());
            return;
        }
        appMapObjects.put(sdkDataElementExtended.getDataElement().getUid()+sdkDataElementExtended.getProgramUid(), questionOrCompositeScore);
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
        TabGroup tabGroup = (TabGroup)appMapObjects.get(event.getProgramStageId());
        Survey survey=new Survey();
        //Any survey that comes from the pull has been sent
        survey.setStatus(Constants.SURVEY_SENT);
        //Completiondate == Event date
        survey.setCompletionDate(sdkEventExtended.getEventDate());

        //Set dates( to prevent a null value, all take the getEventDate date before datavalue visitor)
        survey.setCreationDate(sdkEventExtended.getEventDate());
        survey.setUploadDate(sdkEventExtended.getEventDate());
        //Scheduled date == Due date
        survey.setScheduleDate(sdkEventExtended.getDueDate());
        //Set fks
        survey.setOrgUnit(orgUnit);
        survey.setEventUid(event.getUid());
        survey.setTabGroup(tabGroup);
        survey.save();

        //Annotate object in map
        EventToSurveyBuilder eventToSurveyBuilder=new EventToSurveyBuilder(survey);
        appMapObjects.put(event.getUid(), eventToSurveyBuilder);

        //Visit its values
        for(DataValue dataValue:event.getDataValues()){
            DataValueExtended dataValueExtended=new DataValueExtended(dataValue);
            dataValueExtended.setProgramUid(event.getProgramId());
            dataValueExtended.accept(this);
        }
        //Once all the values are processed save common data across created surveys
        eventToSurveyBuilder.saveCommonData();
    }

    @Override
    public void visit(DataValueExtended sdkDataValueExtended) {

        DataValue dataValue=sdkDataValueExtended.getDataValue();
        EventToSurveyBuilder eventToSurveyBuilder =(EventToSurveyBuilder) appMapObjects.get(dataValue.getEvent());

        //General common data (mainscore, createdby, createdon, uploadedon..)

        //-> mainScore
        if(appMapObjects.get(dataValue.getDataElement()+sdkDataValueExtended.getProgramUid()) instanceof CompositeScore){
            CompositeScore compositeScore = (CompositeScore)appMapObjects.get(dataValue.getDataElement()+sdkDataValueExtended.getProgramUid());
            //Only mainScores are annotated
            eventToSurveyBuilder.setMainScore(compositeScore,dataValue);
            Log.i(TAG,String.format("Event %s with mainScore %s",eventToSurveyBuilder.getEventUid(),dataValue.getValue()));
            return;
        }

        //-> createdOn
        if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.created_on_code))){
            eventToSurveyBuilder.setCreatedOn(dataValue);
            Log.i(TAG,String.format("Event %s created on %s",eventToSurveyBuilder.getEventUid(),dataValue.getValue()));
            return;
        }

        //-> uploadedOn
        if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.upload_on_code))){
            eventToSurveyBuilder.setUploadedOn(dataValue);
            Log.i(TAG,String.format("Event %s uploaded on %s",eventToSurveyBuilder.getEventUid(),dataValue.getValue()));
            return;
        }

        //-> uploadedBy (updatedBy is ignored)
        if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.uploaded_by_code))){
            eventToSurveyBuilder.setUploadedBy(dataValue);
            Log.i(TAG,String.format("Event %s created by %s",eventToSurveyBuilder.getEventUid(),dataValue.getValue()));
            return;
        }

        Value value=new Value();
        //Datavalue is a value from a question
        org.eyeseetea.malariacare.database.model.Option option = null;
        try{
            Question question=(Question)appMapObjects.get(dataValue.getDataElement()+sdkDataValueExtended.getProgramUid());
            value.setQuestion(question);
            option=sdkDataValueExtended.findOptionByQuestion(question);
            value.setOption(option);
        }catch (ClassCastException e){
            Log.d(TAG,"Ignoring controlDataelement in DataValue converting");
        }

        value.setSurvey(eventToSurveyBuilder.getDefaultSurvey());
        //No option -> text question (straight value)
        if(option==null){
            value.setValue(dataValue.getValue());
        }else{
            //Option -> extract value from code
            value.setValue(option.getName());
        }
        value.setUploadDate(new Date());
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
        appQuestion.setRow(dataElementExtended.findRow());
        appQuestion.setColumn(dataElementExtended.findColumn());
        appQuestion.setOutput(compositeScoreBuilder.findAnswerOutput(dataElementExtended));

        //Label does not have an optionset
        if (dataElement.getOptionSet() != null) {
            appQuestion.setAnswer((Answer) appMapObjects.get(dataElement.getOptionSet()));
        }else{
            //A question with NO optionSet is a Label Question
            Log.d(TAG, String.format("Question (%s) is a LABEL", dataElement.getUid()));
            appQuestion.setAnswer(buildAnswerLabel());
        }

        ProgramStageDataElement programStageDataElement = DataElementExtended.findProgramStageDataElementByDataElementExtended(dataElementExtended);
        appQuestion.setCompulsory(programStageDataElement.getCompulsory());
        appQuestion.setHeader(questionBuilder.findOrSaveHeader(dataElementExtended,appMapObjects));
        questionBuilder.registerParentChildRelations(dataElementExtended);
        questionBuilder.attachMedia(dataElementExtended, appQuestion);
        questions.add(appQuestion);
        questionBuilder.add(appQuestion, dataElementExtended.getProgramUid());
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

        compositeScoreBuilder.add(compositeScore, sdkDataElementExtended.getProgramUid());

        return compositeScore;
    }



    private ServerMetadata buildControlDataElement(DataElementExtended sdkDataElementExtended) {
        DataElement dataElement=sdkDataElementExtended.getDataElement();
        ServerMetadata controlDataElement = new ServerMetadata();
        controlDataElement.setUid(dataElement.getUid());
        controlDataElement.setCode(dataElement.getCode());
        controlDataElement.setName(dataElement.getDisplayName());
        controlDataElement.setValueType(dataElement.getValueType().name());

        //Parent score and Order can only be set once every score in saved
        controlDataElement.save();
        return controlDataElement;

    }

    /**
     * Due to permissions programs 'belongs' to a given orgunit and that relationship has a productivity
     * @param sdkOrganisationUnitExtended Extended sdk orgUnit (used to cache array with values)
     */
    public void buildOrgUnitProgramRelation(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        OrgUnit appOrgUnit = sdkOrganisationUnitExtended.getAppOrgUnit();
        Log.d(TAG, "buildOrgUnitProgramRelation " + appOrgUnit.getName());
        //Each assigned program
        for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(appOrgUnit.getUid(), ProgramType.WITHOUT_REGISTRATION)) {
            ProgramExtended sdkProgramExtended = new ProgramExtended(program);
            sdkProgramExtended.setAppProgram((org.eyeseetea.malariacare.database.model.Program) appMapObjects.get(program.getUid()));

            addOrgUnitProgramRelation(sdkOrganisationUnitExtended,sdkProgramExtended);
        }
    }

    /**
     * Updates the relationship between the given orgUnit and program according to their attribute values
     * @param sdkOrganisationUnitExtended
     * @param sdkProgramExtended
     */
    private void addOrgUnitProgramRelation(OrganisationUnitExtended sdkOrganisationUnitExtended, ProgramExtended sdkProgramExtended){
        //Take app references
        OrgUnit appOrgUnit=sdkOrganisationUnitExtended.getAppOrgUnit();
        org.eyeseetea.malariacare.database.model.Program appProgram=sdkProgramExtended.getAppProgram();

        //Add relationship
        OrgUnitProgramRelation orgUnitProgramRelation=appProgram.addOrgUnit(appOrgUnit);

        //Add productivity to that relationship
        Integer productivityIndex=sdkProgramExtended.getProductivityPosition();
        Integer orgUnitProgramRelationProductivity = sdkOrganisationUnitExtended.getProductivity(productivityIndex);
        orgUnitProgramRelation.setProductivity(orgUnitProgramRelationProductivity);
        orgUnitProgramRelation.save();
    }

    @Override
    public void buildScores() {
        compositeScoreBuilder.buildScores();
    }

    /**
     * Builds the orgunit hierarchy whenever is possible
     * @param assignedOrganisationsUnits
     * @return
     */
    public boolean buildOrgUnitHierarchy(List<OrganisationUnit> assignedOrganisationsUnits) {

        for(OrganisationUnit organisationUnit:assignedOrganisationsUnits){
            if(!ProgressActivity.PULL_IS_ACTIVE) return false;

            OrgUnit appOrgUnit = (OrgUnit)appMapObjects.get(organisationUnit.getId());
            String parentUID=organisationUnit.getParent();
            //FIXME: review this algorithm
            if(parentUID==null) {
                //path format=/VaXGMQY18R2/TyoXRBeZ12K/TeqzAowss4n/Doa9u6qkSO3/qeENMD3x6y7
                //path[0] is ""
                //path [1] is the last parent "VaXGMQY18R2"
                String path = organisationUnit.getPath();
                String[] pathUids = path.split("/");
                if (pathUids.length > 2 && !pathUids[1].equals(organisationUnit.getId())) {
                    for (int i = 2; i < pathUids.length; i++) {
                        if (pathUids[i].equals(organisationUnit.getId())) {
                            parentUID = pathUids[i - 1];
                            Log.d(TAG, organisationUnit.getId() + " parent " + parentUID);
                        }
                    }
                }
            }
            //No parent nothing to do
            if(parentUID==null){
                Log.i(TAG,String.format("%s is a root orgUnit",appOrgUnit.getName()));
                continue;
            }

            //Find parent
            OrgUnit parentOrgUnit = (OrgUnit) appMapObjects.get(parentUID);

            //Due to server permissions parent unit might not be loaded
            if(parentOrgUnit==null){
                Log.w(TAG,String.format("Cannot find parent orgunit for %s",appOrgUnit.getName()));
                continue;
            }

            appOrgUnit.setOrgUnit(parentOrgUnit.getId_org_unit());
            appOrgUnit.save();
        }
        return true;

    }

}

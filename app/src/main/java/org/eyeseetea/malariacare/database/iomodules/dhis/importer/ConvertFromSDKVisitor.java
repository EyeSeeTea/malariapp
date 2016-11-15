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
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models
        .ProgramStageDataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.ServerMetadata;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramCompositeScoreDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramQuestionDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramStageSectionTabDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramSurveyDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramTabDict;
import org.eyeseetea.malariacare.sdk.SdkController;
import org.eyeseetea.malariacare.sdk.models.DataValueFlow;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitLevelFlow;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    private final static String TAG=".ConvertFromSDKVisitor";
    static Map<String,org.eyeseetea.malariacare.database.model.Program> programMapObjects;
    static Map<String,Object> controlDataElementMapObjects;
    static Map<String,OrgUnitLevel> orgUnitLevelMap;
    static Map<String,OrgUnit> orgUnitDict;
    static Map<String,Answer> answerMap;
    static ProgramTabDict programTabDict;
    static ProgramStageSectionTabDict programStageSectionTabDict;
    static ProgramSurveyDict programSurveyDict;
    static ProgramCompositeScoreDict programCompositeScoreDict;
    static ProgramQuestionDict programQuestionDict;


    /**
     * Builders that helps while linking compositeScores and questions
     */
    CompositeScoreBuilder compositeScoreBuilder;
    QuestionBuilder questionBuilder;
    List<Question> questions;

    private final String ATTRIBUTE_PRODUCTIVITY_CODE="OUProductivity";
    private final String SDKDateFormat="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    ProgramExtended actualProgram;

    public ConvertFromSDKVisitor(){
        programMapObjects = new HashMap();
        controlDataElementMapObjects =new HashMap();
        orgUnitLevelMap = new HashMap();
        orgUnitDict = new HashMap();
        answerMap = new HashMap();
        programTabDict = new ProgramTabDict();
        programStageSectionTabDict = new ProgramStageSectionTabDict();
        programQuestionDict = new ProgramQuestionDict();
        programSurveyDict = new ProgramSurveyDict();
        programCompositeScoreDict = new ProgramCompositeScoreDict();
        compositeScoreBuilder = new CompositeScoreBuilder();
        questionBuilder = new QuestionBuilder();
        questions = new ArrayList<>();

        //Reload static dataElement codes
        DataElementExtended.reloadDataElementTypeCodes();
    }

    public void saveBatch(){
        SdkController.saveBatch();
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
        actualProgram=sdkProgramExtended;
        org.eyeseetea.malariacare.database.model.Program appProgram=new org.eyeseetea.malariacare.database.model.Program();
        appProgram.setUid(sdkProgramExtended.getUid());
        appProgram.setName(sdkProgramExtended.getDisplayName());
        appProgram.save();


        //Annotate built program
        programMapObjects.put(sdkProgramExtended.getUid(), appProgram);

        //Visit children
        for(ProgramStageFlow ps:sdkProgramExtended.getProgramStages()){
            new ProgramStageExtended(ps).accept(this);
        }
    }

    /**
     * Turns a sdk ProgramStage into a TabGroup
     * @param sdkProgramStageExtended
     */
    @Override
    public void visit(ProgramStageExtended sdkProgramStageExtended) {
        //Build Program
        String sdkProgramUID = sdkProgramStageExtended.getProgramUid();
        org.eyeseetea.malariacare.database.model.Program appProgram = programMapObjects.get(sdkProgramUID);
        appProgram.setStageUid(sdkProgramStageExtended.getUid());
        appProgram.update();

        programMapObjects.put(appProgram.getUid(),appProgram);

        //Visit children
        for(ProgramStageSectionFlow pss:sdkProgramStageExtended.getProgramStageSections()){
            new ProgramStageSectionExtended(pss).accept(this);
        }
    }

    /**
     * Turns a sdk level into an app level
     * @param sdkOrganisationUnitLevelExtended
     */
    @Override
    public void visit(OrganisationUnitLevelExtended sdkOrganisationUnitLevelExtended){
        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
        orgUnitLevel.setUid(sdkOrganisationUnitLevelExtended.getUid());
        orgUnitLevel.setName(sdkOrganisationUnitLevelExtended.getDisplayName());
        orgUnitLevel.save();

        orgUnitLevelMap.put(sdkOrganisationUnitLevelExtended.buildKey(),orgUnitLevel);
    }

    /**
     * Turns a sdk organisationUnit into an app OrgUnit
     *
     * @param sdkOrganisationUnitExtended
     */
    @Override
    public void visit(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        //Create and save OrgUnitLevel
        OrgUnitLevel appOrgUnitLevel = orgUnitLevelMap.get(OrganisationUnitLevelExtended.buildKey(sdkOrganisationUnitExtended.getLevel()));
        //create the orgUnit
        org.eyeseetea.malariacare.database.model.OrgUnit appOrgUnit= new org.eyeseetea.malariacare.database.model.OrgUnit();
        //Set name
        if(sdkOrganisationUnitExtended.getLabel()==null)
            appOrgUnit.setName(sdkOrganisationUnitExtended.getName());
        else
            appOrgUnit.setName(sdkOrganisationUnitExtended.getLabel());
        //Set uid
        appOrgUnit.setUid(sdkOrganisationUnitExtended.getUid());
        //Set orgUnitLevel
        appOrgUnit.setOrgUnitLevel(appOrgUnitLevel);
        //Since there is no guaranteed order in orgunits parent unit might not be yet converted or even pulled at all
        //Thus building hierarchy must be done in a second step

        appOrgUnit.save();
        //Annotate built orgunit
        orgUnitDict.put(sdkOrganisationUnitExtended.getUid(), appOrgUnit);

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
        String programUID = (ProgramStageExtended.getProgramStage(sdkProgramStageSectionExtended.getProgramStage().getUid())).getProgram().getUId();
        org.eyeseetea.malariacare.database.model.Program program = programMapObjects.get(programUID);
        Tab appTab = new Tab();
        appTab.setProgram(program);
        appTab.setName(sdkProgramStageSectionExtended.getDisplayName());
        appTab.setType(Constants.TAB_AUTOMATIC);
        appTab.setOrder_pos(sdkProgramStageSectionExtended.getSortOrder());
        appTab.save();
        //Annotate build tab
        programStageSectionTabDict.put(actualProgram.getUid(),sdkProgramStageSectionExtended.getUid(), appTab);

        programTabDict.put(actualProgram.getUid(),sdkProgramStageSectionExtended.getUid(), appTab);
    }


    /**
     * Turns a sdk OptionSet into an Answer
     * @param sdkOptionSetExtended
     */
    @Override
    public void visit(OptionSetExtended sdkOptionSetExtended) {
        //Build answer
        Answer appAnswer = new Answer();
        appAnswer.setName(sdkOptionSetExtended.getName());
        appAnswer.save();

        //Annotate built answer
        answerMap.put(sdkOptionSetExtended.getUid(), appAnswer);

        //Visit children
        for(OptionFlow option:sdkOptionSetExtended.getOptionsList()){
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
        Answer appAnswer= answerMap.get(sdkOptionExtended.getOptionSet());
        org.eyeseetea.malariacare.database.model.Option appOption= new org.eyeseetea.malariacare.database.model.Option();
        appOption.setName(sdkOptionExtended.getName());
        appOption.setUid(sdkOptionExtended.getUid());
        appOption.setCode(sdkOptionExtended.getCode());
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
        User appUser = new User();
        appUser.setUid(sdkUserAccountExtended.getUid());
        appUser.setName(sdkUserAccountExtended.getName());
        appUser.save();
    }


    /**
     * Turns a dataElement into a question or a compositeScore
     * @param sdkDataElementExtended
     */
    @Override
    public void visit(DataElementExtended sdkDataElementExtended) {
        if(sdkDataElementExtended.isCompositeScore()){
            programCompositeScoreDict.put(actualProgram.getUid(),sdkDataElementExtended.getUid(),buildCompositeScore(sdkDataElementExtended));
        }else if(sdkDataElementExtended.isQuestion()){
            programQuestionDict.put(actualProgram.getUid(),sdkDataElementExtended.getUid(),buildQuestion(sdkDataElementExtended));
            //Question type is annotated in 'answer' from an attribute of the question
        }else if (sdkDataElementExtended.isControlDataElement()) {
            if(!controlDataElementMapObjects.containsKey(sdkDataElementExtended.getUid()))
                controlDataElementMapObjects.put(sdkDataElementExtended.getUid(),buildControlDataElement(sdkDataElementExtended));
        } else {
            Log.d(TAG, "Error" + sdkDataElementExtended.getDataElement().toString());
            return;
        }
    }

    /**
     * Turns an event into a sent survey
     * @param sdkEventExtended
     */
    @Override
    public void visit(EventExtended sdkEventExtended) {
        OrgUnit orgUnit = orgUnitDict.get(sdkEventExtended.getOrganisationUnitId());
        org.eyeseetea.malariacare.database.model.Program program =programMapObjects.get(ProgramStageExtended.getProgramStage(sdkEventExtended.getProgramStageId()).getProgram().getUId());
        Survey survey=new Survey();
        //Any survey that comes from the pull has been sent
        survey.setStatus(Constants.SURVEY_SENT);
        //Completiondate == Event date
        survey.setCompletionDate(sdkEventExtended.getEventDate());

        //Set dates( to prevent a null value, all take the getEventDate date before datavalue visitor)
        survey.setCreationDate(sdkEventExtended.getEventDate());
        survey.setUploadDate(sdkEventExtended.getEventDate());
        //Scheduled date == Due date
        survey.setScheduledDate(sdkEventExtended.getDueDate());
        //Set fks
        survey.setOrgUnit(orgUnit);
        survey.setEventUid(sdkEventExtended.getUid());
        survey.setProgram(program);
        survey.save();

        //Annotate object in map
        //EventToSurveyBuilder eventToSurveyBuilder=new EventToSurveyBuilder(survey);
        programSurveyDict.put(actualProgram.getUid(), sdkEventExtended.getUid(), survey);

        //Visit its values
        for(DataValueFlow dataValue:sdkEventExtended.getDataValues()){
            DataValueExtended dataValueExtended=new DataValueExtended(dataValue);
            dataValueExtended.setProgramUid(sdkEventExtended.getProgramId());
            dataValueExtended.accept(this);
        }
        //Once all the values are processed save common data across created surveys
        //eventToSurveyBuilder.saveCommonData();
    }

    @Override
    public void visit(DataValueExtended sdkDataValueExtended) {
        Survey survey=programSurveyDict.get(actualProgram.getUid(),sdkDataValueExtended.getEvent().getUId());

        //General common data (mainscore, createdby, createdon, uploadedon..)


        //Data value is a value from compositeScore
        if(programCompositeScoreDict.containsKey(actualProgram.getUid(),sdkDataValueExtended.getDataElement())){
            //CHeck if it is a root score -> score
            CompositeScore compositeScore = programCompositeScoreDict.get(actualProgram.getUid(),sdkDataValueExtended.getDataElement());
            //Only root scores are important
            if(!CompositeScoreBuilder.isRootScore(compositeScore)) {
                return;
            }

            Score score = new Score();
            score.setScore(Float.parseFloat(sdkDataValueExtended.getValue()));
            score.setUid(sdkDataValueExtended.getDataElement());
            score.setSurvey(survey);
            score.save();
            Log.i(TAG,String.format("Event %s with mainScore %s",survey.getEventUid(),sdkDataValueExtended.getValue()));
            return;
        }


        //-> createdOn
        if(sdkDataValueExtended.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.created_on_code))){
            survey.setCreationDate(EventExtended.parseShortDate(sdkDataValueExtended.getValue()));
            survey.save();
            Log.i(TAG,String.format("Event %s created on %s",survey.getEventUid(),sdkDataValueExtended.getValue()));
            return;
        }

        //-> uploadedOn
        /*if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.upload_on_code))){
            survey.setUploadDate(EventExtended.parseShortDate(dataValue.getValue());
            Log.i(TAG,String.format("Event %s uploaded on %s",survey.getEventUid(),dataValue.getValue()));
            return;
        }*/

        //-> uploadedBy (updatedBy is ignored)
        if(sdkDataValueExtended.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.uploaded_by_code))){
            User user=User.getUser(sdkDataValueExtended.getValue());
            if(user==null) {
                user = new User(sdkDataValueExtended.getValue(), sdkDataValueExtended.getValue());
                user.save();
            }
            survey.setUser(user);
            survey.save();
            //Annotate object in map
            programSurveyDict.put(actualProgram.getUid(),sdkDataValueExtended.getEvent().getUId(), survey);
            return;
            //eventToSurveyBuilder.setUploadedBy(dataValue);
            //Log.i(TAG,String.format("Event %s created by %s",eventToSurveyBuilder.getEventUid(),dataValue.getValue()));
            //return;
        }

        Value value=new Value();
        //Datavalue is a value from a question
        org.eyeseetea.malariacare.database.model.Option option = null;
        if(programQuestionDict.containsKey(actualProgram.getUid(),sdkDataValueExtended.getDataElement())){
            Question question = programQuestionDict.get(actualProgram.getUid(), sdkDataValueExtended.getDataElement());
            try {
                value.setQuestion(question);
                option = sdkDataValueExtended.findOptionByQuestion(question);
                value.setOption(option);
            } catch (ClassCastException e) {
                Log.d(TAG, "Exception with controlDataelement in DataValue converting");
            }
        }

        value.setSurvey(survey);
        //No option -> text question (straight value)
        if(option==null){
            value.setValue(sdkDataValueExtended.getValue());
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
        Question appQuestion = new Question();
        appQuestion.setDe_name(dataElementExtended.getName());
        appQuestion.setUid(dataElementExtended.getUid());
        appQuestion.setShort_name(dataElementExtended.getShortName());
        appQuestion.setForm_name(dataElementExtended.getFormName());
        appQuestion.setFeedback(dataElementExtended.getDescription());
        appQuestion.setCode(dataElementExtended.getCode());
        appQuestion.setOrder_pos(dataElementExtended.findOrder());
        appQuestion.setNumerator_w(dataElementExtended.findNumerator());
        appQuestion.setDenominator_w(dataElementExtended.findDenominator());
        appQuestion.setRow(dataElementExtended.findRow());
        appQuestion.setColumn(dataElementExtended.findColumn());
        appQuestion.setOutput(compositeScoreBuilder.findAnswerOutput(dataElementExtended));

        //Label does not have an optionset
        if (dataElementExtended.getOptionSet() != null) {
            appQuestion.setAnswer(answerMap.get(dataElementExtended.getOptionSet()));
        }else{
            //A question with NO optionSet is a Label Question
            Log.d(TAG, String.format("Question (%s) is a LABEL", dataElementExtended.getUid()));
            appQuestion.setAnswer(buildAnswerLabel());
        }


        //// FIXME: 11/11/2016
        ProgramStageDataElementExtended programStageDataElement = new ProgramStageDataElementExtended(DataElementExtended.findProgramStageDataElementByDataElementExtended(dataElementExtended));

        appQuestion.setCompulsory(programStageDataElement.getCompulsory());
        appQuestion.setHeader(questionBuilder.findOrSaveHeader(dataElementExtended,programTabDict, actualProgram.getUid()));
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
        Answer answer= answerMap.get(key);

        //First time no Label answer has been created
        if(answer==null){
            answer=new Answer(Constants.LABEL);
            answer.save();
            answerMap.put(key,answer);
        }

        return answer;
    }
    /**
     * Turns a dataElement into a question
     * @param sdkDataElementExtended
     */
    private CompositeScore buildCompositeScore(DataElementExtended sdkDataElementExtended){
        CompositeScore compositeScore = new CompositeScore();
        compositeScore.setUid(sdkDataElementExtended.getUid());
        compositeScore.setLabel(sdkDataElementExtended.getFormName());
        compositeScore.setHierarchical_code(compositeScoreBuilder.findHierarchicalCode(sdkDataElementExtended));
        compositeScore.setOrder_pos(sdkDataElementExtended.findOrder());
        //Parent score and Order can only be set once every score in saved
        compositeScore.save();

        compositeScoreBuilder.add(compositeScore, sdkDataElementExtended.getProgramUid());

        return compositeScore;
    }



    private ServerMetadata buildControlDataElement(DataElementExtended sdkDataElementExtended) {
        ServerMetadata controlDataElement = new ServerMetadata();
        controlDataElement.setUid(sdkDataElementExtended.getUid());
        controlDataElement.setCode(sdkDataElementExtended.getCode());
        controlDataElement.setName(sdkDataElementExtended.getDisplayName());
        controlDataElement.setValueType(sdkDataElementExtended.getValueType().name());

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
        for (ProgramFlow program : SdkController.getProgramsForOrganisationUnit(appOrgUnit.getUid(), ProgramType.WITHOUT_REGISTRATION)) {
            ProgramExtended sdkProgramExtended = new ProgramExtended(program);
            sdkProgramExtended.setAppProgram( programMapObjects.get(sdkProgramExtended.getUid()));

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
    public boolean buildOrgUnitHierarchy(List<OrganisationUnitFlow> assignedOrganisationsUnits) {

        for(OrganisationUnitFlow organisationUnit:assignedOrganisationsUnits){
            if(!ProgressActivity.PULL_IS_ACTIVE) return false;

            OrgUnit appOrgUnit = orgUnitDict.get(organisationUnit.getId());
            String parentUID=organisationUnit.getParent().getUId();
            //FIXME: review this algorithm
            if(parentUID==null) {
                //path format=/VaXGMQY18R2/TyoXRBeZ12K/TeqzAowss4n/Doa9u6qkSO3/qeENMD3x6y7
                //path[0] is ""
                //path [1] is the last parent "VaXGMQY18R2"
                String path = new OrganisationUnitExtended(organisationUnit).getPath();
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
            OrgUnit parentOrgUnit = orgUnitDict.get(parentUID);

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

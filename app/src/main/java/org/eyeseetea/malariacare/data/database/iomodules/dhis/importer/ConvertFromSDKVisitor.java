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

import android.util.Log;

import com.raizlabs.android.dbflow.structure.Model;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OrganisationUnitLevelExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageDataElementExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.multikeydictionaries.ProgramCompositeScoreDict;
import org.eyeseetea.malariacare.data.database.utils.multikeydictionaries.ProgramQuestionDict;
import org.eyeseetea.malariacare.data.database.utils.multikeydictionaries.ProgramStageSectionTabDict;
import org.eyeseetea.malariacare.data.database.utils.multikeydictionaries.ProgramSurveyDict;
import org.eyeseetea.malariacare.data.database.utils.multikeydictionaries.ProgramTabDict;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    private final static String TAG = ".ConvertFromSDKVisitor";
    static Map<String, ProgramDB> programMapObjects;
    static Map<String, Object> controlDataElementMapObjects;
    static Map<String, OrgUnitLevelDB> orgUnitLevelMap;
    static Map<String, OrgUnitDB> orgUnitDict;
    static Map<String, AnswerDB> answerMap;
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
    public static List<Model> questions;

    private final String ATTRIBUTE_PRODUCTIVITY_CODE = "OUProductivity";
    private final String SDKDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public ProgramExtended actualProgram;

    public ConvertFromSDKVisitor() {
        programMapObjects = new HashMap();
        controlDataElementMapObjects = new HashMap();
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
    }

    public void saveBatch() {
        List<Model> models = new ArrayList<>();
        models.addAll(questions);
        SdkQueries.saveBatch(models);
        for (MediaDB media : questionBuilder.listMedia) {
            media.updateQuestion();
        }
        models = new ArrayList<>();
        models.addAll(questionBuilder.listMedia);
        SdkQueries.saveBatch(models);
    }

    /**
     * Turns a sdk Program into an app Program
     */
    public void visit(ProgramExtended program) {
        //Build program
        actualProgram = program;
        ProgramDB appProgram =
                new ProgramDB();
        appProgram.setUid(program.getUid());
        appProgram.setName(program.getDisplayName());
        appProgram.save();


        //Annotate built program
        programMapObjects.put(program.getUid(), appProgram);

        //Visit children
        for (ProgramStageExtended ps : program.getProgramStages()) {
            new ProgramStageExtended(ps).accept(this);
        }
    }

    /**
     * Turns a sdk ProgramStage into a TabGroup
     */
    @Override
    public void visit(ProgramStageExtended programStage) {
        //Build Program
        String sdkProgramUID = programStage.getProgramUid();
        ProgramDB appProgram = programMapObjects.get(
                sdkProgramUID);
        appProgram.setStageUid(programStage.getUid());
        appProgram.update();

        programMapObjects.put(appProgram.getUid(), appProgram);

        //Visit children
        for (ProgramStageSectionExtended pss : programStage.getProgramStageSections()) {
            new ProgramStageSectionExtended(pss).accept(this);
        }
    }

    /**
     * Turns a sdk level into an app level
     */
    @Override
    public void visit(OrganisationUnitLevelExtended sdkOrganisationUnitLevelExtended) {
        OrgUnitLevelDB orgUnitLevel = new OrgUnitLevelDB();
        orgUnitLevel.setUid(sdkOrganisationUnitLevelExtended.getUid());
        orgUnitLevel.setName(sdkOrganisationUnitLevelExtended.getDisplayName());
        orgUnitLevel.save();

        orgUnitLevelMap.put(sdkOrganisationUnitLevelExtended.buildKey(), orgUnitLevel);
    }

    /**
     * Turns a sdk organisationUnit into an app OrgUnit
     */
    @Override
    public void visit(OrganisationUnitExtended organisationUnit) {
        //Create and save OrgUnitLevel
        OrgUnitLevelDB appOrgUnitLevel = orgUnitLevelMap.get(
                OrganisationUnitLevelExtended.buildKey(organisationUnit.getLevel()));
        //create the orgUnit
        OrgUnitDB appOrgUnit =
                new OrgUnitDB();
        //Set name
        if (organisationUnit.getLabel() == null) {
            appOrgUnit.setName(organisationUnit.getName());
        } else {
            appOrgUnit.setName(organisationUnit.getLabel());
        }
        //Set uid
        appOrgUnit.setUid(organisationUnit.getId());
        //Set orgUnitLevel
        appOrgUnit.setOrgUnitLevel(appOrgUnitLevel);
        //Since there is no guaranteed order in orgunits parent unit might not be yet converted
        // or even pulled at all
        //Thus building hierarchy must be done in a second step

        appOrgUnit.save();
        //Annotate built orgunit
        orgUnitDict.put(organisationUnit.getId(), appOrgUnit);

        //Associate programs
        organisationUnit.setAppOrgUnit(appOrgUnit);
        buildOrgUnitProgramRelation(organisationUnit);
    }

    /**
     * Turns a sdk ProgramStageSection into a Tab
     */
    @Override
    public void visit(ProgramStageSectionExtended programStageSection) {
        //Build Tab
        String programUID = (ProgramStageExtended.getProgramStage(
                programStageSection.getProgramStage().getUid())).getProgram().getUId();
        ProgramDB program = programMapObjects.get(
                programUID);
        TabDB appTab = new TabDB();
        appTab.setProgram(program);
        appTab.setName(programStageSection.getDisplayName());
        appTab.setType(Constants.TAB_AUTOMATIC);
        appTab.setOrder_pos(programStageSection.getSortOrder());
        appTab.save();
        //Annotate build tab
        programStageSectionTabDict.put(actualProgram.getUid(), programStageSection.getUid(),
                appTab);

        programTabDict.put(actualProgram.getUid(), programStageSection.getUid(), appTab);
    }


    /**
     * Turns a sdk OptionSet into an Answer
     */
    @Override
    public void visit(OptionSetExtended sdkOptionSet) {
        //Build answer
        AnswerDB appAnswer = new AnswerDB();
        appAnswer.setName(sdkOptionSet.getName());
        appAnswer.save();

        //Annotate built answer
        answerMap.put(sdkOptionSet.getUid(), appAnswer);

        //Visit children
        for (OptionExtended option : sdkOptionSet.getOptions()) {
            new OptionExtended(option).accept(this);
        }
    }

    /**
     * Turns a sdk Option into an Option
     */
    @Override
    public void visit(OptionExtended sdkOption) {
        //Build option
        AnswerDB appAnswer = answerMap.get(sdkOption.getOptionSet());
        OptionDB appOption =
                new OptionDB();
        appOption.setName(sdkOption.getName());
        appOption.setUid(sdkOption.getUid());
        appOption.setCode(sdkOption.getCode());
        appOption.setAnswer(appAnswer);
        appOption.setFactor(sdkOption.getFactor());
        appOption.save();
    }

    /**
     * Turns a sdk userAccount into a User
     */
    @Override
    public void visit(UserAccountExtended userAccount) {
        UserDB appUser = UserDB.getUserByUId(userAccount.getUid());
        if(appUser == null ) {
            appUser = new UserDB();
        }
        appUser.setUid(userAccount.getUId());
        appUser.setName(userAccount.getName());
        appUser.setLastUpdated(null);
        appUser.save();
    }


    /**
     * Turns a dataElement into a question or a compositeScore
     */
    @Override
    public void visit(DataElementExtended sdkDataElementExtended) {
        if (!DataElementExtended.isLoadedDataElementTypeCodes())
            DataElementExtended.reloadDataElementTypeCodes();

        if (sdkDataElementExtended.isCompositeScore()) {
            programCompositeScoreDict.put(actualProgram.getUid(),
                    sdkDataElementExtended.getDataElement().getUid(),
                    buildCompositeScore(sdkDataElementExtended));
        } else if (sdkDataElementExtended.isQuestion()) {
            programQuestionDict.put(actualProgram.getUid(),
                    sdkDataElementExtended.getDataElement().getUid(),
                    buildQuestion(sdkDataElementExtended));
            //Question type is annotated in 'answer' from an attribute of the question
        } else if (sdkDataElementExtended.isControlDataElement()) {
            if (!controlDataElementMapObjects.containsKey(
                    sdkDataElementExtended.getDataElement().getUid())) {
                controlDataElementMapObjects.put(sdkDataElementExtended.getDataElement().getUid(),
                        buildControlDataElement(sdkDataElementExtended));
            }
        } else {
            Log.d(TAG, "Error" + sdkDataElementExtended.getDataElement().toString());
            return;
        }
    }

    /**
     * Turns an event into a sent survey
     */
    @Override
    public void visit(EventExtended event) {
        OrgUnitDB orgUnit = orgUnitDict.get(event.getOrganisationUnitId());
        ProgramDB program = programMapObjects.get(
                ProgramStageExtended.getProgramStage(
                        event.getProgramStageId()).getProgram().getUId());
        SurveyDB survey = new SurveyDB();
        //Any survey that comes from the pull has been sent
        survey.setStatus(Constants.SURVEY_SENT);
        //Completiondate == Event date
        survey.setCompletionDate(event.getEventDate());

        //Set dates( to prevent a null value, all take the getEventDate date before datavalue
        // visitor)
        survey.setCreationDate(event.getEventDate());
        survey.setUploadDate(event.getEventDate());
        //Scheduled date == Due date
        survey.setScheduledDate(event.getDueDate());
        //Set fks
        survey.setOrgUnit(orgUnit);
        survey.setEventUid(event.getUid());
        survey.setProgram(program);
        survey.save();

        //Annotate object in map
        //EventToSurveyBuilder eventToSurveyBuilder=new EventToSurveyBuilder(survey);
        programSurveyDict.put(actualProgram.getUid(), event.getUid(), survey);

        //Visit its values
        for (DataValueExtended dataValueExtended : event.getDataValues()) {
            dataValueExtended.setProgramUid(event.getProgramId());
            dataValueExtended.accept(this);
        }
        //Once all the values are processed save common data across created surveys
        //eventToSurveyBuilder.saveCommonData();
    }

    @Override
    public void visit(DataValueExtended dataValue) {
        SurveyDB survey = programSurveyDict.get(actualProgram.getUid(), dataValue.getEvent());

        //General common data (mainscore, createdby, createdon, uploadedon..)


        //Data value is a value from compositeScore
        if (programCompositeScoreDict.containsKey(actualProgram.getUid(),
                dataValue.getDataElement())) {
            //CHeck if it is a root score -> score
            CompositeScoreDB compositeScore = programCompositeScoreDict.get(actualProgram.getUid(),
                    dataValue.getDataElement());
            //Only root scores are important
            if (!CompositeScoreBuilder.isRootScore(compositeScore)) {
                return;
            }

            ScoreDB score = new ScoreDB();
            score.setScore(Float.parseFloat(dataValue.getValue()));
            score.setUid(dataValue.getDataElement());
            score.setSurvey(survey);
            score.save();
            Log.i(TAG, String.format("Event %s with mainScore %s", survey.getEventUid(),
                    dataValue.getValue()));
            return;
        }


        //-> createdOn
        if (dataValue.getDataElement().equals(ServerMetadataDB.findControlDataElementUid(
                PreferencesState.getInstance().getContext().getString(R.string.created_on_code)))) {
            survey.setCreationDate(EventExtended.parseLongDate(dataValue.getValue()));
            survey.save();
            Log.i(TAG, String.format("Event %s created on %s", survey.getEventUid(),
                    dataValue.getValue()));
            return;
        }

        //-> completionOn
        if (dataValue.getDataElement().equals(ServerMetadataDB.findControlDataElementUid(
                PreferencesState.getInstance().getContext().getString(R.string.completed_on_code)))) {
            survey.setCompletionDate(EventExtended.parseLongDate(dataValue.getValue()));
            survey.save();
            Log.i(TAG, String.format("Event %s created on %s", survey.getEventUid(),
                    dataValue.getValue()));
            return;
        }

        //-> uploadedOn
        if (dataValue.getDataElement().equals(ServerMetadataDB.findControlDataElementUid(
                PreferencesState.getInstance().getContext().getString(
                        R.string.upload_date_code)))) {
            survey.setUploadDate(EventExtended.parseLongDate(dataValue.getValue()));
            Log.i(TAG, String.format("Event %s uploaded on %s", survey.getEventUid(), dataValue
                    .getValue()));
            return;
        }

        //-> uploadedBy (updatedBy is ignored)
        if (dataValue.getDataElement().equals(ServerMetadataDB.findControlDataElementUid(
                PreferencesState.getInstance().getContext().getString(
                        R.string.uploaded_by_code)))) {
            UserDB user = UserDB.getUser(dataValue.getValue());
            if (user == null) {
                user = new UserDB(dataValue.getValue(), dataValue.getValue());
                user.save();
            }
            survey.setUser(user);
            survey.save();
            //Annotate object in map
            programSurveyDict.put(actualProgram.getUid(), dataValue.getEvent(), survey);
            return;
            //eventToSurveyBuilder.setUploadedBy(dataValue);
            //Log.i(TAG,String.format("Event %s created by %s",eventToSurveyBuilder.getEventUid()
            // ,dataValue.getValue()));
            //return;
        }

        ValueDB value = new ValueDB();
        //Datavalue is a value from a question
        OptionDB option = null;
        if (programQuestionDict.containsKey(actualProgram.getUid(), dataValue.getDataElement())) {
            QuestionDB question = programQuestionDict.get(actualProgram.getUid(),
                    dataValue.getDataElement());
            try {
                value.setQuestion(question);
                option = dataValue.findOptionByQuestion(question);
                value.setOption(option);
            } catch (ClassCastException e) {
                Log.d(TAG, "Exception with controlDataelement in DataValue converting");
            }
        }

        value.setSurvey(survey);
        //No option -> text question (straight value)
        if (option == null) {
            value.setValue(dataValue.getValue());
        } else {
            //Option -> extract value from code
            value.setValue(option.getName());
        }
        value.setUploadDate(new Date());
        value.save();
    }


    /**
     * Turns a dataElement into a question
     */
    private QuestionDB buildQuestion(DataElementExtended dataElement) {
        QuestionDB appQuestion = new QuestionDB();
        appQuestion.setDe_name(dataElement.getName());
        appQuestion.setUid(dataElement.getUid());
        appQuestion.setShort_name(dataElement.getShortName());
        appQuestion.setForm_name(dataElement.getFormName());
        appQuestion.setFeedback(dataElement.getDescription());
        appQuestion.setCode(dataElement.getCode());
        appQuestion.setOrder_pos(dataElement.findOrder());
        appQuestion.setNumerator_w(dataElement.findNumerator());
        appQuestion.setDenominator_w(dataElement.findDenominator());
        appQuestion.setRow(dataElement.findRow());
        appQuestion.setColumn(dataElement.findColumn());
        appQuestion.setOutput(compositeScoreBuilder.findAnswerOutput(dataElement));

        //Label does not have an optionset
        if (dataElement.getOptionSet() != null) {
            appQuestion.setAnswer(answerMap.get(dataElement.getOptionSet()));
        } else {
            //A question with NO optionSet is a Label Question
            Log.d(TAG, String.format("Question (%s) is a LABEL", dataElement.getUid()));
            appQuestion.setAnswer(buildAnswerLabel());
        }

        ProgramStageDataElementExtended programStageDataElement =
                new ProgramStageDataElementExtended(
                        DataElementExtended.findProgramStageDataElementByDataElementExtended(
                                dataElement));

        appQuestion.setCompulsory(programStageDataElement.getCompulsory());
        appQuestion.setHeader(questionBuilder.findOrSaveHeader(dataElement, programTabDict,
                actualProgram.getUid()));
        questionBuilder.registerParentChildRelations(dataElement);
        questionBuilder.attachMedia(dataElement, appQuestion);
        questions.add(appQuestion);
        questionBuilder.add(appQuestion, dataElement.getProgramUid());
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
     */
    public AnswerDB buildAnswerLabel() {

        //Build a sintetic Key (AnswerLABEL)
        final String key = AnswerDB.class + Constants.LABEL;
        //Look for sintetic LABEL (answer) already created
        AnswerDB answer = answerMap.get(key);

        //First time no Label answer has been created
        if (answer == null) {
            answer = new AnswerDB(Constants.LABEL);
            answer.save();
            answerMap.put(key, answer);
        }

        return answer;
    }

    /**
     * Turns a dataElement into a question
     */
    private CompositeScoreDB buildCompositeScore(DataElementExtended dataElement) {
        CompositeScoreDB compositeScore = new CompositeScoreDB();
        compositeScore.setUid(dataElement.getUid());
        compositeScore.setLabel(dataElement.getFormName());
        compositeScore.setHierarchical_code(
                compositeScoreBuilder.findHierarchicalCode(dataElement));
        compositeScore.setOrder_pos(dataElement.findOrder());
        //Parent score and Order can only be set once every score in saved
        compositeScore.save();

        compositeScoreBuilder.add(compositeScore, dataElement.getProgramUid());

        return compositeScore;
    }


    private ServerMetadataDB buildControlDataElement(DataElementExtended dataElement) {
        ServerMetadataDB controlDataElement = new ServerMetadataDB();
        controlDataElement.setUid(dataElement.getUid());
        controlDataElement.setCode(dataElement.getCode());
        controlDataElement.setName(dataElement.getDisplayName());
        controlDataElement.setValueType(dataElement.getValueType().name());

        //Parent score and Order can only be set once every score in saved
        controlDataElement.save();
        return controlDataElement;

    }

    /**
     * Due to permissions programs 'belongs' to a given orgunit and that relationship has a
     * productivity
     *
     * @param sdkOrganisationUnitExtended Extended sdk orgUnit (used to cache array with values)
     */
    public void buildOrgUnitProgramRelation(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        OrgUnitDB appOrgUnit = sdkOrganisationUnitExtended.getAppOrgUnit();
        Log.d(TAG, "buildOrgUnitProgramRelation " + appOrgUnit.getName());
        //Each assigned program
        for (ProgramExtended sdkProgramExtended : ProgramExtended.getExtendedList(
                SdkQueries.getProgramsForOrganisationUnit(appOrgUnit.getUid(),
                        PreferencesState.getInstance().getContext().getString(R.string.pull_program_code,
                                ProgramType.WITHOUT_REGISTRATION), ProgramType.WITHOUT_REGISTRATION))) {
                sdkProgramExtended.setAppProgram(
                        programMapObjects.get(sdkProgramExtended.getUid()));

                addOrgUnitProgramRelation(sdkOrganisationUnitExtended, sdkProgramExtended);
        }
    }

    /**
     * Updates the relationship between the given orgUnit and program according to their attribute
     * values
     */
    private void addOrgUnitProgramRelation(OrganisationUnitExtended sdkOrganisationUnitExtended,
            ProgramExtended sdkProgramExtended) {
        //Take app references
        OrgUnitDB appOrgUnit = sdkOrganisationUnitExtended.getAppOrgUnit();
        ProgramDB appProgram =
                sdkProgramExtended.getAppProgram();

        //Add relationship
        OrgUnitProgramRelationDB orgUnitProgramRelation = appProgram.addOrgUnit(appOrgUnit);

        //Add productivity to that relationship
        Integer productivityIndex = sdkProgramExtended.getProductivityPosition();
        Integer orgUnitProgramRelationProductivity = sdkOrganisationUnitExtended.getProductivity(
                productivityIndex);
        orgUnitProgramRelation.setProductivity(orgUnitProgramRelationProductivity);
        orgUnitProgramRelation.save();
    }

    @Override
    public void buildScores() {
        compositeScoreBuilder.buildScores();
    }

    /**
     * Builds the orgunit hierarchy whenever is possible
     */
    public boolean buildOrgUnitHierarchy(
            List<OrganisationUnitExtended> assignedOrganisationsUnits) {

        for (OrganisationUnitExtended organisationUnit : assignedOrganisationsUnits) {
            OrgUnitDB appOrgUnit = orgUnitDict.get(organisationUnit.getId());
            String parentUID = organisationUnit.getParent();
            //FIXME: review this algorithm
            if (parentUID == null) {
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
            if (parentUID == null) {
                Log.i(TAG, String.format("%s is a root orgUnit", appOrgUnit.getName()));
                continue;
            }

            //Find parent
            OrgUnitDB parentOrgUnit = orgUnitDict.get(parentUID);

            //Due to server permissions parent unit might not be loaded
            if (parentOrgUnit == null) {
                Log.w(TAG,
                        String.format("Cannot find parent orgunit for %s", appOrgUnit.getName()));
                continue;
            }

            appOrgUnit.setOrgUnit(parentOrgUnit.getId_org_unit());
            appOrgUnit.save();
        }
        return true;

    }

}

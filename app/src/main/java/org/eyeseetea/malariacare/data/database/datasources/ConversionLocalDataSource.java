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

package org.eyeseetea.malariacare.data.database.datasources;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.CompositeScoreBuilder;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ObservationExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models
        .OrganisationUnitExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models
        .OrganisationUnitLevelExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models
        .ProgramStageDataElementExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.data.database.model.AnswerDB;
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.model.HeaderDB;
import org.eyeseetea.malariacare.data.database.model.MatchDB;
import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.ObservationValueDB;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevelDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionOptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionRelationDB;
import org.eyeseetea.malariacare.data.database.model.ScoreDB;
import org.eyeseetea.malariacare.data.database.model.ServerMetadataDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.model.SurveyScheduleDB;
import org.eyeseetea.malariacare.data.database.model.TabDB;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.model.ValueDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.data.repositories.ServerMetadataRepository;
import org.eyeseetea.malariacare.domain.boundary.IPullController;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.exception.InvalidServerMetadataException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversionLocalDataSource {

    PullController.IPullControllerCallback callback;
    IServerMetadataRepository serverMetadataRepository;

    public ConversionLocalDataSource(PullController.IPullControllerCallback callback,
            IServerMetadataRepository serverMetadataRepository) {
        this.callback = callback;
        this.serverMetadataRepository = serverMetadataRepository;
    }

    private final String TAG = ".ConversionLocalData";

    public void validateCS() {
        if (!PullController.PULL_IS_ACTIVE) return;
        Log.d(TAG, "Validate Composite scores");
        callback.onStep(PullStep.VALIDATE_COMPOSITE_SCORES);
        List<CompositeScoreDB> compositeScores = CompositeScoreDB.list();
        for (CompositeScoreDB compositeScore : compositeScores) {
            if (!compositeScore.hasChildren() && (compositeScore.getQuestions() == null
                    || compositeScore.getQuestions().size() == 0)) {
                Log.d(TAG, "CompositeScoreDB without children and without questions will be removed: "
                        + compositeScore.toString());
                compositeScore.delete();
                continue;
            }
            if (compositeScore.getHierarchical_code() == null) {
                Log.d(TAG, "CompositeScoreDB without hierarchical code will be removed: "
                        + compositeScore.toString());
                compositeScore.delete();
                continue;
            }
            if (compositeScore.getComposite_score() == null
                    && !compositeScore.getHierarchical_code().equals(
                    CompositeScoreBuilder.ROOT_NODE_CODE)) {
                Log.d(TAG, "CompositeScoreDB not root and not parent should be fixed: "
                        + compositeScore.toString());
                continue;
            }
        }
    }

    /**
     * Launches visitor that turns SDK data into APP data
     */
    public void convertFromSDK() {
        if (!PullController.PULL_IS_ACTIVE) return;
        Log.d(TAG, "Converting SDK into APP data");

        //One shared converter to match parents within the hierarchy
        ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor(serverMetadataRepository);
        convertMetaData(converter);
        if (!PullController.PULL_IS_ACTIVE) return;

        convertDataValues(converter);

    }

    /**
     * Turns sdk metadata into app metadata
     */
    private void convertMetaData(ConvertFromSDKVisitor converter) {
        if (!PullController.PULL_IS_ACTIVE) return;
        //Convert Programs, Tabs
        callback.onStep(PullStep.PREPARING_PROGRAMS);
        System.out.printf("Converting programs and tabs...");
        List<String> assignedProgramsIDs = SdkQueries.getAssignedProgramUids(PreferencesState.getInstance().getContext().getString(
                R.string.pull_program_code));
        for (String assignedProgramID : assignedProgramsIDs) {
            ProgramExtended programExtended = new ProgramExtended(
                    SdkQueries.getProgram(assignedProgramID));
            programExtended.accept(converter);
        }

        //Convert Answers, Options
        if (!PullController.PULL_IS_ACTIVE) return;
        callback.onStep(PullStep.PREPARING_ANSWERS);
        List<OptionSetExtended> optionSets = OptionSetExtended.getExtendedList(
                SdkQueries.getOptionSets());
        System.out.printf("Converting answers and options...");
        for (OptionSetExtended optionSet : optionSets) {
            if (!PullController.PULL_IS_ACTIVE) return;
            optionSet.accept(converter);
        }
        //OrganisationUnits
        if (!PullController.PULL_IS_ACTIVE) return;
        if (!convertOrgUnits(converter)) return;

        if (!PullController.PULL_IS_ACTIVE) return;
        //User (from UserAccount)
        System.out.printf("Converting user...");
        UserAccountExtended userAccountExtended = new UserAccountExtended(
                SdkQueries.getUserAccount());
        userAccountExtended.accept(converter);

        if (!PullController.PULL_IS_ACTIVE) return;
        //Convert questions and compositeScores
        callback.onStep(PullStep.PREPARING_QUESTIONS);
        System.out.printf("Ordering questions and compositeScores...");

        int count;
        //Dataelements ordered by program.
        List<ProgramExtended> programs = ProgramExtended.getAllPrograms(PreferencesState.getInstance().getContext().getString(
                R.string.pull_program_code));
        Map<String, List<DataElementExtended>> programsDataelements = new HashMap<>();
        if (!PullController.PULL_IS_ACTIVE) return;
        for (ProgramExtended program : programs) {
            converter.actualProgram = program;
            Log.i(TAG, String.format("\t program '%s' ", program.getName()));
            List<DataElementExtended> dataElements = new ArrayList<>();
            String programUid = program.getUid();
            List<ProgramStageExtended> programStages = program.getProgramStages();
            for (ProgramStageExtended programStage : programStages) {
                Log.d(TAG, "programStage.getProgramStageDataElements size: "
                        + programStage.getProgramStageDataElements().size());
                Log.i(TAG, String.format("\t\t programStage '%s' ", program.getName()));
                List<ProgramStageDataElementExtended> programStageDataElements =
                        programStage.getProgramStageDataElements();
                count = programStage.getProgramStageDataElements().size();
                for (ProgramStageDataElementExtended programStageDataElement :
                        programStageDataElements) {
                    if (!PullController.PULL_IS_ACTIVE) return;

                    //The ProgramStageDataElement without Dataelement uid is not correctly
                    // configured.
                    if (programStageDataElement.getDataelement() == null
                            || programStageDataElement.getDataelement().equals("")) {
                        Log.d(TAG, "Ignoring ProgramStageDataElements without dataelement...");
                        continue;
                    }

                    //Note: the sdk method getDataElement returns the dataElement object, and
                    // getDataelement returns the dataelement uid.
                    DataElementExtended dataElement = programStageDataElement.getDataElement();
                    if (dataElement != null && dataElement.getUid() != null) {
                        dataElements.add(dataElement);
                    } else {
                        DataElementExtended.existsDataElementByUid(
                                programStageDataElement.getDataelement());
                        dataElement = new DataElementExtended(SdkQueries.getDataElement(
                                programStageDataElement.getDataelement()));

                        if (dataElement != null) {
                            dataElements.add(dataElement);
                        } else {
                            //FIXME This query returns random null for some dataelements but
                            // those dataElements are stored in the database. It's a possible bug
                            // of dbflow and DataElement pojo conversion.
                            Log.d(TAG, "Null dataelement on first query "
                                    + programStageDataElement.getProgramStage());
                            int times = 0;
                            while (dataElement == null) {
                                times++;
                                Log.d(TAG, "running : " + times);
                                try {
                                    Thread.sleep(100);
                                    dataElement = new DataElementExtended(SdkQueries.getDataElement(
                                            programStageDataElement.getDataelement()));
                                } catch (InterruptedException e) {//throw new RuntimeException
                                    // ("Null query");
                                    e.printStackTrace();
                                }
                            }
                            Log.d(TAG, "needed : " + times);
                            dataElements.add(dataElement);
                        }
                    }
                }

                if (count != dataElements.size()) {
                    Log.d(TAG, "The programStageDataElements size (" + count
                            + ") is different than the saved dataelements size ("
                            + dataElements.size() + ")");
                }
            }
            Log.i(TAG, String.format("\t program '%s' DONE ", program.getName()));


            if (!PullController.PULL_IS_ACTIVE) return;
            Collections.sort(dataElements, new Comparator<DataElementExtended>() {
                public int compare(DataElementExtended dataElementExtended1,
                        DataElementExtended dataElementExtended2) {
                    Integer dataelementOrder1 = -1, dataelementOrder2 = -1;
                    try {
                        dataelementOrder1 = dataElementExtended1.findOrder();
                    } catch (Exception e) {
                        e.printStackTrace();
                        dataelementOrder1 = null;
                    }
                    try {
                        dataelementOrder2 = dataElementExtended2.findOrder();
                    } catch (Exception e) {
                        e.printStackTrace();
                        dataelementOrder2 = null;
                    }
                    if (dataelementOrder1 == dataelementOrder2) {
                        return 0;
                    } else if (dataelementOrder1 == null) {
                        return 1;
                    } else if (dataelementOrder2 == null) {
                        return -1;
                    }
                    return dataelementOrder1.compareTo(dataelementOrder2);
                }
            });
            programsDataelements.put(programUid, dataElements);
        }

        if (!PullController.PULL_IS_ACTIVE) return;
        System.out.printf("Building questions,compositescores,headers...");
        int i = 0;
        for (ProgramExtended program : programs) {
            converter.actualProgram = program;
            String programUid = program.getUid();
            List<DataElementExtended> sortDataElements = programsDataelements.get(programUid);
            for (DataElementExtended dataElement : sortDataElements) {
                /* // TODO: 23/01/2017
                if (++i % 50 == 0) {
                    postProgress(
                            context.getString(R.string.progress_pull_questions) + String.format(
                                    " %s", i));
                }
                */
                if (!PullController.PULL_IS_ACTIVE) return;
                //Log.i(TAG,"Converting DE "+dataElementExtended.getDataElement().getUid());
                dataElement.setProgramUid(programUid);
                dataElement.accept(converter);
            }
        }

        //Saves questions and media in batch mode
        converter.saveBatch();

        if (!PullController.PULL_IS_ACTIVE) return;
        System.out.printf("Building relationships...");
        callback.onStep(PullStep.PREPARING_RELATIONSHIPS);
        for (ProgramExtended program : programs) {
            converter.actualProgram = program;
            String programUid = program.getUid();
            List<DataElementExtended> sortDataElements = programsDataelements.get(programUid);
            programsDataelements.put(programUid, sortDataElements);
            for (DataElementExtended dataElement : sortDataElements) {
                if (!PullController.PULL_IS_ACTIVE) return;
                dataElement.setProgramUid(programUid);
                converter.buildRelations(dataElement);
            }
        }

        if (!PullController.PULL_IS_ACTIVE) return;
        //Fill order and parent scores
        System.out.printf("Building compositeScore relationships...");
        converter.buildScores();
        System.out.printf("MetaData successfully converted...");
    }

    /**
     * Turns sdk organisationUnit and levels into app info
     */
    private boolean convertOrgUnits(ConvertFromSDKVisitor converter) {
        if (!PullController.PULL_IS_ACTIVE) return false;
        callback.onStep(PullStep.PREPARING_ORGANISATION_UNITS);
        System.out.printf("Converting organisationUnitLevels...");
        List<OrganisationUnitLevelExtended> organisationUnitLevels =
                OrganisationUnitLevelExtended.getExtendedList(
                        SdkQueries.getOrganisationUnitLevels());
        for (OrganisationUnitLevelExtended organisationUnitLevel : organisationUnitLevels) {
            if (!PullController.PULL_IS_ACTIVE) return false;
            organisationUnitLevel.accept(converter);
        }

        System.out.printf("Converting organisationUnits...");
        List<OrganisationUnitExtended> assignedOrganisationsUnits =
                OrganisationUnitExtended.getExtendedList(SdkQueries.getAssignedOrganisationUnits());
        for (OrganisationUnitExtended assignedOrganisationsUnit : assignedOrganisationsUnits) {
            if (!PullController.PULL_IS_ACTIVE) return false;
            assignedOrganisationsUnit.accept(converter);
        }

        System.out.printf("Building orgunit hierarchy...");
        return converter.buildOrgUnitHierarchy(assignedOrganisationsUnits);
    }

    /**
     * Turns events and datavalues into
     */
    private void convertDataValues(ConvertFromSDKVisitor converter) {
        if (!PullController.PULL_IS_ACTIVE) return;
        callback.onStep(PullStep.PREPARING_SURVEYS);
        //XXX This is the right place to apply additional filters to data conversion (only
        // predefined orgunit for instance)
        //For each unit
        for (OrganisationUnitExtended organisationUnit : OrganisationUnitExtended.getExtendedList(
                SdkQueries.getAssignedOrganisationUnits())) {
            //Each assigned program
            for (ProgramExtended program : ProgramExtended.getExtendedList(
                    SdkQueries.getProgramsForOrganisationUnit(organisationUnit.getId(),
                            PreferencesState.getInstance().getContext().getString(R.string.pull_program_code),
                                    ProgramType.WITHOUT_REGISTRATION))) {
                converter.actualProgram = program;
                List<EventExtended> events = EventExtended.getExtendedList(
                        SdkQueries.getEvents(organisationUnit.getId(), program.getUid()));
                System.out.printf("Converting surveys and values for orgUnit: %s | program: %s",
                        organisationUnit.getLabel(), program.getDisplayName());
                for (EventExtended event : events) {
                    if (!PullController.PULL_IS_ACTIVE) return;
                    if (event.getEventDate() == null
                            || event.getEventDate().equals("")) {
                        Log.d(TAG, "Alert, ignoring event without eventdate, event uid:"+event.getUid());
                        continue;
                    }
                    event.accept(converter);

                    ServerMetadata serverMetadata = null;

                    try {
                        serverMetadata = serverMetadataRepository.getServerMetadata();
                    } catch (InvalidServerMetadataException e) {
                        e.printStackTrace();
                    }

                    if (event.hasObservations(serverMetadata)){
                        ObservationExtended observationExtended =
                                new ObservationExtended(event.getEvent());

                        observationExtended.accept(converter);
                    }
                }
            }
        }

        //Plan surveys for the future
        SurveyPlanner.getInstance().buildNext();
    }

    /**
     * Deletes all data from the app database
     */

    public static void wipeDataBase() {
        Delete.tables(
                ValueDB.class,
                ScoreDB.class,
                SurveyDB.class,
                SurveyScheduleDB.class,
                OrgUnitDB.class,
                OrgUnitLevelDB.class,
                OrgUnitProgramRelationDB.class,
                UserDB.class,
                QuestionOptionDB.class,
                MatchDB.class,
                QuestionRelationDB.class,
                QuestionDB.class,
                CompositeScoreDB.class,
                OptionDB.class,
                AnswerDB.class,
                HeaderDB.class,
                TabDB.class,
                ProgramDB.class,
                ServerMetadataDB.class,
                MediaDB.class,
                ObservationDB.class,
                ObservationValueDB.class
        );
    }
}

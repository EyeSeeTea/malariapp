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

import android.support.annotation.NonNull;
import android.util.Log;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.IPullSourceCallback;
import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.DataElementExtended;
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
import org.eyeseetea.malariacare.data.database.model.CompositeScoreDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.sdk.PullDhisSDKDataSource;
import org.eyeseetea.malariacare.data.remote.sdk.SdkQueries;
import org.eyeseetea.malariacare.domain.boundary.IPullMetadataController;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PullMetadataController implements IPullMetadataController {

    private final String TAG = ".PullMetadataController";
    PullDhisSDKDataSource pullRemoteDataSource;
    IPullMetadataController.Callback callback;

    ConvertFromSDKVisitor converter;

    public PullMetadataController() {
        converter = new ConvertFromSDKVisitor();
        pullRemoteDataSource = new PullDhisSDKDataSource();
    }

    @Override
    public void pullMetadata(final IPullMetadataController.Callback callback) {
        //TODO: jsanchez when we decoupled from dhis metadata review this
        //should we remove login user in pull process?
        //In any case if we remove the user table in wipeDatabase then remove also session user
        AppDatabase.wipeDatabase();
        Session.setUser(null);

        this.callback = callback;


        pullRemoteDataSource.wipeDataBase();

        callback.onStep(PullStep.PROGRAMS);

        pullRemoteDataSource.pullMetadata(new IPullSourceCallback() {

            @Override
            public void onComplete() {
                callback.onStep(PullStep.EVENTS);

                convertMetaData();

                Log.d(TAG, "PULL process...OK");
                callback.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }

        });
    }

    /**
     * Turns sdk metadata into app metadata
     */
    public void convertMetaData() {
        //User (from UserAccount)
        convertUser();

        //Convert Programs, Tabs
        convertOptions();

        callback.onStep(PullStep.PREPARING_PROGRAMS);
        System.out.printf("Converting programs and tabs...");

        List<ProgramExtended> assignedProgramExtendeds = ProgramExtended.getAllPrograms(PreferencesState.getInstance().getContext().getString(
                R.string.pull_program_code));

        for (ProgramExtended program : assignedProgramExtendeds) {
            program.accept(converter);
            Log.i(TAG, String.format("\t\t program '%s' ", program.getName()));
        }

        //OrganisationUnits
        convertOrgUnits(converter);

        //Convert questions and compositeScores
        callback.onStep(PullStep.PREPARING_QUESTIONS);
        System.out.printf("Ordering questions and compositeScores...");
        Map<String, List<DataElementExtended>> programsDataelements = new HashMap<>();

        for (ProgramExtended program : assignedProgramExtendeds) {
            converter.actualProgram = program;
            Log.i(TAG, String.format("\t program '%s' ", program.getName()));

            String programUid = program.getUid();

            List<DataElementExtended> dataElements = getDataElements(program);

            Log.i(TAG, String.format("\t program '%s' DONE ", program.getName()));

            orderDataElementsByProgramUsingOrderField(dataElements);

            programsDataelements.put(programUid, dataElements);
        }

        System.out.printf("Building questions,compositescores,headers...");
        for (ProgramExtended program : assignedProgramExtendeds) {
            convertDataElementsOrderedByProgramAndOrderField(programsDataelements, program);
        }

        //Saves questions and media in batch mode
        converter.saveQuestionsInBatch();

        System.out.printf("Building relationships...");
        callback.onStep(PullStep.PREPARING_RELATIONSHIPS);

        //I think this method is executed after save the questions to has a question ID
        buildAndSaveQuestionRelations(assignedProgramExtendeds, programsDataelements);

        //check if the CS are valid cs and remove unused cs
        validateCompositeScore();

        //Fill order and parent scores
        System.out.printf("Building compositeScore relationships...");
        converter.buildScores();
        System.out.printf("MetaData successfully converted...");
    }

    private void buildAndSaveQuestionRelations(List<ProgramExtended> assignedProgramExtendeds, Map<String, List<DataElementExtended>> programsDataelements) {
        for (ProgramExtended program : assignedProgramExtendeds) {
            converter.actualProgram = program;
            String programUid = program.getUid();
            List<DataElementExtended> sortDataElements = programsDataelements.get(programUid);
            programsDataelements.put(programUid, sortDataElements);
            for (DataElementExtended dataElement : sortDataElements) {
                dataElement.setProgramUid(programUid);
                converter.buildRelations(dataElement);
            }
        }
    }

    private void convertDataElementsOrderedByProgramAndOrderField(Map<String, List<DataElementExtended>> programsDataelements, ProgramExtended program) {
        converter.actualProgram = program;
        String programUid = program.getUid();
        List<DataElementExtended> sortDataElements = programsDataelements.get(programUid);
        for (DataElementExtended dataElement : sortDataElements) {
            dataElement.setProgramUid(programUid);
            dataElement.accept(converter);
        }
    }

    private void orderDataElementsByProgramUsingOrderField(List<DataElementExtended> dataElements) {
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
    }

    private List<DataElementExtended> getDataElements(ProgramExtended program) {
        List<DataElementExtended> dataElements = new ArrayList<>();
        for (ProgramStageExtended programStage : program.getProgramStages()) {
            for (ProgramStageDataElementExtended programStageDataElement :
                    programStage.getProgramStageDataElements()) {
                //The ProgramStageDataElement without Dataelement uid is not correctly
                // configured.
                if (hasProgramStageDataElementAConfigurationError(programStageDataElement)) continue;

                //Note: the sdk method getDataElement returns the dataElement object, and
                // getDataelement returns the dataelement uid.
                DataElementExtended dataElement = programStageDataElement.getDataElement();
                if (dataElement != null && dataElement.getUid() != null) {
                    dataElements.add(dataElement);
                } else {
                    getDataElementUIDFromDBFlowDatabaseIfIsNull(dataElements, programStageDataElement);
                }
            }
        }
        return dataElements;
    }

    private boolean hasProgramStageDataElementAConfigurationError(ProgramStageDataElementExtended programStageDataElement) {
        if (programStageDataElement.getDataelement() == null
                || programStageDataElement.getDataelement().equals("")) {
            Log.d(TAG, "Ignoring ProgramStageDataElements without dataelement...");
            return true;
        }
        return false;
    }

    private void getDataElementUIDFromDBFlowDatabaseIfIsNull(List<DataElementExtended> dataElements, ProgramStageDataElementExtended programStageDataElement) {
        DataElementExtended dataElement;
        DataElementExtended.existsDataElementByUid(
                programStageDataElement.getDataelement());
        dataElement = new DataElementExtended(SdkQueries.getDataElement(
                programStageDataElement.getDataelement()));

        if (dataElement != null) {
            dataElements.add(dataElement);
        } else {
            getDataElementUidValueInDBFlowStrangeBug(dataElements, programStageDataElement, dataElement);

        }
    }

    //Sometimes the dataelement was empty in a normal use of the app. This doesn't happen debugging or applying this solution
    private void getDataElementUidValueInDBFlowStrangeBug(List<DataElementExtended> dataElements, ProgramStageDataElementExtended programStageDataElement, DataElementExtended dataElement) {
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

    private void convertUser() {
        System.out.printf("Converting user...");
        UserAccountExtended userAccountExtended = new UserAccountExtended(
                SdkQueries.getUserAccount());
        userAccountExtended.accept(converter);
    }

    private void convertOptions() {
        //Convert Answers, Options
        callback.onStep(PullStep.PREPARING_ANSWERS);
        List<OptionSetExtended> optionSets = OptionSetExtended.getExtendedList(
                SdkQueries.getOptionSets());
        System.out.printf("Converting answers and options...");
        for (OptionSetExtended optionSet : optionSets) {
            optionSet.accept(converter);
        }
    }

    /**
     * Turns sdk organisationUnit and levels into app info
     */
    private void convertOrgUnits(ConvertFromSDKVisitor converter) {
        callback.onStep(PullStep.PREPARING_ORGANISATION_UNITS);
        System.out.printf("Converting organisationUnitLevels...");
        convertOrganisationUnitLevels(converter);

        List<OrganisationUnitExtended> assignedOrganisationsUnits = convertOrganisationUnits(converter);

        System.out.printf("Building orgunit hierarchy...");
        converter.assignOrgUnitParents(assignedOrganisationsUnits);
    }

    private void convertOrganisationUnitLevels(ConvertFromSDKVisitor converter) {
        List<OrganisationUnitLevelExtended> organisationUnitLevels =
                OrganisationUnitLevelExtended.getExtendedList(
                        SdkQueries.getOrganisationUnitLevels());
        for (OrganisationUnitLevelExtended organisationUnitLevel : organisationUnitLevels) {
            organisationUnitLevel.accept(converter);
        }
    }

    @NonNull
    private List<OrganisationUnitExtended> convertOrganisationUnits(ConvertFromSDKVisitor converter) {
        System.out.printf("Converting organisationUnits...");
        List<OrganisationUnitExtended> assignedOrganisationsUnits =
                OrganisationUnitExtended.getExtendedList(SdkQueries.getAssignedOrganisationUnits());
        for (OrganisationUnitExtended assignedOrganisationsUnit : assignedOrganisationsUnits) {
            assignedOrganisationsUnit.accept(converter);
        }
        return assignedOrganisationsUnits;
    }

    //this method exists to identify server metadata configuration errors and remove unused composite scores
    private void validateCompositeScore() {
        Log.d(TAG, "Validate Composite scores");
        callback.onStep(PullStep.VALIDATE_COMPOSITE_SCORES);
        List<CompositeScoreDB> compositeScores = CompositeScoreDB.list();
        for (CompositeScoreDB compositeScore : compositeScores) {
            if (!compositeScore.hasChildren() && (compositeScore.getQuestions() == null
                    || compositeScore.getQuestions().size() == 0)) {
                Log.d(TAG,
                        "CompositeScoreDB without children and without questions will be removed: "
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
}

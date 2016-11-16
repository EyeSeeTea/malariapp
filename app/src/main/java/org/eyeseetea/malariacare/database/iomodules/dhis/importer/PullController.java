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

import android.content.Context;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitLevelExtended;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models
        .ProgramStageDataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.sdk.SdkController;
import org.eyeseetea.malariacare.sdk.SdkPullController;
import org.eyeseetea.malariacare.sdk.SdkQueries;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A static controller that orchestrate the pull process
 * Created by arrizabalaga on 4/11/15.
 */
public class PullController {
    private final String TAG = ".PullController";
    public static final int NUMBER_OF_MONTHS=6;


    private static PullController instance;

    private static Object job;
    /**
     * Context required to i18n error messages while pulling
     */
    private Context context;

    /**
     * Constructs and register this pull controller to the event bus
     */
    PullController() {
    }

    private void register() {
        SdkController.register(PreferencesState.getInstance().getContext());
    }

    /**
     * Unregister pull controller from bus events
     */
    public void unregister() {
        SdkController.unregister(PreferencesState.getInstance().getContext());
    }

    /**
     * Singleton constructor
     *
     * @return
     */
    public static PullController getInstance() {
        if (instance == null) {
            instance = new PullController();
        }
        return instance;
    }

    /**
     * Launches the pull process:
     * - Loads metadata from dhis2 server
     * - Wipes app database
     * - Turns SDK into APP data
     *
     * @param ctx
     */
    public void pull(Context ctx) {
        Log.d(TAG, "Starting PULL process...");
        context = ctx;
        try {

            //Register for event bus
            register();
            //Enabling resources to pull
            enableMetaDataFlags();
            //Delete previous metadata
            SdkPullController.setMaxEvents(PreferencesState.getInstance().getMaxEvents());
            Calendar month = Calendar.getInstance();
            month.add(Calendar.MONTH, -NUMBER_OF_MONTHS);
            SdkPullController.setStartDate(EventExtended.format(month.getTime(),EventExtended.AMERICAN_DATE_FORMAT));
            SdkPullController.setFullOrganisationUnitHierarchy(AppSettingsBuilder.isFullHierarchy());
            SdkPullController.clearMetaDataLoadedFlags();
            SdkPullController.wipe();
            PopulateDB.wipeSDKData();
            PopulateDB.wipeDatabase();
            //Pull new metadata
            postProgress(context.getString(R.string.progress_pull_downloading));
            try {
                if(AppSettingsBuilder.isDownloadOnlyLastEvents()){
                    SdkPullController.loadLastData();
                }
                else{
                    SdkPullController.loadData();
                }
            } catch (Exception ex) {
                Log.e(TAG, "pullS: " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            Log.e(TAG, "pull: " + ex.getLocalizedMessage());
            unregister();
            postException(ex);
        }
    }

    /**
     * Enables loading all metadata
     */
    private void enableMetaDataFlags() {
        SdkPullController.enableMetaDataFlags(PreferencesState.getInstance().getContext());
    }

    private void onPullException(Exception ex) {
        ProgressActivity.PULL_ERROR=true;
        Log.e(TAG, "onLoadMetadataFinished: " + ex.getLocalizedMessage());
        ex.printStackTrace();
        postException(ex);
    }

    private void onPullFinish() {
        postFinish();
        unregister();
    }

    private void onPullError(String message) {
        ProgressActivity.cancellPull(message, message);
        postException(new Exception(context.getString(R.string.dialog_pull_error)));
        ProgressActivity.cancellPull(context.getString(R.string.dialog_pull_error),message);
        postException(new Exception(context.getString(R.string.dialog_pull_error)));
    }

    public void startConversion() {
        //Ok
        wipeDatabase();

        if(!mandatoryMetadataTablesNotEmpty())
            ProgressActivity.cancellPull("Error", "Error downloading metadata");

        convertFromSDK();

        validateCS();
        if (ProgressActivity.PULL_IS_ACTIVE) {
            Log.d(TAG, "PULL process...OK");
        }
    }

    private void validateCS() {
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        Log.d(TAG, "Validate Composite scores");
        postProgress(context.getString(R.string.progress_pull_validating_composite_scores));
        List<CompositeScore> compositeScores=CompositeScore.list();
        for(CompositeScore compositeScore:compositeScores){
            if(!compositeScore.hasChildren() && (compositeScore.getQuestions()==null || compositeScore.getQuestions().size()==0)){
                Log.d(TAG, "CompositeScore without children and without questions will be removed: "+compositeScore.toString());
                compositeScore.delete();
                continue;
            }
            if(compositeScore.getHierarchical_code()==null){
                Log.d(TAG, "CompositeScore without hierarchical code will be removed: "+compositeScore.toString());
                compositeScore.delete();
                continue;
            }
            if(compositeScore.getComposite_score()==null && !compositeScore.getHierarchical_code().equals(CompositeScoreBuilder.ROOT_NODE_CODE)){
                Log.d(TAG, "CompositeScore not root and not parent should be fixed: "+compositeScore.toString());
                continue;
            }
        }
    }

    private boolean mandatoryMetadataTablesNotEmpty(){

        int elementsInTable = 0;
        for(Class table: SdkController.MANDATORY_METADATA_TABLES) {
            elementsInTable = (int) new SQLite().selectCountOf()
                    .from(table).count();
            if (elementsInTable == 0) {
                Log.d(TAG, "Error empty table: " + table.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * Erase data from app database
     */
    public void wipeDatabase() {
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        Log.d(TAG, "Deleting app database...");
        PopulateDB.wipeDatabase();
    }

    /**
     * Launches visitor that turns SDK data into APP data
     */
    private void convertFromSDK() {
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        Log.d(TAG, "Converting SDK into APP data");

        //One shared converter to match parents within the hierarchy
        ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor();
        convertMetaData(converter);
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        convertDataValues(converter);

    }

    /**
     * Turns sdk metadata into app metadata
     *
     * @param converter
     */
    private void convertMetaData(ConvertFromSDKVisitor converter) {
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //Convert Programs, Tabs
        postProgress(context.getString(R.string.progress_pull_preparing_program));
        Log.i(TAG, "Converting programs and tabs...");
        List<String> assignedProgramsIDs = SdkQueries.getAssignedPrograms();
        for (String assignedProgramID : assignedProgramsIDs) {
            ProgramExtended programExtended = new ProgramExtended(SdkQueries.getProgram(assignedProgramID));
            programExtended.accept(converter);
        }

        //Convert Answers, Options
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        postProgress(context.getString(R.string.progress_pull_preparing_answers));
        List<OptionSetExtended> optionSets = OptionSetExtended.getExtendedList(SdkQueries.getOptionSets());
        Log.i(TAG, "Converting answers and options...");
        for (OptionSetExtended optionSet : optionSets) {
            if (!ProgressActivity.PULL_IS_ACTIVE) return;
            optionSet.accept(converter);
        }
        //OrganisationUnits
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        if (!convertOrgUnits(converter)) return;

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //User (from UserAccount)
        Log.i(TAG, "Converting user...");
        UserAccountExtended userAccountExtended = new UserAccountExtended(SdkQueries.getUserAccount());
        userAccountExtended.accept(converter);

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //Convert questions and compositeScores
        postProgress(context.getString(R.string.progress_pull_questions));
        Log.i(TAG, "Ordering questions and compositeScores...");

        int count;
        //Dataelements ordered by program.
        List<ProgramExtended> programs = ProgramExtended.getAllPrograms();
        Map<String, List<DataElementExtended>> programsDataelements = new HashMap<>();
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        for (ProgramExtended program : programs) {
            converter.actualProgram=program;
            Log.i(TAG,String.format("\t program '%s' ",program.getName()));
            List<DataElementExtended> dataElements = new ArrayList<>();
            String programUid = program.getUid();
            List<ProgramStageExtended> programStages = program.getProgramStages();
            for (ProgramStageExtended programStage : programStages) {
                Log.d(TAG, "programStage.getProgramStageDataElements size: "+programStage.getProgramStageDataElements().size());
                Log.i(TAG,String.format("\t\t programStage '%s' ",program.getName()));
                List<ProgramStageDataElementExtended> programStageDataElements = programStage.getProgramStageDataElements();
                count=programStage.getProgramStageDataElements().size();
                for (ProgramStageDataElementExtended programStageDataElement : programStageDataElements) {
                    if (!ProgressActivity.PULL_IS_ACTIVE) return;

                    //The ProgramStageDataElement without Dataelement uid is not correctly configured.
                    if(programStageDataElement.getDataelement()==null || programStageDataElement.getDataelement().equals("")){
                        Log.d(TAG, "Ignoring ProgramStageDataElements without dataelement...");
                        continue;
                    }

                    //Note: the sdk method getDataElement returns the dataElement object, and getDataelement returns the dataelement uid.
                    DataElementExtended dataElement = programStageDataElement.getDataElement();
                    if (dataElement!=null && dataElement.getUid() != null) {
                        dataElements.add(dataElement);
                    }
                    else{
                        DataElementExtended.existsDataElementByUid(programStageDataElement.getDataelement());
                        dataElement = new DataElementExtended(SdkQueries.getDataElement(programStageDataElement.getDataelement()));

                        if (dataElement!=null) {
                            dataElements.add(dataElement);
                        }
                        else{
                            //FIXME This query returns random null for some dataelements but those dataElements are stored in the database. It's a possible bug of dbflow and DataElement pojo conversion.
                            Log.d(TAG,"Null dataelement on first query "+ programStageDataElement.getProgramStage());
                            int times=0;
                            while(dataElement==null){
                                times++;
                                Log.d(TAG, "running : "+times);
                                try {
                                    Thread.sleep(100);
                                    dataElement= new DataElementExtended(SdkQueries.getDataElement(programStageDataElement.getDataelement()));
                                } catch (InterruptedException e) {//throw new RuntimeException("Null query");
                                    e.printStackTrace();
                                }
                            }
                            Log.d(TAG, "needed : "+times);
                            dataElements.add(dataElement);
                        }
                    }
                }

                if(count!=dataElements.size()){
                    Log.d(TAG, "The programStageDataElements size ("+count+") is different than the saved dataelements size ("+dataElements.size()+")");
                }
            }
            Log.i(TAG,String.format("\t program '%s' DONE ",program.getName()));


            if (!ProgressActivity.PULL_IS_ACTIVE) return;
            Collections.sort(dataElements, new Comparator<DataElementExtended>() {
                public int compare(DataElementExtended dataElementExtended1, DataElementExtended dataElementExtended2) {
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
                    if (dataelementOrder1 == dataelementOrder2)
                        return 0;
                    else if (dataelementOrder1 == null)
                        return 1;
                    else if (dataelementOrder2 == null)
                        return -1;
                    return dataelementOrder1.compareTo(dataelementOrder2);
                }
            });
            programsDataelements.put(programUid, dataElements);
        }

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        Log.i(TAG, "Building questions,compositescores,headers...");
        int i=0;
        for (ProgramExtended program : programs) {
            converter.actualProgram=program;
            String programUid = program.getUid();
            List<DataElementExtended> sortDataElements = programsDataelements.get(programUid);
            for (DataElementExtended dataElement : sortDataElements) {
                if (++i%50==0)
                    postProgress(context.getString(R.string.progress_pull_questions) + String.format(" %s", i));
                if (!ProgressActivity.PULL_IS_ACTIVE) return;
                //Log.i(TAG,"Converting DE "+dataElementExtended.getDataElement().getUid());
                dataElement.setProgramUid(programUid);
                dataElement.accept(converter);
            }
        }

        //Saves questions and media in batch mode
        converter.saveBatch();

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        Log.i(TAG, "Building relationships...");
        postProgress(context.getString(R.string.progress_pull_relationships));
        for (ProgramExtended program : programs) {
            converter.actualProgram=program;
            String programUid = program.getUid();
            List<DataElementExtended> sortDataElements = programsDataelements.get(programUid);
            programsDataelements.put(programUid, sortDataElements);
            for (DataElementExtended dataElement : sortDataElements) {
                if (!ProgressActivity.PULL_IS_ACTIVE) return;
                dataElement.setProgramUid(programUid);
                converter.buildRelations(dataElement);
            }
        }

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //Fill order and parent scores
        Log.i(TAG, "Building compositeScore relationships...");
        converter.buildScores();
        Log.i(TAG, "MetaData successfully converted...");
    }

    /**
     * Turns sdk organisationUnit and levels into app info
     * @param converter
     * @return
     */
    private boolean convertOrgUnits(ConvertFromSDKVisitor converter) {
        postProgress(context.getString(R.string.progress_pull_preparing_orgs));
        Log.i(TAG, "Converting organisationUnitLevels...");
        List<OrganisationUnitLevelExtended> organisationUnitLevels = OrganisationUnitLevelExtended.getExtendedList(SdkQueries.getOrganisationUnitLevels());
        for(OrganisationUnitLevelExtended organisationUnitLevel:organisationUnitLevels){
            if(!ProgressActivity.PULL_IS_ACTIVE) return false;
            organisationUnitLevel.accept(converter);
        }

        Log.i(TAG, "Converting organisationUnits...");
        List<OrganisationUnitExtended> assignedOrganisationsUnits = OrganisationUnitExtended.getExtendedList(SdkQueries.getAssignedOrganisationUnits());
        for (OrganisationUnitExtended assignedOrganisationsUnit : assignedOrganisationsUnits) {
            if (!ProgressActivity.PULL_IS_ACTIVE) return false;
            assignedOrganisationsUnit.accept(converter);
        }

        Log.i(TAG,"Building orgunit hierarchy...");
        return converter.buildOrgUnitHierarchy(assignedOrganisationsUnits);
    }

    /**
     * Turns events and datavalues into
     *
     * @param converter
     */
    private void convertDataValues(ConvertFromSDKVisitor converter) {
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        postProgress(context.getString(R.string.progress_pull_surveys));
        //XXX This is the right place to apply additional filters to data conversion (only predefined orgunit for instance)
        //For each unit
        for (OrganisationUnitExtended organisationUnit : OrganisationUnitExtended.getExtendedList(SdkQueries.getAssignedOrganisationUnits())) {
            //Each assigned program
            for (ProgramExtended program : ProgramExtended.getExtendedList(SdkQueries.getProgramsForOrganisationUnit(organisationUnit.getId(), ProgramType.WITHOUT_REGISTRATION))) {
                converter.actualProgram=program;
                List<EventExtended> events = EventExtended.getExtendedList(SdkQueries.getEvents(organisationUnit.getId(), program.getUid()));
                Log.i(TAG, String.format("Converting surveys and values for orgUnit: %s | program: %s", organisationUnit.getLabel(), program.getDisplayName()));
                for (EventExtended event : events) {
                    if (!ProgressActivity.PULL_IS_ACTIVE) return;
                    if(event.getEventDate()==null || event.getEventDate().equals(""))
                        break;
                    event.accept(converter);
                }
            }
        }

        //Plan surveys for the future
        SurveyPlanner.getInstance().buildNext();
    }

    /**
     * Notifies a progress into the bus (the caller activity will be listening)
     *
     * @param msg
     */
    private void postProgress(String msg) {
        SdkController.postProgress(msg);
    }

    /**
     * Notifies an exception while pulling
     *
     * @param ex
     */
    private void postException(Exception ex) {
        SdkController.postException(ex);
    }

    /**
     * Notifies that the pull is over
     */
    private void postFinish() {
        //Fixme maybe it is not the best place to reload the logged user.(Without reload the user after pull, the user had diferent id and application crash).
        SdkController.postFinish();
    }

    //Returns true if the pull thead is finish
    public boolean finishPullJob() {
       return SdkController.finishPullJob();
    }

}

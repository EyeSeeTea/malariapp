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
import org.eyeseetea.malariacare.sdk.models.AttributeFlow;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitLevelFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.UserAccountFlow;
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

    private final static Class MANDATORY_METADATA_TABLES[] = {
            AttributeFlow.class,
            DataElementFlow.class,
            //DataElementAttributeValueFlow.class,
            OptionFlow.class,
            OptionSetFlow.class,
            UserAccountFlow.class,
            OrganisationUnitFlow.class,
            //OrganisationUnitProgramRelationshipFlow.class,
            ProgramStageFlow.class,
            ProgramStageDataElementFlow.class,
            ProgramStageSectionFlow.class
    };

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
            SdkController.setMaxEvents(PreferencesState.getInstance().getMaxEvents());
            Calendar month = Calendar.getInstance();
            month.add(Calendar.MONTH, -NUMBER_OF_MONTHS);
            SdkController.setStartDate(EventExtended.format(month.getTime(),EventExtended.AMERICAN_DATE_FORMAT));
            SdkController.setFullOrganisationUnitHierarchy(AppSettingsBuilder.isFullHierarchy());
            SdkController.clearMetaDataLoadedFlags();
            SdkController.wipe();
            PopulateDB.wipeSDKData();
            PopulateDB.wipeDatabase();
            //Pull new metadata
            postProgress(context.getString(R.string.progress_pull_downloading));
            try {
                if(AppSettingsBuilder.isDownloadOnlyLastEvents()){
                    job = SdkController.loadLastData(context);
                }
                else{
                    job = SdkController.loadData(context);
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
        SdkController.enableMetaDataFlags(PreferencesState.getInstance().getContext());
    }

    /*
    public void onLoadMetadataFinished(final NetworkJob.NetworkJobResult<ResourceType> result) {
        Log.d(TAG, "Subscribe method: onLoadMetadataFinished");
        new Thread() {
            @Override
            public void run() {
                try {
                    if (result == null) {
                        Log.e(TAG, "onLoadMetadataFinished with null");
                        return;
                    }

                    //Error while pulling
                    if (result.getResponseHolder() != null && result.getResponseHolder().getApiException() != null) {
                        Log.e(TAG, result.getResponseHolder().getApiException().getMessage());
                        ProgressActivity.cancellPull(context.getString(R.string.dialog_pull_error),result.getResponseHolder().getApiException().getMessage());
                        postException(new Exception(context.getString(R.string.dialog_pull_error)));
                        return;
                    }

                    //Get SdkLogger messages
                    if(result.getResponseHolder().getItem()!=null) {
                        Object item=(Object) result.getResponseHolder().getItem();
                        List<LogMessage> messagesList = (List<LogMessage>) item;
                        for (LogMessage message:messagesList){
                            switch (message.getType()){
                                case SdkLogger.INFO:
                                    Log.d(TAG,"info"+message.getMessage());
                                    break;
                                case SdkLogger.WARNING:
                                    Log.d(TAG,"Warning"+message.getMessage());
                                    break;
                                case SdkLogger.ERROR:
                                    Log.d(TAG, "Error" + message.getMessage());
                                    ProgressActivity.cancellPull(message.getException().getMessage(), message.getMessage());
                                    postException(new Exception(context.getString(R.string.dialog_pull_error)));
                                    return;
                            }
                        }
                    }
                    //Ok
                    wipeDatabase();

                    if(!mandatoryMetadataTablesNotEmpty())
                        ProgressActivity.cancellPull("Error", "Error downloading metadata");

                    convertFromSDK();

                    validateCS();
                    if (ProgressActivity.PULL_IS_ACTIVE) {
                        Log.d(TAG, "PULL process...OK");
                    }
                } catch (Exception ex) {
                    ProgressActivity.PULL_ERROR=true;
                    Log.e(TAG, "onLoadMetadataFinished: " + ex.getLocalizedMessage());
                    ex.printStackTrace();
                    postException(ex);
                } finally {
                    postFinish();
                    unregister();
                }
            }
        }.start();
    }
    */

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
        for(Class table: MANDATORY_METADATA_TABLES) {
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
        List<String> assignedProgramsIDs = SdkController.getAssignedPrograms();
        for (String assignedProgramID : assignedProgramsIDs) {
            ProgramExtended programExtended = new ProgramExtended(SdkController.getProgram(assignedProgramID));
            programExtended.accept(converter);
        }

        //Convert Answers, Options
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        postProgress(context.getString(R.string.progress_pull_preparing_answers));
        List<OptionSetFlow> optionSets = SdkController.getOptionSets();
        Log.i(TAG, "Converting answers and options...");
        for (OptionSetFlow optionSet : optionSets) {
            if (!ProgressActivity.PULL_IS_ACTIVE) return;
            OptionSetExtended optionSetExtended = new OptionSetExtended(optionSet);
            optionSetExtended.accept(converter);
        }
        //OrganisationUnits
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        if (!convertOrgUnits(converter)) return;

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //User (from UserAccount)
        Log.i(TAG, "Converting user...");
        UserAccountExtended userAccountExtended = new UserAccountExtended(SdkController.getUserAccount());
        userAccountExtended.accept(converter);

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //Convert questions and compositeScores
        postProgress(context.getString(R.string.progress_pull_questions));
        Log.i(TAG, "Ordering questions and compositeScores...");

        int count;
        //Dataelements ordered by program.
        List<ProgramExtended> programs = ProgramExtended.getAllPrograms();
        Map<String, List<DataElementFlow>> programsDataelements = new HashMap<>();
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        for (ProgramExtended program : programs) {
            converter.actualProgram=program;
            Log.i(TAG,String.format("\t program '%s' ",program.getName()));
            List<DataElementFlow> dataElements = new ArrayList<>();
            String programUid = program.getUid();
            List<ProgramStageFlow> programStages = program.getProgramStages();
            for (ProgramStageFlow programStage : programStages) {
                ProgramStageExtended programStageExtended = new ProgramStageExtended(programStage);
                Log.d(TAG, "programStage.getProgramStageDataElements size: "+programStageExtended.getProgramStageDataElements().size());
                Log.i(TAG,String.format("\t\t programStage '%s' ",program.getName()));
                List<ProgramStageDataElementFlow> programStageDataElements = programStageExtended.getProgramStageDataElements();
                count=programStageExtended.getProgramStageDataElements().size();
                for (ProgramStageDataElementFlow programStageDataElement : programStageDataElements) {
                    ProgramStageDataElementExtended programStageDataElementExtended = new ProgramStageDataElementExtended(programStageDataElement);
                    if (!ProgressActivity.PULL_IS_ACTIVE) return;

                    //The ProgramStageDataElement without Dataelement uid is not correctly configured.
                    if(programStageDataElementExtended.getDataelement()==null || programStageDataElementExtended.getDataelement().equals("")){
                        Log.d(TAG, "Ignoring ProgramStageDataElements without dataelement...");
                        continue;
                    }

                    //Note: the sdk method getDataElement returns the dataElement object, and getDataelement returns the dataelement uid.
                    DataElementFlow dataElement = programStageDataElementExtended.getDataElement();
                    if (dataElement!=null && dataElement.getUId() != null) {
                        dataElements.add((DataElementFlow) dataElement);
                    }
                    else{
                        DataElementExtended.existsDataElementByUid(programStageDataElementExtended.getDataelement());
                        dataElement = SdkController.getDataElement(programStageDataElementExtended.getDataelement());

                        if (dataElement!=null) {
                            dataElements.add((DataElementFlow) dataElement);
                        }
                        else{
                            //FIXME This query returns random null for some dataelements but those dataElements are stored in the database. It's a possible bug of dbflow and DataElement pojo conversion.
                            Log.d(TAG,"Null dataelement on first query "+ programStageDataElementExtended.getProgramStage());
                            int times=0;
                            while(dataElement==null){
                                times++;
                                Log.d(TAG, "running : "+times);
                                try {
                                    Thread.sleep(100);
                                    dataElement=SdkController.getDataElement(programStageDataElementExtended.getDataelement());
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
            Collections.sort(dataElements, new Comparator<DataElementFlow>() {
                public int compare(DataElementFlow de1, DataElementFlow de2) {
                    DataElementExtended dataElementExtended1 = new DataElementExtended(de1);
                    DataElementExtended dataElementExtended2 = new DataElementExtended(de2);
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
            List<DataElementFlow> sortDataElements = programsDataelements.get(programUid);
            for (DataElementFlow dataElement : sortDataElements) {
                if (++i%50==0)
                    postProgress(context.getString(R.string.progress_pull_questions) + String.format(" %s", i));
                if (!ProgressActivity.PULL_IS_ACTIVE) return;
                DataElementExtended dataElementExtended = new DataElementExtended(dataElement);
                //Log.i(TAG,"Converting DE "+dataElementExtended.getDataElement().getUid());
                dataElementExtended.setProgramUid(programUid);
                dataElementExtended.accept(converter);
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
            List<DataElementFlow> sortDataElements = programsDataelements.get(programUid);
            programsDataelements.put(programUid, sortDataElements);
            for (DataElementFlow dataElement : sortDataElements) {
                if (!ProgressActivity.PULL_IS_ACTIVE) return;
                DataElementExtended dataElementExtended = new DataElementExtended(dataElement);
                dataElementExtended.setProgramUid(programUid);
                converter.buildRelations(dataElementExtended);
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
        List<OrganisationUnitLevelFlow> organisationUnitLevels = SdkController.getOrganisationUnitLevels();
        for(OrganisationUnitLevelFlow organisationUnitLevel:organisationUnitLevels){
            if(!ProgressActivity.PULL_IS_ACTIVE) return false;
            OrganisationUnitLevelExtended organisationUnitLevelExtended = new OrganisationUnitLevelExtended(organisationUnitLevel);
            organisationUnitLevelExtended.accept(converter);
        }

        Log.i(TAG, "Converting organisationUnits...");
        List<OrganisationUnitFlow> assignedOrganisationsUnits = SdkController.getAssignedOrganisationUnits();
        for (OrganisationUnitFlow assignedOrganisationsUnit : assignedOrganisationsUnits) {
            if (!ProgressActivity.PULL_IS_ACTIVE) return false;
            OrganisationUnitExtended organisationUnitExtended = new OrganisationUnitExtended(assignedOrganisationsUnit);
            organisationUnitExtended.accept(converter);
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
        for (OrganisationUnitFlow organisationUnit : SdkController.getAssignedOrganisationUnits()) {
            //Each assigned program
            for (ProgramExtended program : ProgramExtended.getProgramsExtendedList(SdkController.getProgramsForOrganisationUnit(organisationUnit.getUId(), ProgramType.WITHOUT_REGISTRATION))) {
                converter.actualProgram=program;
                List<EventFlow> events = SdkController.getEvents(organisationUnit.getUId(), program.getUid());
                Log.i(TAG, String.format("Converting surveys and values for orgUnit: %s | program: %s", new OrganisationUnitExtended(organisationUnit).getLabel(), program.getDisplayName()));
                for (EventFlow event : events) {
                    if (!ProgressActivity.PULL_IS_ACTIVE) return;
                    if(event.getEventDate()==null || event.getEventDate().equals(""))
                        break;
                    EventExtended eventExtended = new EventExtended(event);
                    eventExtended.accept(converter);
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

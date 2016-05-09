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

import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitLevelExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.database.utils.planning.SurveyPlanner;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.layout.dashboard.config.AppSettings;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.controllers.LoadingController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.job.Job;
import org.hisp.dhis.android.sdk.job.JobExecutor;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitLevel;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.preferences.AppPreferences;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;
import org.hisp.dhis.android.sdk.utils.log.LogMessage;
import org.hisp.dhis.android.sdk.utils.log.SdkLogger;

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
            org.hisp.dhis.android.sdk.persistence.models.Attribute.class,
            org.hisp.dhis.android.sdk.persistence.models.DataElement.class,
            org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue.class,
            org.hisp.dhis.android.sdk.persistence.models.Option.class,
            org.hisp.dhis.android.sdk.persistence.models.OptionSet.class,
            org.hisp.dhis.android.sdk.persistence.models.UserAccount.class,
            org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit.class,
            org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitProgramRelationship.class,
            org.hisp.dhis.android.sdk.persistence.models.ProgramStage.class,
            org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement.class,
            org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection.class
    };

    private static PullController instance;

    private static Job job;
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
        try {
            Dhis2Application.bus.register(this);
        } catch (Exception e) {
            unregister();
            Dhis2Application.bus.register(this);
        }
    }

    /**
     * Unregister pull controller from bus events
     */
    public void unregister() {
        try {
            Dhis2Application.bus.unregister(this);
        } catch (Exception e) {
        }
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
            TrackerController.setMaxEvents(PreferencesState.getInstance().getMaxEvents());
            Calendar month = Calendar.getInstance();
            month.add(Calendar.MONTH, -NUMBER_OF_MONTHS);
            TrackerController.setStartDate(EventExtended.format(month.getTime(),EventExtended.AMERICAN_DATE_FORMAT));
            MetaDataController.setFullOrganisationUnitHierarchy(AppSettingsBuilder.isFullHierarchy());
            MetaDataController.clearMetaDataLoadedFlags();
            MetaDataController.wipe();
            PopulateDB.wipeSDKData();
            PopulateDB.wipeDatabase();
            //Pull new metadata
            postProgress(context.getString(R.string.progress_pull_downloading));
            try {
                job = DhisService.loadLastData(context);
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
        LoadingController.enableLoading(context, ResourceType.ASSIGNEDPROGRAMS);
        LoadingController.enableLoading(context, ResourceType.PROGRAMS);
        LoadingController.enableLoading(context, ResourceType.OPTIONSETS);
        LoadingController.enableLoading(context, ResourceType.EVENTS);
    }

    @Subscribe
    public void onLoadMetadataFinished(final NetworkJob.NetworkJobResult<ResourceType> result) {
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
                    if (ProgressActivity.PULL_IS_ACTIVE) {
                        Log.d(TAG, "PULL process...OK");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "onLoadMetadataFinished: " + ex.getLocalizedMessage());
                    postException(ex);
                } finally {
                    postFinish();
                    unregister();
                }
            }
        }.start();
    }

    private boolean mandatoryMetadataTablesNotEmpty(){

        int elementsInTable = 0;
        for(Class table: MANDATORY_METADATA_TABLES) {
            elementsInTable = (int) new Select().count()
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
        //Convert Programs, Tabgroups, Tabs
        postProgress(context.getString(R.string.progress_pull_preparing_program));
        Log.i(TAG, "Converting programs, tabgroups and tabs...");
        List<String> assignedProgramsIDs = MetaDataController.getAssignedPrograms();
        for (String assignedProgramID : assignedProgramsIDs) {
            ProgramExtended programExtended = new ProgramExtended(MetaDataController.getProgram(assignedProgramID));
            programExtended.accept(converter);
        }

        //Convert Answers, Options
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        postProgress(context.getString(R.string.progress_pull_preparing_answers));
        List<OptionSet> optionSets = MetaDataController.getOptionSets();
        Log.i(TAG, "Converting answers and options...");
        for (OptionSet optionSet : optionSets) {
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
        UserAccountExtended userAccountExtended = new UserAccountExtended(MetaDataController.getUserAccount());
        userAccountExtended.accept(converter);

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        //Convert questions and compositeScores
        postProgress(context.getString(R.string.progress_pull_questions));
        Log.i(TAG, "Ordering questions and compositeScores...");

        //Dataelements ordered by program.
        List<org.hisp.dhis.android.sdk.persistence.models.Program> programs = ProgramExtended.getAllPrograms();
        Map<String, List<DataElement>> programsDataelements = new HashMap<>();
        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        for (org.hisp.dhis.android.sdk.persistence.models.Program program : programs) {
            List<DataElement> dataElements = new ArrayList<>();
            String programUid = program.getUid();
            List<ProgramStage> programStages = program.getProgramStages();
            for (org.hisp.dhis.android.sdk.persistence.models.ProgramStage programStage : programStages) {
                List<ProgramStageDataElement> programStageDataElements = programStage.getProgramStageDataElements();
                for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                    if (programStageDataElement.getDataElement().getUid() != null) {
                        if (!ProgressActivity.PULL_IS_ACTIVE) return;
                        dataElements.add(programStageDataElement.getDataElement());
                    }
                }
            }
            if (!ProgressActivity.PULL_IS_ACTIVE) return;
            Collections.sort(dataElements, new Comparator<DataElement>() {
                public int compare(DataElement de1, DataElement de2) {
                    DataElementExtended dataElementExtended1 = new DataElementExtended(de1);
                    DataElementExtended dataElementExtended2 = new DataElementExtended(de2);
                    Integer dataelementOrder1 = -1, dataelementOrder2 = -1;
                    try {
                        dataelementOrder1 = dataElementExtended1.findOrder();
                    } catch (Exception e) {
                        dataelementOrder1 = null;
                    }
                    try {
                        dataelementOrder2 = dataElementExtended2.findOrder();
                    } catch (Exception e) {
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
        for (org.hisp.dhis.android.sdk.persistence.models.Program program : programs) {
            String programUid = program.getUid();
            List<DataElement> sortDataElements = programsDataelements.get(programUid);
            for (DataElement dataElement : sortDataElements) {
                if (!ProgressActivity.PULL_IS_ACTIVE) return;
                DataElementExtended dataElementExtended = new DataElementExtended(dataElement);
                //Log.i(TAG,"Converting DE "+dataElementExtended.getDataElement().getUid());
                dataElementExtended.accept(converter);
            }
        }

        if (!ProgressActivity.PULL_IS_ACTIVE) return;
        Log.i(TAG, "Building relationships...");
        for (org.hisp.dhis.android.sdk.persistence.models.Program program : programs) {
            String programUid = program.getUid();
            List<DataElement> sortDataElements = programsDataelements.get(programUid);
            programsDataelements.put(programUid, sortDataElements);
            for (DataElement dataElement : sortDataElements) {
                if (!ProgressActivity.PULL_IS_ACTIVE) return;
                DataElementExtended dataElementExtended = new DataElementExtended(dataElement);
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
        List<OrganisationUnitLevel> organisationUnitLevels = MetaDataController.getOrganisationUnitLevels();
        for(OrganisationUnitLevel organisationUnitLevel:organisationUnitLevels){
            if(!ProgressActivity.PULL_IS_ACTIVE) return false;
            OrganisationUnitLevelExtended organisationUnitLevelExtended = new OrganisationUnitLevelExtended(organisationUnitLevel);
            organisationUnitLevelExtended.accept(converter);
        }

        Log.i(TAG, "Converting organisationUnits...");
        List<OrganisationUnit> assignedOrganisationsUnits = MetaDataController.getAssignedOrganisationUnits();
        for (OrganisationUnit assignedOrganisationsUnit : assignedOrganisationsUnits) {
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
        for (OrganisationUnit organisationUnit : MetaDataController.getAssignedOrganisationUnits()) {
            //Each assigned program
            for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(organisationUnit.getId(), ProgramType.WITHOUT_REGISTRATION)) {
                List<Event> events = TrackerController.getEvents(organisationUnit.getId(), program.getUid());
                Log.i(TAG, String.format("Converting surveys and values for orgUnit: %s | program: %s", organisationUnit.getLabel(), program.getDisplayName()));
                for (Event event : events) {
                    if (!ProgressActivity.PULL_IS_ACTIVE) return;
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
        Dhis2Application.getEventBus().post(new SyncProgressStatus(msg));
    }

    /**
     * Notifies an exception while pulling
     *
     * @param ex
     */
    private void postException(Exception ex) {
        Dhis2Application.getEventBus().post(new SyncProgressStatus(ex));
    }

    /**
     * Notifies that the pull is over
     */
    private void postFinish() {
        //Fixme maybe it is not the best place to reload the logged user.(Without reload the user after pull, the user had diferent id and application crash).
        User user = User.getLoggedUser();
        Session.setUser(user);
        try {
            Dhis2Application.getEventBus().post(new SyncProgressStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Returns true if the pull thead is finish
    public boolean finishPullJob() {
        if (JobExecutor.isJobRunning(job.getJobId())) {
            Log.d(TAG, "Job " + job.getJobId() + " is running");
            job.cancel(true);
            try {
                try {JobExecutor.getInstance().dequeueRunningJob(job);} catch (Exception e) {e.printStackTrace();}
                job.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return true;
            }
        }
        return false;

    }

}

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
import android.provider.ContactsContract;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Match;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Program$Table;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.QuestionOption;
import org.eyeseetea.malariacare.database.model.QuestionRelation;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.hisp.dhis.android.sdk.controllers.DhisService;
import org.hisp.dhis.android.sdk.controllers.LoadingController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.Attribute$Table;
import org.hisp.dhis.android.sdk.persistence.models.AttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataElement$Table;
import org.hisp.dhis.android.sdk.persistence.models.DataElementAttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage$Table;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement$Table;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A static controller that orchestrate the pull process
 * Created by arrizabalaga on 4/11/15.
 */
public class PullController {
    private final String TAG = ".PullController";

    private static PullController instance;

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
        Dhis2Application.bus.register(this);
    }

    /**
     * Unregister pull controller from bus events
     */
    private void unregister() {
        Dhis2Application.bus.unregister(this);
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
            MetaDataController.clearMetaDataLoadedFlags();
            MetaDataController.wipe();
            //Pull new metadata
            postProgress(context.getString(R.string.progress_pull_downloading));
            DhisService.loadData(context);
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
        LoadingController.enableLoading(context, ResourceType.ATTRIBUTEVALUES);
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
                        postException(new Exception(context.getString(R.string.dialog_pull_error)));
                        return;
                    }

                    //Ok
                    wipeDatabase();
                    convertFromSDK();
                    postFinish();
                    Log.d(TAG, "PULL process...OK");
                } catch (Exception ex) {
                    Log.e(TAG, "onLoadMetadataFinished: " + ex.getLocalizedMessage());
                    postException(ex);
                } finally {
                    unregister();
                }
            }
        }.start();
    }

    /**
     * Erase data from app database
     */
    public void wipeDatabase(){
        Log.d(TAG, "Deleting app database...");
        Delete.tables(
                Value.class,
                Score.class,
                Survey.class,
                OrgUnit.class,
                OrgUnitLevel.class,
                User.class,
                QuestionOption.class,
                Match.class,
                QuestionRelation.class,
                Question.class,
                CompositeScore.class,
                Option.class,
                Answer.class,
                Header.class,
                Tab.class,
                TabGroup.class,
                Program.class
        );
    }

    /**
     * Launches visitor that turns SDK data into APP data
     */
    private void convertFromSDK() {
        Log.d(TAG, "Converting SDK into APP data");

        //One shared converter to match parents within the hierarchy
        ConvertFromSDKVisitor converter = new ConvertFromSDKVisitor();
        convertMetaData(converter);
        convertDataValues(converter);

    }

    /**
     * Turns sdk metadata into app metadata
     *
     * @param converter
     */
    private void convertMetaData(ConvertFromSDKVisitor converter) {
        if(ProgressActivity.active) {
            //Convert Programs, Tabgroups, Tabs
            postProgress(context.getString(R.string.progress_pull_preparing_program));
            Log.i(TAG, "Converting programs, tabgroups and tabs...");
            List<String> assignedProgramsIDs = MetaDataController.getAssignedPrograms();
            for (String assignedProgramID : assignedProgramsIDs) {
                ProgramExtended programExtended = new ProgramExtended(MetaDataController.getProgram(assignedProgramID));
                programExtended.accept(converter);
            }
        }

        if(ProgressActivity.active) {
            //Convert Answers, Options
            postProgress(context.getString(R.string.progress_pull_preparing_answers));
            List<OptionSet> optionSets = MetaDataController.getOptionSets();
            Log.i(TAG, "Converting answers and options...");
            for (OptionSet optionSet : optionSets) {
                OptionSetExtended optionSetExtended = new OptionSetExtended(optionSet);
                optionSetExtended.accept(converter);
            }
        }

        if(ProgressActivity.active) {
            //OrganisationUnits
            postProgress(context.getString(R.string.progress_pull_preparing_orgs));
            Log.i(TAG, "Converting organisationUnits...");
            List<OrganisationUnit> assignedOrganisationsUnits = MetaDataController.getAssignedOrganisationUnits();
            for (OrganisationUnit assignedOrganisationsUnit : assignedOrganisationsUnits) {
                OrganisationUnitExtended organisationUnitExtended = new OrganisationUnitExtended(assignedOrganisationsUnit);
                organisationUnitExtended.accept(converter);
            }
        }

        if(ProgressActivity.active) {
            //User (from UserAccount)
            Log.i(TAG, "Converting user...");
            UserAccountExtended userAccountExtended = new UserAccountExtended(MetaDataController.getUserAccount());
            userAccountExtended.accept(converter);
        }

        if(ProgressActivity.active) {
            //Convert questions and compositeScores
            postProgress(context.getString(R.string.progress_pull_questions));
            Log.i(TAG, "Ordering questions and compositeScores...");
        }

        //Dataelements ordered by program.
        List<org.hisp.dhis.android.sdk.persistence.models.Program> programs = new Select().from(org.hisp.dhis.android.sdk.persistence.models.Program.class).queryList();
        Map<String, List<DataElement>> programsDataelements = new HashMap<>();
        if(ProgressActivity.active) {
            for (org.hisp.dhis.android.sdk.persistence.models.Program program : programs) {
                List<DataElement> dataElements = new ArrayList<>();
                String programUid = program.getUid();
                List<ProgramStage> programStages = program.getProgramStages();
                for (org.hisp.dhis.android.sdk.persistence.models.ProgramStage programStage : programStages) {
                    List<ProgramStageDataElement> programStageDataElements = programStage.getProgramStageDataElements();
                    for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                        if (programStageDataElement.getDataElement().getUid() != null)
                            dataElements.add(programStageDataElement.getDataElement());
                    }
                }

                if(ProgressActivity.active) {
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
            }
        }


        if(ProgressActivity.active) {
            Log.i(TAG, "Building questions,compositescores,headers...");
            for (org.hisp.dhis.android.sdk.persistence.models.Program program : programs) {
                String programUid = program.getUid();
                List<DataElement> sortDataElements = programsDataelements.get(programUid);

                for (DataElement dataElement : sortDataElements) {
                    DataElementExtended dataElementExtended = new DataElementExtended(dataElement);
                    dataElementExtended.accept(converter);
                }
            }
        }


        if(ProgressActivity.active) {
            Log.i(TAG, "Building relationships...");
            for (org.hisp.dhis.android.sdk.persistence.models.Program program : programs) {
                String programUid = program.getUid();
                List<DataElement> sortDataElements = programsDataelements.get(programUid);
                programsDataelements.put(programUid, sortDataElements);
                for (DataElement dataElement : sortDataElements) {
                    DataElementExtended dataElementExtended = new DataElementExtended(dataElement);
                    converter.buildRelations(dataElementExtended);
                }
            }
        }

        if(ProgressActivity.active) {
            //Fill order and parent scores
            Log.i(TAG, "Building compositeScore relationships...");
            converter.buildScores();
            Log.i(TAG, "MetaData successfully converted...");
        }
    }

    /**
     * Turns events and datavalues into
     *
     * @param converter
     */
    private void convertDataValues(ConvertFromSDKVisitor converter) {

        postProgress(context.getString(R.string.progress_pull_surveys));
        //XXX This is the right place to apply additional filters to data conversion (only predefined orgunit for instance)
        //For each unit
        for (OrganisationUnit organisationUnit : MetaDataController.getAssignedOrganisationUnits()) {
            //Each assigned program
            for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(organisationUnit.getId(), org.hisp.dhis.android.sdk.persistence.models.Program.ProgramType.WITHOUT_REGISTRATION)) {
                List<Event> events = TrackerController.getEvents(organisationUnit.getId(), program.getUid());
                Log.i(TAG, String.format("Converting surveys and values for orgUnit: %s | program: %s", organisationUnit.getLabel(), program.getDisplayName()));
                for (Event event : events) {
                    EventExtended eventExtended = new EventExtended(event);
                    eventExtended.accept(converter);
                }
            }
        }

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
        Dhis2Application.getEventBus().post(new SyncProgressStatus());
    }


}

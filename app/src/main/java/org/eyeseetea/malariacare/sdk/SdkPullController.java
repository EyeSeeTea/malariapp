package org.eyeseetea.malariacare.sdk;

import static org.hisp.dhis.client.sdk.models.program.ProgramType.WITHOUT_REGISTRATION;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.BaseActivity;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.ExceptionHandler;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.controllers.SyncStrategy;
import org.hisp.dhis.client.sdk.core.common.preferences.ResourceType;
import org.hisp.dhis.client.sdk.core.program.ProgramFields;
import org.hisp.dhis.client.sdk.models.dataelement.DataElement;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenterImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkPullController extends SdkController {

    private static final String TAG = ".SdkPullController";
    static List<org.hisp.dhis.client.sdk.models.program.Program> sdkPrograms;
    static HashMap<org.hisp.dhis.client.sdk.models.program.Program, List<OrganisationUnit>>
            programsAndOrganisationUnits;
    public static boolean errorOnPull = false;

    public static void setMaxEvents(int maxEvents) {
        //TrackerController.setMaxEvents(maxEvents);
    }

    public static void setStartDate(String startDate) {
        //TrackerController.setStartDate(startDate);
    }

    public static void setFullOrganisationUnitHierarchy(boolean fullHierarchy) {
        //MetaDataController.setFullOrganisationUnitHierarchy(fullHierarchy);
    }

    public static void clearMetaDataLoadedFlags() {
        //MetaDataController.clearMetaDataLoadedFlags();
    }

    public static void wipe() {
        //MetaDataController.wipe();
    }

    public static void enableMetaDataFlags(Context context) {
        List<ResourceType> resourceTypes = new ArrayList<>();
        resourceTypes.add(ResourceType.PROGRAMS);
        resourceTypes.add(ResourceType.OPTION_SETS);
        resourceTypes.add(ResourceType.EVENTS);
        enableMetaDataFlags(resourceTypes, context);
    }

    public static void enableMetaDataFlags(List<ResourceType> resources, Context context) {
        for (ResourceType resourceType : resources) {
            //LoadingController.enableLoading(context, resourceType);
        }
    }

    public static void loadLastData() {
        //// FIXME: 16/11/2016  limit by last data
        loadMetaData();

        loadDataValues();

        if (!errorOnPull) {
            PullController.getInstance().startConversion();
        } else {
            pullFail();
        }
    }

    private static void pullFail() {
        //// FIXME: 16/11/2016
    }

    public static void loadData() {
        loadMetaData();

        loadDataValues();

        if (!errorOnPull) {
            PullController.getInstance().startConversion();
        } else {
            pullFail();
        }
    }

    private static void loadMetaData() {
        //Pull metadata
        getPrograms();
    }

    private static void loadDataValues() {
        //Pull events
        getEventsFromListByProgramAndOrganisationUnit();
    }


    /**
     * Pull the programs and all the metadata
     */
    public static void getPrograms() {
        Set<ProgramType> programType = new HashSet<ProgramType>();
        programType.add(WITHOUT_REGISTRATION);
        D2.me().programs().pull(ProgramFields.ALL, programType).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Action1<List<org.hisp.dhis.client.sdk.models.program.Program>>() {
                    @Override
                    public void call(
                            List<org.hisp.dhis.client.sdk.models.program.Program> programs) {
                        sdkPrograms = programs;
                        getProgramStages();
                        Log.d(TAG, "Pull of programs finish");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorOnPull = false;
                        throwable.printStackTrace();
                        Log.e(TAG, "Error pulling programs: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * Pull the OrganisationUnits (not work at this moment)
     */
    //// FIXME: 16/11/2016  this method is throwing a timeout exception in dev server.
    public static void getOrganisationUnits() {
        Set<String> organisationUnitUid = new HashSet<String>();
        for (org.hisp.dhis.client.sdk.models.program.Program program : sdkPrograms) {
            for (OrganisationUnit organisationUnit : program.getOrganisationUnits()) {
                organisationUnitUid.add(organisationUnit.getUId());
            }
        }
        if (organisationUnitUid.size() == 0) {
            return;
        }
        Observable<List<OrganisationUnit>> organisationUnitObservable2 =
                D2.organisationUnits().pull(organisationUnitUid);
        organisationUnitObservable2.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<OrganisationUnit>>() {
                    @Override
                    public void call(List<OrganisationUnit> organisationUnits) {
                        Log.e(TAG, "OrganisationUnit: Done");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorOnPull = false;
                        throwable.printStackTrace();
                        Log.e(TAG, "OrganisationUnit: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * Pull the ProgramStages and continues the pull with the ProgramStageSections
     */
    public static void getProgramStages() {
        Observable<List<ProgramStage>> programStageObservable =
                D2.programStages().pull();
        programStageObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStage>>() {
                    @Override
                    public void call(List<ProgramStage> programStages) {
                        getProgramStageSections();
                        Log.d(TAG, "Pull of ProgramStage finish");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorOnPull = false;
                        throwable.printStackTrace();
                        Log.e(TAG,
                                "Error pulling ProgramStage: " + throwable.getLocalizedMessage());
                    }
                });
    }


    /**
     * Pull the ProgramStageDataSections and continues the pull with the
     * getProgramStageDataElements
     */
    public static void getProgramStageSections() {
        Observable<List<ProgramStageSection>> programStageSectionObservable =
                D2.programStageSections().pull();
        programStageSectionObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageSection>>() {
                    @Override
                    public void call(List<ProgramStageSection> programStageSections) {
                        getProgramStageDataElements();
                        Log.d(TAG, "Pull of ProgramStageSection finish");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorOnPull = false;
                        throwable.printStackTrace();
                        Log.e(TAG, "Error pulling ProgramStageSection: "
                                + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * Pull the ProgramStageDataElements and continues the pull with the getDataElements
     */
    public static void getProgramStageDataElements() {
        Observable<List<ProgramStageDataElement>> programStageDataElementObservable =
                D2.programStageDataElements().pull();
        programStageDataElementObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageDataElement>>() {
                    @Override
                    public void call(List<ProgramStageDataElement> programStageDataElement) {
                        Log.d(TAG, "Pull of ProgramStageDataElements finish");
                        getDataElements();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorOnPull = false;
                        throwable.printStackTrace();
                        Log.e(TAG, "Error pullling ProgramStageDataElement: "
                                + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * Pull the dataElements and finish the pull of metadata
     */
    public static void getDataElements() {
        Observable<List<DataElement>> dataElementObservable =
                D2.dataElements().pull();
        dataElementObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DataElement>>() {
                    @Override
                    public void call(List<DataElement> dataElement) {
                        Log.d(TAG, "Pull of DataElement finish");
                        //getOrganisationUnits();
                        //finish of metadata pull
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error pulling DataElement");
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * This method gets a organisation unit and program for each program(with organisation units)
     * and removes it(it removes the organisation unit and the program without organisation units)
     */
    private static ProgramAndOrganisationUnitDict getProgramAndOrganisationUnit() {
        if (sdkPrograms == null || sdkPrograms.size() == 0) {
            return null;
        }

        List<OrganisationUnit> organisationUnits = programsAndOrganisationUnits.get(
                sdkPrograms.get(0));
        OrganisationUnit localOrganisationUnit = null;
        if (organisationUnits == null || organisationUnits.size() == 0) {
            programsAndOrganisationUnits.remove(sdkPrograms.get(0));
            if (programsAndOrganisationUnits.size() == 0) {
                return null;
            } else {
                organisationUnits = programsAndOrganisationUnits.get(
                        sdkPrograms.get(0));
            }
        }
        localOrganisationUnit = organisationUnits.get(0);
        organisationUnits.remove(0);

        return new ProgramAndOrganisationUnitDict(sdkPrograms.get(0), localOrganisationUnit);
    }

    /**
     * This class is a dictionary for program and organisationunits
     */
    private static class ProgramAndOrganisationUnitDict {
        org.hisp.dhis.client.sdk.models.program.Program program;
        OrganisationUnit organisationUnit;

        ProgramAndOrganisationUnitDict(org.hisp.dhis.client.sdk.models.program.Program program,
                OrganisationUnit organisationUnit) {
            this.program = program;
            this.organisationUnit = organisationUnit;
        }

        public org.hisp.dhis.client.sdk.models.program.Program getProgram() {
            return program;
        }

        public OrganisationUnit getOrganisationUnit() {
            return organisationUnit;
        }
    }

    /**
     * list all of events
     * is not working at this moment.
     */
    private static void listEvents() {
        final Observable<List<Event>> eventObservable = D2.events().list().asObservable();
        eventObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.d(TAG, "listed events: " + events.size());
                        //finish
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error listing events");
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * This method get a list of events by organisationUnit and program, and pull it.
     * Is called recursively to pull, is not working at this moment
     */
    //// FIXME: 16/11/2016  this method is return a timeout exception
    private static void getEventsFromListByProgramAndOrganisationUnit() {
        final ProgramAndOrganisationUnitDict programAndOrganisationUnitDict =
                getProgramAndOrganisationUnit();
        if (programAndOrganisationUnitDict == null) {
            return;
        }
        Observable<List<Event>> eventListObservable = D2.events().list(
                programAndOrganisationUnitDict.getOrganisationUnit(),
                programAndOrganisationUnitDict.getProgram());
        eventListObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.e(TAG, "programs: Done " + events.size());
                        Set<String> eventsUid = new HashSet<String>();
                        for (Event event : events) {
                            eventsUid.add(event.getUId());
                        }
                        if (eventsUid.size() > 0) {
                            pullEvents(eventsUid, true);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        errorOnPull = false;
                        throwable.printStackTrace();
                        Log.e(TAG, "Error pulling events by program and org: "
                                + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * This method pull a list of uid events
     *
     * @param recursivePull if its used in combination with sdkProgram and organisation lists
     *                      downloaded in the pull of the metadata
     *                      It is not working at this moment
     */
    private static void pullEvents(Set<String> events, final boolean recursivePull) {
        Observable<List<Event>> eventObservable = D2.events().pull(SyncStrategy.DEFAULT,
                events).asObservable();
        eventObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.d(TAG, "Pulled events: " + events.size());
                        if (recursivePull) {
                            getEventsFromListByProgramAndOrganisationUnit();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error pulling events");
                        throwable.printStackTrace();
                    }
                });
    }


    /**
     * Pull a event uids
     * It is not working at this moment
     */
    private void pullEvent(String uid) {
        Set events = new HashSet();
        events.add(uid);
        D2.events().pull(events).asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.d(TAG, "Listed events: " + events.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error pulling events: ");
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * Pull a list of event uids
     *
     * @param eventUids list of event uid to be pull
     */
    private void pullEvents(Set<String> eventUids) {
        D2.events().pull(eventUids).asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.d(TAG, "Listed events: " + events.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "Error pulling events: ");
                        throwable.printStackTrace();
                    }
                });
    }
}

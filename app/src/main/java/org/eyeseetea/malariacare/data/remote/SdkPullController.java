package org.eyeseetea.malariacare.data.remote;


import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.controllers.SyncStrategy;
import org.hisp.dhis.client.sdk.core.common.preferences.ResourceType;
import org.hisp.dhis.client.sdk.core.program.ProgramFields;
import org.hisp.dhis.client.sdk.models.attribute.Attribute;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class SdkPullController extends SdkController {


    /**
     * This flag is used to control the async downloads before initialise the conversion from sdk
     * to
     * the app db
     */
    public static int asyncDownloads = 0;
    public static boolean pullData = false;
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
        //// FIXME: 16/11/2016  we need limit the event to be pulled
        pullData = true;
        loadMetaData();
    }


    public static void loadData() {
        pullData = true;
        loadMetaData();
    }

    private static void pullFail() {
        //// FIXME: 16/11/201
        ProgressActivity.showException("Unexpected error");
    }

    private static void next(String msg) {
        ProgressActivity.step(msg);
    }

    public static void loadMetaData() {
        // // FIXME: 23/01/2017 string
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program));
        D2.attributes().pull()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Attribute>>() {
                    @Override
                    public void call(
                            List<Attribute> attributes) {
                        pullProgramsAndOrganisationUnits(attributes);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling attributes: ", throwable);
                    }
                });
    }

    private static void pullProgramsAndOrganisationUnits(List<Attribute> attributes) {
        Log.d(TAG, "Pull of attributes finish" + attributes.size());

        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program));

        Set<ProgramType> programTypes = new HashSet<>();
        programTypes.add(ProgramType.WITHOUT_REGISTRATION);

        Observable.zip(D2.me().organisationUnits().pull(),
                D2.me().programs().pull(ProgramFields.DESCENDANTS, programTypes),
                new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                    @Override
                    public List<Program> call(List<OrganisationUnit> units,
                            List<Program> programs) {
                        Log.d(TAG,
                                "Pull of Programs and OrganisationUnits finished");
                        return programs;
                    }
                })
                .subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        //TODO: uncommnent when merge with branch
                        // feature-new_sdk_update_pull_of_events
                        convertData();
                        //loadDataValues();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling Programs and OrganisationUnits: ",
                                throwable);
                    }
                });
    }

    private static void loadDataValues() {
        asyncDownloads++;
        //Pull events
        pullEventsByProgramAndOrganisationUnit();
    }

    private static void convertData() {
        PullController.getInstance().startConversion();
        postFinish();
    }

    /**
     * This method gets a organisation unit and program for each program(with organisation units)
     * and removes it(it removes the organisation unit and the program without organisation units)
     */
    private static ProgramAndOrganisationUnitDict getProgramAndOrganisationUnit() {
        if (sdkPrograms == null || sdkPrograms.size() == 0 || programsAndOrganisationUnits == null
                || programsAndOrganisationUnits.size() == 0) {
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
     * This method get a list of events by organisationUnit and program, and pull it.
     * Is called recursively to pull, is not working at this moment
     */
    //// FIXME: 16/11/2016  this method is return a timeout exception in the pull of events
    private static void pullEventsByProgramAndOrganisationUnit() {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_events));
        final ProgramAndOrganisationUnitDict programAndOrganisationUnitDict =
                getProgramAndOrganisationUnit();
        if (programAndOrganisationUnitDict == null) {
            asyncDownloads--;
            convertData();
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
                        showError("Error pulling events  by program and org: : ", throwable);
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
                            pullEventsByProgramAndOrganisationUnit();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling events : ", throwable);
                    }
                });
    }

    /**
     * Pull a list of event uids
     *
     * @param eventUids list of event uid to be pull
     */
    private static void pullEvents(Set<String> eventUids) {
        D2.events().pull(eventUids).asObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.d(TAG, "Listed events: " + events.size());
                        asyncDownloads--;
                        convertData();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling events: ", throwable);
                    }
                });
    }


    private static void showError(String errorMessage, Throwable throwable) {
        errorOnPull = true;
        throwable.printStackTrace();
        Log.e(TAG, errorMessage + throwable.getLocalizedMessage());
        showException(errorMessage);
    }

    private static void showException(String message) {
        ProgressActivity.showException(message);
    }
}

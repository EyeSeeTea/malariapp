package org.eyeseetea.malariacare.sdk;

import static org.hisp.dhis.client.sdk.models.program.ProgramType.WITHOUT_REGISTRATION;

import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.controllers.SyncStrategy;
import org.hisp.dhis.client.sdk.core.common.preferences.ResourceType;
import org.hisp.dhis.client.sdk.core.program.ProgramFields;
import org.hisp.dhis.client.sdk.models.attribute.Attribute;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

    private static void loadMetaData() {
        asyncDownloads++;
        //Pull metadata
        pullAttributes();
        pullPrograms();
    }

    private static void loadDataValues() {
        asyncDownloads++;
        //Pull events
        pullEventsByProgramAndOrganisationUnit();
    }

    private static void convertData() {
        if (asyncDownloads == 0) {
            if (!errorOnPull) {
                PullController.getInstance().startConversion();
                postFinish();
            } else {
                pullFail();
            }
        }
    }


    /**
     * Pull the programs and all the metadata
     */
    private static void pullAttributes() {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program));

        D2.attributes().pull()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Attribute>>() {
                    @Override
                    public void call(
                            List<Attribute> attributes) {
                        Log.d(TAG, "Pull of attributes finish"+ attributes.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling attributes: ", throwable);
                    }
                });
    }
    /**
     * Pull the programs and all the metadata
     */
    private static void pullPrograms() {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program));

        Set<ProgramType> programType = new HashSet<ProgramType>();
        programType.add(WITHOUT_REGISTRATION);

        D2.me().programs().pull(ProgramFields.DESCENDANTS, programType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(
                            List<Program> programs) {
                        sdkPrograms = programs;

                        pullProgramStages(SdkModelUtils.getProgramStageUids(programs));
                        Log.d(TAG, "Pull of programs finish");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling programs: ", throwable);
                    }
                });
    }


    /**
     * Pull the ProgramStages and continues the pull with the ProgramStageSections
     */
    private static void pullProgramStages(Set<String> programStagesUids) {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program_stages));
        Observable<List<ProgramStage>> programStageObservable =
                D2.programStages().pull(programStagesUids);
        programStageObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStage>>() {
                    @Override
                    public void call(List<ProgramStage> programStages) {

                        pullProgramStageDataElements(SdkModelUtils.getProgramStageDataElementUids(
                                programStages));

                        pullProgramStageSections(SdkModelUtils.getProgramStageSectionUids(
                                programStages));

                        Log.d(TAG, "Pull of ProgramStage finish");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling ProgramStage: ", throwable);
                    }
                });
    }

    /**
     * Pull the ProgramStageDataSections and continues the pull with the
     * pullProgramStageDataElements
     */
    private static void pullProgramStageSections(Set<String> programStagesSectionUids) {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program_stage_sections));
        Observable<List<ProgramStageSection>> programStageSectionObservable =
                D2.programStageSections().pull(programStagesSectionUids);
        programStageSectionObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageSection>>() {
                    @Override
                    public void call(List<ProgramStageSection> programStageSections) {
                        pullProgramStageDataElements(
                                SdkModelUtils.getProgramStageSectionDataElementUids(
                                        programStageSections));
                        Log.d(TAG, "Pull of ProgramStageSection finish");

                        pullOrganisationUnits();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling ProgramStageSection: ", throwable);
                    }
                });
    }

    /**
     * Pull the ProgramStageDataElements and continues the pull with the pullDataElements
     */
    private static void pullProgramStageDataElements(Set<String> programStagesDataElementsUids) {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_program_stage_dataElements));
        Observable<List<ProgramStageDataElement>> programStageDataElementObservable =
                D2.programStageDataElements().pull(programStagesDataElementsUids);
        programStageDataElementObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ProgramStageDataElement>>() {
                    @Override
                    public void call(List<ProgramStageDataElement> programStageDataElements) {
                        Log.d(TAG, "Pull of ProgramStageDataElements finish");


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling ProgramStageDataElement: ", throwable);
                    }
                });
    }

    /*
     * Pull the OrganisationUnits
     */
    private static void pullOrganisationUnits() {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_organisationUnits));
        Observable<List<OrganisationUnit>> organisationUnitObservable2 =
                D2.me().organisationUnits().pull();
        organisationUnitObservable2.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<OrganisationUnit>>() {
                    @Override
                    public void call(List<OrganisationUnit> organisationUnits) {
                        Log.d(TAG, "OrganisationUnit: Done");
                        asyncDownloads--;
                        if (pullData) {
                            loadDataValues();
                        }
                        convertData();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling OrganisationUnit: ", throwable);
                    }
                });
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

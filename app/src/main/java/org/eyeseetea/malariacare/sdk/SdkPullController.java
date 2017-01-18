package org.eyeseetea.malariacare.sdk;


import android.content.Context;
import android.util.Log;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.preferences.ResourceType;
import org.hisp.dhis.client.sdk.core.program.ProgramFields;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class SdkPullController extends SdkController {

    private static final String TAG = ".SdkPullController";
    static List<Program> sdkPrograms;
    static List<OrganisationUnit> sdkOrganisationUnits;

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
        loadMetaData();
    }


    public static void loadData() {
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
                        Log.d(TAG, "Pull of Programs and OrganisationUnits finished");
                        return programs;
                    }
                })
                .subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Program>>() {
                    @Override
                    public void call(List<Program> programs) {
                        //TODO: uncommnent when merge with branch feature-new_sdk_update_pull_of_events
                        //loadDataValues();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling Programs and OrganisationUnits: ", throwable);
                    }
                });
    }

    private static void loadDataValues() {
        //Pull events
        pullEvents();
    }

    private static void convertData() {
        if (!errorOnPull) {
            Log.d(TAG, "start conversion ");
            try {
                PullController.getInstance().startConversion();
                postFinish();
            } catch (NullPointerException e) {
                pullFail();
            }
        } else {
            pullFail();
        }
    }

    /**
     * This method pull the events and start the conversion of data
     */
    private static void pullEvents() {
        ProgressActivity.step(PreferencesState.getInstance().getContext().getString(
                R.string.progress_push_preparing_events));
        for (Program program : sdkPrograms) {
            for (OrganisationUnit organisationUnit : sdkOrganisationUnits) {
                if (organisationUnit.getPrograms() != null) {
                    for (Program organisationUnitProgram : organisationUnit.getPrograms()) {
                        if (organisationUnitProgram.getUId().equals(program.getUId())) {
                            pullEventsByProgramAndOrganisationUnit(program, organisationUnit);
                            continue;
                        }
                    }
                }
            }
        }
        convertData();
    }

    private static void pullEventsByProgramAndOrganisationUnit(Program program,
            OrganisationUnit organisationUnit) {
        final String organisationUnitUid = organisationUnit.getUId();
        final String programUId = program.getUId();
        //// TODO: 13/01/17 do this call using toBlocking() or similar.
        Observable<List<Event>> eventListObservable = D2.events().pull(
                organisationUnit.getUId(),
                program.getUId());
        eventListObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Event>>() {
                    @Override
                    public void call(List<Event> events) {
                        Log.d(TAG, "Downloaded events with program uid= " + organisationUnitUid
                                + " and orgunit uid:" + programUId + " events size: "
                                + events.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError("Error pulling events  by program and org: ", throwable);
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

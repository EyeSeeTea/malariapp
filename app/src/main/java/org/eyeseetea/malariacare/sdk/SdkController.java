/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.BaseActivity;
import org.eyeseetea.malariacare.EyeSeeTeaApplication;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.ConvertFromSDKVisitor;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.PullController;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.SyncProgressStatus;
import org.eyeseetea.malariacare.database.model.*;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.sdk.models.OrganisationUnitLevel;
import org.eyeseetea.malariacare.sdk.models.ProgramStage;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.core.common.preferences.ResourceType;
import org.eyeseetea.malariacare.sdk.models.*;
import org.eyeseetea.malariacare.sdk.models.Program;
import org.hisp.dhis.client.sdk.models.program.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idelcano on 09/11/2016.
 */

public class SdkController {

    //from pull controller
    public static void register(Context context){
        try {
            Dhis2Application.bus.register(context);
        } catch (Exception e) {
            unregister(context);
            Dhis2Application.bus.register(context);
        }
    }

    public static void unregister(Context context){
        try {
            Dhis2Application.bus.unregister(context);
        } catch (Exception e) {
        }
    }

    public static void setMaxEvents(int maxEvents) {
        TrackerController.setMaxEvents(maxEvents);
    }

    public static void setStartDate(String startDate) {
        TrackerController.setStartDate(startDate);
    }

    public static void setFullOrganisationUnitHierarchy(boolean fullHierarchy) {
        MetaDataController.setFullOrganisationUnitHierarchy(fullHierarchy);
    }

    public static void clearMetaDataLoadedFlags() {
        MetaDataController.clearMetaDataLoadedFlags();
    }

    public static void wipe() {
        MetaDataController.wipe();
    }

    public static void enableMetaDataFlags(Context context) {
        List<ResourceType> resourceTypes = new ArrayList<>();
        resourceTypes.add(ResourceType.ASSIGNEDPROGRAMS);
        resourceTypes.add(ResourceType.PROGRAMS);
        resourceTypes.add(ResourceType.OPTIONSETS);
        resourceTypes.add(ResourceType.EVENTS);
        enableMetaDataFlags(resourceTypes, context);
    }
    public static void enableMetaDataFlags(List<ResourceType> resources, Context context) {
        for(ResourceType resourceType:resources) {
            LoadingController.enableLoading(context, resourceType);
        }
    }

    public static Job loadLastData(Context context) {
        return DhisService.loadLastData(context);
    }

    public static Job loadData(Context context) {
        return DhisService.loadData(context);
    }


    public static void postProgress(String msg) {
        Dhis2Application.getEventBus().post(new SyncProgressStatus(msg));
    }

    public static void postException(Exception ex) {
        Dhis2Application.getEventBus().post(new SyncProgressStatus(ex));
    }

    public static void postFinish() {
        User user = User.getLoggedUser();
        Session.setUser(user);
        try {
            Dhis2Application.getEventBus().post(new SyncProgressStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean finishPullJob() {
        if (JobExecutor.isJobRunning(PullController.job.getJobId())) {
            Log.d(TAG, "Job " + PullController.job.getJobId() + " is running");
            PullController.job.cancel(true);
            try {
                try {JobExecutor.getInstance().dequeueRunningJob(PullController.job);} catch (Exception e) {e.printStackTrace();}
                PullController.job.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return true;
            }
        }
        return false;
    }

    public static List<String> getAssignedPrograms() {
        return MetaDataController.getAssignedPrograms();
    }

    public static Program getProgram(String assignedProgramID) {
        return MetaDataController.getProgram(assignedProgramID);
    }

    public static List<OptionSet> getOptionSets() {
        MetaDataController.getOptionSets();
    }

    public static UserAccount getUserAccount() {
        return etaDataController.getUserAccount();
    }

    public static DataElement getDataElement(DataElement dataElement) {
        return MetaDataController.getDataElement(dataElement.getId());
    }

    public static DataElement getDataElement(String UId) {
        return MetaDataController.getDataElement(UId);
    }

    public static List<OrganisationUnitLevel> getOrganisationUnitLevels() {
        return MetaDataController.getOrganisationUnitLevels();
    }

    public static List<OrganisationUnit> getAssignedOrganisationUnits() {
        return MetaDataController.getAssignedOrganisationUnits();
    }

    public static List<Program> getProgramsForOrganisationUnit(String UId, ProgramType programType) {
        return MetaDataController.getProgramsForOrganisationUnit(UId, programType);
    }

    public static List<Event> getEvents(String organisationUnitUId, String ProgramUId) {
        return TrackerController.getEvents(organisationUnitUId, ProgramUId);
    }

    //ConvertFromSDKVisitor
    public static void saveBatch() {
        //Save questions in batch
        new SaveModelTransaction<>(ProcessModelInfo.withModels(ConvertFromSDKVisitor.questions)).onExecute();

        //Refresh media references
        List<Media> medias = ConvertFromSDKVisitor.questionBuilder.getListMedia();
        for(Media media: medias){
            media.updateQuestion();
        }
        //Save media in batch
        new SaveModelTransaction<>(ProcessModelInfo.withModels(medias)).onExecute();
    }

    public static List<Event> getEventsFromEventsWrapper(JsonNode jsonNode) {
        return  EventsWrapper.getEvents(jsonNode);
    }

    public static ProgramStage getProgramStage(ProgramStageFlow programStage) {
        return MetaDataController.getProgramStage(programStage);
    }

    //BaseActivity
    public static void logOutUser(Activity activity) {
        DhisService.logOutUser(activity);
    }

    public static void sendEventChanges() {
        DhisService.sendEventChanges();
    }


    public static void wipeSDKData() {
        Delete.tables(
                Event.class,
                DataValue.class,
                FailedItem.class
        );
        DateTimeManager.getInstance().delete();
        //Log.d(TAG,"Delete sdk db");
    }

    public static String getDhisDatabaseName() {
        return Dhis2Database.NAME;
    }
}

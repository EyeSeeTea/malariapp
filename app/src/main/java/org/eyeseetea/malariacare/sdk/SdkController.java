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

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.*;
import org.eyeseetea.malariacare.database.utils.Session;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.AttributeFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.DbDhis;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.DataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OptionSetFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.OrganisationUnitFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageSectionFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.UserAccountFlow;

import java.util.List;

/**
 * Created by idelcano on 09/11/2016.
 */

public abstract class SdkController {

    public final static Class[] MANDATORY_METADATA_TABLES = {
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

    //from pull controller
    public static void register(Context context) {
        try {
            //Dhis2Application.bus.register(context);
        } catch (Exception e) {
            unregister(context);
            //Dhis2Application.bus.register(context);
        }
    }

    public static void unregister(Context context) {
        try {
            //Dhis2Application.bus.unregister(context);
        } catch (Exception e) {
        }
    }


    public static void postProgress(String msg) {
        //Dhis2Application.getEventBus().post(new SyncProgressStatus(msg));
    }

    public static void postException(Exception ex) {
        //Dhis2Application.getEventBus().post(new SyncProgressStatus(ex));
    }

    public static void postFinish() {
        User user = User.getLoggedUser();
        Session.setUser(user);
        try {
            //Dhis2Application.getEventBus().post(new SyncProgressStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean finishPullJob() {
        /*
        if (JobExecutor.isJobRunning(PullController.job.getJobId())) {
            Log.d(TAG, "Job " + PullController.job.getJobId() + " is running");
            PullController.job.cancel(true);
            try {
                try {JobExecutor.getInstance().dequeueRunningJob(PullController.job);} catch
                (Exception e) {e.printStackTrace();}
                PullController.job.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return true;
            }
        }
        */
        return false;
    }

    public static List<EventExtended> getEventsFromEventsWrapper(JsonNode jsonNode) {
        /*
        List<EventExtended> eventExtendeds = new ArrayList<>();
        List<EventFlow> eventFlows = EventsWrapper.getEvents(jsonNode);
        for (EventFlow eventFlow:eventFlows){
            eventExtendeds.add(new EventExtended(eventFlow));
        }
        return eventExtendeds;
        */
        return null;
    }


    public static void wipeSDKData() {
        /*Delete.tables(
                EventFlow.class,
                DataValueFlow.class,
                FailedItemFlow.class
        );
        */
        //DateTimeManager.getInstance().delete();
    }

    public static String getDhisDatabaseName() {
        return DbDhis.NAME;
    }

}

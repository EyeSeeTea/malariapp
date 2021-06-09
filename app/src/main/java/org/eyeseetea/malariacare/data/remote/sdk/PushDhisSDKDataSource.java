/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.data.remote.sdk;

import androidx.annotation.NonNull;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.model.ObservationDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushDataController;
import org.eyeseetea.malariacare.data.sync.mappers.PushReportMapper;
import org.eyeseetea.malariacare.domain.entity.pushsummary.PushReport;
import org.eyeseetea.malariacare.domain.exception.push.PushDhisException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.domain.exception.DataToPushNotFoundException;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.StateFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.common.state.Action;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PushDhisSDKDataSource {
    private final String TAG = ".PushControllerB&D";

    public void pushData(final IDataSourceCallback<Map<String, PushReport>> callback,
            PushDataController.Kind kind) {
        pushEvents(callback, kind);
    }

    private void pushEvents(final IDataSourceCallback<Map<String, PushReport>> callback,
            PushDataController.Kind kind) {
        final Set<String> eventUids = getEventUidToBePushed(kind);

        if(eventUids.isEmpty() || eventUids.size()==0){
            callback.onError(new DataToPushNotFoundException("Null events"));
            return;
        }

        org.hisp.dhis.client.sdk.models.common.state.Action action = Action.TO_POST;
        if(kind.equals(PushDataController.Kind.OBSERVATIONS)){
            action = Action.TO_UPDATE;
        }

        Observable<Map<String, ImportSummary>> eventObserver =
                D2.events().push(eventUids, action);

        eventObserver
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Map<String, ImportSummary>>() {
                    @Override
                    public void call(Map<String, ImportSummary> mapEventsImportSummary) {
                        if(mapEventsImportSummary==null){
                            callback.onError(new PushReportException("Error during push"));
                            return;
                        }
                        Log.d(TAG,
                                "Push of events finish. Number of events: "
                                        + mapEventsImportSummary.size());
                        try {
                            callback.onSuccess(PushReportMapper.mapFromImportSummariesToPushReports(mapEventsImportSummary));
                        }catch (NullPointerException e){
                            callback.onError(new PushReportException(e));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onError(new PushDhisException(throwable));
                    }
                });
    }

    @NonNull
    private Set<String> getEventUidToBePushed(PushDataController.Kind kind) {
        final Set<String> eventUids = new HashSet<>();
        final Set<String> sendingEventUids = new HashSet<>();
        if(kind.equals(PushDataController.Kind.EVENTS)) {
            List<SurveyDB> surveys = SurveyDB.getAllSendingSurveys();
            for (SurveyDB survey : surveys) {
                sendingEventUids.add(survey.getEventUid());
            }
        }else if(kind.equals(PushDataController.Kind.OBSERVATIONS)) {
            List<SurveyDB> surveysWithPlans = ObservationDB.getAllSentSurveysWithSendingObservations();
            for (SurveyDB survey : surveysWithPlans) {
                sendingEventUids.add(survey.getEventUid());
            }
        }
        List<EventFlow> eventsFlows = SdkQueries.getEvents();
        Log.d(TAG, "Size of events " + eventsFlows.size() + "size of surveys" + sendingEventUids.size());
        if(sendingEventUids.size()!=eventsFlows.size())
            Log.d(TAG, "Error in size of events");
        for(EventFlow eventFlow:eventsFlows){
            if(eventFlow.getEventDate() !=null && sendingEventUids.contains(eventFlow.getUId())){
                eventUids.add(eventFlow.getUId());
            }
            else {
                Log.d(TAG,
                        "Error pushing events. The event uid: " + eventFlow.getUId() + "haven't eventDate or is not listed to send");
            }
        }
        Log.d(TAG, "Size of valid events " + eventsFlows.size());
        return eventUids;
    }

    public void wipeEvents() {
        Delete.tables(
                EventFlow.class,
                TrackedEntityDataValueFlow.class,
                StateFlow.class
        );
    }
}

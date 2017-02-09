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

package org.eyeseetea.malariacare.data.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.EventFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.StateFlow;
import org.hisp.dhis.client.sdk.android.api.persistence.flow.TrackedEntityDataValueFlow;
import org.hisp.dhis.client.sdk.models.common.importsummary.ImportSummary;
import org.hisp.dhis.client.sdk.models.event.Event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PushDhisSDKDataSource {

    public void pushData(final IDataSourceCallback<Map<String, ImportSummary>> callback) {
        boolean isNetworkAvailable = isNetworkAvailable();

        if (!isNetworkAvailable) {
            callback.onError(new NetworkException());
        } else {
            pushEvents(callback);
        }
    }

    private void pushEvents(final IDataSourceCallback<Map<String, ImportSummary>> callback) {
        final Set<String> eventUids= new HashSet<>();

        for(EventFlow eventFlow:SdkQueries.getEvents()){
                eventUids.add(eventFlow.getUId());
           }
        Observable<Map<String,ImportSummary>> eventObserver =
                       D2.events().push(eventUids);

        eventObserver
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Map<String, ImportSummary>>() {
            @Override
            public void call(Map<String, ImportSummary> mapEventsImportSummary) {
                Log.d(this.getClass().getSimpleName(),
                        "Push of events finish. Number of events: "
                                + mapEventsImportSummary.size());

                //TODO: from data source should comverto always from SDK object to domain object
                // this class should not return sdk objects directly
                //create a object similar to Map<String,ImportSummary> in domain and convert before
                // to invoke callback.onSuccess
                callback.onSuccess(mapEventsImportSummary);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

                callback.onError(throwable);

                Log.e(this.getClass().getSimpleName(),
                        "Error pushing Events: " + throwable.getLocalizedMessage());
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) PreferencesState.getInstance().getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void wipeEvents() {
        Delete.tables(
                EventFlow.class,
                TrackedEntityDataValueFlow.class,
                StateFlow.class
        );
    }
}

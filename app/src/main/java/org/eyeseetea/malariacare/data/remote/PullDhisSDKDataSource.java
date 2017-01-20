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

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.IPullDataSource;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.exception.InvalidCredentialsException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.PullException;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.controllers.SyncStrategy;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.program.ProgramFields;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


public class PullDhisSDKDataSource implements IPullDataSource {

    @Override
    public void pullMetadata(final IDataSourceCallback<Void> callback) {

        boolean isNetworkAvailable = isNetworkAvailable();

        if (!isNetworkAvailable)
            callback.onError(new NetworkException());
        else {
            Set<ProgramType> programTypes = new HashSet<>();
            programTypes.add(ProgramType.WITHOUT_REGISTRATION);

            Observable.zip(D2.me().organisationUnits().pull(SyncStrategy.NO_DELETE),
                    D2.me().programs().pull(SyncStrategy.NO_DELETE, ProgramFields.DESCENDANTS,
                            programTypes),
                    new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                        @Override
                        public List<Program> call(List<OrganisationUnit> organisationUnits,
                                List<Program> programs) {
                            return programs;
                        }
                    })
                    .subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Program>>() {
                        @Override
                        public void call(List<Program> programs) {
                            callback.onSuccess(null);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Throwable throwableResult = mapThrowable(throwable);

                            callback.onError(throwableResult);
                        }
                    });
        }

    }

    @Override
    public void pullData(IDataSourceCallback<Void> callback) {
        boolean isNetworkAvailable = isNetworkAvailable();

        if (!isNetworkAvailable)
            callback.onError(new NetworkException());
        else {
            try {
                pullEvents();
                callback.onSuccess(null);
            }catch (Exception e){
                Throwable throwableResult = mapThrowable(e);

                callback.onError(throwableResult);
            }
        }
    }

    private static void pullEvents() {
        Scheduler listThread = Schedulers.newThread();
        List<Program> sdkPrograms = D2.me().programs().list().subscribeOn(listThread)
                .observeOn(listThread).toBlocking().single();
        List<OrganisationUnit> sdkOrganisationUnits =
                D2.me().organisationUnits().list().subscribeOn(listThread)
                        .observeOn(listThread).toBlocking().single();
        for (Program program : sdkPrograms) {
            for (OrganisationUnit organisationUnit : sdkOrganisationUnits) {
                for (Program orgunitProgram : organisationUnit.getPrograms()) {
                    if (orgunitProgram.getUId().equals(program.getUId())) {
                        Scheduler pullEventsThread = Schedulers.newThread();
                        D2.events().pull(
                                organisationUnit.getUId(),
                                program.getUId()).subscribeOn(pullEventsThread)
                                .observeOn(pullEventsThread).toBlocking().single();
                    }
                }
            }
        }
    }


    private boolean isNetworkAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) PreferencesState.getInstance().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private Throwable mapThrowable(Throwable throwable) {
        Throwable throwableResult = throwable;

        if (throwable.getCause() != null) {
            throwableResult = throwable.getCause();
        } else if (throwable instanceof ApiException) {
            ApiException apiException = (ApiException) throwable;

            if (apiException.getResponse() != null
                    && apiException.getResponse().getStatus()
                    == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throwableResult = new InvalidCredentialsException();
            }
        }
        else{
            throwableResult = new PullException();
        }

        return throwableResult;
    }
}

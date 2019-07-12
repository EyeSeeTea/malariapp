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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.exception.InvalidCredentialsException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;

import java.net.HttpURLConnection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UserAccountDhisSDKDataSource implements IUserAccountDataSource {
    private Context mContext;

    public UserAccountDhisSDKDataSource(Context context) {
        mContext = context;
    }

    @Override
    public void logout(final IDataSourceCallback<Void> callback) {
        D2.me().signOut()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        callback.onSuccess(null);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        callback.onError(throwable);
                    }
                });
    }

    @Override
    public void login(final Credentials credentials,
            final IDataSourceCallback<UserAccount> callback) {

        boolean isNetworkAvailable = isNetworkAvailable();

        if (!isNetworkAvailable) {
            callback.onError(new NetworkException());
        } else {

            Configuration configuration = new Configuration(credentials.getServerURL());

            D2.configure(configuration)
                    .flatMap(
                            new Func1<Void, Observable<org.hisp.dhis.client.sdk.models.user
                                    .UserAccount>>() {
                                @Override
                                public Observable<org.hisp.dhis.client.sdk.models.user.UserAccount>
                                call(
                                        Void aVoid) {
                                    return D2.me().signIn(credentials.getUsername(),
                                            credentials.getPassword());
                                }
                            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<org.hisp.dhis.client.sdk.models.user.UserAccount>() {
                        @Override
                        public void call(
                                org.hisp.dhis.client.sdk.models.user.UserAccount dhisUserAccount) {
                            UserAccount userAccount = new UserAccount(credentials.getUsername(),
                                    dhisUserAccount.getUId());
                            callback.onSuccess(userAccount);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

        return throwableResult;
    }
}
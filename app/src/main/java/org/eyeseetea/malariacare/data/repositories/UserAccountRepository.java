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

package org.eyeseetea.malariacare.data.repositories;

import android.content.Context;

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.IUserAccountDataSource;
import org.eyeseetea.malariacare.data.database.datasources.UserAccountLocalDataSource;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.sdk.UserAccountDhisSDKDataSource;
import org.eyeseetea.malariacare.domain.boundary.IRepositoryCallback;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class UserAccountRepository implements IUserAccountRepository {
    IUserAccountDataSource userAccountLocalDataSource;
    IUserAccountDataSource userAccountRemoteDataSource;

    public UserAccountRepository(Context context) {

        userAccountLocalDataSource = new UserAccountLocalDataSource(context);
        userAccountRemoteDataSource = new UserAccountDhisSDKDataSource(context);
    }

    @Override
    public void login(final Credentials credentials,
            final IRepositoryCallback<UserAccount> callback) {
        if (credentials.isDemoCredentials()) {
            localLogin(credentials, callback);
        } else {
            remoteLogin(credentials, callback);
        }
    }

    @Override
    public void logout(final IRepositoryCallback<Void> callback) {

        //TODO: jsanchez fix find out IsDemo from current UserAccount getting from DataSource
        Credentials credentials = Session.getCredentials();

        if (credentials.isDemoCredentials()) {
            localLogout(callback);
        } else {
            remoteLogout(callback);
        }
    }

    private void remoteLogout(final IRepositoryCallback<Void> callback) {
        userAccountRemoteDataSource.logout(new IDataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localLogout(callback);
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    private void remoteLogin(final Credentials credentials,
            final IRepositoryCallback<UserAccount> callback) {
        userAccountRemoteDataSource.login(credentials, new IDataSourceCallback<UserAccount>() {
            @Override
            public void onSuccess(UserAccount result) {
                credentials.setUserUid(result.getUserUid());
                localLogin(credentials, callback);
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    private void localLogout(final IRepositoryCallback<Void> callback) {
        userAccountLocalDataSource.logout(new IDataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }

    private void localLogin(Credentials credentials,
            final IRepositoryCallback<UserAccount> callback) {
        userAccountLocalDataSource.login(credentials, new IDataSourceCallback<UserAccount>() {
            @Override
            public void onSuccess(UserAccount userAccount) {
                callback.onSuccess(userAccount);
            }

            @Override
            public void onError(Throwable throwable) {
                callback.onError(throwable);
            }
        });
    }
}

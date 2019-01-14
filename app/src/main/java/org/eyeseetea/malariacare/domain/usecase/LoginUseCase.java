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

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.IRepositoryCallback;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.data.IServerInfoDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.exception.InvalidCredentialsException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class LoginUseCase implements UseCase{

    public interface Callback {
        void onLoginSuccess();

        void onServerURLNotValid();

        void onInvalidCredentials();

        void onNetworkError();

        void onServerVersionError();
    }

    private IMainExecutor mMainExecutor;
    private IAsyncExecutor mAsyncExecutor;
    private IUserAccountRepository mUserAccountRepository;
    private IServerInfoDataSource mServerVersionDataSource;
    private Credentials credentials;
    private int apiMinimalVersion;
    private Callback callback;

    public LoginUseCase(IUserAccountRepository userAccountRepository,
                        IServerInfoDataSource serverVersionDataSource,
                        IMainExecutor mainExecutor,
                        IAsyncExecutor asyncExecutor) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mUserAccountRepository = userAccountRepository;
        mServerVersionDataSource = serverVersionDataSource;
    }

    public void execute(Credentials credentials, int apiMinimalVersion, final Callback callback) {
        this.credentials = credentials;
        this.apiMinimalVersion = apiMinimalVersion;
        this.callback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        if(isValidServerVersion()){
            mUserAccountRepository.login(credentials,
                    new IRepositoryCallback<UserAccount>() {
                        @Override
                        public void onSuccess(UserAccount userAccount) {
                            notifyOnLoginSuccess();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            if (throwable instanceof MalformedURLException
                                    || throwable instanceof UnknownHostException) {
                                notifyOnServerURLNotValid();
                            } else if (throwable instanceof InvalidCredentialsException) {
                                notifyOnInvalidCredentials();
                            } else if (throwable instanceof NetworkException) {
                                notifyOnNetworkError();
                            }
                        }
                    });
        } else {
            notifyOnServerVersionError();
        }
    }

    private boolean isValidServerVersion() {
        boolean isValidServer = false;
        if(credentials.isDemoCredentials()) {
            isValidServer = true;
        } else {
            ServerInfo serverInfo = mServerVersionDataSource.get();
            if(serverInfo.getVersion() <= apiMinimalVersion){
                isValidServer = true;
            }
        }
        return isValidServer;
    }

    private void notifyOnLoginSuccess() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onLoginSuccess();
            }
        });
    }

    private void notifyOnServerURLNotValid() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onServerURLNotValid();
            }
        });
    }
    private void notifyOnInvalidCredentials() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onInvalidCredentials();
            }
        });
    }
    private void notifyOnNetworkError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onNetworkError();
            }
        });
    }
    private void notifyOnServerVersionError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onServerVersionError();
            }
        });
    }
}


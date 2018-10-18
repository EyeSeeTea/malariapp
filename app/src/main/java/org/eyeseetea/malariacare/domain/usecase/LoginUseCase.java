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
import org.eyeseetea.malariacare.domain.boundary.repositories.IAuthenticationManager;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.exception.InvalidCredentialsException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class LoginUseCase implements UseCase {
    public interface Callback {
        void onLoginSuccess();

        void onServerURLNotValid();

        void onInvalidCredentials();

        void onNetworkError();
    }

    private IAuthenticationManager mUserAccountRepository;
    private IAsyncExecutor mAsyncExecutor;
    private IMainExecutor mMainExecutor;
    private Callback mCallback;
    private Credentials mCredentials;

    public LoginUseCase(IAuthenticationManager userAccountRepository, IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mUserAccountRepository = userAccountRepository;
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
    }

    @Override
    public void run() {
        mUserAccountRepository.login(mCredentials,
                new IRepositoryCallback<UserAccount>() {
                    @Override
                    public void onSuccess(UserAccount userAccount) {
                        mCallback.onLoginSuccess();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (throwable instanceof MalformedURLException
                                || throwable instanceof UnknownHostException) {
                            mMainExecutor.run(new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onServerURLNotValid();
                                }
                            });
                        } else if (throwable instanceof InvalidCredentialsException) {
                            mMainExecutor.run(new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onInvalidCredentials();
                                }
                            });
                        } else if (throwable instanceof NetworkException) {
                            mMainExecutor.run(new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onNetworkError();
                                }
                            });
                        }
                    }
                });
    }

    public void execute(Credentials credentials, final Callback callback) {
        mCallback = callback;
        mCredentials = credentials;
        mAsyncExecutor.run(this);
    }

}


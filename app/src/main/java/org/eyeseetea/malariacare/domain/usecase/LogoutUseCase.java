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

public class LogoutUseCase implements UseCase {
    public interface Callback {
        void onLogoutSuccess();

        void onLogoutError(String message);
    }

    private IAuthenticationManager mUserAccountRepository;
    private IAsyncExecutor mAsyncExecutor;
    private IMainExecutor mMainExecutor;
    private Callback mCallback;

    public LogoutUseCase(IAuthenticationManager userAccountRepository, IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mUserAccountRepository = userAccountRepository;
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
    }

    public void execute(final Callback callback) {
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        mUserAccountRepository.logout(
                new IRepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        notifyOnLogoutSucces();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        notifyOnLogoutError(throwable);
                    }
                });
    }

    private void notifyOnLogoutSucces() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onLogoutSuccess();
            }
        });
    }

    private void notifyOnLogoutError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onLogoutError(throwable.getMessage());
            }
        });
    }
}

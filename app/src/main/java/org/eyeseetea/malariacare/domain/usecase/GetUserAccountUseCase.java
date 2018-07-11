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

import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class GetUserAccountUseCase implements UseCase {

    public interface Callback {
        void onSuccess(UserAccount user);

        void onError();
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IUserAccountRepository userAccountRepository;
    private Callback mCallback;
    private NetworkStrategy networkStrategy;

    public GetUserAccountUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IUserAccountRepository userAccountRepository) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.userAccountRepository = userAccountRepository;
    }

    public void execute( NetworkStrategy networkStrategy, final Callback callback) {
        this.mCallback = callback;
        this.networkStrategy = networkStrategy;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        UserAccount updatedUserAccount = null;
        try {
            updatedUserAccount = userAccountRepository.getUser(networkStrategy);
        } catch (Exception e){
            notifyError();
        }
        notifyOnComplete(updatedUserAccount);
    }

    private void notifyOnComplete(final UserAccount userAccount) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(userAccount);
            }
        });
    }

    private void notifyError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onError();
            }
        });
    }
}

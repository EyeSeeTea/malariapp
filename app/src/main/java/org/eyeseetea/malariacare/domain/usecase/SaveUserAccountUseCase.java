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

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;

public class SaveUserAccountUseCase implements UseCase {

    public interface Callback {
        void onSuccess();
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IUserAccountRepository userAccountRepository;
    private Callback mCallback;
    private UserAccount userAccount;

    public SaveUserAccountUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IUserAccountRepository userAccountRepository) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.userAccountRepository = userAccountRepository;
    }

    public void execute(final Callback callback, UserAccount userAccount) {
        this.mCallback = callback;
        this.userAccount = userAccount;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        userAccountRepository.saveUser(userAccount);
        notifyOnComplete();
    }

    private void notifyOnComplete() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess();
            }
        });
    }
}

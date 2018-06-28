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

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IUserAttributesRemoteDataSource;
import org.eyeseetea.malariacare.domain.boundary.IUserLocalDataSource;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.UserAccount;
import org.eyeseetea.malariacare.domain.entity.UserAttributes;
import org.eyeseetea.malariacare.domain.exception.PullUserAttributesException;

public class PullLoggedUserAttributesUseCase implements UseCase {

    public interface Callback {
        void onSuccess(UserAttributes userAttributes);

        void onError();
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IUserLocalDataSource userLocalDataSource;
    private final IUserAttributesRemoteDataSource userAttributesRemoteDataSource;
    private Callback mCallback;

    public PullLoggedUserAttributesUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IUserLocalDataSource userLocalDataSource, IUserAttributesRemoteDataSource userAttributesRemoteDataSource) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.userLocalDataSource = userLocalDataSource;
        this.userAttributesRemoteDataSource = userAttributesRemoteDataSource;
    }

    public void execute(final Callback callback) {
        this.mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        UserAccount userAccount = userLocalDataSource.getLoggedUser();
        UserAttributes userAttributes;

        try {
            userAttributes = userAttributesRemoteDataSource.getUser(userAccount.getUserUid());
        }catch (PullUserAttributesException pullException){
            pullException.printStackTrace();
            mCallback.onError();
            return;
        }

        if(userAttributes.getAnnouncement()!=null && !userAttributes.getAnnouncement().isEmpty()
                && !userAttributes.getAnnouncement().equals(userAccount.getUserAttributes().getAnnouncement())){
            PreferencesState.getInstance().setUserAccept(false);
        }
        userAccount.setUserAttributes(userAttributes);
        userLocalDataSource.saveUser(userAccount);
        notifyOnComplete(userAttributes);
    }

    private void notifyOnComplete(final UserAttributes userAttributes) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(userAttributes);
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

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

import org.eyeseetea.malariacare.data.NetworkStrategy;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.boundary.IUserAccountRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.UserAccount;

public class GetUserAccountUseCase implements UseCase {

    public interface Callback {
        void onSuccess(UserAccount user);

        void onError(UserAccount user);
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IUserAccountRepository userAccountRepository;
    private Callback mCallback;

    public GetUserAccountUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IUserAccountRepository userAccountRepository) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.userAccountRepository = userAccountRepository;
    }

    public void execute(final Callback callback) {
        this.mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        //get logged user
        UserFilter userFilter = new UserFilter("", true);
        UserAccount localUserAccount = userAccountRepository.getUser(userFilter, NetworkStrategy.LocalFirst);
        //set logged user uid in User Filter and get user from network
        userFilter = new UserFilter(localUserAccount.getUserUid(), false);
        UserAccount updatedUserAccount = userAccountRepository.getUser(userFilter, NetworkStrategy.NetworkFirst);
        if(updatedUserAccount==null){
            notifyError(localUserAccount);
            return;
        }
        //set user accept as false if announcement was changed
        if(localUserAccount.getAnnouncement()!=null && !localUserAccount.getAnnouncement().isEmpty()
                && !localUserAccount.getAnnouncement().equals(updatedUserAccount.getAnnouncement())){
            PreferencesState.getInstance().setUserAccept(false);
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

    private void notifyError(final UserAccount userAccount) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(userAccount);
            }
        });
    }
}

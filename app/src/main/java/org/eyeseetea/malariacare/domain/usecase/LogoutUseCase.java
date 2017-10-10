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
import org.eyeseetea.malariacare.domain.boundary.repositories.IUserAccountRepository;

public class LogoutUseCase {
    public interface Callback {
        void onLogoutSuccess();

        void onLogoutError(String message);
    }

    private IUserAccountRepository mUserAccountRepository;

    public LogoutUseCase(IUserAccountRepository userAccountRepository) {
        mUserAccountRepository = userAccountRepository;
    }

    public void execute(final Callback callback) {
        mUserAccountRepository.logout(
                new IRepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        callback.onLogoutSuccess();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onLogoutError(throwable.getMessage());
                    }
                });
    }
}

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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;

public class LoadCredentialsUseCase implements  UseCase{

    public interface Callback {
        void onSuccess(Credentials credentials);
    }

    Context mContext;
    private IAsyncExecutor mAsyncExecutor;
    private IMainExecutor mMainExecutor;
    private Callback mCallback;

    public LoadCredentialsUseCase(Context context, IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mContext = context;
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
    }


    @Override
    public void run() {
        loadCredentials();
    }

    public void execute(Callback callback) {
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    private void loadCredentials() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);

        String serverURL = sharedPreferences.getString(mContext.getString(R.string.dhis_url), "");
        String username = sharedPreferences.getString(mContext.getString(R.string.dhis_user), "");
        String password = sharedPreferences.getString(mContext.getString(R.string.dhis_password),
                "");

        final Credentials credentials = new Credentials(serverURL, username, password);

        Session.setCredentials(credentials);
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(credentials);
            }
        });
    }


}

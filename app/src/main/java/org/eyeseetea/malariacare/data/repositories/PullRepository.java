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

import org.eyeseetea.malariacare.data.IDataSourceCallback;
import org.eyeseetea.malariacare.data.IPullDataSource;
import org.eyeseetea.malariacare.data.remote.PullDhisSDKDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPullRepository;
import org.eyeseetea.malariacare.domain.boundary.IRepositoryCallback;

public class PullRepository implements IPullRepository {
    IPullDataSource pullRemoteDataSource;

    public PullRepository() {
        pullRemoteDataSource = new PullDhisSDKDataSource();
    }

    @Override
    public void pullMetadata(final IRepositoryCallback<Void> callback) {
        pullRemoteDataSource.pullMetadata(new IDataSourceCallback<Void>() {
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

    @Override
    public void pullData(final IRepositoryCallback<Void> callback) {
        pullRemoteDataSource.pullData(new IDataSourceCallback<Void>() {
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
}

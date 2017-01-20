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

import org.eyeseetea.malariacare.domain.boundary.IPullRepository;
import org.eyeseetea.malariacare.domain.boundary.IRepositoryCallback;
import org.eyeseetea.malariacare.domain.exception.InvalidCredentialsException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.PullException;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class PullUseCase {

    public interface Callback {
        void onSuccess();

        void onPullError();

        void onServerURLNotValid();

        void onInvalidCredentials();

        void onNetworkError();
    }

    String mStartDate;
    boolean fullHierarchy;
    boolean downloadOnlyLastEvents;
    int maxEvents;

    private IPullRepository mPullRepository;

    public PullUseCase(IPullRepository pullRepository) {
        mPullRepository = pullRepository;
    }

    public void setStartDate(String startDate) {
        mStartDate = startDate;
    }

    public void setFullOrganisationUnitHierarchy(boolean fullHierarchy) {
        this.fullHierarchy = fullHierarchy;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public void setDownloadOnlyLastEvents(boolean downloadOnlyLastEvents) {
        this.downloadOnlyLastEvents = downloadOnlyLastEvents;
    }

    public void pullMetadata(final Callback callback) {
        //// TODO: 20/01/17 include fullHierarchy functionality
        mPullRepository.pullMetadata(new IRepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess();
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof MalformedURLException
                        || throwable instanceof UnknownHostException) {
                    callback.onServerURLNotValid();
                } else if (throwable instanceof InvalidCredentialsException) {
                    callback.onInvalidCredentials();
                } else if (throwable instanceof NetworkException) {
                    callback.onNetworkError();
                } else if (throwable instanceof PullException) {
                    callback.onPullError();
                }
            }
        });
    }

    public void pullData(final Callback callback) {
        //// TODO: 20/01/17 include downloadOnlyLastEvents, maxEvents and mStartDate functionality
        mPullRepository.pullData(new IRepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess();
            }

            @Override
            public void onError(Throwable throwable) {
                if (throwable instanceof MalformedURLException
                        || throwable instanceof UnknownHostException) {
                    callback.onServerURLNotValid();
                } else if (throwable instanceof InvalidCredentialsException) {
                    callback.onInvalidCredentials();
                } else if (throwable instanceof NetworkException) {
                    callback.onNetworkError();
                } else if (throwable instanceof PullException) {
                    callback.onPullError();
                }
            }
        });
    }
}

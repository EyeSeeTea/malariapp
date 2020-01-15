/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer;

import android.util.Log;

import org.eyeseetea.malariacare.data.IPullSourceCallback;
import org.eyeseetea.malariacare.data.database.datasources.ConversionLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.UserDB;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.sdk.PullDhisSDKDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPullController;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.InvalidServerMetadataException;
import org.eyeseetea.malariacare.domain.exception.PullException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;

public class PullController implements IPullController {
    /**
     * Used for control new steps
     */
    public static Boolean PULL_IS_ACTIVE = false;
    private final String TAG = ".PullController";
    ConversionLocalDataSource conversionLocalDataSource;
    PullDhisSDKDataSource pullRemoteDataSource;
    IPullControllerCallback callback;

    IServerMetadataRepository serverMetadataRepository;
    ServerMetadata serverMetadata;

    public PullController(IServerMetadataRepository serverMetadataRepository) {
        this.serverMetadataRepository = serverMetadataRepository;
    }

    public void conversions() {

        conversionLocalDataSource.convertFromSDK();

        conversionLocalDataSource.validateCS();
    }

    /**
     * Notifies that the pull is over
     */
    public void postFinish() {
        UserDB user = UserDB.getLoggedUser();
        if (user == null) {
            user = new UserDB();
            user.save();
        }
        Session.setUser(user);
        if (!PULL_IS_ACTIVE) {
            callback.onError(new PullException());
        } else {
            callback.onComplete();
        }
    }

    @Override
    public void pull(final PullFilters filters, final IPullControllerCallback callback) {
        conversionLocalDataSource.wipeDataBase();
        PULL_IS_ACTIVE = true;
        this.callback = callback;
        conversionLocalDataSource = new ConversionLocalDataSource(callback, serverMetadataRepository);
        pullRemoteDataSource = new PullDhisSDKDataSource();
        pullRemoteDataSource.wipeDataBase();

        callback.onStep(PullStep.PROGRAMS);

        pullRemoteDataSource.pullMetadata(new IPullSourceCallback() {

            @Override
            public void onComplete() {
                if (!PULL_IS_ACTIVE) {
                    callback.onCancel();
                    return;
                }
                callback.onStep(PullStep.EVENTS);

                pullData(filters);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }

        });
    }

    private void pullData(final PullFilters filters) {
        pullRemoteDataSource.pullData(filters, new IPullSourceCallback() {
            @Override
            public void onComplete() {
                try {
                    if (!PULL_IS_ACTIVE) {
                        callback.onCancel();
                        return;
                    }
                    if (!pullRemoteDataSource
                            .mandatoryMetadataTablesNotEmpty()) {
                        callback.onError(new
                                ConversionException());
                        return;
                    }
                    try {
                        if (!PULL_IS_ACTIVE) {
                            callback.onCancel();
                            return;
                        }
                        conversions();
                    } catch (Exception e) {
                        callback.onError(new
                                ConversionException(e));
                        return;
                    }

                    if (PULL_IS_ACTIVE) {
                        Log.d(TAG, "PULL process...OK");
                        callback.onComplete();
                    } else {
                        callback.onCancel();
                    }
                } catch (NullPointerException e) {
                    callback.onError(new
                            ConversionException(e));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }

        });
    }

    @Override
    public void cancel() {
        PULL_IS_ACTIVE = false;
    }

    @Override
    public boolean isPullActive() {
        return PULL_IS_ACTIVE;
    }
}

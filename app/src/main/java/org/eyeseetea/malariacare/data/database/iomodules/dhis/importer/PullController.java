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

import org.eyeseetea.malariacare.data.IDhisPullSourceCallback;
import org.eyeseetea.malariacare.data.database.datasources.ConversionLocalDataSource;
import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.remote.PullDhisSDKDataSource;
import org.eyeseetea.malariacare.domain.boundary.IPullController;
import org.eyeseetea.malariacare.domain.boundary.IPullControllerCallback;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.PullException;
import org.eyeseetea.malariacare.domain.usecase.pull.PullFilters;
import org.eyeseetea.malariacare.domain.usecase.pull.PullStep;

/**
 * A static controller that orchestrate the pull process
 * Created by arrizabalaga on 4/11/15.
 */
public class PullController implements IPullController {
    private final String TAG = ".PullController";

    private static PullController instance;

    ConversionLocalDataSource conversionLocalDataSource;
    PullDhisSDKDataSource pullRemoteDataSource;

    IPullControllerCallback callback;
    /**
     * Used for control new steps
     */
    public static Boolean PULL_IS_ACTIVE = false;

    /**
     * Constructs and register this pull controller to the event bus
     */
    public PullController() {
    }

    /**
     * Singleton constructor
     */
    public static PullController getInstance() {
        if (instance == null) {
            instance = new PullController();
        }
        return instance;
    }

    public void conversions() {

        conversionLocalDataSource.convertFromSDK();

        conversionLocalDataSource.validateCS();

        if (PULL_IS_ACTIVE) {
            Log.d(TAG, "PULL process...OK");
            postFinish();
        } else {
            this.callback.onCancel();
        }
    }


    /**
     * Notifies that the pull is over
     */
    public void postFinish() {
        User user = User.getLoggedUser();
        if (user == null) {
            user = new User();
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
        PULL_IS_ACTIVE = true;
        conversionLocalDataSource = new ConversionLocalDataSource(callback);
        pullRemoteDataSource = new PullDhisSDKDataSource();
        pullRemoteDataSource.wipeDataBase();
        conversionLocalDataSource.wipeDataBase();

        this.callback = callback;
        callback.onStep(PullStep.PROGRAMS);

        pullRemoteDataSource.pullMetadata(new IDhisPullSourceCallback() {

            @Override
            public void onComplete() {
                callback.onStep(PullStep.EVENTS);
                if (!PULL_IS_ACTIVE) {
                    callback.onCancel();
                    return;
                }
                pullData();
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                callback.onError(throwable);
            }

            @Override
            public void onStep(PullStep pullStep) {
                callback.onStep(pullStep);
            }

        });
    }

    private void pullData() {
        pullRemoteDataSource.pullData(new IDhisPullSourceCallback() {
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
                        callback.onStep(PullStep.PREPARING_PROGRAMS);
                        conversions();
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError(e);
                        return;
                    }
                    if (!PULL_IS_ACTIVE) {
                        callback.onCancel();
                        return;
                    }
                    callback.onComplete();
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

            @Override
            public void onStep(PullStep pullStep) {

            }

        });
    }

    @Override
    public void cancel() {
        PULL_IS_ACTIVE = false;
        pullRemoteDataSource.cancel();
        conversionLocalDataSource.cancel();
    }

    @Override
    public boolean isPullActive() {
        return PULL_IS_ACTIVE;
    }
}

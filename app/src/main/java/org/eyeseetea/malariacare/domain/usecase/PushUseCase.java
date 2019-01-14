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

import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerInfoDataSource;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.SurveysToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.data.remote.SurveyChecker;

public class PushUseCase implements UseCase {

    private IPushController mPushController;
    private IServerInfoDataSource mServerVersionDataSource;
    private Credentials credentials;
    private int apiMinimalVersion;

    private IMainExecutor mMainExecutor;
    private IAsyncExecutor mAsyncExecutor;
    private Callback callback;

    public PushUseCase(IPushController pushController,
                       IMainExecutor mainExecutor,
                       IAsyncExecutor asyncExecutor,
                       IServerInfoDataSource serverVersionDataSource) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mPushController = pushController;
        mServerVersionDataSource = serverVersionDataSource;
    }

    public void execute(Credentials credentials, int apiMinimalVersion, final Callback callback) {
        this.credentials = credentials;
        this.apiMinimalVersion = apiMinimalVersion;
        this.callback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        if (mPushController.isPushInProgress()) {
            callback.onPushInProgressError();
            return;
        }
        if(!isValidServerVersion()){
            callback.onServerVersionError();
            return;
        }
        mPushController.changePushInProgress(true);

        SurveyChecker.launchQuarantineChecker();

        mPushController.push(new IPushController.IPushControllerCallback() {
            @Override
            public void onComplete(PushController.Kind kind) {
                System.out.println("PushUseCase Complete");

                mPushController.changePushInProgress(false);

                notifyOnComplete(kind);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("PushUseCase error");

                if (throwable instanceof NetworkException) {
                    mPushController.changePushInProgress(false);
                    notifyOnNetworkError();
                } else if (throwable instanceof ConversionException) {
                    mPushController.changePushInProgress(false);
                    notifyOnConversionError();
                } else if (throwable instanceof SurveysToPushNotFoundException) {
                    mPushController.changePushInProgress(false);
                    notifyOnSurveysNotFoundError();
                } else if (throwable instanceof PushReportException){
                    mPushController.changePushInProgress(false);
                    notifyOnPushError();
                } else {
                    mPushController.changePushInProgress(false);
                    notifyOnPushError();
                }
            }

            @Override
            public void onInformativeError(Throwable throwable) {
                notifyOnInformativeError(throwable);
            }
        });

    }

    private boolean isValidServerVersion() {
        boolean isValidServer = false;
        if(credentials.isDemoCredentials()) {
            isValidServer = true;
        } else {
            ServerInfo serverInfo = mServerVersionDataSource.get();
            if(serverInfo.getVersion() <= apiMinimalVersion){
                isValidServer = true;
            }
        }
        return isValidServer;
    }

    private void notifyOnComplete(final PushController.Kind kind) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(kind);
            }
        });
    }

    private void notifyOnPushError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onPushError();
            }
        });
    }

    private void notifyOnSurveysNotFoundError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onSurveysNotFoundError();
            }
        });
    }

    private void notifyOnConversionError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onConversionError();
            }
        });
    }

    private void notifyOnNetworkError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onNetworkError();
            }
        });
    }

    private void notifyOnInformativeError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onInformativeError(throwable.getMessage());
            }
        });
    }

    public interface Callback {
        void onComplete(PushController.Kind kind);

        void onPushError();

        void onPushInProgressError();

        void onSurveysNotFoundError();

        void onInformativeError(String message);

        void onConversionError();

        void onNetworkError();

        void onServerVersionError();
    }
}

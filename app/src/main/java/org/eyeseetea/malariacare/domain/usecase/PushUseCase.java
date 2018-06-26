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

import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.MetadataException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.SurveysToPushNotFoundException;
import org.eyeseetea.malariacare.domain.exception.push.PushReportException;
import org.eyeseetea.malariacare.data.remote.SurveyChecker;

public class PushUseCase implements UseCase{

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private IPushController mPushController;
    private Callback mCallback;

    public PushUseCase(IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IPushController pushController) {
        mAsyncExecutor = asyncExecutor;
        mMainExecutor = mainExecutor;
        mPushController = pushController;
    }

    public void execute(final Callback callback) {
        this.mCallback = callback;

        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        if (mPushController.isPushInProgress()) {
            notifyOnPushInProgressError();
            return;
        }
        mPushController.changePushInProgress(true);

        SurveyChecker.launchQuarantineChecker();

        mPushController.push(new IPushController.IPushControllerCallback() {
            @Override
            public void onComplete() {
                System.out.println("PusUseCase Complete");

                mPushController.changePushInProgress(false);

                notifyOnComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("PusUseCase error");

                notifyOnError(throwable);
            }

            @Override
            public void onInformativeError(Throwable throwable) {
                notifyOnInformativeError(throwable.getMessage());
            }
        });
    }


    private void notifyOnComplete() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete();
            }
        });
    }

    private void notifyOnError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                if (throwable instanceof NetworkException) {
                    mPushController.changePushInProgress(false);
                    mCallback.onNetworkError();
                } else if (throwable instanceof ConversionException) {
                    mPushController.changePushInProgress(false);
                    mCallback.onConversionError();
                } else if (throwable instanceof SurveysToPushNotFoundException) {
                    mPushController.changePushInProgress(false);
                    mCallback.onSurveysNotFoundError();
                } else if (throwable instanceof PushReportException){
                    mPushController.changePushInProgress(false);
                    mCallback.onPushError();
                } else {
                    mPushController.changePushInProgress(false);
                    mCallback.onPushError();
                }
            }
        });
    }

    private void notifyOnInformativeError(final String message) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onInformativeError(message);
            }
        });
    }

    private void notifyOnPushInProgressError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onPushInProgressError();
            }
        });
    }

    public interface Callback {
        void onComplete();

        void onPushError();

        void onPushInProgressError();

        void onSurveysNotFoundError();

        void onInformativeError(String message);

        void onConversionError();

        void onNetworkError();
    }
}

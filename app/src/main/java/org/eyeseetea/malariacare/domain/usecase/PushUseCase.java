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
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.exception.PushReportException;
import org.eyeseetea.malariacare.domain.exception.PushValueException;
import org.eyeseetea.malariacare.domain.exception.SurveysToPushNotFoundException;
import org.eyeseetea.malariacare.network.SurveyChecker;

public class PushUseCase {

    private IPushController mPushController;

    public PushUseCase(IPushController pushController) {
        mPushController = pushController;
    }

    public void execute(final Callback callback) {
        if (mPushController.isPushInProgress()) {
            callback.onPushInProgressError();
            return;
        }

        SurveyChecker.launchQuarantineChecker();

        mPushController.changePushInProgress(true);

        mPushController.push(new IPushController.IPushControllerCallback() {
            @Override
            public void onComplete() {
                mPushController.changePushInProgress(false);

                callback.onComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                mPushController.changePushInProgress(false);

                if (throwable instanceof NetworkException) {
                    callback.onNetworkError();
                } else if (throwable instanceof ConversionException) {
                    callback.onConversionError();
                } else if (throwable instanceof SurveysToPushNotFoundException) {
                    callback.onSurveysNotFoundError();
                } else if (throwable instanceof PushReportException){
                    callback.onPushError();
                }else if (throwable instanceof PushValueException){
                    callback.onInformativeError(throwable.getMessage());
                }else {
                    callback.onPushError();
                }
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

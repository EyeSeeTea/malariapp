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

package org.eyeseetea.malariacare.strategies;

import android.util.Log;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.domain.usecase.MockedPushSurveysUseCase;
import org.eyeseetea.malariacare.domain.usecase.PushUseCase;
import org.eyeseetea.malariacare.receivers.AlarmPushReceiver;
import org.eyeseetea.malariacare.services.PushService;

public class PushServiceStrategy {
    public static final String TAG = ".PushServiceStrategy";

    protected PushService mPushService;

    PushUseCase pushUseCase;

    public PushServiceStrategy(PushService pushService) {
        mPushService = pushService;
    }

    public void push(PushUseCase pushUseCase) {
        this.pushUseCase = pushUseCase;

        if (Session.getCredentials().isDemoCredentials()) {
            Log.d(TAG, "execute mocked push");
            executeMockedPush();
        } else {
            Log.d(TAG, "execute push");
            executePush();
        }
    }

    private void executeMockedPush() {
        MockedPushSurveysUseCase mockedPushSurveysUseCase = new MockedPushSurveysUseCase();

        mockedPushSurveysUseCase.execute(new MockedPushSurveysUseCase.Callback() {
            @Override
            public void onPushFinished() {
                Log.d(TAG, "onPushMockFinished");
                mPushService.onPushFinished();
            }
        });
    }

    private void executePush() {
        Log.d(TAG, "Starting push process...");

        pushUseCase.execute(new PushUseCase.Callback() {
            @Override
            public void onComplete() {
                AlarmPushReceiver.isDoneSuccess();
                Log.d(TAG, "push complete");
            }

            @Override
            public void onPushInProgressError() {
                AlarmPushReceiver.isDoneFail();
                Log.e(TAG, "Push stopped, There is already a push in progress");
            }

            @Override
            public void onPushError() {
                AlarmPushReceiver.isDoneFail();
                Log.e(TAG, "Unexpected error has occurred in push process");
            }

            @Override
            public void onSurveysNotFoundError() {
                AlarmPushReceiver.isDoneFail();
                Log.e(TAG, "Pending surveys not found");
            }

            @Override
            public void onInformativeError(String message) {
                Log.e(TAG, "An error has occurred to the conversion in push process"+message);
                showInDialog(PreferencesState.getInstance().getContext().getString(
                        R.string.error_message), message);
            }

            @Override
            public void onConversionError() {
                AlarmPushReceiver.isDoneFail();
                Log.e(TAG, "An error has occurred to the conversion in push process");
            }

            @Override
            public void onNetworkError() {
                AlarmPushReceiver.isDoneFail();
                Log.e(TAG, "Network not available");
            }
        });
    }
    public void showInDialog(String title, String message) {
        DashboardActivity.dashboardActivity.showException(title, message);
    }
}
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

import static org.eyeseetea.malariacare.domain.usecase.CallbackInvoked.invokedInProgressCallback;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PushUseCaseTest {

    IPushController mPushController = mock(IPushController.class);

    private CountDownLatch lock = new CountDownLatch(1);

    @Test
    public void should_invoke_in_progress_error_callback_when_is_in_progress() throws Exception {
        givenThereIsAInProgressPushController();

        PushUseCase pushUseCase = new PushUseCase(mPushController);

        pushUseCase.execute(new PushUseCase.Callback() {
            @Override
            public void onComplete() {
                callbackInvoked(false);
            }

            @Override
            public void onPushError() {
                callbackInvoked(false);
            }

            @Override
            public void onPushInProgressError() {
                callbackInvoked(true);
            }

            @Override
            public void onSurveysNotFoundError() {
                callbackInvoked(false);
            }

            @Override
            public void onInformativeError(String message) {
                callbackInvoked(false);
            }

            @Override
            public void onConversionError() {
                callbackInvoked(false);
            }

            @Override
            public void onNetworkError() {
                callbackInvoked(false);
            }
        });

        lock.await(100, TimeUnit.MILLISECONDS);
        assertThat(invokedInProgressCallback, is(true));
    }

    private void callbackInvoked(boolean inProgressCallback) {
        lock.countDown();
        invokedInProgressCallback = inProgressCallback;
    }

    private void givenThereIsAInProgressPushController() {
            when(mPushController.isPushInProgress()).thenReturn(true);
    }


}
class CallbackInvoked{
    public static boolean invokedInProgressCallback;
}


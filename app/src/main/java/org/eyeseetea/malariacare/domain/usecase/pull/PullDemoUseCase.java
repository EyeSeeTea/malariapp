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

package org.eyeseetea.malariacare.domain.usecase.pull;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullDemoController;
import org.eyeseetea.malariacare.domain.boundary.IPullDemoController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.usecase.UseCase;

public class PullDemoUseCase implements UseCase {


    public interface Callback {
        void onComplete();

        void onPullError();
    }

    IPullDemoController mPullController;
    private IAsyncExecutor mAsyncExecutor;
    private IMainExecutor mMainExecutor;
    private Callback mCallback;

    public PullDemoUseCase(IPullDemoController pullController, IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mPullController = pullController;
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
    }

    @Override
    public void run() {
        mPullController.pull(new PullDemoController.IPullDemoControllerCallback() {
            @Override
            public void onComplete() {
                notifyOnComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                notifyOnPullError();
            }
        });
    }

    public void execute(final Callback callback) {
        mCallback = callback;
        mMainExecutor.run(this);
    }

    private void notifyOnComplete() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete();
            }
        });
    }

    private void notifyOnPullError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onPullError();
            }
        });
    }
}

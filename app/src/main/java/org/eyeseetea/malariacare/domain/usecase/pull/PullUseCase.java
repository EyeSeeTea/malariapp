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

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullMetadataController;
import org.eyeseetea.malariacare.domain.boundary.IMetadataValidator;
import org.eyeseetea.malariacare.domain.boundary.IPullDataController;
import org.eyeseetea.malariacare.domain.boundary.IPullMetadataController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.exception.MetadataException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;
import org.eyeseetea.malariacare.domain.usecase.UseCase;

public class PullUseCase implements UseCase {

    public interface Callback {
        void onComplete();

        void onPullError();

        void onCancel();

        void onMetadataError();

        void onStep(PullStep pullStep);

        void onNetworkError();
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IPullMetadataController mPullMetadataController;
    private final IPullDataController mPullDataController;
    private final IMetadataValidator mMetadataValidator;
    private Callback mCallback;
    private PullFilters mPullDataFilters;
    private Boolean pullCanceled = false;

    public PullUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IPullMetadataController pullMetadataController,
            IPullDataController pullDataController,
            IMetadataValidator metadataValidator) {
        mAsyncExecutor = asyncExecutor;
        mMainExecutor = mainExecutor;
        mPullMetadataController = pullMetadataController;
        mPullDataController = pullDataController;
        mMetadataValidator = metadataValidator;
    }

    public void execute(PullFilters pullFilters, final Callback callback) {
        mPullDataFilters = pullFilters;
        mCallback = callback;

        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        pullMetadata();
    }

    public void cancel() {
        pullCanceled = true;
    }

    public boolean isPullCanceled() {
        return pullCanceled;
    }

    private void pullMetadata() {
        mPullMetadataController.pullMetadata(new PullMetadataController.Callback() {

            @Override
            public void onComplete() {
                if(pullCanceled){
                    notifyCancel();
                }else {
                    if(mMetadataValidator.isValid()){
                        pullData();
                    } else {
                        notifyError(new MetadataException());
                    }
                }
            }

            @Override
            public void onStep(PullStep step) {
                notifyOnStep(step);
            }

            @Override
            public void onError(Throwable throwable) {
                notifyError(throwable);
            }
        });
    }

    private void pullData() {
        mPullDataController.pullData(mPullDataFilters, new IPullDataController.Callback() {

            @Override
            public void onComplete() {
                if(pullCanceled){
                    notifyCancel();
                }else {
                    notifyOnComplete();
                }
            }

            @Override
            public void onStep(PullStep step) {
                notifyOnStep(step);
            }

            @Override
            public void onError(Throwable throwable) {
                notifyError(throwable);
            }
        });
    }

    private void notifyOnStep(final PullStep step) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onStep(step);
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

    private void notifyError(final Throwable throwable) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                if (throwable instanceof NetworkException) {
                    mCallback.onNetworkError();
                } else if (throwable instanceof MetadataException) {
                    mCallback.onMetadataError();
                } else {
                    mCallback.onPullError();
                }
            }
        });
    }

    private void notifyCancel() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onCancel();
            }
        });
    }
}

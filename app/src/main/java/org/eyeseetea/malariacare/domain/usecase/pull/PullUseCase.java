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
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.MetadataException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;

public class PullUseCase {

    public interface Callback {
        void onComplete();

        void onPullError();

        void onCancel();

        void onConversionError();

        void onStep(PullStep pullStep);

        void onNetworkError();
    }

    IPullMetadataController mPullMetadataController;
    IPullDataController mPullDataController;
    IMetadataValidator mMetadataValidator;

    PullFilters mPullDataFilters;

    public PullUseCase(IPullMetadataController pullMetadataController,
            IPullDataController pullDataController,
            IMetadataValidator metadataValidator) {
        mPullMetadataController = pullMetadataController;
        mPullDataController = pullDataController;
        mMetadataValidator = metadataValidator;
    }

    public void execute(PullFilters pullFilters, final Callback callback) {
        mPullDataFilters = pullFilters;

        pullMetadata(callback);
    }

    private void pullMetadata(final Callback callback) {
        mPullMetadataController.pullMetadata(new PullMetadataController.Callback() {

            @Override
            public void onComplete() {
                if(mMetadataValidator.isValid()){
                    pullData(callback);
                } else {
                    onError(new MetadataException());
                }
            }

            @Override
            public void onStep(PullStep step) {
                callback.onStep(step);
            }

            @Override
            public void onError(Throwable throwable) {
                manageError(throwable, callback);
            }

            @Override
            public void onCancel() {
                callback.onCancel();
            }
        });
    }

    private void pullData(final Callback callback) {
        mPullDataController.pullData(mPullDataFilters, new IPullDataController.Callback() {

            @Override
            public void onComplete() {
                callback.onComplete();
            }

            @Override
            public void onStep(PullStep step) {
                callback.onStep(step);
            }

            @Override
            public void onError(Throwable throwable) {
                manageError(throwable, callback);
            }

            @Override
            public void onCancel() {
                callback.onCancel();
            }
        });
    }

    private void manageError(Throwable throwable, Callback callback) {
        if (throwable instanceof NetworkException) {
            callback.onNetworkError();
        } else if (throwable instanceof ConversionException) {
            callback.onConversionError();
        } else {
            callback.onPullError();
        }
    }


    public void cancel() {
        mPullMetadataController.cancel();
        mPullDataController.cancel();
    }

    public boolean isPullActive() {
        return mPullMetadataController.isPullActive() && mPullDataController.isPullActive();
    }
}

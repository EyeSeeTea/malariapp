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

import android.util.Log;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.PullMetadataController;
import org.eyeseetea.malariacare.domain.boundary.IPullDataController;
import org.eyeseetea.malariacare.domain.boundary.IPullMetadataController;
import org.eyeseetea.malariacare.domain.boundary.IValidatorController;
import org.eyeseetea.malariacare.domain.exception.ConversionException;
import org.eyeseetea.malariacare.domain.exception.NetworkException;

public class PullUseCase {

    public interface Callback {
        void onPullComplete();

        void onPullError();

        void onConversionError();

        void onStep(PullStep pullStep);

        void onNetworkError();

        void onCancel();
    }

    IPullMetadataController mPullMetadataController;
    IPullDataController mPullDataController;
    IValidatorController mValidatorController;

    PullFilters mPullDataFilters;

    public static Boolean PULL_IS_ACTIVE;

    public PullUseCase(IPullMetadataController pullMetadataController,
                       IPullDataController pullDataController,
                       IValidatorController validatorController) {
        mPullMetadataController = pullMetadataController;
        mPullDataController = pullDataController;
        mValidatorController = validatorController;
    }

    public void execute(PullFilters pullFilters, final Callback callback) {
        mPullDataFilters = pullFilters;
        PULL_IS_ACTIVE = true;
        pullMetadata(callback);
    }

    private void pullMetadata(final Callback callback) {
        mPullMetadataController.pullMetadata(new PullMetadataController.Callback() {

            @Override
            public void onComplete() {
                Log.i("pullMetadata", "onComplete");
                mValidatorController.removeInvalidCS();

                mValidatorController.validateTables(new IValidatorController.IValidatorControllerCallback() {
                    @Override
                    public void validate(boolean result) {
                        if(!result) {
                            callback.onConversionError();
                        }else{
                            pullData(callback);
                        }
                    }
                });
            }

            @Override
            public void onStep(PullStep step) {
                if(isPullActive()){
                    if(step.equals(PullStep.METADATA_COMPLETED)){
                        Log.i("pullMetadata", step.toString());
                        System.out.printf("MetaData successfully converted...");
                        onComplete();
                    }
                    else {
                        Log.i("pullMetadata", step.toString());
                        callback.onStep(step);
                        mPullMetadataController.nextStep(step);
                    }
                }else{
                    callback.onCancel();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                manageError(throwable, callback);
            }
        });
    }

    private void pullData(final Callback callback) {
        mPullDataController.pullData(mPullDataFilters, new IPullDataController.Callback() {

            @Override
            public void onComplete() {
                Log.i("pullEvent", "onComplete");
                callback.onPullComplete();
            }

            @Override
            public void onStep(PullStep step) {
                Log.i("pullEvent", step.toString());
                callback.onStep(step);
                if(isPullActive()){
                    if(step.equals(PullStep.DATA_COMPLETED)){
                        onComplete();
                    }else {
                        mPullDataController.nextStep(step);
                    }
                }else{
                    callback.onCancel();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                manageError(throwable, callback);
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

    public boolean isPullActive() {
        return PULL_IS_ACTIVE;
    }

    public void cancel() {
        PULL_IS_ACTIVE = false;
    }
}

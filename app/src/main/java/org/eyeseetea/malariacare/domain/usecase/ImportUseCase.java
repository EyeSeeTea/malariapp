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

import android.net.Uri;

import org.eyeseetea.malariacare.domain.boundary.IImportController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;

public class ImportUseCase implements UseCase {

    Uri uri;


    public interface Callback {
        void onComplete();

        void onImportError();
    }

    private IAsyncExecutor mAsyncExecutor;
    private IMainExecutor mMainExecutor;
    private Callback mCallback;
    IImportController mImportController;

    public ImportUseCase(Uri uri, IImportController importController, IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor) {
        mImportController = importController;
        this.uri = uri;
        mAsyncExecutor = asyncExecutor;
        mMainExecutor = mainExecutor;
    }


    @Override
    public void run() {
        mImportController.importDB(uri, new IImportController.IImportControllerCallback() {
            @Override
            public void onComplete() {
                notifyOnComplete();
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                notifyOnError();
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

    private void notifyOnError() {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onImportError();
            }
        });
    }


    public void execute(final Callback callback) {
        mCallback = callback;
        mAsyncExecutor.run(this);
    }
}
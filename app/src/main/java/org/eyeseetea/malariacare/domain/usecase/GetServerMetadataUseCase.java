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

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;

public class GetServerMetadataUseCase implements UseCase {

    public interface Callback {
        void onSuccess(ServerMetadata serverMetadata);
        void onError(Exception e);
    }

    private final IAsyncExecutor mAsyncExecutor;
    private final IMainExecutor mMainExecutor;
    private final IServerMetadataRepository mServerMetadataRepository;

    private Callback mCallback;

    public GetServerMetadataUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IServerMetadataRepository serverMetadataRepository) {
        this.mAsyncExecutor = asyncExecutor;
        this.mMainExecutor = mainExecutor;
        this.mServerMetadataRepository = serverMetadataRepository;
    }

    public void execute(final Callback callback) {
        this.mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        ServerMetadata serverMetadata = null;
        try {
            serverMetadata = mServerMetadataRepository.getServerMetadata();
        }  catch (Exception e){
            notifyError(e);
        }
        notifyOnComplete(serverMetadata);
    }

    private void notifyOnComplete(final ServerMetadata serverMetadata) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(serverMetadata);
            }
        });
    }

    private void notifyError(final Exception e) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onError(e);
            }
        });
    }
}

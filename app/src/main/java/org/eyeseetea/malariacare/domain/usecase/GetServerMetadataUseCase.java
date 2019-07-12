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

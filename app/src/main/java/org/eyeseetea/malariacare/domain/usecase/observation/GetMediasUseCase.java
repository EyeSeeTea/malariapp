package org.eyeseetea.malariacare.domain.usecase.observation;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IMediaRepository;
import org.eyeseetea.malariacare.domain.entity.Media;
import org.eyeseetea.malariacare.domain.usecase.UseCase;

import java.util.List;

public class GetMediasUseCase implements UseCase {
    private IAsyncExecutor mAsyncExecutor;
    private IMainExecutor mMainExecutor;
    private IMediaRepository mMediaRepository;
    private Callback mCallback;

    public GetMediasUseCase(
            IAsyncExecutor asyncExecutor,
            IMainExecutor mainExecutor,
            IMediaRepository mediaRepository) {
        mAsyncExecutor = asyncExecutor;
        mMainExecutor = mainExecutor;
        mMediaRepository = mediaRepository;
    }

    public void execute(Callback callback) {
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        List<Media> medias = mMediaRepository.getMedias();
        notifyOnSuccess(medias);
    }

    private void notifyOnSuccess(final List<Media> medias) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onSuccess(medias);
            }
        });
    }

    public interface Callback {
        void onSuccess(List<Media> medias);
    }
}

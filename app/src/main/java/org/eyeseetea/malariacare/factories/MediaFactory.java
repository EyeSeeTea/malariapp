package org.eyeseetea.malariacare.factories;

import org.eyeseetea.malariacare.data.boundaries.IMediaDataSource;
import org.eyeseetea.malariacare.data.database.datasources.MediaLocalDataSource;
import org.eyeseetea.malariacare.data.repositories.MediaRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IMediaRepository;
import org.eyeseetea.malariacare.domain.usecase.observation.GetMediasUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class MediaFactory {
    private IMainExecutor mMainExecutor = new UIThreadExecutor();
    private IAsyncExecutor mAsyncExecutor = new AsyncExecutor();

    public GetMediasUseCase getGetMediasUseCase() {
        IMediaDataSource mediaDataSource = new MediaLocalDataSource();
        IMediaRepository mediaRepository = new MediaRepository(mediaDataSource);
        return new GetMediasUseCase(mAsyncExecutor, mMainExecutor, mediaRepository);
    }
}

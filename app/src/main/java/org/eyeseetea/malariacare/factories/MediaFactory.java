package org.eyeseetea.malariacare.factories;

import org.eyeseetea.malariacare.data.database.datasources.MediaDataSource;
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
        IMediaRepository mediaRepository = new MediaDataSource();
        return new GetMediasUseCase(mAsyncExecutor, mMainExecutor, mediaRepository);
    }
}

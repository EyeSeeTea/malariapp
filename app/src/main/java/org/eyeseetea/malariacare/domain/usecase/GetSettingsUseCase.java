package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.Settings;

public class GetSettingsUseCase implements UseCase{

    private ISettingsRepository settingsRepository;
    private ISettingsRepository.ISettingsRepositoryCallback mCallback;
    private IMainExecutor mainExecutor;
    private IAsyncExecutor asyncExecutor;

    public GetSettingsUseCase(
            ISettingsRepository settingsRepository,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        this.mainExecutor = mainExecutor;
        this.asyncExecutor = asyncExecutor;
        this.settingsRepository = settingsRepository;
    }

    public void execute(ISettingsRepository.ISettingsRepositoryCallback callback) {
        mCallback = callback;
        asyncExecutor.run(this);
    }

    @Override
    public void run() {
        onComplete(settingsRepository.getSettings());
    }

    private void onComplete(final Settings settings) {
        mainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete(settings);
            }
        });
    }
}
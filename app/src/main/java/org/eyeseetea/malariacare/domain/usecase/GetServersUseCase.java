package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.boundary.repositories.ISettingsRepository;
import org.eyeseetea.malariacare.domain.entity.Server;
import org.eyeseetea.malariacare.domain.entity.Settings;
import org.eyeseetea.malariacare.domain.entity.SurveyAnsweredRatio;

import java.util.List;

public class GetServersUseCase implements UseCase{

    private IServerRepository serverRepository;
    private IMainExecutor mainExecutor;
    private IAsyncExecutor asyncExecutor;
    IServerRepository.IServerRepositoryCallback callback;

    public GetServersUseCase(
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor,
            IServerRepository serverRepository) {
        this.mainExecutor = mainExecutor;
        this.asyncExecutor = asyncExecutor;
        this.serverRepository = serverRepository;
    }

    public void execute(IServerRepository.IServerRepositoryCallback callback) {
        asyncExecutor.run(this);
        this.callback = callback;
    }

    @Override
    public void run() {
        onComplete(serverRepository.getServers());
    }

    private void onComplete(final List<Server> servers) {
        mainExecutor.run(new Runnable() {
            @Override
            public void run() {
                callback.onComplete(servers);
            }
        });
    }
}
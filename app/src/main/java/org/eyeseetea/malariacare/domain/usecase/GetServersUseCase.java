package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.List;

public class GetServersUseCase implements UseCase{

    public interface Callback{
        void onSuccess(List<Server> servers);
    }

    private IServerRepository serverRepository;
    private IMainExecutor mainExecutor;
    private IAsyncExecutor asyncExecutor;
    private Callback mCallback;

    public GetServersUseCase(
            IServerRepository serverRepository,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        this.mainExecutor = mainExecutor;
        this.asyncExecutor = asyncExecutor;
        this.serverRepository = serverRepository;
    }

    public void execute(Callback callback) {
        mCallback = callback;
        asyncExecutor.run(this);
    }

    @Override
    public void run() {
        try {
            notifyComplete(serverRepository.getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyComplete(List<Server> servers) {
        mainExecutor.run(() -> mCallback.onSuccess(servers));
    }

}
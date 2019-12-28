package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.entity.Server;

public class GetServerUseCase implements UseCase{

    public interface Callback{
        void onSuccess(Server server);
    }

    private IServerRepository serverRepository;
    private IMainExecutor mainExecutor;
    private IAsyncExecutor asyncExecutor;
    private Callback mCallback;

    public GetServerUseCase(
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
            notifyComplete(serverRepository.getGetLoggedServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyComplete(Server server) {
        mainExecutor.run(() -> mCallback.onSuccess(server));
    }

}
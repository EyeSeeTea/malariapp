package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerInfoRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;

public class GetServerInfoUseCase implements UseCase{

    public interface Callback{
        void onComplete(ServerInfo serverStatus);
    }

    private IServerInfoRepository mServerInfoRepository;
    private IMainExecutor mMainExecutor;
    private IAsyncExecutor mAsyncExecutor;
    private Callback mCallback;

    public GetServerInfoUseCase(
            IServerInfoRepository serverInfoRepository,
            IMainExecutor mainExecutor,
            IAsyncExecutor asyncExecutor) {
        mMainExecutor = mainExecutor;
        mAsyncExecutor = asyncExecutor;
        mServerInfoRepository = serverInfoRepository;
    }

    public void execute(Callback callback) {
        mCallback = callback;
        mAsyncExecutor.run(this);
    }

    @Override
    public void run() {
        try {
            notifyComplete(mServerInfoRepository.getServerInfo(ReadPolicy.CACHE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyComplete(ServerInfo serverStatus) {
        mMainExecutor.run(new Runnable() {
            @Override
            public void run() {
                mCallback.onComplete(serverStatus);
            }
        });
    }

}
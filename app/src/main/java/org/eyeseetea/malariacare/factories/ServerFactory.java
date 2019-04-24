package org.eyeseetea.malariacare.factories;

import android.content.Context;

import org.eyeseetea.malariacare.data.repositories.ServerRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class ServerFactory {
    public GetServersUseCase getServersUseCase(Context context){
        IMainExecutor mainExecutor = new UIThreadExecutor();
        IAsyncExecutor asyncExecutor = new AsyncExecutor();

        IServerRepository serverRepository = new ServerRepository(context);
        GetServersUseCase serversUseCase = new GetServersUseCase(serverRepository,mainExecutor,asyncExecutor);

        return serversUseCase;
    }
}

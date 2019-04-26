package org.eyeseetea.malariacare.factories;

import android.content.Context;

import org.eyeseetea.malariacare.data.database.datasources.ServerLocalDataSource;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.remote.api.ServerRemoteDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerRepository;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.usecase.GetServerUseCase;
import org.eyeseetea.malariacare.domain.usecase.GetServersUseCase;
import org.eyeseetea.malariacare.presentation.executors.AsyncExecutor;
import org.eyeseetea.malariacare.presentation.executors.UIThreadExecutor;

public class ServerFactory {
    IMainExecutor mainExecutor = new UIThreadExecutor();
    IAsyncExecutor asyncExecutor = new AsyncExecutor();

    public GetServersUseCase getServersUseCase(Context context) {


        IServerRepository serverRepository = getServerRepository(context);

        GetServersUseCase serversUseCase = new GetServersUseCase(serverRepository, mainExecutor,
                asyncExecutor);

        return serversUseCase;
    }

    public GetServerUseCase getServerUseCase(Context context) {

        IServerRepository serverRepository = getServerRepository(context);

        GetServerUseCase getServersUseCase = new GetServerUseCase(serverRepository, mainExecutor,
                asyncExecutor);

        return getServersUseCase;
    }

    public IServerRepository getServerRepository(Context context) {
        ServerLocalDataSource serverLocalDataSource = new ServerLocalDataSource(context);
        ServerRemoteDataSource serverRemoteDataSource =
                new ServerRemoteDataSource(PreferencesState.getInstance().getCreedentials());

        return new ServerRepository(serverLocalDataSource, serverLocalDataSource,
                serverRemoteDataSource);
    }
}

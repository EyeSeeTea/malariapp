package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.IAllServersDataSource;
import org.eyeseetea.malariacare.data.IServerDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.List;

public class ServerRepository implements IServerRepository {
    IAllServersDataSource allServersDataSource;
    IServerDataSource serverLocalDataSource;
    IServerDataSource serverRemoteDataSource;

    public ServerRepository(
            IAllServersDataSource allServersDataSource,
            IServerDataSource serverLocalDataSource,
            IServerDataSource serverRemoteDataSource) {
        this.allServersDataSource = allServersDataSource;
        this.serverLocalDataSource = serverLocalDataSource;
        this.serverRemoteDataSource = serverRemoteDataSource;
    }

    @Override
    public List<Server> getAll() {
        return allServersDataSource.getAll();
    }

    @Override
    public Server getLoggedServer() throws Exception {
        Server cachedServer = serverLocalDataSource.get();

        if (cachedServer.getUrl() != null &&
                cachedServer.getName() != null && cachedServer.getLogo() != null){
            return cachedServer;
        } else {
            try{
                Server remoteServer = serverRemoteDataSource.get();

                serverLocalDataSource.save(remoteServer);

                return remoteServer;
            } catch (Exception e){
                return cachedServer;
            }
        }
    }
}

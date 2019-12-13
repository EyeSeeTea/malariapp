package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.ReadableServerDataSource;
import org.eyeseetea.malariacare.data.WritableServerDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerRepository;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.List;

public class ServerRepository implements IServerRepository {
    private WritableServerDataSource writableServerLocalDataSource;
    private ReadableServerDataSource readableServerLocalDataSource;
    private ReadableServerDataSource readableServerRemoteDataSource;
    private ReadableServerDataSource readableServerStaticDataSource;

    public ServerRepository(
            WritableServerDataSource writableServerLocalDataSource,
            ReadableServerDataSource readableServerLocalDataSource,
            ReadableServerDataSource readableServerRemoteDataSource,
            ReadableServerDataSource readableServerStaticDataSource) {
        this.writableServerLocalDataSource = writableServerLocalDataSource;
        this.readableServerLocalDataSource = readableServerLocalDataSource;
        this.readableServerRemoteDataSource = readableServerRemoteDataSource;
        this.readableServerStaticDataSource = readableServerStaticDataSource;
    }

    @Override
    public List<Server> getAll(ReadPolicy readPolicy) {
        List<Server> servers;

        if (readPolicy == ReadPolicy.NETWORK_FIRST){
            servers = readableServerRemoteDataSource.getAll();

            if(servers.size() == 0){
                servers = readableServerLocalDataSource.getAll();
            }

            if (servers.size() == 0){
                servers = readableServerStaticDataSource.getAll();
            }
        } else {
            servers = readableServerLocalDataSource.getAll();

            if(servers.size() == 0){
                servers = readableServerRemoteDataSource.getAll();
            }

            if (servers.size() == 0){
                servers = readableServerStaticDataSource.getAll();
            }
        }

        return servers;
    }

    @Override
    public Server getLoggedServer() throws Exception {
        Server cachedServer = readableServerLocalDataSource.get();

        if (cachedServer != null && cachedServer.getUrl() != null &&
                cachedServer.getName() != null && cachedServer.getLogo() != null){
            return cachedServer;
        } else {
            try{
                Server remoteServer = readableServerRemoteDataSource.get();

                writableServerLocalDataSource.save(remoteServer);

                return remoteServer;
            } catch (Exception e){
                return cachedServer;
            }
        }
    }
}

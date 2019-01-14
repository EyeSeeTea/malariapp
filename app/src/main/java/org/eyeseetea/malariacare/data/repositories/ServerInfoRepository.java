/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.repositories;
import org.eyeseetea.malariacare.data.IServerInfoDataSource;
import org.eyeseetea.malariacare.data.database.datasources.ServerInfoLocalDataSource;
import org.eyeseetea.malariacare.data.remote.api.ServerInfoRemoteDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IServerInfoRepository;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;

public class ServerInfoRepository implements IServerInfoRepository {
    IServerInfoDataSource serverInfoLocalDataSource;
    IServerInfoDataSource serverInfoRemoteDataSource;

    public ServerInfoRepository(ServerInfoLocalDataSource serverInfoLocalDataSource,
                                ServerInfoRemoteDataSource serverInfoRemoteDataSource) {
        this.serverInfoLocalDataSource = serverInfoLocalDataSource;
        this.serverInfoRemoteDataSource = serverInfoRemoteDataSource;
    }

    @Override
    public ServerInfo getServerInfo(NetworkStrategy networkStrategy) {
        ServerInfo serverInfo = null;
        if(networkStrategy.equals(NetworkStrategy.LOCAL_FIRST)){
            serverInfo = serverInfoLocalDataSource.get();
            if(serverInfo.getVersion()==-1){
                serverInfo = serverInfoRemoteDataSource.get();
            }
        }else if (networkStrategy.equals(NetworkStrategy.NETWORK_FIRST)){
            serverInfo = serverInfoRemoteDataSource.get();
        }
        return serverInfo;
    }

    @Override
    public void save(ServerInfo serverInfo) {
        serverInfoLocalDataSource.save(serverInfo);
    }
}

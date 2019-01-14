package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.domain.enums.NetworkStrategy;

public interface IServerInfoRepository {
    ServerInfo getServerInfo(NetworkStrategy networkStrategy) throws Exception;

    void save(ServerInfo serverInfo);
}

package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.ServerInfo;
import org.eyeseetea.malariacare.domain.common.ReadPolicy;

public interface IServerInfoRepository {
    ServerInfo getServerInfo(ReadPolicy readPolicy) throws Exception;
    void save(ServerInfo serverInfo);
}

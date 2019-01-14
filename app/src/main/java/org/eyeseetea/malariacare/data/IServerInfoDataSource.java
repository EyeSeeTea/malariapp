package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.entity.ServerInfo;

public interface IServerInfoDataSource {
    ServerInfo get();
    void save(ServerInfo serverInfo);
    void clear();
}

package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.eyeseetea.malariacare.domain.entity.ServerInfo;

public interface IServerInfoDataSource {
    ServerInfo get(String server, Credentials credentials);
}

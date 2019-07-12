package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.entity.Server;

public interface IServerDataSource {
    Server get() throws Exception;
    void save(Server server);
}

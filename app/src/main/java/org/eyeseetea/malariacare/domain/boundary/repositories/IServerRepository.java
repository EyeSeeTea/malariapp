package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.common.ReadPolicy;
import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.List;

public interface IServerRepository {
    List<Server> getAll(ReadPolicy readPolicy);
    Server getLoggedServer() throws Exception;
}

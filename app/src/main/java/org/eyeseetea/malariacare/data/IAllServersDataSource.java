package org.eyeseetea.malariacare.data;

import org.eyeseetea.malariacare.domain.entity.Server;

import java.util.List;

public interface IAllServersDataSource {
    List<Server> getAll();
}

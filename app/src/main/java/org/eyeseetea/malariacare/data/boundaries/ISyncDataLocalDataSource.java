package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.ISyncData;

import java.util.List;

public interface ISyncDataLocalDataSource {
    List<? extends ISyncData> getDataToSync() throws Exception;
    List<? extends ISyncData> getAll();

    void save(List<? extends ISyncData> syncData) throws Exception;
    void save(ISyncData syncData);

}

package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.IData;

import java.util.List;

public interface IDataLocalDataSource {
    List<? extends IData> getDataToSync() throws Exception;
    List<? extends IData> getAll();
    IData getByUId(String uid);

    void save(List<? extends IData> dataList) throws Exception;
    void save(IData data);

}

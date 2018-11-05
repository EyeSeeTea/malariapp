package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.IData;

import java.util.List;

public interface IDataLocalDataSource {
    List<? extends IData> getDataToSync() throws Exception;
    List<? extends IData> getAllData();

    void saveData(List<? extends IData> dataList) throws Exception;
    void saveData(IData data);

}

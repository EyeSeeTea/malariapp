package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.IMetadata;

import java.util.List;

public interface IMetadataLocalDataSource<T extends IMetadata> {
    List<T> getAll();
    void clearAndSave(List<T> metadataList) throws Exception;
}

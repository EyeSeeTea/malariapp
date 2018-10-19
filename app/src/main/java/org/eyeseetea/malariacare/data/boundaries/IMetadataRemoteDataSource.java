package org.eyeseetea.malariacare.data.boundaries;

import org.eyeseetea.malariacare.domain.entity.IMetadata;
import java.util.List;

public interface IMetadataRemoteDataSource <T extends IMetadata>{
    List<T> getAll() throws Exception;
}

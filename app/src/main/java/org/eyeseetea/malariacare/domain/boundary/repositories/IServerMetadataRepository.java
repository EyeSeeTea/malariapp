package org.eyeseetea.malariacare.domain.boundary.repositories;

import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.exception.InvalidServerMetadataException;

public interface IServerMetadataRepository {
    ServerMetadata getServerMetadata() throws InvalidServerMetadataException;
}

package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;
import org.eyeseetea.malariacare.domain.exception.InvalidServerMetadataException;

public class GetServerMetadataUseCase {
    private final IServerMetadataRepository mServerMetadataRepository;

    public GetServerMetadataUseCase(
            IServerMetadataRepository serverMetadataRepository) {
        this.mServerMetadataRepository = serverMetadataRepository;
    }

    public ServerMetadata execute() throws InvalidServerMetadataException {
        return mServerMetadataRepository.getServerMetadata();
    }
}

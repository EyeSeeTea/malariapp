package org.eyeseetea.malariacare.domain.usecase;

import org.eyeseetea.malariacare.domain.boundary.repositories.IServerMetadataRepository;
import org.eyeseetea.malariacare.domain.entity.ServerMetadata;

public class GetServerMetadataUseCase {
    private final IServerMetadataRepository mServerMetadataRepository;

    public GetServerMetadataUseCase(
            IServerMetadataRepository serverMetadataRepository) {
        this.mServerMetadataRepository = serverMetadataRepository;
    }

    public ServerMetadata execute() {
        return mServerMetadataRepository.getServerMetadata();
    }
}

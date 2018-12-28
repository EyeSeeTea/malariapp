package org.eyeseetea.malariacare.data.repositories;

import org.eyeseetea.malariacare.data.boundaries.IMediaDataSource;
import org.eyeseetea.malariacare.domain.boundary.repositories.IMediaRepository;
import org.eyeseetea.malariacare.domain.entity.Media;

import java.util.List;

public class MediaRepository implements IMediaRepository {
    private IMediaDataSource mMediaDataSource;

    public MediaRepository(IMediaDataSource mediaDataSource) {
        mMediaDataSource = mediaDataSource;
    }

    @Override
    public List<Media> getMedias() {
        return mMediaDataSource.getMedias();
    }
}

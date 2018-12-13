package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.domain.boundary.repositories.IMediaRepository;
import org.eyeseetea.malariacare.domain.entity.Media;

import java.util.ArrayList;
import java.util.List;

public class MediaDataSource implements IMediaRepository {
    @Override
    public List<Media> getMedias() {
        return mapMediaListFromMEdiaDBList(MediaDB.getAllMedia());
    }


    private List<Media> mapMediaListFromMEdiaDBList(List<MediaDB> mediaDBS) {
        List<Media> medias = new ArrayList<>();
        for (MediaDB mediaDB : mediaDBS) {
            medias.add(mapMediaFromMediaDB(mediaDB));
        }
        return medias;
    }

    private Media mapMediaFromMediaDB(MediaDB mediaDB) {
        return new Media(mediaDB.getMediaType(), mediaDB.getFilename());
    }
}

package org.eyeseetea.malariacare.data.database.datasources;

import org.eyeseetea.malariacare.data.boundaries.IMediaDataSource;
import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.domain.entity.Media;

import java.util.ArrayList;
import java.util.List;

public class MediaLocalDataSource implements IMediaDataSource {
    @Override
    public List<Media> getMedias() {
        return mapMediaListFromMediaDBList(MediaDB.getAllMedia());
    }


    private List<Media> mapMediaListFromMediaDBList(List<MediaDB> mediaDBS) {
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

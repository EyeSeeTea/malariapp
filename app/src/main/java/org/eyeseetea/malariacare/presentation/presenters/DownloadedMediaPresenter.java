package org.eyeseetea.malariacare.presentation.presenters;

import org.eyeseetea.malariacare.domain.entity.Media;
import org.eyeseetea.malariacare.domain.usecase.observation.GetMediasUseCase;
import org.eyeseetea.malariacare.factories.MediaFactory;

import java.util.List;

public class DownloadedMediaPresenter {

    View mView;


    public void attachView(final View view) {
        mView = view;
        loadMedia();
    }

    private void loadMedia() {
        MediaFactory mediaFactory = new MediaFactory();
        GetMediasUseCase getMediasUseCase = mediaFactory.getGetMediasUseCase();
        getMediasUseCase.execute(new GetMediasUseCase.Callback() {
            @Override
            public void onSuccess(List<Media> medias) {
                mView.showMediaItems(medias);
            }
        });

    }

    public void detachView() {
        mView = null;
    }


    public void onMediaClick(Media media) {
        if (media.isVideo()) {
            mView.openVideo(media);
        } else if (media.isPicture()) {
            mView.openImage(media);
        }
    }

    public void onBackPressed() {
        mView.returnToDashboardActivity();
    }

    public boolean onHomeMenuItemPressed() {
        return mView.returnToDashboardAndReturn();
    }

    public interface View {
        void openVideo(Media media);

        void openImage(Media media);

        void returnToDashboardActivity();

        boolean returnToDashboardAndReturn();

        void showMediaItems(List<Media> mediaList);
    }

}

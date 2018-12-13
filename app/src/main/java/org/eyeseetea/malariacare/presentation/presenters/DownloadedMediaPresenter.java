package org.eyeseetea.malariacare.presentation.presenters;

import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;

import java.util.List;

public class DownloadedMediaPresenter {

    View mView;


    public void attachView(final View view) {
        mView = view;
        loadMedia();
    }

    private void loadMedia() {
        mView.showMediaItems(MediaDB.getAllInLocal());
    }

    public void detachView() {
        mView = null;
    }


    public void onMediaClick(MediaDB media) {
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
        void openVideo(MediaDB mediaDB);

        void openImage(MediaDB mediaDB);

        void returnToDashboardActivity();

        boolean returnToDashboardAndReturn();

        void showMediaItems(List<MediaDB> mediaList);
    }

}

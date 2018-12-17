package org.eyeseetea.malariacare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Media;
import org.eyeseetea.malariacare.domain.usecase.observation.GetMediasUseCase;
import org.eyeseetea.malariacare.factories.MediaFactory;
import org.eyeseetea.malariacare.layout.adapters.downloaded_media.DownloadedMediaAdapter;
import org.eyeseetea.malariacare.presentation.presenters.DownloadedMediaPresenter;
import org.eyeseetea.malariacare.strategies.ActionBarStrategy;

import java.io.File;
import java.util.List;

public class DownloadedMediaActivity extends BaseActivity implements DownloadedMediaPresenter.View {

    private DownloadedMediaPresenter mDownloadedMediaPresenter;
    private RecyclerView mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.EyeSeeTheme);
        setContentView(R.layout.downloaded_media_activity);
        setActivityActionBar();
        initRecyclerView();
        initializePresenter();
    }
    private void initRecyclerView(){
        mList = (RecyclerView) findViewById(R.id.downloaded_media_list);
        mList.setHasFixedSize(true);
        // RecyclerView layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(mLayoutManager);
    }

    private void initializePresenter() {
        MediaFactory mediaFactory = new MediaFactory();
        GetMediasUseCase getMediasUseCase = mediaFactory.getGetMediasUseCase();
        mDownloadedMediaPresenter = new DownloadedMediaPresenter(getMediasUseCase);
        mDownloadedMediaPresenter.attachView(this);
    }

    @Override
    public void openVideo(Media media) {
        Intent videoIntent = new Intent(DashboardActivity.dashboardActivity,
                VideoActivity.class);
        videoIntent.putExtra(VideoActivity.VIDEO_PATH_PARAM, media.getFilename());
        DashboardActivity.dashboardActivity.startActivity(videoIntent);
    }

    @Override
    public void openImage(Media media) {
        Intent implicitIntent = new Intent();
        implicitIntent.setAction(Intent.ACTION_VIEW);
        File file = new File(media.getFilename());
        Uri contentUri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".layout.adapters.survey.FeedbackAdapter", file);

        implicitIntent.setDataAndType(contentUri,
                PreferencesState.getInstance().getContext().getContentResolver().getType(
                        contentUri));
        implicitIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        implicitIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        DashboardActivity.dashboardActivity.startActivity(Intent.createChooser(implicitIntent,
                PreferencesState.getInstance().getContext().getString(
                        R.string.feedback_view_image)));
    }

    @Override
    public void returnToDashboardActivity() {
        Intent returnIntent = new Intent(this, DashboardActivity.class);
        returnIntent.putExtra(getString(R.string.show_announcement_key), false);
        startActivity(returnIntent);
    }

    @Override
    public boolean returnToDashboardAndReturn() {
        mDownloadedMediaPresenter.onBackPressed();
        return true;
    }

    @Override
    public void showMediaItems(final List<Media> mediaList) {
        DownloadedMediaAdapter downloadedMediaAdapter = new DownloadedMediaAdapter(mediaList,
                getBaseContext());
        mList.setAdapter(downloadedMediaAdapter);
        downloadedMediaAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDownloadedMediaPresenter.onMediaClick(mediaList.get(position));
            }
        });
    }

    private void setActivityActionBar() {
        ActionBarStrategy.setActionBarDashboard(this, this.getString(R.string.downloaded_media_menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDownloadedMediaPresenter.onHomeMenuItemPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Go back
     */
    @Override
    public void onBackPressed() {
        mDownloadedMediaPresenter.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mDownloadedMediaPresenter.detachView();
        super.onDestroy();
    }
}
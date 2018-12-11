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

import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.downloaded_media.DownloadedMediaAdapter;
import org.eyeseetea.malariacare.strategies.ActionBarStrategy;

import java.io.File;
import java.util.List;

public class DownloadedMediaActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.EyeSeeTheme);
        setActivityActionBar();
        PreferencesState.getInstance().initalizateActivityDependencies();
        setContentView(R.layout.downloaded_media_activity);
        final List<MediaDB> mediaList = MediaDB.getAllInLocal();
        RecyclerView list = (RecyclerView) findViewById(R.id.downloaded_media_list);
        final DownloadedMediaAdapter downloadedMediaAdapter = new DownloadedMediaAdapter(mediaList,
                getBaseContext());
        list.setAdapter(downloadedMediaAdapter);
        list.setHasFixedSize(true);

        // RecyclerView layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
        downloadedMediaAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openMediaFile(mediaList.get(position));
            }
        });
    }

    private void openMediaFile(MediaDB media) {
        if (media.isVideo()) {
            openVideo(media);
        } else if (media.isPicture()) {
            openImage(media);
        }
    }

    private void openVideo(MediaDB media) {
        Intent videoIntent = new Intent(DashboardActivity.dashboardActivity,
                VideoActivity.class);
        videoIntent.putExtra(VideoActivity.VIDEO_PATH_PARAM, media.getFilename());
        DashboardActivity.dashboardActivity.startActivity(videoIntent);
    }

    private void openImage(MediaDB media) {
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

    private void setActivityActionBar() {
        ActionBarStrategy.setActionBarDashboard(this, this.getString(R.string.downloaded_media_menu));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Go back
     */
    @Override
    public void onBackPressed() {
        Intent returnIntent=new Intent(this, DashboardActivity.class);
        returnIntent.putExtra(getString(R.string.show_announcement_key), false);
        startActivity(returnIntent);
    }
}
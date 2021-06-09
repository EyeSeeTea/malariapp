package org.eyeseetea.malariacare;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;

import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.downloaded_media.DownloadedMediaAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.jetbrains.annotations.NotNull;

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
        downloadedMediaAdapter.setOnItemClickListener(
                (parent, view, position, id) -> openMediaFile(mediaList.get(position)));

        downloadedMediaAdapter.setOnMenuMediaClickListener(
                (view,media) -> {
                    showMediaPopupMenu(view, media);
                });
    }

    public void showMediaPopupMenu(View view, MediaDB media) {

        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.media_popup_menu, popup.getMenu());


        popup.setOnMenuItemClickListener(item -> {
            shareMediaFile(media);
            return true;
        });

        popup.show();
    }

    private void shareMediaFile(MediaDB media) {
        File file = new File(media.getFilename());
        Uri fileURI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? FileProvider.getUriForFile(this, this.getPackageName()
                + ".DownloadedMediaActivity", file)
                : Uri.fromFile(file);
        ShareCompat.IntentBuilder.from(this)
                .setStream(fileURI)
                .setType(getMimeType(fileURI))
                .setChooserTitle("Share media...")
                .startChooser();
    }

    @NotNull
    private String getMimeType(Uri fileURI) {
        String type = null;
        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension = mime.getExtensionFromMimeType(cR.getType(fileURI));

        if (extension != null) {
            type = mime.getMimeTypeFromExtension(extension);
        }

        return type;
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
        androidx.appcompat.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        LayoutUtils.setActionBarDashboard(this, this.getString(R.string.downloaded_media_menu));
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
        Intent returnIntent = new Intent(this, DashboardActivity.class);
        returnIntent.putExtra(getString(R.string.show_announcement_key), false);
        startActivity(returnIntent);
    }
}
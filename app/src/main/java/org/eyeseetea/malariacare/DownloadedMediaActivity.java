package org.eyeseetea.malariacare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import org.eyeseetea.malariacare.data.database.model.MediaDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.downloaded_media.DownloadedMediaAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.views.ActionBarStrategy;

import java.util.List;

public class DownloadedMediaActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.EyeSeeTheme);
        setActivityActionBar();
        PreferencesState.getInstance().initalizateActivityDependencies();
        setContentView(R.layout.downloaded_media_activity);
        List<MediaDB> mediaList = MediaDB.getAllInLocal();
        RecyclerView list = (RecyclerView) findViewById(R.id.downloaded_media_list);
        final DownloadedMediaAdapter downloadedMediaAdapter = new DownloadedMediaAdapter(mediaList,
                getBaseContext());
        list.setAdapter(downloadedMediaAdapter);
        list.setHasFixedSize(true);

        // RecyclerView layout manager
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(mLayoutManager);
    }

    private void setActivityActionBar() {
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        ActionBarStrategy.setActionBarLogo(actionBar);
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
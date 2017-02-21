package org.eyeseetea.malariacare;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;

import org.eyeseetea.malariacare.data.database.model.Media;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.layout.adapters.downloaded_media.DownloadedMediaAdapter;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;

import java.util.List;

public class DownloadedMediaActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.EyeSeeTheme);
        android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        LayoutUtils.setActionBarLogo(actionBar);
        PreferencesState.getInstance().initalizateActivityDependencies();
        setContentView(R.layout.downloaded_media_activity);
        List<Media> mediaList = Media.getAllInLocal();
        RecyclerView list= (RecyclerView) findViewById(R.id.downloaded_media_list);
        DownloadedMediaAdapter downloadedMediaAdapter = new DownloadedMediaAdapter(mediaList,getBaseContext());
        list.setAdapter(downloadedMediaAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
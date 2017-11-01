package org.eyeseetea.malariacare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.utils.FileIOUtils;

import java.io.File;

public class VideoActivity extends Activity {

    public static final String VIDEO_PATH_PARAM = "videoPathParam";
    VideoView mVideoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesState.getInstance().initalizateActivityDependencies();
        setContentView(R.layout.video_activity);

        //Displays a video file.
        MediaController mediaController = new MediaController(VideoActivity.this);
        mVideoView = (VideoView) findViewById(R.id.videoview);
        String videoPathParam = getIntent().getStringExtra(VIDEO_PATH_PARAM);
        File file = new File(videoPathParam);
        if (file.exists()) {
            mVideoView.setVideoPath(videoPathParam);
        } else {
            mVideoView.setVideoURI(FileIOUtils.getRawUri(videoPathParam));
        }
        mVideoView.setMediaController(mediaController);
        mediaController.setAnchorView(mVideoView);

        mVideoView.requestFocus();
        if (savedInstanceState != null) {
            mVideoView.seekTo(savedInstanceState.getInt("video", 0));
        }
        mVideoView.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("video", mVideoView.getCurrentPosition());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVideoView.seekTo(savedInstanceState.getInt("video", 0));
    }
}


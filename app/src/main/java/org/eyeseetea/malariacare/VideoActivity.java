package org.eyeseetea.malariacare;

import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by arrizabalaga on 30/05/16.
 */
public class VideoActivity extends Activity {

    public static final String VIDEO_PATH_PARAM="videoPathParam";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);

        //Displays a video file.
        MediaController mediaController=new MediaController(VideoActivity.this);
        VideoView mVideoView = (VideoView)findViewById(R.id.videoview);
        String videoPathParam=getIntent().getStringExtra(VIDEO_PATH_PARAM);
        mVideoView.setVideoPath(videoPathParam);
        mVideoView.setMediaController(mediaController);
        mediaController.setAnchorView(mVideoView);

        mVideoView.requestFocus();
        mVideoView.start();
    }
}


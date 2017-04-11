package org.eyeseetea.malariacare.drive;

import android.content.Intent;

import org.eyeseetea.malariacare.DashboardActivity;

/**
 * Created by arrizabalaga on 28/05/16.
 */
public class DriveRestControllerStrategy {


    private static final String TAG = "DriveRestControllerStrategy";
    private static DriveRestControllerStrategy instance;

    DriveRestControllerStrategy() {

    }

    public static DriveRestControllerStrategy getInstance() {
        if (instance == null) {
            instance = new DriveRestControllerStrategy();
        }
        return instance;
    }

    public void init(DashboardActivity dashboardActivity) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void syncMedia() {
    }
}

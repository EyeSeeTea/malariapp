package org.eyeseetea.malariacare.sdk;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;

import org.eyeseetea.malariacare.LoginActivity;
import org.hisp.dhis.client.sdk.android.api.D2;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkLoginController extends SdkController {
    //BaseActivity
    public static void logOutUser() {
        D2.me().signOut();
    }

    public static void logOutUser(Activity activity) {
        logOutUser();
        Intent intent = new Intent(activity, LoginActivity.class);
        ActivityCompat.startActivity(activity, intent, null);
    }

    public static void login(String username, String password) {
        D2.me().signIn(username, password);
    }
}

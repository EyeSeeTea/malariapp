package org.eyeseetea.malariacare.sdk;

import android.app.Activity;

import org.hisp.dhis.client.sdk.android.api.D2;

/**
 * Created by idelcano on 15/11/2016.
 */

public class SdkLoginController  extends  SdkController{
    //BaseActivity
    public static void logOutUser(Activity activity) {
        D2.me().signOut();
    }
}

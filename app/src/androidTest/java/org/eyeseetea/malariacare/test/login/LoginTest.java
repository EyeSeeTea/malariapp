/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test.login;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;

/**
 * Created by arrizabalaga on 3/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class LoginTest {

    private static final String TAG="TestingLogin";

    //private LoginActivity mReceiptCaptureActivity;
    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @BeforeClass
    public static void setupClass(){
        PopulateDB.wipeDatabase();
    }

    @Before
    public void setup(){
        //force init go to logging activity.
        SDKTestUtils.goToLogin();
        //set the test limit( and throw exception if the time is exceded)
        SDKTestUtils.setTestTimeoutSeconds(SDKTestUtils.DEFAULT_TEST_TIME_LIMIT);
    }

    @AfterClass
    public static void exitApp() throws Exception {
        SDKTestUtils.exitApp();
    }

    @Test
    public void loginWithRightCredentials(){
        Intents.init();

        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(SDKTestUtils.HNQIS_DEV_STAGING));
        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText(SDKTestUtils.TEST_USERNAME_WITH_PERMISSION));
        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText(SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION));
        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());

        intended(hasComponent(ProgressActivity.class.getName()));

        //Waiting for pull to finish in order to clear the state of the app
        waitForPull(DEFAULT_WAIT_FOR_PULL);
        Intents.release();
    }

//    @Test
//    public void loginWithBadCredentials(){
//        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(SDKTestUtils.HNQIS_DEV_STAGING));
//        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText(SDKTestUtils.TEST_USERNAME_WITH_PERMISSION));
//        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText("bad"));
//        Log.d(TAG, "loginWithBadCredentials before click");
//        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());
//
//        Log.d(TAG, "loginWithBadCredentials checking popup");

//        onView(withText(android.R.string.ok)).perform(click());
        //XXX The error dialog should be captured but it is not because the sdk customdialog blocks the main ui thread
        //onView(withText(SDKTestUtils.UNABLE_TO_LOGIN)).check(matches(isDisplayed()));
//    }

}

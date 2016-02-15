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

package org.eyeseetea.malariacare.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.DialogIdlingResource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by arrizabalaga on 3/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PushTest {

    public static final String HNQIS_DEV_STAGING = "https://hnqis-dev-staging.psi-mis.org";
    public static final String TEST_USERNAME_OK = "iarrizabalaga";
    public static final String TEST_PASSWORD_OK = "Arrizabalaga2015";
    public static final String TAG = "PushTest";

    DialogIdlingResource dialogIdlingResource;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @BeforeClass
    public static void setup(){
        PopulateDB.wipeDatabase();
    }

    @Before
    public void register(){
        dialogIdlingResource = new DialogIdlingResource(getInstrumentation().getTargetContext());
        Espresso.registerIdlingResources(dialogIdlingResource);
    }

    @After
    public void unregister(){
        Espresso.unregisterIdlingResources(dialogIdlingResource);
        dialogIdlingResource=null;
    }

    @Test
    public void login(){

        //given: a monitor that waits for progressactivity
        Instrumentation instrumentation = getInstrumentation();
        Instrumentation.ActivityMonitor monitor =  instrumentation.addMonitor(ProgressActivity.class.getName(), null, false);

        //when: login
        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(HNQIS_DEV_STAGING));
        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText(TEST_USERNAME_OK));
        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText(TEST_PASSWORD_OK));
        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());

        //then: wait for progressactivity + dialog + ok (to move to dashboard)
        ProgressActivity progressActivity = (ProgressActivity)instrumentation.waitForMonitorWithTimeout(monitor, 5000);
        dialogIdlingResource.setProgressActivity(progressActivity);
        onView(withText(android.R.string.ok)).perform(click());

    }

}

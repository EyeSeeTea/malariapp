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

package org.eyeseetea.malariacare.test.logout;

import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
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
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;

/**
 * Created by idelcano on 06/04/2016.
 */
@RunWith(AndroidJUnit4.class)
public class WipeDataOnLogout {

    private static final String TAG="TestingLogout";

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
    public void wipeDataOnLogout(){
        //Given
        Intents.init();

        onView(withId(org.hisp.dhis.android.sdk.R.id.server_url)).perform(replaceText(SDKTestUtils.HNQIS_DEV_CI));
        onView(withId(org.hisp.dhis.android.sdk.R.id.username)).perform(replaceText(SDKTestUtils.TEST_USERNAME_WITH_PERMISSION));
        onView(withId(org.hisp.dhis.android.sdk.R.id.password)).perform(replaceText(SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION));
        onView(withId(org.hisp.dhis.android.sdk.R.id.login_button)).perform(click());

        intended(hasComponent(ProgressActivity.class.getName()));

        //Waiting for pull to finish in order to clear the state of the app
        waitForPull(DEFAULT_WAIT_FOR_PULL);

        //When
        SDKTestUtils.goToLogin();

        //Then

        //Removed sdk data
        assertTrue(EventExtended.count() == 0);
        assertTrue(DataValueExtended.count() == 0);


        //removed app data
        assertTrue(Survey.count() == 0);
        assertTrue(Value.count() == 0);

        Intents.release();
    }
}

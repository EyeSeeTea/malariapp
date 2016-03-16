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

package org.eyeseetea.malariacare.test.AssessAction;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Delete;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.fillCompulsorySurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.fillSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.startSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPush;

/**
 * Created by idelcano on 14/03/2016.
 */
@RunWith(AndroidJUnit4.class)
public class AssessCompulsoryTest {

    private static final String TAG = "AssessActionTest";

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Before
    public void setup() {
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
    public void assessCompulsoryComplete() {
        //GIVEN
        if(LoginActivity.class.equals(SDKTestUtils.getActivityInstance().getClass())) {
            login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
            waitForPull(DEFAULT_WAIT_FOR_PULL);
        }
        startSurvey(SDKTestUtils.TEST_FACILITY_1_IDX, SDKTestUtils.TEST_FACILITY_1_IDX);
        fillCompulsorySurvey(13, "Yes");

        //WHEN
        Espresso.pressBack();
        //select continue later and go to assess
        onView(withText(R.string.dialog_continue_later_option)).perform(click());

        //THEN
        onView(withText(R.string.dashboard_info_ready_to_upload)).check(matches(isDisplayed()));


        //WHEN
        Long idSurvey = SDKTestUtils.editSurvey();
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        Espresso.pressBack();
        Espresso.unregisterIdlingResources(idlingResource);
        //select complete survey on exit
        onView(withText(R.string.dialog_complete_option)).perform(click());
        //confirm(are you sure?)
        onView(withText(android.R.string.yes)).perform(click());
        //info dialog
        onView(withText(android.R.string.ok)).perform(click());

        //THEN
        //then: Survey is pushed (UID)
        Log.d(TAG, "Session user ->" + Session.getUser());
        Survey survey = waitForPush(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH, idSurvey);
        assertTrue(survey.isCompleted() || survey.isSent());
    }
}

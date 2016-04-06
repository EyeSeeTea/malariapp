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

package org.eyeseetea.malariacare.test.push;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.getActivityInstance;

/**
 * Created by idelcano on 24/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PushConflictTest {

    private static final String TAG="PushConflictTest";

    // private LoginActivity mReceiptCaptureActivity;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

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
    public void pushWithDataElementConflict(){
        //GIVEN
        /*
        login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(DEFAULT_WAIT_FOR_PULL);

        startSurvey(1, 2);
        fillSurvey(7, "Yes");
        Survey survey=Survey.findById(getSurveyId());

        Long idSurvey=markInProgressAsCompleted();




        //WHEN
        //Simulate the conflict dataelement

        //then: Survey is pushed (UID)
        Log.d(TAG, "Session user ->" + Session.getUser());
        survey=waitForPush(20,survey.getId_survey());


        onView(withText(android.R.string.ok)).perform(click());
        checkConflict();
        //then: Row is gone
        onView(withId(R.id.score)).check(doesNotExist());
        survey=Survey.findById(survey.getId_survey());
        Log.d(TAG,survey.toString());
        assertTrue(survey.getEventUid() == null);

        assertTrue(survey.getStatus() == Constants.SURVEY_CONFLICT);
        */
    }


    public static void checkConflict() {
        //when: click on assess tab + plus button
        onView(withTagValue(Matchers.is((Object) PreferencesState.getInstance().getContext().getString(R.string.tab_tag_improve)))).perform(click());


        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        String text=getActivityInstance().getApplicationContext().getString(R.string.feedback_info_conflict);
        text=text.toUpperCase();
        onView(withText(text)).perform(click());

        Espresso.unregisterIdlingResources(idlingResource);

        //Wait for SurveyService loads feedback
        idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        try {
            onView(withId(R.string.feedback_info_conflict)).perform(click());
        }catch (Exception e){
            //It can fail if the mobile resolution fill the text
        }

        Espresso.unregisterIdlingResources(idlingResource);

    }
}

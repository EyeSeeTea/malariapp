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

package org.eyeseetea.malariacare.test.deprecated.AssessAction;

import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.eyeseetea.malariacare.utils.AUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagKey;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.fillSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.startSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by idelcano on 21/03/2016.
 */
@RunWith(AndroidJUnit4.class)
public class AssessCompleteGoToImprove {
/*
    private static final String TAG="AssessActionTest";

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
    public void assessCompleteAndGoImprove(){
        //GIVEN
        login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(DEFAULT_WAIT_FOR_PULL);
        startSurvey(SDKTestUtils.TEST_FACILITY_1_IDX, SDKTestUtils.TEST_CC);
        //randomResponseNumber is used to randomize the survey answers to obtain a different main score between tests.
        int randomResponseNumber=2 + (int)(Math.random() * 15);
        fillSurvey(randomResponseNumber, "Yes");

        //WHEN
        Long idSurvey=SDKTestUtils.markCompleteAndGoImprove();
        Survey survey = Survey.findById(idSurvey);
        Date completionDate= survey.getCompletionDate();


        //THEN
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        try {
            onView(withText(String.valueOf(AUtils.formatDate(completionDate)))).check(matches(isDisplayed()));
        }catch(AmbiguousViewMatcherException e){
            Log.i(TAG, "Multiple surveys have the same date " + AUtils.formatDate(completionDate));
        }
        try {
            onView(withText(String.format("%.1f %%", survey.getMainScore()))).check(matches(isDisplayed()));
        }catch(AmbiguousViewMatcherException e){
            Log.i(TAG, "Multiple surveys have the same score " + String.format("%.1f %%", survey.getMainScore()));
        }

        //WHEN
        if(survey.isCompleted())
            onView(withText( "* "  + survey.getProgram().getName())).check(matches(isDisplayed())).perform(click());
        else
            onView(withText("- "   + survey.getProgram().getName())).check(matches(isDisplayed())).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        //THEN
        idlingResource = new ElapsedTimeIdlingResource(5 * 1000);
        Espresso.registerIdlingResources(idlingResource);
        //check if is in feedback
        onView(withText(R.string.quality_of_care)).check(matches(isDisplayed()));
        onView(withText(String.format("%.1f%%", survey.getMainScore()))).check(matches(isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }
    */
}

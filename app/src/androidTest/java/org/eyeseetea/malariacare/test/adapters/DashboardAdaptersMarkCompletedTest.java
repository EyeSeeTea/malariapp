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

package org.eyeseetea.malariacare.test.adapters;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.exitSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.fillSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.selectRow;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.startSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;
import static org.eyeseetea.malariacare.test.utils.SizeListMatcher.withListSize;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by arrizabalaga on 3/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class DashboardAdaptersMarkCompletedTest {

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
    public void fillsASurveyMarksItCompletedAndDisappears(){
        //GIVEN
        login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(DEFAULT_WAIT_FOR_PULL);

        startSurvey(SDKTestUtils.TEST_FACILITY_1_IDX, SDKTestUtils.TEST_CERVICAL_CANCER);
        exitSurvey();
        startSurvey(SDKTestUtils.TEST_FACILITY_1_IDX, SDKTestUtils.TEST_FAMILY_PLANNING_IDX);
        fillSurvey(7, "No");

        //WHEN: One survey is deleted
        selectRow(0).onChildView(withId(R.id.survey_type)).perform(click());
        onView(withText(R.string.option_mark_completed)).perform(click());

        //THEN Survey is gone
        onView(allOf(withId(android.R.id.list), withChild(withId(R.id.assessment_row)))).check(matches(withListSize(1)));

    }

}

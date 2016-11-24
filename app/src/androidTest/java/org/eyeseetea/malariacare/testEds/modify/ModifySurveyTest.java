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

package org.eyeseetea.malariacare.testEds.modify;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.test.utils.ElapsedTimeIdlingResource;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.eyeseetea.malariacare.sdk.models.Event;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.DEFAULT_WAIT_FOR_PULL;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_CI;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.fillSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.markInProgressAsCompleted;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.selectStartSurvey;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPush;

/**
 * Created by idelcano on 25/03/2016.
 */
@RunWith(AndroidJUnit4.class)
public class ModifySurveyTest {

    private static final String TAG="ModifySurveyTest";

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Before
    public void setup(){
        //force init go to logging activity.
        SDKTestUtils.goToLogin();
        //set the test limit( and throw exception if the time is exceded)
        SDKTestUtils.setTestTimeoutSeconds(SDKTestUtils.DEFAULT_TEST_TIME_LIMIT*2);
    }

    @AfterClass
    public static void exitApp() throws Exception {
        SDKTestUtils.exitApp();
    }

    @Test
    public void pushAndModifySurveyTest(){
        //Given
        login(HNQIS_DEV_CI, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(DEFAULT_WAIT_FOR_PULL);
        int modifcatedOptions = 7;
        //The first is "Real/Simulation" and it is not saved
        int expectedOptions = 6;
        long numberOfEvents = 1;
        String eventUid = "";
        selectStartSurvey(SDKTestUtils.TEST_FACILITY_1_IDX, SDKTestUtils.TEST_CC);

        IdlingResource idlingResource = new ElapsedTimeIdlingResource(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH * 500);
        Espresso.registerIdlingResources(idlingResource);
        onView(withText(R.string.cancel)).perform(click());
        onView(withId(R.id.create_form_button)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        idlingResource = new ElapsedTimeIdlingResource(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH * 500);
        Espresso.registerIdlingResources(idlingResource);
        onView(withText(R.string.create)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        fillSurvey(17, "Yes");
        Long idSurvey = markInProgressAsCompleted();

        //When
        Log.d(TAG, "Session user ->" + Session.getUser());
        Survey survey = waitForPush(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH * 1000, idSurvey);
        eventUid = survey.getEventUid();
        //then: Survey is pushed (UID)
        assertTrue(survey.getEventUid() != null);
        assertTrue(numberOfEvents == EventExtended.count());

        //WHEN
        selectStartSurvey(SDKTestUtils.TEST_FACILITY_1_IDX, SDKTestUtils.TEST_CC);

        idlingResource = new ElapsedTimeIdlingResource(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH * 1000);

        Espresso.registerIdlingResources(idlingResource);
        onView(withText(R.string.modify)).perform(click());
        Espresso.unregisterIdlingResources(idlingResource);

        fillSurvey(modifcatedOptions, "No");

        idSurvey = markInProgressAsCompleted();
        survey = waitForPush(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH * 1000, idSurvey);

        //THEN
        assertTrue(survey.getEventUid() != null);
        idlingResource = new ElapsedTimeIdlingResource(SDKTestUtils.DEFAULT_WAIT_FOR_PUSH * 0000);
        Espresso.registerIdlingResources(idlingResource);
        assertTrue(survey.getEventUid().equals(eventUid));
        Espresso.unregisterIdlingResources(idlingResource);
        if (survey.isCompleted()) {
            int modificatedValues = 0;
            for (Value value : survey.getValues()) {
                if (value.getUploadDate().after(survey.getUploadDate())) {
                    modificatedValues++;
                }
            }
            assertTrue(modificatedValues == expectedOptions);
            assertTrue(numberOfEvents - 1 == EventExtended.count());
        } else if (survey.isSent()) {
            Event event = EventExtended.getEvent(survey.getEventUid());
            assertTrue(event.getDataValues().size() == expectedOptions);
            assertTrue(numberOfEvents == EventExtended.count());
        }
    }
}
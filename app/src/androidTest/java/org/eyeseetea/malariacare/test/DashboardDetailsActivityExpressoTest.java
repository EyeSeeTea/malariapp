/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.eyeseetea.malariacare.CreateSurveyActivity;
import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.DashboardDetailsActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.test.utils.IntentServiceIdlingResource;
import org.eyeseetea.malariacare.test.utils.MalariaEspressoActions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.eyeseetea.malariacare.test.utils.MalariaEspressoActions.waitId;
import static org.eyeseetea.malariacare.test.utils.MalariaEspressoActions.waitSnippet;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class DashboardDetailsActivityExpressoTest extends MalariaEspressoTest{

    private final static int _EXPECTED_SURVEYS=2;

    @Rule
    public IntentsTestRule<DashboardDetailsActivity> mActivityRule = new IntentsTestRule<>(
            DashboardDetailsActivity.class);

    @BeforeClass
    public static void init(){
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSessionSurvey(_EXPECTED_SURVEYS, 1, 0);
    }

    @Before
    public void registerIntentServiceIdlingResource(){
        super.setup();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        idlingResource = new IntentServiceIdlingResource(instrumentation.getTargetContext(), SurveyService.class);
        Espresso.registerIdlingResources(idlingResource);
    }

    @After
    public void unregisterIntentServiceIdlingResource(){
        Espresso.unregisterIdlingResources(idlingResource);
    }

    @Test
    public void form_views() {
        onView(isRoot()).perform(waitId(R.id.plusButton, 3000));
    }

    @Test
    public void new_survey_launches_intent(){
        //WHEN
        onView(withId(R.id.plusButton)).perform(click());

        //THEN
        intended(anyIntent());
    }

    @Test
    public void delete_survey(){
        //WHEN
        whenAssessmentSwipeAndOk("Health Facility 0", "ICM");

        //THEN: Check font size has properly changed
        checkAssessmentDoesntExist("Health Facility 0", "ICM");
    }

    /**
     * From Dashboard delete survey
     * @param orgUnit orgUnit of the survey we want to delete
     * @param program program of the survey we want to delete
     */
    protected void whenAssessmentSwipeAndOk(String orgUnit, String program) {
        onView(allOf(withId(R.id.assessment_row),
                withChild(allOf(
                        withChild(allOf(withId(R.id.facility), withText(orgUnit))),
                        withChild(allOf(withId(R.id.survey_type), withText("- " + program)))))))
                .perform(swipeRight());

        //Espresso is NOT waiting for the SwipeListener to finish, thus some forced waiting is required
        try {
            Thread.sleep(1000);
            onView(withText(android.R.string.ok)).perform(click());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkAssessmentDoesntExist(String orgUnit, String program) {
        onView(allOf(withId(R.id.assessment_row),
                withChild(allOf(
                        withChild(allOf(withId(R.id.facility), withText(orgUnit))),
                        withChild(allOf(withId(R.id.survey_type), withText("- " + program))))))).check(doesNotExist());
    }

}
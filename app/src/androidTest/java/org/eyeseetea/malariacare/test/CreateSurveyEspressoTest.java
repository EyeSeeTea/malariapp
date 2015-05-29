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

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.widget.Button;
import android.widget.Spinner;

import org.eyeseetea.malariacare.CreateSurveyActivity;
import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.utils.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.eyeseetea.malariacare.test.utils.ErrorTextMatcher.hasErrorText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class CreateSurveyEspressoTest extends MalariaEspressoTest{

    @Rule
    public IntentsTestRule<CreateSurveyActivity> mActivityRule = new IntentsTestRule<>(
            CreateSurveyActivity.class);

    @BeforeClass
    public static void init(){
        populateData(InstrumentationRegistry.getTargetContext().getAssets());
        mockSurveys(1);
    }

    @Before
    public void setup(){
        super.setup();
    }

    @Test
    public void form_views() {
        onView(withId(R.id.org_unit)).check(matches(isDisplayed()));
        onView(withId(R.id.program)).check(matches(isDisplayed()));
        onView(withId(R.id.create_form_button)).check(matches(isDisplayed()));
    }

    @Test
    public void no_selection_no_survey(){

        //WHEN
        onView(withId(R.id.create_form_button)).perform(click());

        //THEN
        onView(withText("Missing selection")).check(matches(isDisplayed())).perform(click());
    }

    @Test
    public void select_data_create_survey(){

        //WHEN: Showing 2 ways of clicking spinners
        onView(withId(R.id.org_unit)).perform(click());
        onView(withText("Health Facility 2")).perform(click());

        onView(withId(R.id.program)).perform(click());
        onData(allOf(is(instanceOf(Program.class)))).atPosition(1).perform(click());

        onView(withId(R.id.create_form_button)).perform(click());

        //THEN
        intended(anyIntent());
        assertNotNull(Session.getSurvey());
    }

    @Test
    public void select_repeated_data_no_survey(){

        //WHEN
        onView(withId(R.id.org_unit)).perform(click());
        onView(withText("Health Facility 0")).perform(click());

        onView(withId(R.id.program)).perform(click());
        onView(withText("Clinical Case Management")).perform(click());

        onView(withId(R.id.create_form_button)).perform(click());

        //THEN
        onView(withText("Existing Survey")).check(matches(isDisplayed())).perform(click());
    }

}
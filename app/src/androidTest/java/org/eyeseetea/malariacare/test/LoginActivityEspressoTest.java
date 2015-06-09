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

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.utils.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.eyeseetea.malariacare.R;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.eyeseetea.malariacare.test.utils.ErrorTextMatcher.hasErrorText;

/**
 *
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityEspressoTest extends MalariaEspressoTest{

    @Rule
    public IntentsTestRule<LoginActivity> mActivityRule = new IntentsTestRule<>(
            LoginActivity.class);


    @BeforeClass
    public static void init(){
        MalariaEspressoTest.init();
    }

    @Before
    public void setup(){
        super.setup();
    }

    @Test
    public void form_views() {
        onView(withId(R.id.user)).check(matches(isDisplayed()));
        onView(withId(R.id.password)).check(matches(isDisplayed()));
    }

    @Test
    public void login_bad_credentials(){
        //GIVEN
        onView(withId(R.id.user)).perform(typeText("bad"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("bad"), closeSoftKeyboard());

        //WHEN
        onView(withId(R.id.email_sign_in_button)).perform(click());

        //THEN
        onView(withId(R.id.user)).check(matches(hasErrorText(res.getString(R.string.login_error_bad_credentials))));
    }

    @Test
    @LargeTest
    public void login_with_good_credentials(){
        //GIVEN
        cleanDB();
        onView(withId(R.id.user)).perform(typeText("user"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("user"), closeSoftKeyboard());

        //WHEN
        onView(withId(R.id.email_sign_in_button)).perform(click());

        //THEN
        intended(anyIntent());
        assertNotNull(Session.getUser());
    }

}